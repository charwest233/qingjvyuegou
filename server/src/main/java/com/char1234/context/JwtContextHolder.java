package com.char1234.context;

import com.char1234.util.JwtPrincipalType;

/**
 * 当前请求 JWT 上下文（需在拦截器结束前清理）。
 */
public final class JwtContextHolder {

    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private JwtContextHolder() {
    }

    public static void set(Context ctx) {
        CONTEXT.set(ctx);
    }

    public static Context get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public record Context(JwtPrincipalType principalType, Long principalId, String bearerTokenRaw) {

        public boolean isAdmin() {
            return principalType == JwtPrincipalType.ADMIN;
        }

        public boolean isMpUser() {
            return principalType == JwtPrincipalType.MP_USER;
        }
    }
}
