package com.yinfu.app.dto;

import lombok.Data;

@Data
public class HealthRecordRequest {
    private String recordType;
    private String value;
    private String unit;
    private String remark;
}