package com.example.mc_jacoco.service.serviceImpl;

import com.example.mc_jacoco.dao.CoverageReportDao;
import com.example.mc_jacoco.dao.DeployInfoDao;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.entity.vo.EnvCoverRequest;
import com.example.mc_jacoco.enums.CoverageFrom;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.enums.ReportTypeEnum;
import com.example.mc_jacoco.service.CodeCovService;
import com.example.mc_jacoco.util.DoubleUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author luping
 * @date 2023/9/18 22:04
 */
@Slf4j
@Service
public class CodeCovServiceImpl implements CodeCovService {

    @Resource
    private CoverageReportDao coverageReportDao;

    @Resource
    private DeployInfoDao deployInfoDao;

    /**
     * 收集覆盖率
     *
     * @param envCoverRequest
     */
    @Override
    public void triggerEnvCov(EnvCoverRequest envCoverRequest) {
        log.info("【方法triggerEnvCov】【收集覆盖率】【入参信息：{}】", envCoverRequest.toString());
        try {
            CoverageReportEntity coverageReportEntity = coverageReportEntityBuild(envCoverRequest);
            if (envCoverRequest.getBaseVersion().equals(envCoverRequest.getNowVersion()) && envCoverRequest.getType().equals(ReportTypeEnum.FULL.getCode())) {
                log.info("【覆盖率基准版本与当前版本一致且计算全量覆盖率，没有增量方法...】");
                coverageReportEntity.setBranchCoverage(DoubleUtil.resultDouble("100"));
                coverageReportEntity.setLineCoverage(DoubleUtil.resultDouble("100"));
                coverageReportEntity.setRequestStatus(JobStatusEnum.NODIFF.getCode());
                coverageReportEntity.setErrMsg("没有增量方法");
                log.info("【没有增量方法插入数据，插入数据：{}】", coverageReportEntity);
                Integer resultId = coverageReportDao.insertCoverageReportById(coverageReportEntity);
                log.info("【没有增量方法插入数据成功，插入条数：{}】", resultId);
            }
            coverageReportEntity.setRequestStatus(JobStatusEnum.WAITING.getCode());
            Integer resultId = coverageReportDao.insertCoverageReportById(coverageReportEntity);
            log.info("【增量数据状态是：{}】【数据落库成功：{}】", JobStatusEnum.WAITING.getCodeMsg(), resultId);
            Integer deployId = deployInfoDao.insertDeployInfo(envCoverRequest);
            log.info("【服务部署数据保存成功：{}】", deployId);
            new Thread(() -> {
                log.info("【开始执行代码编译...】");
                cloneCodeAndCompileCode(coverageReportEntity);

            }).start();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 代码编译
     *
     * @param coverageReportEntity
     */
    public void cloneCodeAndCompileCode(CoverageReportEntity coverageReportEntity) {
        coverageReportEntity.setRequestStatus(JobStatusEnum.CLONING.getCode());
        Integer updateId = coverageReportDao.updateCoverageReportById(coverageReportEntity);
        log.info("【数据更新成功：{}】", updateId);

    }

    private CoverageReportEntity coverageReportEntityBuild(EnvCoverRequest envCoverRequest) {
        CoverageReportEntity coverageReportEntity = new CoverageReportEntity();
        coverageReportEntity.setFrom(CoverageFrom.ENV.getEnv());
        coverageReportEntity.setEnvType("");
        coverageReportEntity.setJobRecordUuid(envCoverRequest.getUuid());
        coverageReportEntity.setGitUrl(envCoverRequest.getGitUrl());
        coverageReportEntity.setNowVersion(envCoverRequest.getNowVersion());
        coverageReportEntity.setBaseVersion(envCoverRequest.getBaseVersion());
        coverageReportEntity.setType(envCoverRequest.getType());
        coverageReportEntity.setSubModule(envCoverRequest.getSubModule());
        coverageReportEntity.setLineCoverage(-1.00);
        coverageReportEntity.setBranchCoverage(-1.00);
        return coverageReportEntity;
    }
}
