package com.example.mc_jacoco.service;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.entity.vo.EnvCoverRequest;
import com.example.mc_jacoco.entity.vo.LocalHostRequest;
import com.example.mc_jacoco.entity.vo.ResultReponse;

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

}
