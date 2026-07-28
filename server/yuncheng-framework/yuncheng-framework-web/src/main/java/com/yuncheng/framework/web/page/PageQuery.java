package com.yuncheng.framework.web.page;

import static com.yuncheng.common.constant.PageConstants.DEFAULT_PAGE;
import static com.yuncheng.common.constant.PageConstants.DEFAULT_PAGE_SIZE;
import static com.yuncheng.common.constant.PageConstants.MAX_PAGE_SIZE;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 分页请求的公共参数。 */
public class PageQuery {

    @Min(value = 1, message = "页码不能小于 1")
    private int page = DEFAULT_PAGE;

    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = MAX_PAGE_SIZE, message = "每页数量不能超过 " + MAX_PAGE_SIZE)
    private int pageSize = DEFAULT_PAGE_SIZE;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
