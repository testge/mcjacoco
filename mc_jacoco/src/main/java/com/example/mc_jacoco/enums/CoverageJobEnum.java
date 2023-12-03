package com.example.mc_jacoco.enums;

/**
 * @author luping
 * @date 2023/12/2 17:51
 */
public enum CoverageJobEnum {
    // 计算单元测试覆盖率
    UNIT_COVERAGE_COMPUTE("unitCoverageCompute");


    CoverageJobEnum(String jobName) {
        this.jobName = jobName;
    }

    private String jobName;

    public String getJobName() {
        return jobName;
    }
}
