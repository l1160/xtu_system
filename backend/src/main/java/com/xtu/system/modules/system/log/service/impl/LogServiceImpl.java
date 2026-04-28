package com.xtu.system.modules.system.log.service.impl;

import com.xtu.system.modules.system.log.entity.LoginLogEntity;
import com.xtu.system.modules.system.log.mapper.LogMapper;
import com.xtu.system.modules.system.log.service.LogService;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;

    public LogServiceImpl(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public void recordLogin(LoginLogEntity entity) {
        logMapper.insertLoginLog(entity);
    }
}
