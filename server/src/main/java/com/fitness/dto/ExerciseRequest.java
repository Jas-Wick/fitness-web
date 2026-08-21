package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 动作库新增/更新请求（管理端）
 */
@Data
public class ExerciseRequest {

    @NotBlank(message = "动作名称不能为空")
    @Size(max = 128, message = "动作名称长度不能超过 128")
    private String name;

    @NotBlank(message = "训练部位不能为空")
    private String bodyPart;

    private String description;
    private String steps;
    private String precautions;
    private String imageUrl;
    private String videoUrl;
    private Integer sortOrder;
    private Integer status;
}
