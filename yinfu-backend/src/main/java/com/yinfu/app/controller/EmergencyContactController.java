package com.yinfu.app.controller;

import com.yinfu.app.common.Constants;
import com.yinfu.app.common.JwtUtil;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.EmergencyContactRequest;
import com.yinfu.app.service.EmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/emergency-contact")
public class EmergencyContactController {

    @Autowired
    private EmergencyContactService contactService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<?> getList(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return contactService.getList(userId);
    }

    @PostMapping
    public Result<?> add(HttpServletRequest request, @RequestBody EmergencyContactRequest contactRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return contactService.add(userId, contactRequest);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, HttpServletRequest request, @RequestBody EmergencyContactRequest contactRequest) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return contactService.update(id, userId, contactRequest);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return contactService.delete(id, userId);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}