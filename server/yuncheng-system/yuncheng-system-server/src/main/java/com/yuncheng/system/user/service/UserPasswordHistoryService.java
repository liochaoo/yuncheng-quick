package com.yuncheng.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.entity.SystemUserPasswordHistory;
import com.yuncheng.system.user.enums.PasswordChangeSource;
import com.yuncheng.system.user.mapper.SystemUserPasswordHistoryMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** 校验、记录并限制用户密码历史。 */
@Service
public class UserPasswordHistoryService {

    private static final int BATCH_SIZE = 500;
    private static final int RETAIN_COUNT = 10;

    private final SystemUserPasswordHistoryMapper historyMapper;
    private final PasswordEncoder passwordEncoder;

    public UserPasswordHistoryService(
            SystemUserPasswordHistoryMapper historyMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.historyMapper = historyMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public void requireNotRecentlyUsed(Long userId, String password, int historyCount) {
        List<SystemUserPasswordHistory> histories = historyMapper.selectList(
                new LambdaQueryWrapper<SystemUserPasswordHistory>()
                        .eq(SystemUserPasswordHistory::getUserId, userId)
                        .orderByDesc(SystemUserPasswordHistory::getCreatedAt)
                        .orderByDesc(SystemUserPasswordHistory::getId)
                        .last("LIMIT " + historyCount)
        );
        for (SystemUserPasswordHistory history : histories) {
            if (passwordEncoder.matches(password, history.getPasswordHash())) {
                throw PlatformException.badRequest(
                        "新密码不能与最近 " + historyCount + " 次使用过的密码相同"
                );
            }
        }
    }

    public void record(Long userId, String passwordHash, PasswordChangeSource source) {
        historyMapper.insert(toHistory(userId, passwordHash, source));
        prune(userId);
    }

    public void recordCreatedUsers(List<SystemUser> users) {
        for (int from = 0; from < users.size(); from += BATCH_SIZE) {
            List<SystemUser> batch = users.subList(
                    from,
                    Math.min(from + BATCH_SIZE, users.size())
            );
            List<SystemUserPasswordHistory> histories = new ArrayList<>(batch.size());
            batch.forEach(user -> histories.add(toHistory(
                    user.getId(),
                    user.getPasswordHash(),
                    PasswordChangeSource.CREATE
            )));
            historyMapper.insert(histories, BATCH_SIZE);
        }
    }

    public void deleteByUserId(Long userId) {
        historyMapper.delete(new LambdaQueryWrapper<SystemUserPasswordHistory>()
                .eq(SystemUserPasswordHistory::getUserId, userId));
    }

    private void prune(Long userId) {
        List<SystemUserPasswordHistory> histories = historyMapper.selectList(
                new LambdaQueryWrapper<SystemUserPasswordHistory>()
                        .eq(SystemUserPasswordHistory::getUserId, userId)
                        .orderByDesc(SystemUserPasswordHistory::getCreatedAt)
                        .orderByDesc(SystemUserPasswordHistory::getId)
        );
        if (histories.size() <= RETAIN_COUNT) {
            return;
        }
        historyMapper.deleteByIds(
                histories.subList(RETAIN_COUNT, histories.size())
                        .stream()
                        .map(SystemUserPasswordHistory::getId)
                        .toList()
        );
    }

    private SystemUserPasswordHistory toHistory(
            Long userId,
            String passwordHash,
            PasswordChangeSource source
    ) {
        SystemUserPasswordHistory history = new SystemUserPasswordHistory();
        history.setUserId(userId);
        history.setPasswordHash(passwordHash);
        history.setChangeSource(source);
        return history;
    }
}
