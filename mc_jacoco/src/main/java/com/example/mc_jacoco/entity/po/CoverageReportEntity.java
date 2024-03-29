package com.example.mc_jacoco.entity.po;

import lombok.Data;

import java.util.Date;

/**
 * @author luping
 * @date 2023/9/18 22:06
 * 覆盖率报告实体
 */
@Data
public class CoverageReportEntity {

    /**
     * 自增ID
     */
    private Integer id;

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
     * 分支覆盖率
     */
    private Double branchCoverage;

    /**
     * 行覆盖率
     */
    private Double lineCoverage;

    /**
     * 方法覆盖率
     */
    private Double methodCoverage;

    /**
     * 类覆盖率
     */
    private Double classCoverage;

    /**
     * 错误消息
     */
    private String errMsg;

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
     * 重试次数 默认0次
     */
    private Integer retryCount;

    /**
     * 当前版本路径
     */
    private String nowLocalPath;

    /**
     * 对比版本路径
     */
    private String baseLocalPath;

    /**
     * 执行日志路径
     */
    private String log_file;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 环境类型，环境部署收集覆盖率暂时用不到这个字段,单测类型会用到
     */
    private String envType;

    /**
     * 报告地址
     */
    private String reportFile;

    /**
     * 当前版本路径含项目地址
     */
    private String nowLocalPathProject;

    /**
     * 对比版本路径含项目地址
     */
    private String baseLocalPathProject;

    @Override
    public String toString() {
        return "CoverageReportEntity{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", jobRecordUuid='" + jobRecordUuid + '\'' +
                ", requestStatus=" + requestStatus +
                ", gitUrl='" + gitUrl + '\'' +
                ", nowVersion='" + nowVersion + '\'' +
                ", baseVersion='" + baseVersion + '\'' +
                ", diffMethod='" + diffMethod + '\'' +
                ", type=" + type +
                ", reportUrl='" + reportUrl + '\'' +
                ", branchCoverage=" + branchCoverage +
                ", lineCoverage=" + lineCoverage +
                ", methodCoverage=" + methodCoverage +
                ", classCoverage='" + classCoverage + '\'' +
                ", errMsg='" + errMsg + '\'' +
                ", subModule='" + subModule + '\'' +
                ", from=" + from +
                ", retryCount=" + retryCount +
                ", nowLocalPath='" + nowLocalPath + '\'' +
                ", baseLocalPath='" + baseLocalPath + '\'' +
                ", log_file='" + log_file + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", envType='" + envType + '\'' +
                ", reportFile='" + reportFile + '\'' +
                ", nowLocalPathProject='" + nowLocalPathProject + '\'' +
                ", baseLocalPathProject='" + baseLocalPathProject + '\'' +
                '}';
    }
}
