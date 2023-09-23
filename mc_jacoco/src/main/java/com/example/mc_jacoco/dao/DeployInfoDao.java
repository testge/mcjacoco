package com.example.mc_jacoco.dao;

import com.example.mc_jacoco.entity.vo.EnvCoverRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luping
 * @date 2023/9/23 13:12
 */
@Mapper
public interface DeployInfoDao {

    Integer insertDeployInfo(EnvCoverRequest envCoverRequest);
}
