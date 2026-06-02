package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ai_session")
public class AiSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String model;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
