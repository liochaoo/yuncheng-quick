package com.yuncheng.framework.file.service;

/**
 * 某个业务类型关联文件的访问边界。
 *
 * <p>业务模块使用文件三元组时必须注册对应实现。默认方法表示该业务类型当前不需要额外
 * 数据范围控制；后续引入业务数据权限时，只需覆盖对应方法。</p>
 */
public interface FileBusinessAccessHandler {

    /** 当前处理器负责的业务类型。 */
    String businessType();

    /** 校验读取关联文件的权限，默认允许。 */
    default void requireRead(Long businessId) {
    }

    /** 校验关联、解除关联和删除关联文件的权限，默认允许。 */
    default void requireWrite(Long businessId) {
    }
}
