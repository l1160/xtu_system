package com.xtu.system.common.util;

public final class UserAgentUtils {

    private UserAgentUtils() {
    }

    public static String resolveBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }

        String normalized = userAgent.toLowerCase();
        if (normalized.contains("edg")) {
            return "Edge";
        }
        if (normalized.contains("chrome")) {
            return "Chrome";
        }
        if (normalized.contains("firefox")) {
            return "Firefox";
        }
        if (normalized.contains("safari")) {
            return "Safari";
        }
        return "Unknown";
    }

    public static String resolveOs(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }

        String normalized = userAgent.toLowerCase();
        if (normalized.contains("windows")) {
            return "Windows";
        }
        if (normalized.contains("mac os")) {
            return "macOS";
        }
        if (normalized.contains("android")) {
            return "Android";
        }
        if (normalized.contains("iphone") || normalized.contains("ipad") || normalized.contains("ios")) {
            return "iOS";
        }
        if (normalized.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }
}
