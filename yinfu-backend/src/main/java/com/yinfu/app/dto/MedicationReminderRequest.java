package com.yinfu.app.dto;

import lombok.Data;

@Data
public class MedicationReminderRequest {
    private String name;
    private String dosage;
    private String time;
    private String frequency;
    private String remark;
    private Integer isActive;
}