package com.yinfu.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_medical_history")
public class MedicalHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String diseaseName;
    private LocalDate diagnosisDate;
    private String currentStatus;
    private String description;
    private LocalDateTime createdAt;
}