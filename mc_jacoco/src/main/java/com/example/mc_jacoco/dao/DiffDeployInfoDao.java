package com.example.mc_jacoco.dao;

import com.example.mc_jacoco.entity.po.DeployInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author luping
 * @date 2023/11/8 23:11
 */

@Mapper
public interface DiffDeployInfoDao {

    /**
     * 通过唯一jobRecordUUid查询机器部署信息
     */
    DeployInfoEntity queryInfoById(@Param("jobRecordUUid") String jobRecordUUid);
}
