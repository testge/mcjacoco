package com.example.mc_jacoco.enums;

/**
 * @author luping
 * @date 2023/9/16 17:25
 */
public enum ResultEnum {
    SUCCESS(200,"成功"),
    FAIL(2001,"处理失败");

    private Integer code;

    private String msg;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
