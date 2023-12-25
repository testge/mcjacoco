package com.example.mc_jacoco.entity.dto;

import lombok.Data;

/**
 * @author luping
 * @date 2023/12/25 23:52
 */
@Data
public class CovReportInfoDto {

    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 唯一标识uuid
     */
    private String jobRecordUuid;

    /**
     * 请求执行状态
     */
    private Integer requestStatus;

    /**
     * 仓库地址
     */
    private String gitUrl;

    /**
     * 本次提交的commit
     */
    private String nowVersion;

    /**
     * 基准commit
     */
    private String baseVersion;

    /**
     * 增量diff方法集合
     */
    private String diffMethod;

    /**
     * 覆盖率收集类型
     */
    private Integer type;

    /**
     * 覆盖率报告Url
     */
    private String reportUrl;

    /**
     * 行覆盖率
     */
    private Double lineCoverage;

    /**
     * 分支覆盖率
     */
    private Double branchCoverage;

    /**
     * 子模块
     */
    private String subModule;

    /**
     * 通过什么方式收集覆盖率数据
     * 1= 单元测试、2=环境收集
     */
    private Integer from;

    /**
     * 报告地址
     */
    private String reportFile;
}
