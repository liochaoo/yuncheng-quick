package com.yuncheng.framework.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;

/** 平台文件记录。 */
@TableName("platform_file")
public class PlatformFile extends BaseEntity {

    private String storagePlatform;
    private String objectKey;
    private String originalName;
    private String fileExtension;
    private String contentType;
    private Long fileSize;
    private String sha256;
    private String policyCode;
    private String accessType;
    private String businessType;
    private Long businessId;
    private String businessPosition;
    private Integer sortOrder;

    public String getStoragePlatform() {
        return storagePlatform;
    }

    public void setStoragePlatform(String storagePlatform) {
        this.storagePlatform = storagePlatform;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessPosition() {
        return businessPosition;
    }

    public void setBusinessPosition(String businessPosition) {
        this.businessPosition = businessPosition;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
