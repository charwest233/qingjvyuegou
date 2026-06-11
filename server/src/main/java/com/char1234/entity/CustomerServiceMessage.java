package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_customer_service_message")
public class CustomerServiceMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    /** 1=用户, 2=客服 */
    private Integer senderType;

    private String content;

    /** text / order */
    private String messageType;

    private String userNickname;

    private LocalDateTime createdAt;
}
