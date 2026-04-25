package com.yinfu.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinfu.app.common.Constants;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.MedicationReminderRequest;
import com.yinfu.app.entity.MedicationRecord;
import com.yinfu.app.entity.MedicationReminder;
import com.yinfu.app.mapper.MedicationRecordMapper;
import com.yinfu.app.mapper.MedicationReminderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MedicationService {

    @Autowired
    private MedicationReminderMapper reminderMapper;

    @Autowired
    private MedicationRecordMapper recordMapper;

    public Result<?> getList(Long userId) {
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationReminder::getUserId, userId);
        wrapper.eq(MedicationReminder::getIsActive, 1);
        wrapper.orderByAsc(MedicationReminder::getTime);
        List<MedicationReminder> list = reminderMapper.selectList(wrapper);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        for (MedicationReminder reminder : list) {
            LambdaQueryWrapper<MedicationRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(MedicationRecord::getReminderId, reminder.getId());
            recordWrapper.between(MedicationRecord::getTakenAt, startOfDay, endOfDay);
            Long count = recordMapper.selectCount(recordWrapper);
            reminder.setIsTaken(count.intValue() > 0 ? 1 : 0);
        }

        return Result.success(list);
    }

    public Result<?> getToday(Long userId) {
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationReminder::getUserId, userId);
        wrapper.eq(MedicationReminder::getIsActive, 1);
        wrapper.orderByAsc(MedicationReminder::getTime);
        List<MedicationReminder> reminders = reminderMapper.selectList(wrapper);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        for (MedicationReminder reminder : reminders) {
            LambdaQueryWrapper<MedicationRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(MedicationRecord::getReminderId, reminder.getId());
            recordWrapper.between(MedicationRecord::getTakenAt, startOfDay, endOfDay);
            Long count = recordMapper.selectCount(recordWrapper);
            reminder.setIsTaken(count.intValue() > 0 ? 1 : 0);
        }

        return Result.success(reminders);
    }

    public Result<?> add(Long userId, MedicationReminderRequest request) {
        MedicationReminder reminder = new MedicationReminder();
        reminder.setUserId(userId);
        reminder.setName(request.getName());
        reminder.setDosage(request.getDosage());
        reminder.setTime(request.getTime());
        reminder.setFrequency(request.getFrequency());
        reminder.setRemark(request.getRemark() != null ? request.getRemark() : "");
        reminder.setIsActive(request.getIsActive() != null ? request.getIsActive() : 1);
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());

        reminderMapper.insert(reminder);
        return Result.success("添加成功");
    }

    public Result<?> update(Long id, Long userId, MedicationReminderRequest request) {
        MedicationReminder reminder = reminderMapper.selectById(id);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            return Result.error(404, "提醒不存在");
        }

        if (request.getName() != null) reminder.setName(request.getName());
        if (request.getDosage() != null) reminder.setDosage(request.getDosage());
        if (request.getTime() != null) reminder.setTime(request.getTime());
        if (request.getFrequency() != null) reminder.setFrequency(request.getFrequency());
        if (request.getRemark() != null) reminder.setRemark(request.getRemark());
        if (request.getIsActive() != null) reminder.setIsActive(request.getIsActive());
        reminder.setUpdatedAt(LocalDateTime.now());

        reminderMapper.updateById(reminder);
        return Result.success("更新成功");
    }

    public Result<?> delete(Long id, Long userId) {
        MedicationReminder reminder = reminderMapper.selectById(id);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            return Result.error(404, "提醒不存在");
        }

        reminderMapper.deleteById(id);
        return Result.success("删除成功");
    }

    public Result<?> take(Long id, Long userId) {
        MedicationReminder reminder = reminderMapper.selectById(id);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            return Result.error(404, "提醒不存在");
        }

        MedicationRecord record = new MedicationRecord();
        record.setReminderId(id);
        record.setUserId(userId);
        record.setTakenAt(LocalDateTime.now());
        record.setStatus(Constants.MedicationStatus.TAKEN);

        recordMapper.insert(record);
        return Result.success("已记录服药");
    }

    public Result<?> untake(Long id, Long userId) {
        MedicationReminder reminder = reminderMapper.selectById(id);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            return Result.error(404, "提醒不存在");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        LambdaQueryWrapper<MedicationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationRecord::getReminderId, id);
        wrapper.eq(MedicationRecord::getUserId, userId);
        wrapper.between(MedicationRecord::getTakenAt, startOfDay, endOfDay);

        recordMapper.delete(wrapper);
        return Result.success("已取消服药记录");
    }
}