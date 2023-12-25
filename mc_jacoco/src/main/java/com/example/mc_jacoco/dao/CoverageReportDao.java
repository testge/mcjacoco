package com.example.mc_jacoco.dao;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luping
 * @date 2023/9/18 23:07
 */
@Mapper
public interface CoverageReportDao {

    /**
     * 保存覆盖率数据
     *
     * @param coverageReportEntity
     * @return
     */
    Integer insertCoverageReportById(CoverageReportEntity coverageReportEntity);

    /**
     * 根据ID修改coverageReportEntity表数据
     *
     * @param coverageReportEntity
     * @return
     */
    Integer updateCoverageReportById(@Param("coverageReportEntity") CoverageReportEntity coverageReportEntity);

    CoverageReportEntity queryCoverageReportByUuid(@Param("uuid") String uuid);

    List<CoverageReportEntity> querByStatusAndfrom(@Param("requestStatue") Integer requestStatue, @Param("from") Integer from);

    Integer casUpdateByStatus(@Param("expectStatus") Integer expectStatus, @Param("newStatus") int newStatus,@Param("retryCount") Integer retryCount, @Param("uuid") String uuid);

    /**
     * 项目覆盖率报告数据
     * @param coverageReportEntity
     * @return
     */
    List<CoverageReportEntity> queryAllCoverageReport(CoverageReportEntity coverageReportEntity);

}
