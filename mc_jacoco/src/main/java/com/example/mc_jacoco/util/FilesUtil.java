package com.example.mc_jacoco.util;

import ch.qos.logback.core.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author luping
 * @date 2023/9/26 22:07
 */
@Slf4j
public class FilesUtil {

    public static boolean fileExists(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                FileUtils.cleanDirectory(new File(fileName));
            }
        } catch (IOException io) {
            log.error("【文件校验是否存在出错...】【原因：{}】", io.getMessage());
        }
        return false;
    }

//    public static void main(String[] args) {
//        System.out.println(FilesUtil.fileExists("/Users/luping/app/mcs_jacoco/clonecode/sdsdsadasdada/feature_newTest01"));
//    }
}
