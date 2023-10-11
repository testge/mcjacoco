package com.example.mc_jacoco.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author luping
 * @date 2023/10/10 22:17
 */
@Slf4j
public class MavenModuleUtil {

    /**
     * 匹配Pom文件中的Module
     *
     * @param path
     * @return
     */
    public static ArrayList<String> getValidModules(String path) {
        ArrayList<String> arrayList = new ArrayList<>();
        BufferedReader render = null;
        try {
            render = new BufferedReader(new FileReader(path));
            StringBuffer buffer = new StringBuffer();
            String s;
            while ((s = render.readLine()) != null) {
                buffer.append(s.trim());
            }
            String pom = buffer.toString();
            // 开始正则匹配Module
            String moduleRegex = "<modules>.*?</modules>";
            Pattern modulePattern = Pattern.compile(moduleRegex);
            Matcher matcher = modulePattern.matcher(pom);
            String modules;
            while (matcher.find()) {
                modules = matcher.group();
                // 过滤掉已经注释的modules
                modules = modules.replaceAll("<!--.*?<module>.*?</module>.*?-->", ",");
                // 将module替换成逗号
                modules = modules.replaceAll("</?modules?>", ",");
                String[] split = modules.split(",");
                // 最后将项目中关联的模块加入到列表
                for (String m : split) {
                    if (!m.equals("")) {
                        arrayList.add(m);
                    }
                }
            }
        } catch (IOException e) {
            log.error("【匹配Pom文件中的Module】【getValidModules】【读取文件出现异常...】【异常原因是：{}】", e.getMessage());
        } finally {
            try {
                render.close();
            } catch (IOException e) {
                log.error("【文件流关闭异常...原因是：{}】", e.getMessage());
            }
        }
        log.info("【getValidModules】返回内容：{}", arrayList);
        return arrayList;
    }
}
