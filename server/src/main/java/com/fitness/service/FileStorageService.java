package com.fitness.service;

import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * 本地文件存储服务：图片上传（头像/动作图片等），统一类型校验与文件名生成
 */
@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp");

    @Value("${fitness.file.upload-dir}")
    private String uploadDir;

    /** 保存图片，返回可访问的 URL（/uploads/xxx） */
    public String storeImage(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持图片文件");
        }
        String ext = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 png/jpg/jpeg/webp 格式");
        }
        String filename = prefix + "_" + System.currentTimeMillis() + "." + ext;
        Path dir = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败");
        }
        return "/uploads/" + filename;
    }

    /** 从原始文件名解析出小写扩展名（无扩展名时返回空串） */
    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dot + 1).toLowerCase();
    }
}
