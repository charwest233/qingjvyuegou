package com.char1234.service;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.char1234.config.WechatMiniappProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 微信小程序 jscode2session（mock 兜底便于无 AppSecret 调试）。
 */
@Service
@RequiredArgsConstructor
public class WechatMiniappCodeService {

    private final WechatMiniappProperties props;

    public WxSessionPayload exchange(String jsCode) {
        if (!StringUtils.hasText(jsCode)) {
            throw new IllegalArgumentException("登录 code 不能为空");
        }
        if (props.isMockWhenSecretEmpty() && !StringUtils.hasText(props.getSecret())) {
            String hash = DigestUtil.md5Hex(jsCode.getBytes(StandardCharsets.UTF_8));
            return new WxSessionPayload("mock_mp_" + hash, null);
        }
        if (!StringUtils.hasText(props.getAppid()) || !StringUtils.hasText(props.getSecret())) {
            throw new IllegalStateException("未配置 wechat.miniapp.appid 或 secret");
        }
        java.nio.charset.Charset utf8 = StandardCharsets.UTF_8;
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + URLEncoder.encode(props.getAppid(), utf8)
                + "&secret=" + URLEncoder.encode(props.getSecret(), utf8)
                + "&js_code=" + URLEncoder.encode(jsCode, utf8)
                + "&grant_type=" + URLEncoder.encode("authorization_code", utf8);
        String body = HttpUtil.get(url, 8000);
        JSONObject jo = JSONUtil.parseObj(body);
        if (jo.containsKey("errcode") && jo.getInt("errcode") != 0) {
            String msg = jo.getStr("errmsg", "未知错误");
            throw new IllegalStateException("微信登录失败: " + msg);
        }
        String openid = jo.getStr("openid");
        if (!StringUtils.hasText(openid)) {
            throw new IllegalStateException("微信未返回 openid");
        }
        String unionId = jo.getStr("unionid");
        return new WxSessionPayload(openid, unionId);
    }

    public record WxSessionPayload(String openid, String unionId) {
    }
}
