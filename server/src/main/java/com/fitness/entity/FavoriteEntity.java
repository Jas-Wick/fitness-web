package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏表实体（无逻辑删除，取消收藏即物理删除）
 */
@Data
@TableName("t_favorite")
public class FavoriteEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long postId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
