package com.example.mc_jacoco.executor;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.util.JDiffFiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author luping
 * @date 2023/10/10 23:33
 */
@Slf4j
@Component
public class DiffMethodsExecutor {

    /**
     * 执行diff方法
     *
     * @param coverageReport
     */
    public void executeDiffMethods(CoverageReportEntity coverageReport) {
        StringBuffer buf = new StringBuffer();
        long start = System.currentTimeMillis();
        HashMap<String, String> hash = JDiffFiles.diffMethodsListNew(coverageReport);
        log.info("【增量方法集合返回信息是：{}】",hash);
        if (!hash.isEmpty()) {
            for (String key : hash.keySet()) {
                buf.append(key).append(":").append(hash.get(key)).append("%");
            }
        }
        long end = System.currentTimeMillis();
        log.info("【计算增量方法耗时：{}ms...增量方法内容是：{}】",(end - start),buf.toString());
        coverageReport.setDiffMethod(buf.toString());
    }

    public String executeDiffMethodsForEnv(String basePath,String nowPath,String baseVersion,String nowVersion){
        log.info("【计算增量方法入参：{}--{}--{}--{}】",basePath,nowPath,baseVersion,nowVersion);
        StringBuffer buf = new StringBuffer();
        long start = System.currentTimeMillis();
        HashMap<String,String> hashMap = JDiffFiles.diffMethodsListForEnv(basePath,nowPath,baseVersion,nowVersion);
        if (hashMap != null && !hashMap.isEmpty()){
            Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry = iterator.next();
                buf.append(entry.getKey()).append(":").append(entry.getValue()).append("%");
            }
            long end = System.currentTimeMillis();
            log.info("【计算增量方法耗时：{}ms...增量方法内容是：{}】",(end - start),buf.toString());
            return buf.toString();
        }
        return null;
    }
}
