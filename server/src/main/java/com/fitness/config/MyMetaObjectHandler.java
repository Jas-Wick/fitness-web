package com.fitness.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 字段自动填充：createTime / updateTime
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 使用 fillStrategy 仅在字段存在且为 null 时填充，兼容无 update_time 的表
        this.fillStrategy(metaObject, "createTime", LocalDateTime.now());
        this.fillStrategy(metaObject, "updateTime", LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.fillStrategy(metaObject, "updateTime", LocalDateTime.now());
    }
}
