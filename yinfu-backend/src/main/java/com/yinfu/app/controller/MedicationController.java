package com.yinfu.app.controller;

import com.yinfu.app.common.Constants;
import com.yinfu.app.common.JwtUtil;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.MedicationReminderRequest;
import com.yinfu.app.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/medication")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<?> getList(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return medicationService.getList(userId);
    }

    @GetMapping("/today")
    public Result<?> getToday(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return medicationService.getToday(userId);
    }

    @PostMapping
    public Result<?> add(HttpServletRequest request, @RequestBody MedicationReminderRequest reminderRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return medicationService.add(userId, reminderRequest);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, HttpServletRequest request, @RequestBody MedicationReminderRequest reminderRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return medicationService.update(id, userId, reminderRequest);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return medicationService.delete(id, userId);
    }

    @PostMapping("/{id}/take")
    public Result<?> take(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return medicationService.take(id, userId);
    }

    @DeleteMapping("/{id}/take")
    public Result<?> untake(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return medicationService.untake(id, userId);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}