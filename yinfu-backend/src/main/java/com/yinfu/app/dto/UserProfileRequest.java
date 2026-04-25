package com.yinfu.app.dto;

import lombok.Data;

@Data
public class UserProfileRequest {
    private String realName;
    private Integer age;
    private String gender;
    private String bloodType;
    private String allergy;
    private String phone;
    private String email;
    private String avatarUrl;
}