package com.example.mc_jacoco.service;

import com.example.mc_jacoco.entity.dto.CovReportInfoDto;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.entity.po.ProjectEntity;
import com.example.mc_jacoco.entity.vo.*;
import com.example.mc_jacoco.util.Result;

import java.util.List;

/**
 * @author luping
 * @date 2023/9/18 22:02
 */
public interface CodeCovService {

    /**
     * 采集覆盖率
     * @param envCoverRequest
     */
    void triggerEnvCov(EnvCoverRequest envCoverRequest);

    /**
     * 计算增量方法Diff集合
     */
    void calculateDeployDiffMethods(CoverageReportEntity coverageReportEntity);

    /**
     * 计算手工覆盖率，与环境一起使用
     * @param coverageReportEntity
     */
    void calculateEnvCov(CoverageReportEntity coverageReportEntity);

    void calculateUnitCover(CoverageReportEntity coverageReportEntity);

    /**
     * 查询覆盖率结果
     * @param uuid
     * @return
     */
    ResultReponse getResultEnvCover(String uuid);


    /**
     * 手工触发覆盖率的计算（前提是项目代码已经下载到覆盖率的服务器中）
     * @param localHostRequest
     * @return
     */
    ResultReponse getLocalCoverResult(LocalHostRequest localHostRequest);

    /**
     * 触发单元覆盖率收集
     * @param untiCoverRequest
     */
    Result triggerUnitCov(UntiCoverRequest untiCoverRequest);

    /**
     * 根据项目查询覆盖率报告数据
     * @param projectEntity
     * @return
     */
    List<CovReportInfoDto> coverageReportList(ProjectRequest projectRequest);

    CovReportInfoDto coverageReportEntity(String uuid);
}
