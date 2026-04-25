package com.yinfu.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinfu.app.common.Constants;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.SosTriggerRequest;
import com.yinfu.app.entity.EmergencyContact;
import com.yinfu.app.entity.SosRecord;
import com.yinfu.app.mapper.EmergencyContactMapper;
import com.yinfu.app.mapper.SosRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SosService {

    @Autowired
    private SosRecordMapper sosRecordMapper;

    @Autowired
    private EmergencyContactMapper contactMapper;

    public Result<?> trigger(Long userId, SosTriggerRequest request) {
        SosRecord record = new SosRecord();
        record.setUserId(userId);
        record.setLatitude(request.getLatitude());
        record.setLongitude(request.getLongitude());
        record.setAddress(request.getAddress());
        record.setStatus(Constants.SosStatus.TRIGGERED);
        record.setCreatedAt(LocalDateTime.now());

        sosRecordMapper.insert(record);

        LambdaQueryWrapper<EmergencyContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmergencyContact::getUserId, userId);
        wrapper.eq(EmergencyContact::getIsPrimary, 1);
        wrapper.last("LIMIT 1");
        EmergencyContact primaryContact = contactMapper.selectOne(wrapper);

        return Result.success(new SosResult(record, primaryContact));
    }

    public Result<?> getRecords(Long userId) {
        LambdaQueryWrapper<SosRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SosRecord::getUserId, userId);
        wrapper.orderByDesc(SosRecord::getCreatedAt);
        List<SosRecord> list = sosRecordMapper.selectList(wrapper);
        return Result.success(list);
    }

    public Result<?> updateStatus(Long id, Long userId, String status) {
        SosRecord record = sosRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error(404, "记录不存在");
        }

        record.setStatus(status);
        sosRecordMapper.updateById(record);
        return Result.success("状态更新成功");
    }

    public static class SosResult {
        public SosRecord record;
        public EmergencyContact primaryContact;

        public SosResult(SosRecord record, EmergencyContact primaryContact) {
            this.record = record;
            this.primaryContact = primaryContact;
        }
    }
}