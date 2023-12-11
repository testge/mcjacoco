package com.example.mc_jacoco.executor;

import com.example.mc_jacoco.constants.AddressConstants;
import com.example.mc_jacoco.constants.NumberConstants;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.util.LocalIpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author luping
 * @date 2023/12/9 17:23
 * 分析Jacoco覆盖率报告
 */
@Slf4j
@Component
public class ReportAnalyzeExecutor {

    /**
     * 覆盖率HTML报告分析
     *
     * @param coverageReport 覆盖率报告实体
     * @return
     */
    public boolean parseReport(CoverageReportEntity coverageReport) {
        log.info("【分析覆盖率报告入参：{}】", coverageReport);
        if (StringUtils.isBlank(coverageReport.getReportFile())) {
            log.warn("【reportFile路径为空】");
            return false;
        }
        log.info("【reportFile路径：{}】",coverageReport.getReportFile());
        File reportFile = new File(coverageReport.getReportFile());
        try {
            if (reportFile.exists()) {
                log.info("【开始分析覆盖率报告...】");
                // 解析index.html文件
                Document document = Jsoup.parse(reportFile.getAbsoluteFile(), "UTF-8", "");
                // 获取字节码指令数据
                Elements bars = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("bar");
                // 获取未覆盖的方法、行、类、判断条件
                Elements lineCtr1 = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("ctr1");
                // 获取方法总数、行总数、类总数、圈复杂度
                Elements lineCtr2 = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("ctr2");
                double lineCoverage = 100;
                double branchCoverage = 100;
                if (document != null && bars != null) {
                    // 覆盖率报告文件行未覆盖的数量
                    double lineMolecule = Double.parseDouble(lineCtr1.get(1).text());
                    log.info("【lineMolecule结果是：{}】", lineMolecule);
                    // 覆盖率报告文件行的总数量
                    double lineDenominator = Double.parseDouble(lineCtr2.get(3).text());
                    log.info("【lineDenominator结果是：{}】", lineDenominator);
                    // 计算行覆盖率
                    lineCoverage = (lineDenominator - lineMolecule) / lineDenominator * 100;
                    log.info("【行覆盖率计算结果是：{}】", lineCoverage);
                    String[] barString = bars.get(1).text().split(" of ");
                    // 获取分支的未覆盖数
                    double branchMolecule = Double.parseDouble(barString[0]);
                    log.info("【branchMolecule结果是：{}】", branchMolecule);
                    // 获取分支的全部总数
                    double branchDenominator = Double.parseDouble(barString[1]);
                    log.info("【branchDenominator结果是：{}】", branchDenominator);
                    // 计算分支覆盖率，如果总分支数<=0的时候，意味着覆盖率报告是没有分支数据的，默认赋值为0
                    if (branchDenominator > 0.0) {
                        branchCoverage = (branchDenominator - branchMolecule) / branchDenominator * 100;
                    } else {
                        branchCoverage = 0;
                    }
                    log.info("【行覆盖率计算结果是：{}，分支覆盖率计算结果是：{}】", lineCoverage, branchCoverage);
                }
                coverageReport.setLineCoverage(lineCoverage);
                coverageReport.setBranchCoverage(branchCoverage);
                coverageReport.setRequestStatus(JobStatusEnum.PARSEREPORT_DONE.getCode());
                return true;
            } else {
                log.warn("【覆盖率报告分析失败...ThreadName：{},uuid：{}】", Thread.currentThread().getName(), coverageReport.getJobRecordUuid());
                coverageReport.setRequestStatus(JobStatusEnum.FAILPARSEREPOAT.getCode());
                coverageReport.setErrMsg("项目中不存在单元测试...");
                return false;
            }
        } catch (Exception e) {
            log.error("【覆盖率报告解析异常...ThreadName：{},uuid：{}，异常信息：{}】", Thread.currentThread().getName(), coverageReport.getJobRecordUuid(), e.getMessage());
            coverageReport.setErrMsg("解析报告发生异常:" + e.getMessage());
            coverageReport.setRequestStatus(JobStatusEnum.FAILPARSEREPOAT.getCode());
            return false;
        }
    }


    /**
     * 复制报告地址
     *
     * @param coverageReport
     */
    public void copyReport(CoverageReportEntity coverageReport) {
        String[] cmd = new String[]{"cp -rf " + new File(coverageReport.getReportFile()).getParent() + "/ " + AddressConstants.REPORT_PATH + coverageReport.getJobRecordUuid()};
        try {
            Integer executorCode = CmdExecutor.cmdExecutor(cmd, 600000L);
            if (executorCode == NumberConstants.ZERO) {
                log.info("【报告复制成功...】");
                coverageReport.setReportUrl(LocalIpUtil.getBaseUrl() + coverageReport.getJobRecordUuid() + "/index.html");
                coverageReport.setRequestStatus(JobStatusEnum.COPYREPORT_DONE.getCode());
            }else {
                coverageReport.setRequestStatus(JobStatusEnum.COPYREPORT_FAIL.getCode());
                coverageReport.setErrMsg("复制覆盖率报告失败...");
            }
        } catch (Exception e) {
            log.error("【覆盖率报告复制异常...ThreadName：{},uuid：{}，异常信息：{}】", Thread.currentThread().getName(), coverageReport.getJobRecordUuid(), e.getMessage());
            coverageReport.setRequestStatus(JobStatusEnum.COPYREPORT_FAIL.getCode());
            coverageReport.setErrMsg("复制覆盖率报告失败...");
        }
    }

}
