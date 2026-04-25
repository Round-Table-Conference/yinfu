package com.yinfu.app.controller;

import com.yinfu.app.common.Constants;
import com.yinfu.app.common.JwtUtil;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.PasswordRequest;
import com.yinfu.app.dto.UserProfileRequest;
import com.yinfu.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/profile")
    public Result<?> getProfile(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(HttpServletRequest request, @RequestBody UserProfileRequest profileRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return userService.updateProfile(userId, profileRequest);
    }

    @PutMapping("/password")
    public Result<?> updatePassword(HttpServletRequest request, @RequestBody PasswordRequest passwordRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return userService.updatePassword(userId, passwordRequest);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}