package com.example.mc_jacoco.executor;

import com.example.mc_jacoco.constants.AddressConstants;
import com.example.mc_jacoco.constants.NumberConstants;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.util.LocalIpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

/**
 * @author luping
 * @date 2023/12/9 16:40
 */
@Slf4j
@Component
public class UnitTestExecutor {

    // 单元测试命令设置超时时间1小时
    private static final Long UNITTEST_TIMEOUT = 3600000L;

    /**
     * 单元测试执行
     *
     * @param coverageReportEntity
     */
    public void executeUnitTest(CoverageReportEntity coverageReportEntity) {
        log.info("【单元测试执行方法入参：{}】", coverageReportEntity);
        long startTime = System.currentTimeMillis();
        String unitTestCmd = "cd " + coverageReportEntity.getNowLocalPathProject() + "&&mvn clean";
        if (StringUtils.isNotBlank(coverageReportEntity.getEnvType())) {
            unitTestCmd = unitTestCmd + " -P=" + coverageReportEntity.getEnvType();
        }
        log.info("【unitTestCmd组合：{}】", unitTestCmd);
        // 日志写入地址
        String logFile = coverageReportEntity.getLog_file().replace(LocalIpUtil.getBaseUrl() + "logs/", AddressConstants.LOG_PATH);
        String[] cmd = new String[]{unitTestCmd +
                " -Dmaven.test.skip=false org.jacoco:jacoco-maven-plugin:1.0.2-SNAPSHOT:prepare-agent " +
                "compile test-compile org.apache.maven.plugins:maven-surefire-plugin:2.22.1:test " +
                "org.apache.maven.plugins:maven-jar-plugin:2.4:jar " +
                "org.jacoco:jacoco-maven-plugin:1.0.2-SNAPSHOT:report " +
                "-Dmaven.test.failure.ignore=true -Dfile.encoding=UTF-8 " +
                (StringUtils.isBlank(coverageReportEntity.getDiffMethod()) ? "" : ("-Djacoco.diffFile=" + coverageReportEntity.getDiffMethod())) +
                ">>" + logFile
        };
        try {
            int exitCode = CmdExecutor.cmdExecutor(cmd, UNITTEST_TIMEOUT);
            log.info("【单元测试执行，线程ID：{}，uuid：{}，exitCode:{}】", Thread.currentThread().getName(), coverageReportEntity.getJobRecordUuid(), exitCode);
            if (exitCode == NumberConstants.ZERO) {
                log.info("【单元测试执行成功...】");
                coverageReportEntity.setRequestStatus(JobStatusEnum.UNITTEST_DONE.getCode());
            } else {
                log.error("【单元测试执行失败...线程ID：{}，uuid：{}】", Thread.currentThread().getName(), coverageReportEntity.getJobRecordUuid());
                coverageReportEntity.setRequestStatus(JobStatusEnum.UNITTEST_FAIL.getCode());
                coverageReportEntity.setErrMsg("单元测试执行器失败...");
            }
        } catch (TimeoutException tie) {
            log.error("【单元测试执行超时...线程ID:{}，uuid:{}，超时信息:{}】", Thread.currentThread().getName(), coverageReportEntity.getJobRecordUuid(), tie.getMessage());
            coverageReportEntity.setRequestStatus(JobStatusEnum.UNITTEST_FAIL.getCode());
            coverageReportEntity.setErrMsg("单元测试执行超时...");
        } catch (Exception e) {
            log.error("【单元测试执行异常...线程ID:{}，uuid:{}，异常信息:{}】", Thread.currentThread().getName(), coverageReportEntity.getJobRecordUuid(), e.getMessage());
            coverageReportEntity.setRequestStatus(JobStatusEnum.UNITTEST_FAIL.getCode());
            coverageReportEntity.setErrMsg("单元测试执行异常...");
        } finally {
            log.info("【uuid:{}单元测试执行耗时：{}秒】", coverageReportEntity.getJobRecordUuid(), (System.currentTimeMillis() - startTime) / 1000);
        }
    }
}
