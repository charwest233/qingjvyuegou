package com.char1234.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * JWT 工具（含管理员 / 小程序用户两类主体）。
 */
public class JwtUtil {

    private static final String SECRET = "mall_admin_secret_key_2026";

    private static final String CLAIM_PT = "pt";

    private static final long EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;

    public static String generateAdminToken(Long adminId, String username) {
        return buildToken(adminId, username, JwtPrincipalType.ADMIN);
    }

    public static String generateMpUserToken(Long userId, String nicknameOrPlaceholder) {
        return buildToken(userId, nicknameOrPlaceholder != null ? nicknameOrPlaceholder : "wx", JwtPrincipalType.MP_USER);
    }

    /**
     * 兼容旧调用，等同于管理员 token。
     */
    public static String generateToken(Long userId, String username) {
        return generateAdminToken(userId, username);
    }

    private static String buildToken(Long userId, String username, JwtPrincipalType pt) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim(CLAIM_PT, pt.name())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(stripBearer(token));
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(stripBearer(token));
            return claims.get("username", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static JwtPrincipalType getPrincipalTypeFromToken(String token) {
        try {
            Claims claims = parseToken(stripBearer(token));
            String pt = claims.get(CLAIM_PT, String.class);
            if (pt == null || pt.isEmpty()) {
                return JwtPrincipalType.ADMIN;
            }
            JwtPrincipalType mapped = JwtPrincipalType.fromClaim(pt);
            return mapped != null ? mapped : JwtPrincipalType.ADMIN;
        } catch (Exception e) {
            return JwtPrincipalType.ADMIN;
        }
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(stripBearer(token));
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    private static String stripBearer(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
