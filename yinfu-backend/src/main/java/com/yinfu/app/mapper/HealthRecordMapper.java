package com.yinfu.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yinfu.app.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
}