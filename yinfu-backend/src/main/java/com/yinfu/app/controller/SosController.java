package com.yinfu.app.controller;

import com.yinfu.app.common.Constants;
import com.yinfu.app.common.JwtUtil;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.SosTriggerRequest;
import com.yinfu.app.service.SosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/sos")
public class SosController {

    @Autowired
    private SosService sosService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/trigger")
    public Result<?> trigger(HttpServletRequest request, @RequestBody SosTriggerRequest sosRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return sosService.trigger(userId, sosRequest);
    }

    @GetMapping("/records")
    public Result<?> getRecords(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return sosService.getRecords(userId);
    }

    @PutMapping("/records/{id}")
    public Result<?> updateStatus(@PathVariable Long id, HttpServletRequest request, @RequestParam String status) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return sosService.updateStatus(id, userId, status);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}