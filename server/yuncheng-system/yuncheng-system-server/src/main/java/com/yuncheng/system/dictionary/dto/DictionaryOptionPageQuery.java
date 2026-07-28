package com.yuncheng.system.dictionary.dto;

import com.yuncheng.framework.web.page.PageQuery;

/** 数据字典选项分页查询参数。 */
public class DictionaryOptionPageQuery extends PageQuery {

    private String keyword;
    private Boolean enabled;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
