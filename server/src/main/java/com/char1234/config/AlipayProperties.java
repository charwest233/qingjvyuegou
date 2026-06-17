package com.char1234.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /** 支付宝沙箱 APPID */
    private String appId;

    /** 应用私钥（PKCS8 格式 RSA2） */
    private String appPrivateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 异步通知 URL（外网可访问） */
    private String notifyUrl;

    /** 同步跳转 URL */
    private String returnUrl;

    /** 支付宝网关（沙箱） */
    private String gatewayUrl;

    /** 签名类型 */
    private String signType = "RSA2";

    /** 编码格式 */
    private String charset = "UTF-8";

    /** 格式 */
    private String format = "json";

    /** 前端地址（用于同步跳转重定向） */
    private String frontendUrl;
}
