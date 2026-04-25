package com.yinfu.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_medication_reminder")
public class MedicationReminder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String dosage;
    private String time;
    private String frequency;
    private String remark;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isTaken;
}
