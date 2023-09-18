package com.example.mc_jacoco.enums;

/**
 * @author luping
 * @date 2023/9/18 22:39
 * 收集覆盖率方式枚举值
 */
public enum CoverageFrom {

    UNIT(1,"单元测试"),
    ENV(2,"环境部署");

    private Integer env;

    private String envMsg;

    CoverageFrom(Integer env, String envMsg) {
        this.env = env;
        this.envMsg = envMsg;
    }

    public Integer getEnv() {
        return env;
    }

    public String getEnvMsg() {
        return envMsg;
    }

    public void setEnv(Integer env) {
        this.env = env;
    }

    public void setEnvMsg(String envMsg) {
        this.envMsg = envMsg;
    }
}
