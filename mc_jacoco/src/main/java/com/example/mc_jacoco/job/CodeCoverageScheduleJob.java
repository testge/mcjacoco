package com.example.mc_jacoco.job;

import com.example.mc_jacoco.task.BaseTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;



/**
 * @author luping
 * @date 2023/12/2 17:53
 */
@Slf4j
@Component
public class CodeCoverageScheduleJob implements SchedulingConfigurer {

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("【定时任务开始执行...】");
        ThreadPoolTaskScheduler poolTaskScheduler = poolTaskSchedulerBuild();
        taskRegistrar.setTaskScheduler(poolTaskScheduler);
        Map<String, BaseTask> taskMap = applicationContext.getBeansOfType(BaseTask.class);
        for (String key:taskMap.keySet()){
            BaseTask baseTask = taskMap.get(key);
            taskRegistrar.addTriggerTask(baseTask,triggerContext -> {
                return new CronTrigger(baseTask.getCorn()).nextExecutionTime(triggerContext);
            });
        }
    }


    /**
     * 定义线程池，线程10个
     * @return
     */
    private ThreadPoolTaskScheduler poolTaskSchedulerBuild(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("job-coverage-unit-");
        taskScheduler.setAwaitTerminationMillis(60);
        taskScheduler.initialize();
        return taskScheduler;

    }
}
