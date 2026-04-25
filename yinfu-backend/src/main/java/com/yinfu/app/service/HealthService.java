package com.yinfu.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinfu.app.common.Constants;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.HealthRecordRequest;
import com.yinfu.app.dto.MedicalHistoryRequest;
import com.yinfu.app.entity.HealthRecord;
import com.yinfu.app.entity.MedicalHistory;
import com.yinfu.app.mapper.HealthRecordMapper;
import com.yinfu.app.mapper.MedicalHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthService {

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private MedicalHistoryMapper medicalHistoryMapper;

    public Result<?> getLatestHealthRecords(Long userId) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getUserId, userId);
        wrapper.orderByDesc(HealthRecord::getMeasuredAt);

        List<HealthRecord> allRecords = healthRecordMapper.selectList(wrapper);

        HealthRecord bloodPressure = null;
        HealthRecord heartRate = null;
        HealthRecord bloodSugar = null;
        HealthRecord temperature = null;

        for (HealthRecord record : allRecords) {
            if (bloodPressure == null && Constants.HealthRecordType.BLOOD_PRESSURE.equals(record.getRecordType())) {
                bloodPressure = record;
            } else if (heartRate == null && Constants.HealthRecordType.HEART_RATE.equals(record.getRecordType())) {
                heartRate = record;
            } else if (bloodSugar == null && Constants.HealthRecordType.BLOOD_SUGAR.equals(record.getRecordType())) {
                bloodSugar = record;
            } else if (temperature == null && Constants.HealthRecordType.TEMPERATURE.equals(record.getRecordType())) {
                temperature = record;
            }
        }

        HealthProfile profile = new HealthProfile();
        profile.bloodPressure = bloodPressure;
        profile.heartRate = heartRate;
        profile.bloodSugar = bloodSugar;
        profile.temperature = temperature;

        return Result.success(profile);
    }

    public Result<?> addHealthRecord(Long userId, HealthRecordRequest request) {
        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setRecordType(request.getRecordType());
        record.setValue(request.getValue());
        record.setUnit(request.getUnit());
        record.setRemark(request.getRemark() != null ? request.getRemark() : "");
        record.setMeasuredAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());

        healthRecordMapper.insert(record);
        return Result.success("添加成功");
    }

    public Result<?> updateHealthRecord(Long id, Long userId, HealthRecordRequest request) {
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error(404, "记录不存在");
        }

        if (request.getValue() != null) record.setValue(request.getValue());
        if (request.getUnit() != null) record.setUnit(request.getUnit());
        if (request.getRemark() != null) record.setRemark(request.getRemark());

        healthRecordMapper.updateById(record);
        return Result.success("更新成功");
    }

    public Result<?> getMedicalHistory(Long userId) {
        LambdaQueryWrapper<MedicalHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalHistory::getUserId, userId);
        wrapper.orderByDesc(MedicalHistory::getCreatedAt);
        List<MedicalHistory> list = medicalHistoryMapper.selectList(wrapper);
        return Result.success(list);
    }

    public Result<?> addMedicalHistory(Long userId, MedicalHistoryRequest request) {
        MedicalHistory history = new MedicalHistory();
        history.setUserId(userId);
        history.setDiseaseName(request.getDiseaseName());
        if (request.getDiagnosisDate() != null) {
            history.setDiagnosisDate(LocalDate.parse(request.getDiagnosisDate()));
        }
        history.setCurrentStatus(request.getCurrentStatus());
        history.setDescription(request.getDescription());
        history.setCreatedAt(LocalDateTime.now());

        medicalHistoryMapper.insert(history);
        return Result.success("添加成功");
    }

    public static class HealthProfile {
        public HealthRecord bloodPressure;
        public HealthRecord heartRate;
        public HealthRecord bloodSugar;
        public HealthRecord temperature;
    }
}