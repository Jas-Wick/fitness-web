package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 健身动作库表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_exercise")
public class ExerciseEntity extends BaseEntity {

    private String name;
    private String bodyPart;
    private String description;
    private String steps;
    private String precautions;
    private String imageUrl;
    private String videoUrl;
    private Integer viewCount;
    private Integer status;
    private Integer sortOrder;
}
