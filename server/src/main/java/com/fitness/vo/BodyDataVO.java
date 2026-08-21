package com.fitness.vo;

import com.fitness.entity.BodyDataEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 身体数据记录响应
 */
@Data
public class BodyDataVO {

    private Long id;
    private LocalDate recordDate;
    private BigDecimal weight;
    private BigDecimal bodyFatRate;
    private BigDecimal muscleMass;
    private BigDecimal chest;
    private BigDecimal waist;
    private BigDecimal hip;
    private BigDecimal bmi;
    private String remark;
    private LocalDateTime createTime;

    public static BodyDataVO from(BodyDataEntity e) {
        BodyDataVO vo = new BodyDataVO();
        vo.setId(e.getId());
        vo.setRecordDate(e.getRecordDate());
        vo.setWeight(e.getWeight());
        vo.setBodyFatRate(e.getBodyFatRate());
        vo.setMuscleMass(e.getMuscleMass());
        vo.setChest(e.getChest());
        vo.setWaist(e.getWaist());
        vo.setHip(e.getHip());
        vo.setBmi(e.getBmi());
        vo.setRemark(e.getRemark());
        vo.setCreateTime(e.getCreateTime());
        return vo;
    }
}
