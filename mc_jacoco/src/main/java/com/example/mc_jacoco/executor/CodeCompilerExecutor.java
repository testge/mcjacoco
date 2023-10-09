package com.example.mc_jacoco.executor;

import com.example.mc_jacoco.constants.AddressConstants;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.util.FilesUtil;
import com.example.mc_jacoco.util.LocalIpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeoutException;

/**
 * @author luping
 * @date 2023/10/8 21:40
 */
@Component
@Slf4j
public class CodeCompilerExecutor {

    /**
     * 代码编译执行器
     *
     * @param coverageReportEntity
     */
    public void compileCode(CoverageReportEntity coverageReportEntity) {
        log.info("【compileCode入参：{}】", coverageReportEntity.toString());
        String logFile = coverageReportEntity.getLog_file().replace(LocalIpUtil.getBaseUrl() + "logs/", AddressConstants.LOG_PATH);
        // 创建目录及log文件
        FilesUtil.mkdir(AddressConstants.LOG_PATH, logFile);
        String[] compileCmd = new String[]{"cd " + coverageReportEntity.getNowLocalPath() + "/" + FilesUtil.resultfileDirectory(coverageReportEntity.getNowLocalPath()) + "&&mvn clean compile " +
                (StringUtils.isEmpty(coverageReportEntity.getEnvType()) ? "" : "-p=" + coverageReportEntity.getEnvType()) + ">>" + logFile};
        try {
            Integer cmdExecutor = CmdExecutor.cmdExecutor(compileCmd, 600000L);
            log.info("【cmd编译返回结果：{}】", cmdExecutor);
            if (cmdExecutor != 0) {
                coverageReportEntity.setRequestStatus(JobStatusEnum.COMPILE_FAIL.getCode());
                coverageReportEntity.setErrMsg("代码编译失败...");
            } else {
                coverageReportEntity.setRequestStatus(JobStatusEnum.COMPILE_DONE.getCode());
            }
        } catch (TimeoutException oute) {
            log.error("【代码编译超过10分钟...】【异常原因是：{}】", oute.getMessage());
            coverageReportEntity.setRequestStatus(JobStatusEnum.COMPILE_FAIL.getCode());
            coverageReportEntity.setErrMsg("代码编译超过10分钟...");
        } catch (Exception e) {
            log.error("【代码编译执出现异常...】【异常原因是：{}】", e.getMessage());
            coverageReportEntity.setRequestStatus(JobStatusEnum.COMPILE_FAIL.getCode());
            coverageReportEntity.setErrMsg("代码编译失败...");
        }
    }
}
