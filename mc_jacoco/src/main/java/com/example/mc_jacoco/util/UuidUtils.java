package com.example.mc_jacoco.util;

import java.util.UUID;

/**
 * @author luping
 * @date 2023/12/24 15:54
 */
public class UuidUtils {


    public static String getUUid() {
        return createUUid();
    }

    /**
     * 基于随机数的 UUID：基于伪随机数，生成 16byte 随机值填充 UUID
     *
     * @return
     */
    private static String createUUid() {
        return String.valueOf(UUID.randomUUID());
    }
}
