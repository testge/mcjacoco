package com.example.mc_jacoco.enums;

/**
 * @author luping
 * @date 2023/9/18 23:03
 */
public enum JobStatusEnum {
    NODIFF(100, "无增量"),
    WAITING(1, "待执行"),
    CLONING(2, "下载代码中");


    private Integer code;

    private String codeMsg;

    public Integer getCode() {
        return code;
    }

    public String getCodeMsg() {
        return codeMsg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setCodeMsg(String codeMsg) {
        this.codeMsg = codeMsg;
    }

    JobStatusEnum(Integer code, String codeMsg) {
        this.code = code;
        this.codeMsg = codeMsg;
    }
}
