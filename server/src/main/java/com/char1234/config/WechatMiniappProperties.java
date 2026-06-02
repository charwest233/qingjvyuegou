package com.char1234.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatMiniappProperties {

    private String appid = "";

    private String secret = "";

    /**
     * 未配置 secret 时使用 mock（开发联调）。
     */
    private boolean mockWhenSecretEmpty = true;
}
