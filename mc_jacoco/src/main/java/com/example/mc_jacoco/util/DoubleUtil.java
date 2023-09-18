package com.example.mc_jacoco.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author luping
 * @date 2023/9/18 22:53
 */
@Slf4j
public class DoubleUtil {

    private static final Integer ZERO = 0;

    public static double resultDouble(String str){
        try {
            Double value = Double.parseDouble(str);
            return value;
        }catch (NumberFormatException e) {
            log.error("【DoubleUtil】【resultDouble方法转换出错...】【报错信息：{}】",e.getMessage());
        }
        return ZERO;
    }
}
