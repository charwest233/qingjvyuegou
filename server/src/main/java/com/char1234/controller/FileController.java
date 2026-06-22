package com.char1234.controller;

import com.char1234.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传 Controller
 * POST /api/upload/image → 保存图片到 uploads 目录，返回访问 URL
 */
@RestController
@RequestMapping("/api/upload")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只允许上传图片文件");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过 5MB");
        }

        try {
            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + ext;

            // 确保目录存在
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            // 保存文件
            File destFile = uploadPath.resolve(fileName).toFile();
            file.transferTo(destFile);

            // 返回 URL
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + fileName);
            result.put("fileName", fileName);
            return Result.success(result);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
