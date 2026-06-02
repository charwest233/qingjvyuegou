package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ai_message")
public class AiMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private String role;

    private String content;

    private String imageUrl;

    private LocalDateTime createdAt;
}
