package com.xtu.system.modules.system.log.mapper;

import com.xtu.system.modules.system.log.entity.LoginLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper {

    int insertLoginLog(LoginLogEntity entity);
}
