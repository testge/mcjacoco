package com.example.mc_jacoco.entity.vo;

import com.example.mc_jacoco.enums.JobStatusEnum;
import lombok.Data;

/**
 * @author luping
 * @date 2023/11/12 18:23
 */
@Data
public class ResultReponse {

    /**
     * 返回的状态码
     * 0代表失败 其他均返回覆盖率报告的requestCode状态码
     */
    private Integer coverageCode;

    /**
     * 返回的消息
     */
    private String coverageMsg;

    /**
     * 行覆盖率结果
     */
    private Double lineCoverage;

    /**
     * 分支覆盖率结果
     */
    private Double branchCoverage;

    /**
     * 覆盖率报告地址
     */
    private String reportUrl;

    /**
     * 执行日志
     */
    private String log_file;

    @Override
    public String toString() {
        return "ResultReponse{" +
                "coverageCode=" + coverageCode +
                ", coverageMsg='" + coverageMsg + '\'' +
                ", lineCoverage=" + lineCoverage +
                ", branchCoverage=" + branchCoverage +
                ", reportUrl='" + reportUrl + '\'' +
                ", log_file='" + log_file + '\'' +
                '}';
    }

    /**
     * 返回固定信息 - 失败的覆盖率信息查询
     */
    public static ResultReponse ResultReponseFailBuid(String msg){
        ResultReponse resultReponse = new ResultReponse();
        resultReponse.setCoverageCode(JobStatusEnum.COVERGER_RESULT_FAIL_MSG.getCode());
        resultReponse.setCoverageMsg(msg);
        resultReponse.setLineCoverage(Double.parseDouble("-1"));
        resultReponse.setBranchCoverage(Double.parseDouble("-1"));
        resultReponse.setReportUrl("");
        resultReponse.setLog_file("");
        return resultReponse;
    }

    public static ResultReponse ResultReponseFailBuid(String msg,Integer code){
        ResultReponse resultReponse = new ResultReponse();
        resultReponse.setCoverageCode(code);
        resultReponse.setCoverageMsg(msg);
        resultReponse.setLineCoverage(Double.parseDouble("-1"));
        resultReponse.setBranchCoverage(Double.parseDouble("-1"));
        resultReponse.setReportUrl("");
        resultReponse.setLog_file("");
        return resultReponse;
    }
}
