package com.fitness.vo;

import com.fitness.entity.TrainingSetEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 训练动作组响应
 */
@Data
public class TrainingSetVO {

    private Long id;
    private String bodyPart;
    private String exerciseName;
    private BigDecimal weight;
    private Integer reps;
    private Integer sets;

    public static TrainingSetVO from(TrainingSetEntity e) {
        TrainingSetVO vo = new TrainingSetVO();
        vo.setId(e.getId());
        vo.setBodyPart(e.getBodyPart());
        vo.setExerciseName(e.getExerciseName());
        vo.setWeight(e.getWeight());
        vo.setReps(e.getReps());
        vo.setSets(e.getSets());
        return vo;
    }
}
