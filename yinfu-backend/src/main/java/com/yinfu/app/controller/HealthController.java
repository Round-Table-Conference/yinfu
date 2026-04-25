package com.yinfu.app.controller;

import com.yinfu.app.common.Constants;
import com.yinfu.app.common.JwtUtil;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.HealthRecordRequest;
import com.yinfu.app.dto.MedicalHistoryRequest;
import com.yinfu.app.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private HealthService healthService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/record")
    public Result<?> getHealthRecord(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return healthService.getLatestHealthRecords(userId);
    }

    @PostMapping("/record")
    public Result<?> addHealthRecord(HttpServletRequest request, @RequestBody HealthRecordRequest recordRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return healthService.addHealthRecord(userId, recordRequest);
    }

    @PutMapping("/record/{id}")
    public Result<?> updateHealthRecord(@PathVariable Long id, HttpServletRequest request, @RequestBody HealthRecordRequest recordRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return healthService.updateHealthRecord(id, userId, recordRequest);
    }

    @GetMapping("/history")
    public Result<?> getMedicalHistory(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return healthService.getMedicalHistory(userId);
    }

    @PostMapping("/history")
    public Result<?> addMedicalHistory(HttpServletRequest request, @RequestBody MedicalHistoryRequest historyRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return healthService.addMedicalHistory(userId, historyRequest);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}