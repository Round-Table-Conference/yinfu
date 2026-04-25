package com.yinfu.app.controller;

import com.yinfu.app.common.Constants;
import com.yinfu.app.common.JwtUtil;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.LoginRequest;
import com.yinfu.app.dto.RegisterRequest;
import com.yinfu.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return authService.getUserInfo(userId);
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("登出成功");
    }
}