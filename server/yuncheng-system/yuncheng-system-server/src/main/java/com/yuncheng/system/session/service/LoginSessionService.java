package com.yuncheng.system.session.service;

import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.redis.store.RedisJsonStore;
import com.yuncheng.framework.security.config.JwtProperties;
import com.yuncheng.framework.security.session.LoginSessionVerifier;
import com.yuncheng.system.session.model.LoginSession;
import com.yuncheng.system.session.model.LoginSessionPage;
import com.yuncheng.system.session.model.RefreshTokenReplay;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/** 管理和验证登录 Session、Refresh JTI。 */
@Service("loginSessionVerifier")
public class LoginSessionService implements LoginSessionVerifier {

    private static final DefaultRedisScript<String> ROTATE_REFRESH_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local replay_json = redis.call('get', KEYS[4])
                    if replay_json then
                        local session_json = redis.call('get', KEYS[2])
                        if not session_json then
                            return false
                        end
                        local replay = cjson.decode(replay_json)
                        local session = cjson.decode(session_json)
                        if session['refreshJti'] ~= replay['refreshJti'] then
                            return false
                        end
                        return replay_json
                    end

                    if redis.call('get', KEYS[1]) ~= ARGV[1] then
                        return false
                    end
                    local old_ttl = redis.call('pttl', KEYS[1])
                    if old_ttl <= 0 then
                        return false
                    end
                    local session_json = redis.call('get', KEYS[2])
                    if not session_json then
                        return false
                    end
                    local session = cjson.decode(session_json)
                    if session['refreshJti'] ~= ARGV[2] then
                        return false
                    end

                    local session_ttl = tonumber(ARGV[3])
                    local overlap_ttl = tonumber(ARGV[4])
                    local replay_ttl = math.min(old_ttl, session_ttl, overlap_ttl)
                    if replay_ttl <= 0 then
                        return false
                    end

                    redis.call('psetex', KEYS[3], session_ttl, ARGV[1])
                    redis.call('psetex', KEYS[2], session_ttl, ARGV[5])
                    redis.call('psetex', KEYS[4], replay_ttl, ARGV[6])
                    redis.call('pexpire', KEYS[1], replay_ttl)
                    return ARGV[6]
                    """,
                    String.class
            );

    private final StringRedisTemplate redisTemplate;
    private final RedisJsonStore redisJsonStore;
    private final AuthRedisProperties redisProperties;
    private final JwtProperties jwtProperties;
    private final JsonMapper jsonMapper;

    public LoginSessionService(
            StringRedisTemplate redisTemplate,
            RedisJsonStore redisJsonStore,
            AuthRedisProperties redisProperties,
            JwtProperties jwtProperties,
            JsonMapper jsonMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.redisJsonStore = redisJsonStore;
        this.redisProperties = redisProperties;
        this.jwtProperties = jwtProperties;
        this.jsonMapper = jsonMapper;
    }

    public void create(LoginSession session) {
        Duration ttl = remaining(session.expiresAt());
        redisJsonStore.set(sessionKey(session.sessionId()), session, ttl);
        redisTemplate.opsForValue().set(refreshKey(session.refreshJti()), session.sessionId(), ttl);
        String userSessionsKey = userSessionsKey(session.userId());
        redisTemplate.opsForZSet().add(userSessionsKey, session.sessionId(), session.expiresAt().toEpochMilli());
        extendIndexTtl(userSessionsKey);
        String allSessionsKey = allSessionsKey();
        redisTemplate.opsForZSet().add(allSessionsKey, session.sessionId(), session.expiresAt().toEpochMilli());
        extendIndexTtl(allSessionsKey);
    }

    public LoginSession findByRefreshJti(String refreshJti) {
        String sessionId = redisTemplate.opsForValue().get(refreshKey(refreshJti));
        return sessionId == null ? null : findSession(sessionId);
    }

    public LoginSession findSession(String sessionId) {
        return redisJsonStore.get(sessionKey(sessionId), LoginSession.class);
    }

    public LoginSessionPage page(Long userId, int page, int pageSize) {
        String indexKey = userId == null ? allSessionsKey() : userSessionsKey(userId);
        removeExpiredIndexEntries(indexKey);
        long total = defaultLong(redisTemplate.opsForZSet().zCard(indexKey));
        long start = (long) (page - 1) * pageSize;
        if (start >= total) {
            return new LoginSessionPage(List.of(), total);
        }
        Set<String> sessionIds = redisTemplate.opsForZSet().reverseRange(
                indexKey,
                start,
                start + pageSize - 1L
        );
        if (sessionIds == null || sessionIds.isEmpty()) {
            return new LoginSessionPage(List.of(), total);
        }
        List<String> orderedIds = List.copyOf(sessionIds);
        List<LoginSession> values = redisJsonStore.multiGet(
                orderedIds.stream().map(this::sessionKey).toList(),
                LoginSession.class
        );
        List<LoginSession> sessions = new ArrayList<>(values.size());
        for (LoginSession session : values) {
            if (session != null) {
                sessions.add(session);
            }
        }
        return new LoginSessionPage(sessions, total);
    }

    public RefreshTokenReplay findValidRefreshReplay(String oldJti) {
        RefreshTokenReplay replay = redisJsonStore.get(
                refreshReplayKey(oldJti),
                RefreshTokenReplay.class
        );
        if (replay == null || replay.refreshJti() == null || replay.refreshJti().isBlank()) {
            return null;
        }
        LoginSession session = findSession(replay.sessionId());
        return session != null && Objects.equals(session.refreshJti(), replay.refreshJti())
                ? replay
                : null;
    }

    public RefreshTokenReplay rotateRefresh(
            String oldJti,
            String newJti,
            LoginSession session,
            RefreshTokenReplay replay
    ) {
        if (!oldJti.equals(session.refreshJti())
                || !newJti.equals(replay.refreshJti())
                || !session.sessionId().equals(replay.sessionId())
                || !session.userId().equals(replay.userId())
                || !session.clientType().equals(replay.clientType())) {
            return null;
        }
        long sessionTtlMillis = Math.max(1, remaining(session.expiresAt()).toMillis());
        LoginSession rotated = new LoginSession(
                session.sessionId(), session.userId(), session.username(), session.realName(),
                session.clientType(), session.loginIp(), session.userAgent(),
                session.createdAt(), session.expiresAt(), newJti
        );
        String result = redisTemplate.execute(
                ROTATE_REFRESH_SCRIPT,
                List.of(
                        refreshKey(oldJti),
                        sessionKey(session.sessionId()),
                        refreshKey(newJti),
                        refreshReplayKey(oldJti)
                ),
                session.sessionId(),
                oldJti,
                Long.toString(sessionTtlMillis),
                Long.toString(refreshOverlapMillis()),
                writeJson(rotated),
                writeJson(replay)
        );
        return result == null ? null : readJson(result, RefreshTokenReplay.class);
    }

    public void deleteSession(String sessionId, String refreshJti) {
        if (refreshJti != null && !refreshJti.isBlank()) {
            redisTemplate.delete(refreshKey(refreshJti));
        }
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        LoginSession session = findSession(sessionId);
        if (session != null && session.refreshJti() != null) {
            redisTemplate.delete(refreshKey(session.refreshJti()));
        }
        redisTemplate.delete(sessionKey(sessionId));
        if (session == null) {
            return;
        }
        String userSessionsKey = userSessionsKey(session.userId());
        redisTemplate.opsForZSet().remove(userSessionsKey, sessionId);
        deleteEmptyIndex(userSessionsKey);
        String allSessionsKey = allSessionsKey();
        redisTemplate.opsForZSet().remove(allSessionsKey, sessionId);
        deleteEmptyIndex(allSessionsKey);
    }

    public void deleteAllByUserId(Long userId) {
        String indexKey = userSessionsKey(userId);
        Set<String> sessionIds = redisTemplate.opsForZSet().range(indexKey, 0, -1);
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                LoginSession session = findSession(sessionId);
                deleteSession(sessionId, session == null ? null : session.refreshJti());
            }
        }
        redisTemplate.delete(indexKey);
    }

    @Override
    public boolean isActive(String sessionId, Long userId) {
        LoginSession session = findSession(sessionId);
        return session != null
                && session.userId().equals(userId)
                && session.expiresAt().isAfter(Instant.now());
    }

    private Duration remaining(Instant expiresAt) {
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        if (remaining.isZero() || remaining.isNegative()) {
            throw new IllegalStateException("登录 Session 已过期");
        }
        return remaining;
    }

    private String sessionKey(String sessionId) {
        return redisProperties.runtimeKey("session:" + sessionId);
    }

    private String refreshKey(String refreshJti) {
        return redisProperties.runtimeKey("refresh:" + refreshJti);
    }

    private String refreshReplayKey(String oldRefreshJti) {
        return redisProperties.runtimeKey("refresh-replay:" + oldRefreshJti);
    }

    private String userSessionsKey(Long userId) {
        return redisProperties.runtimeKey("user-sessions:" + userId);
    }

    private String allSessionsKey() {
        return redisProperties.runtimeKey("sessions");
    }

    private void extendIndexTtl(String indexKey) {
        redisTemplate.expire(indexKey, jwtProperties.getRefreshTokenTtl());
    }

    private void removeExpiredIndexEntries(String indexKey) {
        redisTemplate.opsForZSet().removeRangeByScore(
                indexKey,
                Double.NEGATIVE_INFINITY,
                Instant.now().toEpochMilli()
        );
    }

    private void deleteEmptyIndex(String indexKey) {
        if (defaultLong(redisTemplate.opsForZSet().zCard(indexKey)) == 0) {
            redisTemplate.delete(indexKey);
        }
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private long refreshOverlapMillis() {
        Duration overlap = jwtProperties.getRefreshTokenOverlap();
        Duration configured = overlap == null || overlap.isZero() || overlap.isNegative()
                ? Duration.ofSeconds(1)
                : overlap;
        return Math.max(1, configured.toMillis());
    }

    private String writeJson(Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("写入 Refresh Token 轮换数据失败", exception);
        }
    }

    private <T> T readJson(String value, Class<T> type) {
        try {
            return jsonMapper.readValue(value, type);
        } catch (Exception exception) {
            throw new IllegalStateException("读取 Refresh Token 轮换数据失败", exception);
        }
    }
}
