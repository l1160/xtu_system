package com.xtu.system.modules.auth.service;

import com.xtu.system.modules.auth.dto.LoginRequest;
import com.xtu.system.modules.auth.vo.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
