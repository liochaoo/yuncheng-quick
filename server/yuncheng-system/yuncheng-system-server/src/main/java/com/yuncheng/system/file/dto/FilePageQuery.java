package com.yuncheng.system.file.dto;

import com.yuncheng.framework.file.enums.FileAccessType;
import com.yuncheng.framework.web.page.PageQuery;

/** 文件管理分页查询参数。 */
public class FilePageQuery extends PageQuery {

    private String originalName;
    private String storagePlatform;
    private String policyCode;
    private FileAccessType accessType;
    private String businessType;

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoragePlatform() {
        return storagePlatform;
    }

    public void setStoragePlatform(String storagePlatform) {
        this.storagePlatform = storagePlatform;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public FileAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(FileAccessType accessType) {
        this.accessType = accessType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
}
