package com.example.mc_jacoco.executor;

import com.example.mc_jacoco.constants.AddressConstants;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.handler.GitHandler;
import com.example.mc_jacoco.util.FilesUtil;
import com.example.mc_jacoco.util.LocalIpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author luping
 * @date 2023/9/23 14:30
 */
@Component
@Slf4j
public class CodeCloneExecutor {

    @Resource
    private GitHandler gitHandler;

    /**
     * 克隆代码
     *
     * @param coverageReportEntity
     */
    public void cloneCode(CoverageReportEntity coverageReportEntity) {
        log.info("【进入代码克隆方法入参：{}】", coverageReportEntity.toString());
        // 定义log文件地址（基础地址+uuid）
        String logFile = LocalIpUtil.getBaseUrl() + "logs/" + coverageReportEntity.getJobRecordUuid() + ".log";
        coverageReportEntity.setLog_file(logFile);
        String fileName = "";
        try {
            String uuid = coverageReportEntity.getJobRecordUuid();
            String nowLoaclPath = AddressConstants.CODE_ROOT + uuid + "/" + coverageReportEntity.getNowVersion().replace("/", "_");
            FilesUtil.fileExists(AddressConstants.CODE_ROOT + uuid + "/");
            String getUrl = coverageReportEntity.getGitUrl();
            log.info("【uuid:【{}】开始下载代码...】", uuid);
            log.info("【下载当前版本分支代码...】");
            gitHandler.cloneRepository(getUrl, nowLoaclPath, coverageReportEntity.getNowVersion());
            fileName = nowLoaclPath + "/" + FilesUtil.resultfileDirectory(nowLoaclPath);
            coverageReportEntity.setNowLocalPath(fileName);
            String baseLocalPath = AddressConstants.CODE_ROOT + uuid + "/" + coverageReportEntity.getBaseVersion().replace("/", "_");
            log.info("【下载基准版本分支代码...】");
            gitHandler.cloneRepository(getUrl, baseLocalPath, coverageReportEntity.getBaseVersion());
            fileName = baseLocalPath + "/" + FilesUtil.resultfileDirectory(baseLocalPath);
            coverageReportEntity.setBaseLocalPath(fileName);
            coverageReportEntity.setRequestStatus(JobStatusEnum.CLONE_DONE.getCode());
        } catch (Exception e) {
            log.error("【下载代码失败...】【UUID：{}】【异常信息：{}】", coverageReportEntity.getJobRecordUuid(), e.getMessage());
            coverageReportEntity.setErrMsg("代码下载失败");
            coverageReportEntity.setRequestStatus(JobStatusEnum.CLONE_FAIL.getCode());
        }
    }

}
