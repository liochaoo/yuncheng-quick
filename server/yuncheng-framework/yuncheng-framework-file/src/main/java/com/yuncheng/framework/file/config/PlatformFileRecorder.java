package com.yuncheng.framework.file.config;

import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;

/**
 * 文件组件记录器。
 *
 * 平台使用自己的文件表管理元数据，因此这里只接管组件回调，避免产生第二套记录模型。
 */
@Component
public class PlatformFileRecorder implements FileRecorder {

    @Override
    public boolean save(FileInfo fileInfo) {
        return true;
    }

    @Override
    public void update(FileInfo fileInfo) {
    }

    @Override
    public FileInfo getByUrl(String url) {
        return null;
    }

    @Override
    public boolean delete(String url) {
        return true;
    }

    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
    }

    @Override
    public void deleteFilePartByUploadId(String uploadId) {
    }
}
