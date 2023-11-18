package com.example.mc_jacoco.exception;

/**
 * @author luping
 * @date 2023/11/18 16:13
 */
public class ReponseException extends RuntimeException{

    private Integer code;

    private String message;

    public ReponseException(String message, Integer code) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
