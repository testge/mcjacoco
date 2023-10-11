package com.example.mc_jacoco.service;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.entity.vo.EnvCoverRequest;

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
}
