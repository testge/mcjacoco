package com.example.mc_jacoco.enums;

/**
 * @author luping
 * @date 2023/9/18 22:49
 */
public enum ReportTypeEnum {
    FULL(1, "全量覆盖率"),
    DIFF(2, "增量覆盖率");

    private Integer code;
    private String desc;

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public void setValue(Integer value) {
        this.code = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    ReportTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
