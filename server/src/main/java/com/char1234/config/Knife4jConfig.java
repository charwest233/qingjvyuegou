package com.char1234.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("青桔悦购 API 文档")
                        .version("1.0.0")
                        .description("青桔悦购前后端分离接口文档\n\n" +
                                "## 接口分组说明\n" +
                                "- **前台接口**：面向消费者（前台 Vue3 应用），需 mp_user 类型 Token\n" +
                                "- **后台接口**：面向管理员（admin Vue3 应用），需 admin 类型 Token\n" +
                                "- **公共接口**：无登录也可调用（商品浏览、分类、登录注册）\n")
                        .contact(new Contact()
                                .name("青桔悦购团队")
                                .url("https://github.com/example"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

    @Bean
    public GroupedOpenApi frontApi() {
        return GroupedOpenApi.builder()
                .group("前台接口")
                .displayName("前台接口 - 消费者端")
                .pathsToMatch(
                        "/api/product/list",
                        "/api/product/hot",
                        "/api/product/*",
                        "/api/category/list",
                        "/api/category/tree",
                        "/api/cart/**",
                        "/api/user/wx-login",
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/profile",
                        "/api/user/addresses/**",
                        "/api/user/favorites/**",
                        "/api/order",
                        "/api/order/list",
                        "/api/order/*",
                        "/api/order/*/pay",
                        "/api/order/*/cancel",
                        "/api/order/*/confirm"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("后台接口")
                .displayName("后台接口 - 管理端")
                .pathsToMatch(
                        "/api/admin/**",
                        "/api/dashboard/**",
                        "/api/product/**",
                        "/api/category/**",
                        "/api/user/list",
                        "/api/user/statistics",
                        "/api/order/list",
                        "/api/order/statistics",
                        "/api/order/*/ship",
                        "/api/order/*/status"
                )
                .build();
    }
}
