package com.yinfu.app.dto;

import lombok.Data;

@Data
public class EmergencyContactRequest {
    private String name;
    private String phone;
    private String relationship;
    private Integer isPrimary;
}