package com.example.mc_jacoco.dao;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;

/**
 * @author luping
 * @date 2023/9/18 23:07
 */
public interface CoverageReportDao {

    /**
     * 保存覆盖率数据
     * @param coverageReportEntity
     * @return
     */
    Integer insertCoverageReportById(CoverageReportEntity coverageReportEntity);

}
