package com.yuncheng.system.dictionary.cache;

import com.yuncheng.common.constant.CacheTtlConstants;
import com.yuncheng.framework.mybatis.transaction.AfterCommitExecutor;
import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.redis.store.RedisJsonStore;
import com.yuncheng.system.dictionary.dto.DictionaryOptionItem;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 缓存业务模块消费的数据字典选项，缓存异常时回源数据库。 */
@Service
public class DictionaryOptionCacheService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryOptionCacheService.class);

    private final RedisJsonStore redisJsonStore;
    private final AuthRedisProperties redisProperties;
    private final AfterCommitExecutor afterCommitExecutor;

    public DictionaryOptionCacheService(
            RedisJsonStore redisJsonStore,
            AuthRedisProperties redisProperties,
            AfterCommitExecutor afterCommitExecutor
    ) {
        this.redisJsonStore = redisJsonStore;
        this.redisProperties = redisProperties;
        this.afterCommitExecutor = afterCommitExecutor;
    }

    public List<DictionaryOptionItem> getOrLoad(
            String dictionaryCode,
            Supplier<List<DictionaryOptionItem>> loader
    ) {
        String key = key(dictionaryCode);
        try {
            List<DictionaryOptionItem> cached = redisJsonStore.getList(
                    key,
                    DictionaryOptionItem.class
            );
            if (cached != null) {
                return List.copyOf(cached);
            }
        } catch (RuntimeException exception) {
            log.warn("读取数据字典选项缓存失败，将回源数据库：dictionaryCode={}",
                    dictionaryCode, exception);
        }

        List<DictionaryOptionItem> options = List.copyOf(loader.get());
        try {
            redisJsonStore.set(key, options, CacheTtlConstants.MEDIUM);
        } catch (RuntimeException exception) {
            log.warn("写入数据字典选项缓存失败：dictionaryCode={}", dictionaryCode, exception);
        }
        return options;
    }

    public void deleteAfterCommit(String dictionaryCode) {
        afterCommitExecutor.execute(() -> delete(dictionaryCode));
    }

    private void delete(String dictionaryCode) {
        try {
            redisJsonStore.delete(key(dictionaryCode));
        } catch (RuntimeException exception) {
            log.error("清理数据字典选项缓存失败，将由缓存 TTL 最终淘汰：dictionaryCode={}",
                    dictionaryCode, exception);
        }
    }

    private String key(String dictionaryCode) {
        return redisProperties.getKeyPrefix() + ":cache:dictionary-options:" + dictionaryCode;
    }
}
