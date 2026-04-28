package com.xtu.system.modules.system.user.mapper;

import com.xtu.system.modules.system.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserEntity selectUserEntityByUsername(@Param("username") String username);

    int updateLastLoginAt(@Param("id") Long id);
}
