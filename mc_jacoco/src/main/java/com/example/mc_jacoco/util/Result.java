package com.example.mc_jacoco.util;

import com.example.mc_jacoco.enums.ResultEnum;

/**
 * @author luping
 * @date 2023/9/16 17:12
 * 返回数据工具类
 */
public class Result<T> {

    /**
     * 枚举Code码
     */
    private Integer code;

    /**
     * 枚举消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    // 成功带返回值
    public static <T> Result<T> success(T data) {
        return build(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg(), data);
    }

    // 成功不带返回值
    public static Result success() {
        return build(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg());
    }

    // 返回带参数的失败
    public static <T> Result<T> fail(T data){
        return build(ResultEnum.FAIL.getCode(), ResultEnum.FAIL.getMsg(),data);
    }

    public static <T> Result<T> fail(){
        return build(ResultEnum.FAIL.getCode(), ResultEnum.FAIL.getMsg());
    }

    // 带返回值的Build
    private static <T> Result<T> build(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 不带返回值的Build
    private static Result build(Integer code, String message) {
        Result result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

}
