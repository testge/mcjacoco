package com.example.mc_jacoco.constants;

import com.example.mc_jacoco.util.LocalIpUtil;

/**
 * @author luping
 * @date 2023/9/26 22:07
 */
public class AddressConstants {

    // code代码存放地址
    public static final String CODE_ROOT = System.getProperty("user.home") + "/app/mcs_jacoco/clonecode/";

    // 系统编译代码日志文件
    public static final String LOG_PATH = System.getProperty("user.home") + "/report/logs/";

    public static final String NO_CODE_COVERGET_REPORT = LocalIpUtil.getBaseUrl() + "nodiffcode.html";

    public static final String REPORT_PATH = System.getProperty("user.home") + "/report/";

    // Jacoco-Cli包存放地址
    public static final String JACOCO_PATH = System.getProperty("user.home") + "/jacocoJar/org.jacoco.cli-1.0.2-SNAPSHOT-nodeps.jar";
}
