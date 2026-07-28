package com.yuncheng.system.user.service;

import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;

/** 按角色分批遍历用户主键，供缓存失效使用。 */
@Service
public class UserIdBatchQueryService {

    private static final int BATCH_SIZE = 500;

    private final SystemUserMapper userMapper;

    public UserIdBatchQueryService(SystemUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void forEachRoleBatch(
            Collection<Long> roleIds,
            Collection<String> roleCodes,
            Consumer<List<Long>> consumer
    ) {
        if ((roleIds == null || roleIds.isEmpty()) && (roleCodes == null || roleCodes.isEmpty())) {
            return;
        }
        forEachBatch(
                (lastId, limit) -> userMapper.selectIdsByRolesAfter(
                        roleIds, roleCodes, lastId, limit
                ),
                consumer
        );
    }

    private void forEachBatch(IdBatchLoader loader, Consumer<List<Long>> consumer) {
        long lastId = 0L;
        while (true) {
            List<Long> userIds = loader.load(lastId, BATCH_SIZE);
            if (userIds.isEmpty()) {
                return;
            }
            consumer.accept(userIds);
            lastId = userIds.getLast();
        }
    }

    @FunctionalInterface
    private interface IdBatchLoader {

        List<Long> load(Long lastId, int limit);
    }
}
