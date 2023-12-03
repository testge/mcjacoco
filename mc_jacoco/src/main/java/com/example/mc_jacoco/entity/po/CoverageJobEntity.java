package com.example.mc_jacoco.entity.po;

import lombok.Data;

/**
 * @author luping
 * @date 2023/12/2 17:17
 */
@Data
public class CoverageJobEntity extends BaseEntity {

    private Integer id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务表达式
     */
    private String jobCorn;

    /**
     * 任务状态  0表示开启 1表示未开启
     */
    private String jobStatus;

    /**
     * 扩展字段
     */
    private String jobExtend;
}
