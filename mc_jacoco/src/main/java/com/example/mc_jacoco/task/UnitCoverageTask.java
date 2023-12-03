package com.example.mc_jacoco.task;

import com.example.mc_jacoco.dao.CoverageJobDao;
import com.example.mc_jacoco.dao.CoverageReportDao;
import com.example.mc_jacoco.entity.po.CoverageJobEntity;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.enums.CoverageFrom;
import com.example.mc_jacoco.enums.CoverageJobEnum;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.service.CodeCovService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author luping
 * @date 2023/12/2 18:08
 */
@Slf4j
@Component
public class UnitCoverageTask implements BaseTask {

    @Resource
    private CoverageJobDao coverageJobDao;

    @Resource
    private CoverageReportDao coverageReportDao;

    @Resource
    private CodeCovService codeCovService;

    private static AtomicInteger counter = new AtomicInteger(0);

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 5 * 60, TimeUnit.SECONDS,
            new SynchronousQueue<>(), r -> new Thread(r, "Code-Coverage-Thread-pool" + counter.getAndIncrement()));

    @Override
    public String getCorn() {
        CoverageJobEntity coverageJob = coverageJobDao.queryByJobNameAndStatue(CoverageJobEnum.UNIT_COVERAGE_COMPUTE.getJobName(), 0);
        if (coverageJob != null) {
            log.info("【任务：{}：Cron时间是：{}】",CoverageJobEnum.UNIT_COVERAGE_COMPUTE.getJobName(),coverageJob.getJobCorn());
            return coverageJob.getJobCorn();
        }
        return null;
    }

    @Override
    public void execute() {
        List<CoverageReportEntity> entityList = coverageReportDao.querByStatusAndfrom(JobStatusEnum.INITIAL.getCode(), CoverageFrom.UNIT.getEnv());
        log.info("【查询diff_coverage_report表单元数据{}条】", entityList.size());
        entityList.forEach(value -> {
            try {
                Integer num = coverageReportDao.casUpdateByStatus(JobStatusEnum.INITIAL.getCode(), JobStatusEnum.WAITING.getCode(),value.getRetryCount(), value.getJobRecordUuid());
                if (num > 0) {
                    log.info("【数据修改成功...uuid：{}】",value.getJobRecordUuid());
                    CompletableFuture.runAsync(()->{
                        codeCovService.calculateUnitCover(value);
                    },executor);
                    int a = 1 / 0;
                }else {
                    log.info("【初始数据修改异常...uuid：{}】",value.getJobRecordUuid());
                }
            }catch (Exception ex){
                log.error("【单测覆盖率数据计算异常...uuid：{}】",value.getJobRecordUuid());
                log.error("【单测覆盖率数据计算异常信息：{}】",ex.getMessage());
                // 将数据恢复到初始状态
                coverageReportDao.casUpdateByStatus(JobStatusEnum.WAITING.getCode(),JobStatusEnum.INITIAL.getCode(),null, value.getJobRecordUuid());
            }
        });
    }

    @Override
    public void run() {
        execute();
    }
}
