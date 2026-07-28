package com.yuncheng.system.dictionary.dto;

import com.yuncheng.framework.web.page.PageQuery;

/** 数据字典分页查询参数。 */
public class DictionaryPageQuery extends PageQuery {

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
