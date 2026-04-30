package com.xtu.system.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xtu.system.common.util.RequestContextUtils;
import com.xtu.system.modules.system.log.entity.OperationLogEntity;
import com.xtu.system.modules.system.log.service.LogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class OperationLogFilter extends OncePerRequestFilter {

    private static final String REDACTED = "******";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
        "password",
        "oldPassword",
        "newPassword",
        "confirmPassword"
    );

    private final LogService logService;
    private final ObjectMapper objectMapper;

    public OperationLogFilter(LogService logService, ObjectMapper objectMapper) {
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        return !uri.startsWith("/api/")
            || "GET".equalsIgnoreCase(method)
            || "OPTIONS".equalsIgnoreCase(method)
            || "/api/auth/login".equals(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        Exception requestException = null;

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception exception) {
            requestException = exception;
            throw exception;
        } finally {
            recordOperationLog(requestWrapper, responseWrapper, requestException);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void recordOperationLog(
        ContentCachingRequestWrapper request,
        ContentCachingResponseWrapper response,
        Exception requestException
    ) {
        try {
            OperationLogEntity entity = new OperationLogEntity();
            entity.setModuleName(resolveModuleName(request.getRequestURI()));
            entity.setBizType(resolveBizType(request.getRequestURI()));
            entity.setBizId(resolveBizId(request.getRequestURI()));
            entity.setOperationType(resolveOperationType(request.getMethod(), request.getRequestURI()));
            entity.setRequestUri(request.getRequestURI());
            entity.setRequestMethod(request.getMethod());
            entity.setRequestParams(resolveRequestParams(request));
            entity.setOperatorIp(RequestContextUtils.resolveClientIp(request));
            entity.setResultStatus(resolveResultStatus(response, requestException));
            entity.setErrorMessage(requestException == null ? resolveErrorMessage(response) : requestException.getMessage());

            try {
                entity.setOperatorId(SecurityUtils.getCurrentUserId());
                entity.setOperatorName(SecurityUtils.getCurrentUsername());
            } catch (Exception ignored) {
            }

            logService.recordOperation(entity);
        } catch (Exception ignored) {
        }
    }

    private String resolveModuleName(String uri) {
        if (uri.startsWith("/api/workflow")) {
            return "workflow";
        }
        if (uri.startsWith("/api/attachments")) {
            return "attachment";
        }
        if (uri.startsWith("/api/login-logs")) {
            return "login-log";
        }
        if (uri.startsWith("/api/operation-logs")) {
            return "operation-log";
        }
        String[] segments = uri.split("/");
        return segments.length > 2 ? segments[2] : "api";
    }

    private String resolveBizType(String uri) {
        String moduleName = resolveModuleName(uri);
        if ("users".equals(moduleName)) {
            return "user";
        }
        if ("departments".equals(moduleName)) {
            return "department";
        }
        if ("students".equals(moduleName)) {
            return "student";
        }
        if ("teachers".equals(moduleName)) {
            return "teacher";
        }
        if ("courses".equals(moduleName)) {
            return "course";
        }
        if ("notices".equals(moduleName)) {
            return "notice";
        }
        if ("applications".equals(moduleName)) {
            return "application";
        }
        return moduleName;
    }

    private Long resolveBizId(String uri) {
        String[] segments = uri.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            try {
                return Long.parseLong(segments[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String resolveOperationType(String method, String uri) {
        if ("POST".equalsIgnoreCase(method)) {
            return uri.contains("/upload") ? "upload" : "create";
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            return "delete";
        }
        if ("PUT".equalsIgnoreCase(method)) {
            if (uri.endsWith("/approve")) {
                return "approve";
            }
            if (uri.endsWith("/reject")) {
                return "reject";
            }
            if (uri.endsWith("/revoke")) {
                return "revoke";
            }
            if (uri.endsWith("/publish")) {
                return "publish";
            }
            if (uri.endsWith("/status")) {
                return "status";
            }
            if (uri.endsWith("/reset-password")) {
                return "reset-password";
            }
            return "update";
        }
        return method.toLowerCase();
    }

    private String resolveRequestParams(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            return "[multipart/form-data]";
        }
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return request.getQueryString();
        }
        String value = new String(content, StandardCharsets.UTF_8);
        return sanitizeRequestParams(value);
    }

    private String sanitizeRequestParams(String value) {
        try {
            JsonNode root = objectMapper.readTree(value);
            maskSensitiveFields(root);
            String sanitized = objectMapper.writeValueAsString(root);
            return sanitized.length() > 1000 ? sanitized.substring(0, 1000) : sanitized;
        } catch (Exception ignored) {
            return value.length() > 1000 ? value.substring(0, 1000) : value;
        }
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode child = objectNode.get(fieldName);
                if (isSensitiveKey(fieldName) && child != null && !child.isNull()) {
                    objectNode.put(fieldName, REDACTED);
                    return;
                }
                maskSensitiveFields(child);
            });
            return;
        }
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode item : arrayNode) {
                maskSensitiveFields(item);
            }
        }
    }

    private boolean isSensitiveKey(String fieldName) {
        return SENSITIVE_KEYS.stream().anyMatch(item -> item.equalsIgnoreCase(fieldName));
    }

    private int resolveResultStatus(ContentCachingResponseWrapper response, Exception requestException) {
        if (requestException != null) {
            return 0;
        }
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return response.getStatus() >= 400 ? 0 : 1;
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            return root.path("code").asInt(500) == 200 ? 1 : 0;
        } catch (Exception ignored) {
            return response.getStatus() >= 400 ? 0 : 1;
        }
    }

    private String resolveErrorMessage(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            if (root.path("code").asInt(200) == 200) {
                return null;
            }
            return root.path("message").asText(null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
