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

    /**
     * 创建文件路径及文件
     * @param directory 目录地址
     * @param fileName 文件地址
     */
    public static void mkdir(String directory,String fileName){
        try {
            File fileDirectory = new File(directory);
            File fileNames = new File(directory);
            if (!fileDirectory.isDirectory()){
                FileUtils.forceMkdir(fileDirectory);
            }
            if (!fileNames.exists()){
                FileUtils.touch(fileNames);
            }
        }catch (IOException io) {
            log.error("【目录及文件创建失败....原因是：{}】",io.getMessage());
        }
    }

    /**
     * 返回当前目录下的项目目录，目前只会存在一个
     * @param fileName
     * @return
     */
    public static String resultfileDirectory(String fileName){
        File fileDirectory = new File(fileName);
        String[] filenames = fileDirectory.list();
        String directory = "";
        for (String name: filenames) {
            if(!name.startsWith(".")){
                directory = name;
                break;
            }
        }
        return directory;
    }
}
