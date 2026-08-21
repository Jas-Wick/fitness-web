package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞表实体（无逻辑删除，取消点赞即物理删除）
 */
@Data
@TableName("t_like")
public class LikeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Integer targetType;
    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
