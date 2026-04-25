package com.yinfu.app.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SosTriggerRequest {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
}