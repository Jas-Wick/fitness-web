package com.fitness.vo;

import com.fitness.entity.ExerciseEntity;
import lombok.Data;

/**
 * 健身动作响应
 */
@Data
public class ExerciseVO {

    private Long id;
    private String name;
    private String bodyPart;
    private String description;
    private String steps;
    private String precautions;
    private String imageUrl;
    private String videoUrl;
    private Integer viewCount;

    public static ExerciseVO from(ExerciseEntity e) {
        ExerciseVO vo = new ExerciseVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setBodyPart(e.getBodyPart());
        vo.setDescription(e.getDescription());
        vo.setSteps(e.getSteps());
        vo.setPrecautions(e.getPrecautions());
        vo.setImageUrl(e.getImageUrl());
        vo.setVideoUrl(e.getVideoUrl());
        vo.setViewCount(e.getViewCount());
        return vo;
    }
}
