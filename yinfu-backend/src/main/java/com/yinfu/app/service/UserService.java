package com.yinfu.app.service;

import com.yinfu.app.common.Result;
import com.yinfu.app.dto.UserProfileRequest;
import com.yinfu.app.dto.PasswordRequest;
import com.yinfu.app.entity.User;
import com.yinfu.app.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Result<?> getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<?> updateProfile(Long userId, UserProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getBloodType() != null) user.setBloodType(request.getBloodType());
        if (request.getAllergy() != null) user.setAllergy(request.getAllergy());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        userMapper.updateById(user);
        return Result.success("更新成功");
    }

    public Result<?> updatePassword(Long userId, PasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Result.error(400, "原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        return Result.success("密码修改成功");
    }
}