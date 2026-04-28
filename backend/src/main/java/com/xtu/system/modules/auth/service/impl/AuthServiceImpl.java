package com.xtu.system.modules.auth.service.impl;

import com.xtu.system.common.exception.BusinessException;
import com.xtu.system.common.util.RequestContextUtils;
import com.xtu.system.common.util.UserAgentUtils;
import com.xtu.system.config.security.JwtTokenProvider;
import com.xtu.system.modules.auth.dto.LoginRequest;
import com.xtu.system.modules.auth.service.AuthService;
import com.xtu.system.modules.auth.vo.LoginResponse;
import com.xtu.system.modules.system.log.entity.LoginLogEntity;
import com.xtu.system.modules.system.log.service.LogService;
import com.xtu.system.modules.system.user.entity.UserEntity;
import com.xtu.system.modules.system.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LogService logService;

    public AuthServiceImpl(
        UserMapper userMapper,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        LogService logService
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.logService = logService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        HttpServletRequest currentRequest = RequestContextUtils.getCurrentRequest();
        UserEntity user = userMapper.selectUserEntityByUsername(request.getUsername());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            recordLoginLog(currentRequest, null, request.getUsername(), 0, "账号或密码错误");
            throw new BusinessException("账号或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordLoginLog(currentRequest, user, request.getUsername(), 0, "账号或密码错误");
            throw new BusinessException("账号或密码错误");
        }

        userMapper.updateLastLoginAt(user.getId());
        recordLoginLog(currentRequest, user, user.getUsername(), 1, null);
        return new LoginResponse(
            jwtTokenProvider.generateToken(user.getId(), user.getUsername()),
            "Bearer",
            jwtTokenProvider.getExpirationSeconds()
        );
    }

    private void recordLoginLog(
        HttpServletRequest request,
        UserEntity user,
        String username,
        int loginStatus,
        String failReason
    ) {
        try {
            String userAgent = request == null ? null : request.getHeader("User-Agent");
            LoginLogEntity logEntity = new LoginLogEntity();
            logEntity.setUserId(user == null ? null : user.getId());
            logEntity.setUsername(username);
            logEntity.setRealName(user == null ? null : user.getRealName());
            logEntity.setLoginType("password");
            logEntity.setLoginIp(RequestContextUtils.resolveClientIp(request));
            logEntity.setLoginLocation("本地环境");
            logEntity.setUserAgent(userAgent);
            logEntity.setBrowser(UserAgentUtils.resolveBrowser(userAgent));
            logEntity.setOs(UserAgentUtils.resolveOs(userAgent));
            logEntity.setLoginStatus(loginStatus);
            logEntity.setFailReason(failReason);
            logService.recordLogin(logEntity);
        } catch (Exception ignored) {
        }
    }
}
