package com.example.mc_jacoco.handler;

import com.example.mc_jacoco.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author luping
 * @date 2023/9/16 18:27
 */
@Slf4j
@RestControllerAdvice
public class GlobaExceptionHandler {

    @ExceptionHandler(BindException.class)
    public Result parameterException(BindException ex) {
        log.error("【Controller】【参数校验出现异常...】");
        BindingResult bindingResult = ex.getBindingResult();
        List<Map<String, String>> errorList = new ArrayList<>();
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().stream().forEach(item -> {
            errorMap.put(item.getField(), item.getDefaultMessage());
        });
        log.error("【异常参数校验不通过的是】：【{}】", errorMap);
        errorList.add(errorMap);
        return Result.fail(errorList);
    }
}
