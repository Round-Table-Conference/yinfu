package com.yinfu.app.dto;

import lombok.Data;

@Data
public class MedicalHistoryRequest {
    private String diseaseName;
    private String diagnosisDate;
    private String currentStatus;
    private String description;
}