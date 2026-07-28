package com.yuncheng.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** 登录专用用户查询，始终直接访问数据库。 */
@Service
public class UserLoginQueryService {

    private final SystemUserMapper userMapper;

    public UserLoginQueryService(SystemUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public SystemUser findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<SystemUser>()
                .eq(SystemUser::getUsername, username.trim().toLowerCase(Locale.ROOT)));
    }
}
