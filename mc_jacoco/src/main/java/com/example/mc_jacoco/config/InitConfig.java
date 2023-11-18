package com.example.mc_jacoco.config;

import com.example.mc_jacoco.constants.AddressConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author luping
 * @date 2023/11/18 18:05
 */
@Slf4j
@Component
public class InitConfig implements CommandLineRunner {

    /**
     * 启动创建文件目录
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("【项目启动创建LOG_PATH文件目录】");
        File file = new File(AddressConstants.LOG_PATH);
        log.info("【项目是否存在检查：{}】",file.isDirectory());
        if (!file.isDirectory()){
            FileUtils.forceMkdir(file);
        }
    }
}
