package com.char1234.config;

import com.char1234.context.JwtContextHolder;
import com.char1234.util.JwtPrincipalType;
import com.char1234.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * JWT 鉴权：公开读接口放行；其余需合法 token；管理员与小程序用户路径隔离。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String path = request.getServletPath();

        if (isPublic(path, method)) {
            return true;
        }

        String bearer = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearer) || !bearer.startsWith("Bearer ")) {
            writeJson(response, 401, "未登录或缺少 Token");
            return false;
        }

        String raw = bearer.substring(7);
        if (!JwtUtil.validateToken(raw)) {
            writeJson(response, 401, "登录已失效");
            return false;
        }

        Long id = JwtUtil.getUserIdFromToken(raw);
        JwtPrincipalType pt = JwtUtil.getPrincipalTypeFromToken(raw);
        if (id == null || pt == null) {
            writeJson(response, 401, "Token 无效");
            return false;
        }

        JwtContextHolder.set(new JwtContextHolder.Context(pt, id, raw));

        if (!authorize(path, method, pt)) {
            writeJson(response, 403, "无访问权限");
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        JwtContextHolder.clear();
    }

    private static boolean isPublic(String path, String method) {
        if ("POST".equals(method) && "/api/admin/login".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && "/api/user/wx-login".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && "/api/user/login".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && "/api/user/register".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && "/api/sms/send-code".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && "/api/sms/verify-code".equals(path)) {
            return true;
        }
        if (!"GET".equals(method)) {
            return false;
        }
        if ("/api/category/list".equals(path) || "/api/category/tree".equals(path)) {
            return true;
        }
        if ("/api/product/list".equals(path) || "/api/product/hot".equals(path)) {
            return true;
        }
        if (path.matches("^/api/product/\\d+$")) {
            return true;
        }
        // 评价公开 GET 接口：查看商品评价
        return path.matches("^/api/review/product/\\d+$");
    }

    private static boolean authorize(String path, String method, JwtPrincipalType pt) {
        if (pt == JwtPrincipalType.ADMIN) {
            return !("POST".equals(method) && "/api/order".equals(path));
        }

        if (pt == JwtPrincipalType.MP_USER) {
            if (path.startsWith("/api/dashboard")) {
                return false;
            }
            if (path.equals("/api/user/list") || path.equals("/api/user/statistics")) {
                return false;
            }
            if ("PUT".equals(method) && path.matches("^/api/user/\\d+/status$")) {
                return false;
            }
            if ("GET".equals(method) && path.matches("^/api/user/\\d+$")) {
                return false;
            }

            if (path.startsWith("/api/category")) {
                return "GET".equals(method)
                        && ("/api/category/list".equals(path) || "/api/category/tree".equals(path));
            }

            if (path.startsWith("/api/product")) {
                return "GET".equals(method)
                        && ("/api/product/list".equals(path)
                        || "/api/product/hot".equals(path)
                        || path.matches("^/api/product/\\d+$"));
            }

            if ("PUT".equals(method) && "/api/user/profile".equals(path)) {
                return true;
            }

            if (isMpUserAddressRoutes(path, method)) {
                return true;
            }
            if (isMpFavoriteRoutes(path, method)) {
                return true;
            }

            // 评价用户接口
            if (isMpReviewRoutes(path, method)) {
                return true;
            }

            // 购物车接口
            if (path.startsWith("/api/cart")) {
                return true;
            }

            // 短信验证码接口
            if (path.startsWith("/api/sms")) {
                return true;
            }

            if (path.startsWith("/api/order")) {
                if ("GET".equals(method) && "/api/order/statistics".equals(path)) {
                    return false;
                }
                if (path.matches("^/api/order/\\d+/ship$")) {
                    return false;
                }
                if (path.matches("^/api/order/\\d+/status$")) {
                    return false;
                }
                if ("GET".equals(method) && "/api/order/list".equals(path)) {
                    return true;
                }
                if ("GET".equals(method) && path.matches("^/api/order/\\d+$")) {
                    return true;
                }
                if ("POST".equals(method) && "/api/order".equals(path)) {
                    return true;
                }
                // 用户可删除自己的已完成/已取消订单
                if ("DELETE".equals(method) && path.matches("^/api/order/\\d+$")) {
                    return true;
                }
                return path.matches("^/api/order/\\d+/pay$") || path.matches("^/api/order/\\d+/cancel$") || path.matches("^/api/order/\\d+/confirm$");
            }

            return false;
        }

        return false;
    }

    private static boolean isMpUserAddressRoutes(String path, String method) {
        if (!path.startsWith("/api/user/addresses")) {
            return false;
        }
        if ("GET".equals(method) && "/api/user/addresses/default".equals(path)) {
            return true;
        }
        if ("GET".equals(method) && "/api/user/addresses".equals(path)) {
            return true;
        }
        if ("GET".equals(method) && path.matches("^/api/user/addresses/\\d+$")) {
            return true;
        }
        if ("POST".equals(method) && "/api/user/addresses".equals(path)) {
            return true;
        }
        if ("PUT".equals(method) && path.matches("^/api/user/addresses/\\d+$")) {
            return true;
        }
        if ("DELETE".equals(method) && path.matches("^/api/user/addresses/\\d+$")) {
            return true;
        }
        return "PUT".equals(method) && path.matches("^/api/user/addresses/\\d+/default$");
    }

    private static boolean isMpFavoriteRoutes(String path, String method) {
        if (!path.startsWith("/api/user/favorites")) {
            return false;
        }
        if ("GET".equals(method) && "/api/user/favorites".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && "/api/user/favorites".equals(path)) {
            return true;
        }
        if ("GET".equals(method) && path.matches("^/api/user/favorites/check/\\d+$")) {
            return true;
        }
        return "DELETE".equals(method) && path.matches("^/api/user/favorites/\\d+$");
    }

    private static boolean isMpReviewRoutes(String path, String method) {
        if (!path.startsWith("/api/review")) {
            return false;
        }
        // 提交评价
        if ("POST".equals(method) && "/api/review".equals(path)) {
            return true;
        }
        // 查询订单评价状态
        if ("GET".equals(method) && path.matches("^/api/review/order/\\d+$")) {
            return true;
        }
        // 用户自己的评价列表
        if ("GET".equals(method) && "/api/review/user".equals(path)) {
            return true;
        }
        // 用户删除自己的评价
        if ("DELETE".equals(method) && path.matches("^/api/review/\\d+$")) {
            return true;
        }
        return false;
    }

    private static void writeJson(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = String.format("{\"code\":%d,\"message\":\"%s\"}", code, escapeJson(message));
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
