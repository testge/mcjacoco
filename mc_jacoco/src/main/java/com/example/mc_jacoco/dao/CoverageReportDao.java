package com.example.mc_jacoco.dao;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 根据ID修改coverageReportEntity表数据
     * @param coverageReportEntity
     * @return
     */
    Integer updateCoverageReportById(@Param("coverageReportEntity") CoverageReportEntity coverageReportEntity);

}
