package com.yuncheng.framework.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.yuncheng.common.constant.OperatorConstants;
import com.yuncheng.common.context.CurrentUserContext;
import java.time.Instant;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/** 统一填充数据库审计字段。 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private final CurrentUserContext currentUserContext;

    public AuditMetaObjectHandler(CurrentUserContext currentUserContext) {
        this.currentUserContext = currentUserContext;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        Instant now = Instant.now();
        Long operatorId = currentOperatorId();
        setFieldValByName("createdAt", now, metaObject);
        setFieldValByName("createdBy", operatorId, metaObject);
        setFieldValByName("updatedAt", now, metaObject);
        setFieldValByName("updatedBy", operatorId, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updatedAt", Instant.now(), metaObject);
        setFieldValByName("updatedBy", currentOperatorId(), metaObject);
    }

    private Long currentOperatorId() {
        return currentUserContext.findUserId().orElse(OperatorConstants.SYSTEM_OPERATOR_ID);
    }
}
