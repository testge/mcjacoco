package com.example.mc_jacoco.dao;

import com.example.mc_jacoco.entity.po.CoverageJobEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author luping
 * @date 2023/12/2 17:42
 */
@Mapper
public interface CoverageJobDao {

    CoverageJobEntity queryByJobNameAndStatue(@Param("jobName") String jobName,@Param("jobStatus") Integer jobStatus);
}
