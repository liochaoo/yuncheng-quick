package com.yuncheng.framework.web.page;

import java.util.List;

/**
 * 分页响应。
 *
 * @param items 当前页数据
 * @param total 符合条件的数据总数
 * @param page 当前页码
 * @param pageSize 每页数量
 */
public record PageResult<T>(
        List<T> items,
        long total,
        int page,
        int pageSize
) {

    public PageResult {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static <T> PageResult<T> of(List<T> items, long total, PageQuery query) {
        return new PageResult<>(items, total, query.getPage(), query.getPageSize());
    }
}
