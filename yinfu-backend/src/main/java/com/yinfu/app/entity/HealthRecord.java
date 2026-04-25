package com.yinfu.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_health_record")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String recordType;
    private String value;
    private String unit;
    private LocalDateTime measuredAt;
    private String remark;
    private LocalDateTime createdAt;
}