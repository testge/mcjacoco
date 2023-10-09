package com.example.mc_jacoco.enums;

/**
 * @author luping
 * @date 2023/9/18 23:03
 */
public enum JobStatusEnum {
    NODIFF(100, "无增量"),
    WAITING(1, "待执行"),
    CLONING(2, "下载代码中"),
    COMPILING(3, "编译中"),
    CLONE_DONE(102, "下载代码成功"),
    COMPILE_DONE(103, "编译成功"),
    CLONE_FAIL(202, "下载代码失败"),
    COMPILE_FAIL(203, "编译失败"),
    ;


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
