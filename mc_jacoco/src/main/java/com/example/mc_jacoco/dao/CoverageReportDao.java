package com.example.mc_jacoco.dao;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luping
 * @date 2023/9/18 23:07
 */
@Mapper
public interface CoverageReportDao {

    /**
     * 保存覆盖率数据
     * @param coverageReportEntity
     * @return
     */
    Integer insertCoverageReportById(CoverageReportEntity coverageReportEntity);

}
