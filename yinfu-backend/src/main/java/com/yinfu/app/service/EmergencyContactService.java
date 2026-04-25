package com.yinfu.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.EmergencyContactRequest;
import com.yinfu.app.entity.EmergencyContact;
import com.yinfu.app.mapper.EmergencyContactMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmergencyContactService {

    @Autowired
    private EmergencyContactMapper contactMapper;

    public Result<?> getList(Long userId) {
        LambdaQueryWrapper<EmergencyContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmergencyContact::getUserId, userId);
        wrapper.orderByDesc(EmergencyContact::getIsPrimary)
                .orderByDesc(EmergencyContact::getCreatedAt);
        List<EmergencyContact> list = contactMapper.selectList(wrapper);
        return Result.success(list);
    }

    public Result<?> add(Long userId, EmergencyContactRequest request) {
        EmergencyContact contact = new EmergencyContact();
        contact.setUserId(userId);
        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setRelationship(request.getRelationship());
        contact.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : 0);
        contact.setCreatedAt(LocalDateTime.now());

        contactMapper.insert(contact);
        return Result.success("添加成功");
    }

    public Result<?> update(Long id, Long userId, EmergencyContactRequest request) {
        EmergencyContact contact = contactMapper.selectById(id);
        if (contact == null || !contact.getUserId().equals(userId)) {
            return Result.error(404, "联系人不存在");
        }

        if (request.getName() != null) contact.setName(request.getName());
        if (request.getPhone() != null) contact.setPhone(request.getPhone());
        if (request.getRelationship() != null) contact.setRelationship(request.getRelationship());
        if (request.getIsPrimary() != null) contact.setIsPrimary(request.getIsPrimary());

        contactMapper.updateById(contact);
        return Result.success("更新成功");
    }

    public Result<?> delete(Long id, Long userId) {
        EmergencyContact contact = contactMapper.selectById(id);
        if (contact == null || !contact.getUserId().equals(userId)) {
            return Result.error(404, "联系人不存在");
        }

        contactMapper.deleteById(id);
        return Result.success("删除成功");
    }
}