package com.yuncheng.framework.file.service;

import com.yuncheng.framework.file.dto.FileRecord;
import java.io.OutputStream;
import java.util.function.Consumer;

/** 文件内容及其流式写出入口。 */
public record FileContent(
        FileRecord record,
        Consumer<OutputStream> writer
) {

    public void writeTo(OutputStream outputStream) {
        writer.accept(outputStream);
    }
}
