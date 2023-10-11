package com.example.mc_jacoco.entity.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author luping
 * @date 2023/10/11 23:23
 */

@Data
public class CoverageReportDto {

    private Integer id;

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
     * 环境类型，环境部署收集覆盖率暂时用不到这个字段
     */
    private String envType;

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
        return "CoverageReportDto{" +
                "id=" + id +
                ", jobRecordUuid='" + jobRecordUuid + '\'' +
                ", requestStatus=" + requestStatus +
                ", gitUrl='" + gitUrl + '\'' +
                ", nowVersion='" + nowVersion + '\'' +
                ", baseVersion='" + baseVersion + '\'' +
                ", diffMethod='" + diffMethod + '\'' +
                ", type=" + type +
                ", reportUrl='" + reportUrl + '\'' +
                ", lineCoverage=" + lineCoverage +
                ", branchCoverage=" + branchCoverage +
                ", errMsg='" + errMsg + '\'' +
                ", subModule='" + subModule + '\'' +
                ", from=" + from +
                ", nowLocalPath='" + nowLocalPath + '\'' +
                ", baseLocalPath='" + baseLocalPath + '\'' +
                ", log_file='" + log_file + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", envType='" + envType + '\'' +
                ", nowLocalPathProject='" + nowLocalPathProject + '\'' +
                ", baseLocalPathProject='" + baseLocalPathProject + '\'' +
                '}';
    }
}
