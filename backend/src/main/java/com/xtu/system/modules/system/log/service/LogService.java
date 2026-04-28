package com.xtu.system.modules.system.log.service;

import com.xtu.system.modules.system.log.entity.LoginLogEntity;

public interface LogService {

    void recordLogin(LoginLogEntity entity);
}
