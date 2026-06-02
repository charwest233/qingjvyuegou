package com.char1234.util;

/**
 * JWT 主体类型：管理员与小程序用户隔离，避免同名 ID 误判。
 */
public enum JwtPrincipalType {
    ADMIN,
    MP_USER;

    public static JwtPrincipalType fromClaim(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return JwtPrincipalType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
