package com.yuncheng.framework.file.service;

import com.yuncheng.framework.file.dto.FileAssociationRequest;
import com.yuncheng.framework.file.dto.FileRecord;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/** 通用文件服务。 */
public interface FileService {

    FileRecord upload(
            MultipartFile file,
            String policyCode,
            String businessType,
            Long businessId,
            String businessPosition,
            Integer sortOrder
    );

    FileRecord get(Long id);

    List<FileRecord> list(String businessType, Long businessId, String businessPosition);

    void associate(Long id, FileAssociationRequest request);

    void removeAssociation(Long id);

    FileContent openContent(Long id, boolean publicOnly);

    void delete(Long id);
}
