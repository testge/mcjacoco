package com.example.mc_jacoco.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author luping
 * @date 2023/10/8 21:56
 */
@Slf4j
@Component
public class CmdExecutor {

    private static Integer maxThread = 64;

    private static AtomicInteger atomic = new AtomicInteger();

    private static RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            20,
            maxThread,
            5 * 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(64),
            r -> new Thread(r, "CmdThread-" + atomic.getAndIncrement()),
            rejectedExecutionHandler);

    /**
     * Linux命令执行器
     *
     * @param values
     * @param time
     * @return
     */
    public static Integer cmdExecutor(String[] values, Long time) throws Exception {
        log.info("【cmdExecutor入参：{}】【长度是：{}】", Arrays.asList(values), values.length);
        if (values.length == 0 || values == null) {
            throw new IllegalArgumentException("【命令是空...请检查】");
        }
        // 定义执行进程
        Process proces = null;
        try {
            // 将执行命令转化字符串
            String buffer = Arrays.stream(values).collect(Collectors.joining());
//            StringBuffer buffer = new StringBuffer();
//            for (int i = 0; i < values.length; i++) {
//                buffer.append(values[i]);
//                if (i < values.length - 1) {
//                    buffer.append(" && ");
//                }
//            }
            log.info("【cmdExecutor：{}】", executor);
            // 当核心线程数大于最大线程数发出警告
            if (executor.getPoolSize() > maxThread) {
                log.warn("【核心线程数大于最大线程数...】");
            }
            log.info("执行命令 bash -c {}", buffer);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(new String[]{"bash", "-c", buffer});
            builder.redirectErrorStream(true);
            proces = builder.start();
            CmdExecutor.readLine cmdExecutor = new CmdExecutor.readLine(proces.getInputStream(), true);
            executor.submit(cmdExecutor);
            // 计算进程执行的耗时
            long begin = System.currentTimeMillis();
            if (proces.waitFor(time, TimeUnit.MILLISECONDS)) {
                log.info("【readLine.stop()】");
                cmdExecutor.setFlag(false);
                long end = System.currentTimeMillis();
                log.info("【执行完成耗时：{}s】", ((end - begin) / 1000L));
                return proces.exitValue();
            } else {
                throw new TimeoutException("【执行超时...】");
            }
        } catch (Exception e) {
            log.error("【executeCmd builder.start(); IOException :{}】", e.getMessage());
            throw e;
        } finally {
            if (proces != null) {
                proces.destroyForcibly();
            }
        }

    }

    public static void main(String[] args) throws IOException {
//        String mvn = "cd /Users/luping/app/mcs_jacoco/clonecode/101000000170035/feature_newtest02&&mvn clean compile >>/Users/luping/report/logs/101000000170035.log";
//        ProcessBuilder builder = new ProcessBuilder();
////        builder.command(new String[]{"bash", "-c", mvn});
//        builder.command(new String[]{"bash", "-c", "echo $PATH"});
//        builder.redirectErrorStream(true);
//        Process process = builder.start();
//        String result = IOUtils.toString(process.getInputStream());
//        System.out.println(result);

//        File file = new File("/Users/luping/app/mcs_jacoco/clonecode/101000000170036/feature_newtest02");
//        String [] fileName = file.list();
//        File [] files = file.listFiles();
//        System.out.println(fileName.length);
//        for (String name:fileName){
//            if (!name.startsWith(".")){
//                System.out.println(name);
//            }
//        }

    }

    private static class readLine implements Runnable {

        /**
         * 输入流
         */
        private final InputStream in;

        /**
         * 标记
         */
        private volatile boolean flag;

        public readLine(InputStream in, boolean flag) {
            this.in = in;
            this.flag = flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        @Override
        public void run() {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.in));
            try {
                String line;
                try {
                    while (flag && (line = bufferedReader.readLine()) != null) {
                        String e = line.trim();
                        if (e.length() != 0) {
                            log.info("【{}】", e);
                        }
                    }
                } catch (IOException ioe) {
                    log.error("【@@@@@@@@@@@@@@ ReadLine Thread, read IOException：{}】", ioe.getMessage());
                }
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ioe) {
                    log.error("【@@@@@@@@@@@@@@ ReadLine Thread, read IOException：{}】", ioe.getMessage());
                }
            }
        }
    }
}


