package com.example.mc_jacoco.service.serviceImpl;

import com.example.mc_jacoco.constants.AddressConstants;
import com.example.mc_jacoco.constants.NumberConstants;
import com.example.mc_jacoco.dao.CoverageReportDao;
import com.example.mc_jacoco.dao.DeployInfoDao;
import com.example.mc_jacoco.dao.DiffDeployInfoDao;
import com.example.mc_jacoco.entity.dto.CoverageReportDto;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.entity.po.DeployInfoEntity;
import com.example.mc_jacoco.entity.vo.EnvCoverRequest;
import com.example.mc_jacoco.entity.vo.LocalHostRequest;
import com.example.mc_jacoco.entity.vo.ResultReponse;
import com.example.mc_jacoco.entity.vo.UntiCoverRequest;
import com.example.mc_jacoco.enums.CoverageFrom;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.enums.ReportTypeEnum;
import com.example.mc_jacoco.executor.*;
import com.example.mc_jacoco.service.CodeCovService;
import com.example.mc_jacoco.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author luping
 * @date 2023/9/18 22:04
 */
@Slf4j
@Service
public class CodeCovServiceImpl implements CodeCovService {

    @Resource
    private CoverageReportDao coverageReportDao;

    @Resource
    private DeployInfoDao deployInfoDao;

    @Resource
    private DiffDeployInfoDao diffDeployInfoDao;

    @Resource
    private CodeCloneExecutor codeCloneExecutor;

    @Resource
    private CodeCompilerExecutor codeCompilerExecutor;

    @Resource
    private DiffMethodsExecutor diffMethodsExecutor;

    @Resource
    private MavenModuleAddExecutor mavenModuleAddExecutor;

    @Resource
    private UnitTestExecutor unitTestExecutor;

    @Resource
    private ReportAnalyzeExecutor reportAnalyzeExecutor;

    /**
     * 收集覆盖率
     *
     * @param envCoverRequest
     */
    @Override
    public void triggerEnvCov(EnvCoverRequest envCoverRequest) {
        log.info("【方法triggerEnvCov】【收集覆盖率】【入参信息：{}】", envCoverRequest.toString());
        try {
            CoverageReportEntity coverageReportEntity = coverageReportEntityBuild(envCoverRequest);
            if (envCoverRequest.getBaseVersion().equals(envCoverRequest.getNowVersion()) && envCoverRequest.getType().equals(ReportTypeEnum.FULL.getCode())) {
                log.info("【覆盖率基准版本与当前版本一致且计算全量覆盖率，没有增量方法...】");
                coverageReportEntity.setBranchCoverage(DoubleUtil.resultDouble("100"));
                coverageReportEntity.setLineCoverage(DoubleUtil.resultDouble("100"));
                coverageReportEntity.setRequestStatus(JobStatusEnum.NODIFF.getCode());
                coverageReportEntity.setErrMsg("没有增量方法");
                log.info("【没有增量方法插入数据，插入数据：{}】", coverageReportEntity);
                Integer resultId = coverageReportDao.insertCoverageReportById(coverageReportEntity);
                log.info("【没有增量方法插入数据成功，插入条数：{}】", resultId);
            }
            coverageReportEntity.setRequestStatus(JobStatusEnum.WAITING.getCode());
            Integer resultId = coverageReportDao.insertCoverageReportById(coverageReportEntity);
            log.info("【增量数据状态是：{}】【数据落库成功：{}】", JobStatusEnum.WAITING.getCodeMsg(), resultId);
            Integer deployId = deployInfoDao.insertDeployInfo(envCoverRequest);
            log.info("【服务部署数据保存成功：{}】", deployId);
            new Thread(() -> {
                log.info("【开始执行代码克隆...】");
                codeClone(coverageReportEntity);
                log.info("【开始执行代码编译...】");
                cloneCodeAndCompileCode(coverageReportEntity);
                log.info("【计算增量方法diff集合...】");
                calculateDeployDiffMethods(coverageReportEntity);
                log.info("【开始计算代码覆盖率...】");
                calculateEnvCov(coverageReportEntity);
            }).start();
        } catch (Exception e) {
            log.error("【收集覆盖率失败...原因：{}】", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 计算增量方法Diff集合
     *
     * @param coverageReportEntity
     */
    @Override
    public void calculateDeployDiffMethods(CoverageReportEntity coverageReportEntity) {
        log.info("【calculateDeployDiffMethods的入参信息：{}】", coverageReportEntity);
        coverageReportEntity.setRequestStatus(JobStatusEnum.DIFF_METHODS_EXECUTING.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        // 计算diff方法集合
        diffMethodsExecutor.executeDiffMethods(coverageReportEntity);
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
    }

    /**
     * 从项目机器上拉取功能测试的执行轨迹.exec文件，计算增量方法覆盖率
     *
     * @param coverageReportEntity
     */
    @Override
    public void calculateEnvCov(CoverageReportEntity coverageReportEntity) {
        log.info("【计算增量覆盖率方法入参：{}】", coverageReportEntity.toString());
        // 执行日志地址
        String logFile = coverageReportEntity.getLog_file().replace(LocalIpUtil.getBaseUrl() + "logs/", AddressConstants.LOG_PATH);
        String uuid = coverageReportEntity.getJobRecordUuid();
        DeployInfoEntity deployInfoEntity = diffDeployInfoDao.queryInfoById(uuid);
        log.info("【查询机器信息结果：{}】", deployInfoEntity.toString());
        // 根据是增量覆盖率还是全部覆盖率，设置报告名称
        String reportName;
        if (Objects.equals(coverageReportEntity.getType(), ReportTypeEnum.DIFF.getCode())) {
            reportName = "ManualDiffCoverage";
        } else {
            reportName = "ManualCoverage";
        }
        try {
            // 在最近的代码分支执行覆盖率下载
            int executeCmd = CmdExecutor.cmdExecutor(new String[]{"cd " + coverageReportEntity.getNowLocalPathProject() + "&&java -jar " +
                    AddressConstants.JACOCO_PATH + " dump --address " + deployInfoEntity.getAddress() + " --port " + deployInfoEntity.getPort() +
                    " --destfile " + coverageReportEntity.getNowLocalPathProject() + "/jacoco.exec"}, NumberConstants.CMD_TIMEOUT);
            if (executeCmd == 0) {
                String nowLocalJacocoExec = coverageReportEntity.getNowLocalPathProject() + "/jacoco.exec";
                log.info("【覆盖率Dump路径：{}】", nowLocalJacocoExec);
                // 根据UUid删除报告文件
                CmdExecutor.cmdExecutor(new String[]{"rm -rf " + AddressConstants.REPORT_PATH + coverageReportEntity.getJobRecordUuid()}, NumberConstants.CMD_TIMEOUT);
                String[] moduleList = new String[]{};
                if (deployInfoEntity.getChildModules() != null) {
                    moduleList = deployInfoEntity.getChildModules().split(",");
                }
                StringBuffer buffer = new StringBuffer("java -jar " + AddressConstants.JACOCO_PATH + " report " + nowLocalJacocoExec);
                if (moduleList.length == 0) {
                    buffer.append(" --sourcefiles ./src/main/java/ ");
                    buffer.append(" --classfiles ./target/classes/com/ ");
                } else {
                    log.info("【开始计算多module覆盖率....】");
                    for (String module : moduleList) {
                        buffer.append(" --sourcefiles ./" + module + "/src/main/java/ ");
                        buffer.append(" --classfiles ./" + module + "/target/classes/com/ ");
                    }
                }
                // 执行jacoco获取exec文件时，增加Diff方法集合参数，用于后面计算增量方法覆盖率使用
                if (!StringUtils.isEmpty(coverageReportEntity.getDiffMethod())) {
                    buffer.append(" --diffFile " + coverageReportEntity.getDiffMethod());
                }
                buffer.append(" --html ./jacocoreport/ --encoding utf-8 --name " + reportName + ">>" + logFile);
                int covExitCode = CmdExecutor.cmdExecutor(new String[]{"cd " + coverageReportEntity.getNowLocalPathProject() + "&&" + buffer.toString()}, NumberConstants.CMD_TIMEOUT);
                // 加载项目下的index.html文件
                File covFile = new File(coverageReportEntity.getNowLocalPathProject() + "/jacocoreport/index.html");
                if (covExitCode == 0 && covFile.isFile()) {
                    log.info("【增量覆盖率HTML文件生成成功】【地址：{}】", covFile.getAbsolutePath());
                    try {
                        // 解析index.html文件
                        Document document = Jsoup.parse(covFile.getAbsoluteFile(), "UTF-8", "");
                        // 获取字节码指令数据
                        Elements bars = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("bar");
                        // 获取未覆盖的方法、行、类、判断条件
                        Elements lineCtr1 = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("ctr1");
                        // 获取方法总数、行总数、类总数、圈复杂度
                        Elements lineCtr2 = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("ctr2");
                        double lineCoverage = 100;
                        double branchCoverage = 100;
                        if (document != null && bars != null) {
                            // 覆盖率报告文件行未覆盖的数量
                            double lineMolecule = Double.parseDouble(lineCtr1.get(1).text());
                            log.info("【lineMolecule结果是：{}】", lineMolecule);
                            // 覆盖率报告文件行的总数量
                            double lineDenominator = Double.parseDouble(lineCtr2.get(3).text());
                            log.info("【lineDenominator结果是：{}】", lineDenominator);
                            // 计算行覆盖率
                            lineCoverage = (lineDenominator - lineMolecule) / lineDenominator * 100;
                            log.info("【行覆盖率计算结果是：{}】", lineCoverage);
                            String[] barString = bars.get(1).text().split(" of ");
                            // 获取分支的未覆盖数
                            double branchMolecule = Double.parseDouble(barString[0]);
                            log.info("【branchMolecule结果是：{}】", branchMolecule);
                            // 获取分支的全部总数
                            double branchDenominator = Double.parseDouble(barString[1]);
                            log.info("【branchDenominator结果是：{}】", branchDenominator);
                            // 计算分支覆盖率，如果总分支数<=0的时候，意味着覆盖率报告是没有分支数据的，默认赋值为0
                            if (branchDenominator > 0.0) {
                                branchCoverage = (branchDenominator - branchMolecule) / branchDenominator * 100;
                            } else {
                                branchCoverage = 0;
                            }
                            log.info("【分支覆盖率计算结果是：{}】", branchCoverage);
                        }
                        // 将生成的覆盖率报告拷贝到，根目录下的report目录中
                        String[] cpReportCmd = new String[]{"cp -rf " + covFile.getParent() + " " + AddressConstants.REPORT_PATH + coverageReportEntity.getJobRecordUuid() + "/"};
                        CmdExecutor.cmdExecutor(cpReportCmd, NumberConstants.CMD_TIMEOUT);
                        coverageReportEntity.setReportUrl(LocalIpUtil.getBaseUrl() + coverageReportEntity.getJobRecordUuid() + "/index.html");
                        coverageReportEntity.setRequestStatus(JobStatusEnum.SUCCESS.getCode());
                        coverageReportEntity.setLineCoverage(lineCoverage);
                        coverageReportEntity.setBranchCoverage(branchCoverage);
                        return;
                    } catch (Exception e) {
                        coverageReportEntity.setRequestStatus(JobStatusEnum.ENVREPORT_FAIL.getCode());
                        coverageReportEntity.setErrMsg("覆盖率报告文件解析失败...");
                        log.error("【解析jacoco报告失败】【失败原因：{}】", e.getMessage());
                        throw new RuntimeException("【覆盖率报告文件解析失败...原因：】：" + e.getMessage());
                    }
                } else {
                    // 覆盖率生成失败后考虑到是多module情况，重新针对每个module生成覆盖率，将覆盖率的报告进行合并
                    coverageReportEntity.setRequestStatus(JobStatusEnum.ENVREPORT_FAIL.getCode());
                    int littleExitCode = 0;
                    List<String> childReportList = new ArrayList<>();
                    if (!(moduleList.length == 0)) {
                        log.info("【多module分开计算覆盖率】【多module列表：{}】", Arrays.asList(moduleList));
                        for (String module : moduleList) {
                            StringBuffer moduleBuffer = new StringBuffer("java -jar " + AddressConstants.JACOCO_PATH + " report ./jacoco.exec");
                            moduleBuffer.append(" --sourcefiles ./" + module + "/src/main/java/");
                            moduleBuffer.append(" --classfiles ./" + module + "/target/classes/com/");
                            if (!StringUtils.isEmpty(coverageReportEntity.getDiffMethod())) {
                                moduleBuffer.append(" --diffFile " + coverageReportEntity.getDiffMethod());
                            }
                            moduleBuffer.append(" --html jacocoreport/" + module + " --encoding utf-8 --name " + reportName + ">>" + logFile);
                            littleExitCode += CmdExecutor.cmdExecutor(new String[]{"cd " + coverageReportEntity.getNowLocalPathProject() + "&&" + moduleBuffer.toString()}, NumberConstants.CMD_TIMEOUT);
                            // 每个module生成的覆盖率报告地址
                            String moduleReport = coverageReportEntity.getNowLocalPathProject() + "/jacocoreport/" + module + "/index.html";
                            log.info("【Module生成的覆盖率报告地址：{}】", moduleReport);
                            if (littleExitCode == 0) {
                                // 将每个module的覆盖率报告文件加入到childReportList
                                childReportList.add(moduleReport);
                            }
                        }
                        log.info("【多module覆盖率报告数据：{}】", childReportList);
                        // 将覆盖率报告进行合并
                        if (littleExitCode == 0) {
                            // 将覆盖率的报告全部拷贝到根目录下的文件里
                            CmdExecutor.cmdExecutor(new String[]{"cd " + coverageReportEntity.getNowLocalPathProject() + "&&" + "cp -rf jacocoreport " + AddressConstants.REPORT_PATH + coverageReportEntity.getJobRecordUuid() + "/"}, NumberConstants.CMD_TIMEOUT);
                            Integer[] result = MergeReportHtml.mergerHtml(childReportList, AddressConstants.REPORT_PATH + coverageReportEntity.getJobRecordUuid() + "/index.html");
                            if (result[0] == 1) {
                                // 将图像拷贝JacocoSource路径下
                                CmdExecutor.cmdExecutor(new String[]{"cp -r " + AddressConstants.JACOCO_RESOURE_PATH + "/" + " " + AddressConstants.REPORT_PATH + coverageReportEntity.getJobRecordUuid()}, NumberConstants.CMD_TIMEOUT);
                                coverageReportEntity.setReportUrl(LocalIpUtil.getBaseUrl() + coverageReportEntity.getJobRecordUuid() + "/index.html");
                                coverageReportEntity.setRequestStatus(JobStatusEnum.SUCCESS.getCode());
                                coverageReportEntity.setLineCoverage(Double.valueOf(result[2]));
                                coverageReportEntity.setBranchCoverage(Double.valueOf(result[1]));
                            } else {
                                log.error("【覆盖率生成失败...】");
                                coverageReportEntity.setRequestStatus(JobStatusEnum.ENVREPORT_FAIL.getCode());
                                coverageReportEntity.setErrMsg("覆盖率报告生成失败...");
                            }
                        } else {
                            log.error("【覆盖率报告合并失败...】");
                            coverageReportEntity.setRequestStatus(JobStatusEnum.COVHTML_FAIL.getCode());
                            coverageReportEntity.setErrMsg("覆盖率报告合并失败...");
                        }
                    } else {
                        log.error("【单模块覆盖率生成失败...】");
                        coverageReportEntity.setRequestStatus(JobStatusEnum.ENVREPORT_FAIL.getCode());
                        coverageReportEntity.setErrMsg("单模块覆盖率生成失败...");
                    }
                }
            } else {
                log.error("【解析Jacoco.exec文件失败...】");
                coverageReportEntity.setRequestStatus(JobStatusEnum.ENVREPORT_FAIL.getCode());
                coverageReportEntity.setErrMsg("解析Jacoco.exec文件失败...");
            }
        } catch (Exception e) {
            coverageReportEntity.setRequestStatus(JobStatusEnum.ENVREPORT_FAIL.getCode());
            coverageReportEntity.setErrMsg("计算覆盖率失败...");
            log.error("【计算覆盖率失败...】【失败原因：{}】", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            coverageReportDao.updateCoverageReportById(coverageReportEntity);
        }
    }

    /**
     * 单元测试覆盖率计算
     * @param coverageReportEntity
     */
    @Override
    public void calculateUnitCover(CoverageReportEntity coverageReportEntity) {
        log.info("【{}:计算增量代码覆盖率步骤...开始执行uuid:{}】",Thread.currentThread().getName(),coverageReportEntity.getJobRecordUuid());
        coverageReportEntity.setRequestStatus(JobStatusEnum.CLONING.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        // 执行代码克隆
        codeClone(coverageReportEntity);
        // 计算增量方法
        coverageReportEntity.setRequestStatus(JobStatusEnum.DIFF_METHODS_EXECUTING.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        diffMethodsExecutor.executeDiffMethods(coverageReportEntity);
        coverageReportEntity.setRequestStatus(JobStatusEnum.DIFF_METHOD_DONE.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        // 添加集成模块
        coverageReportEntity.setRequestStatus(JobStatusEnum.ADDMODULING.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        mavenModuleAddExecutor.addMavenModule(coverageReportEntity);
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        if (!coverageReportEntity.getRequestStatus().equals(JobStatusEnum.ADDMODULE_DONE.getCode())){
            return;
        }
        // 执行单元测试
        coverageReportEntity.setRequestStatus(JobStatusEnum.UNITTESTEXECUTING.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        unitTestExecutor.executeUnitTest(coverageReportEntity);
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        if (!coverageReportEntity.getRequestStatus().equals(JobStatusEnum.UNITTEST_DONE.getCode())){
            return;
        }
        // 分析覆盖率报告
        coverageReportEntity.setRequestStatus(JobStatusEnum.REPORTPARSING.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        reportAnalyzeExecutor.parseReport(coverageReportEntity);
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        if (!coverageReportEntity.getRequestStatus().equals(JobStatusEnum.PARSEREPORT_DONE.getCode())){
            return;
        }
        // 复制报告
        coverageReportEntity.setRequestStatus(JobStatusEnum.REPORTCOPYING.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        reportAnalyzeExecutor.copyReport(coverageReportEntity);
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        if (!coverageReportEntity.getRequestStatus().equals(JobStatusEnum.COPYREPORT_DONE.getCode())){
            return;
        }
        // 更新最终状态
        coverageReportEntity.setRequestStatus(JobStatusEnum.SUCCESS.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        log.info("【uuid:{}的覆盖率任务计算成功...状态：{}】",coverageReportEntity.getJobRecordUuid(),coverageReportEntity.getRequestStatus());
    }

    /**
     * 查询覆盖率数据
     */
    @Override
    public ResultReponse getResultEnvCover(String uuid) {
        CoverageReportEntity coverageReport = coverageReportDao.queryCoverageReportByUuid(uuid);
        log.info("【getResultEnvCover方法-查询覆盖率信息结果：{}】", coverageReport);
        ResultReponse resultReponse = new ResultReponse();
        if (coverageReport != null) {
            BeanUtils.copyProperties(coverageReport, resultReponse);
            resultReponse.setCoverageCode(coverageReport.getRequestStatus());
            resultReponse.setCoverageMsg(StringUtils.isEmpty(coverageReport.getErrMsg()) ? JobStatusEnum.COVERGER_RESULT_SUCCESS_MSG.getCodeMsg() : coverageReport.getErrMsg());
            log.info("【getResultEnvCover方法-返回覆盖率信息结果：{}】", resultReponse);
            return resultReponse;
        } else {
            return ResultReponse.ResultReponseFailBuid("uuid不存在");
        }
    }

    /**
     * 手工触发代码覆盖率计算
     *
     * @param localHostRequest
     * @return
     */
    @Override
    public ResultReponse getLocalCoverResult(LocalHostRequest localHostRequest) {
        // 补充Path信息
        localHostRequest.setBaseLocalPath(localHostRequest.getBaseLocalPath().endsWith("/") ? localHostRequest.getBaseLocalPath() : localHostRequest.getBaseLocalPath() + "/");
        localHostRequest.setNowLocalPath(localHostRequest.getNowLocalPath().endsWith("/") ? localHostRequest.getNowLocalPath() : localHostRequest.getNowLocalPath() + "/");
        // 计算增量代码覆盖率
        String diffMethods = diffMethodsExecutor.executeDiffMethodsForEnv(localHostRequest.getBaseLocalPath(), localHostRequest.getNowLocalPath(), localHostRequest.getBaseVersion(), localHostRequest.getNowVersion());
        if (diffMethods == null) {
            return ResultReponse.ResultReponseFailBuid("未检测到增量代码", JobStatusEnum.COVERGER_RESULT_FAIL_MSG.getCode());
        }
        localHostRequest.setBaseLocalPathProject(localHostRequest.getBaseLocalPath() + FilesUtil.resultfileDirectory(localHostRequest.getBaseLocalPath()));
        localHostRequest.setNowLocalPathProject(localHostRequest.getNowLocalPath() + FilesUtil.resultfileDirectory(localHostRequest.getNowLocalPath()));
        ResultReponse pullExcel = pullExecFile(localHostRequest, diffMethods);
        if (pullExcel == null) {
            return ResultReponse.ResultReponseFailBuid("代码覆盖率计算失败...请联系管理员介入", JobStatusEnum.COVERGER_RESULT_FAIL_MSG.getCode());
        }
        return pullExcel;
    }

    /**
     * 触发单元测试覆盖率
     *
     * @param untiCoverRequest
     */
    @Override
    public Result triggerUnitCov(UntiCoverRequest untiCoverRequest) {
        CoverageReportEntity coverRequest = coverageReportDao.queryCoverageReportByUuid(untiCoverRequest.getUuid());
        if (coverRequest != null) {
            log.error(String.format("uuid：%s已经调用过，请勿重复调用", untiCoverRequest.getUuid()));
            return Result.fail(String.format("uuid：%s已经调用过，请勿重复调用", untiCoverRequest.getUuid()));
        }
        CoverageReportEntity coverReport = new CoverageReportEntity();
        coverReport.setJobRecordUuid(untiCoverRequest.getUuid());
        coverReport.setGitUrl(untiCoverRequest.getGitUrl());
        coverReport.setBaseVersion(untiCoverRequest.getBaseVersion());
        coverReport.setNowVersion(untiCoverRequest.getNowVersion());
        coverReport.setSubModule(StringUtils.isEmpty(untiCoverRequest.getSubModule()) ? "" : untiCoverRequest.getSubModule());
        coverReport.setType(untiCoverRequest.getType());
        coverReport.setLineCoverage(Double.parseDouble("-1"));
        coverReport.setBranchCoverage(Double.parseDouble("-1"));
        coverReport.setEnvType(untiCoverRequest.getEnvType());
        coverReport.setFrom(CoverageFrom.UNIT.getEnv());
        coverReport.setRequestStatus(JobStatusEnum.INITIAL.getCode());
        log.info("【保存覆盖率数据：{}】", coverReport.toString());
        try {
            Integer reportInsert = coverageReportDao.insertCoverageReportById(coverReport);
            if (reportInsert > 0) {
                log.info(String.format("uuid：%s单元测试数据保存成功", coverReport.getJobRecordUuid()));
                return Result.success(String.format("uuid：%s单元测试数据保存成功", coverReport.getJobRecordUuid()));
            } else {
                log.error(String.format("uuid：%s单元测试数据保存失败", coverReport.getJobRecordUuid()));
                return Result.fail(String.format("uuid：%s单元测试数据保存失败", coverReport.getJobRecordUuid()));
            }
        } catch (Exception e) {
            log.error(String.format("uuid：%s单元测试数据保存失败", coverReport.getJobRecordUuid()) + "【失败原因：{}】",e.getMessage());
            return Result.fail(String.format("uuid：%s单元测试数据保存失败", coverReport.getJobRecordUuid()));
        }
    }

    private ResultReponse pullExecFile(LocalHostRequest localHostRequest, String diffFile) {
        log.info("【计算增量覆盖率方法入参：{}】", localHostRequest.toString());
        ResultReponse resultRequest = new ResultReponse();
        // 根据是增量覆盖率还是全部覆盖率，设置报告名称
        String reportName = "ManualDiffCoverage";
        try {
            // 在最近的代码分支执行覆盖率下载
            int executeCmd = CmdExecutor.cmdExecutor(new String[]{"cd " + localHostRequest.getNowLocalPathProject() + "&&java -jar " +
                    AddressConstants.JACOCO_PATH + " dump --address " + localHostRequest.getAddress() + " --port " + localHostRequest.getPort() +
                    " --destfile " + localHostRequest.getNowLocalPathProject() + "/jacoco.exec"}, NumberConstants.CMD_TIMEOUT);
            if (executeCmd == 0) {
                String nowLocalJacocoExec = localHostRequest.getNowLocalPathProject() + "/jacoco.exec";
                log.info("【覆盖率Dump路径：{}】", nowLocalJacocoExec);
                // 根据UUid删除报告文件
                CmdExecutor.cmdExecutor(new String[]{"rm -rf " + AddressConstants.REPORT_PATH + localHostRequest.getUuid()}, NumberConstants.CMD_TIMEOUT);
                String[] moduleList = new String[]{};
                if (!TextUtils.isEmpty(localHostRequest.getSubModule())) {
                    moduleList = localHostRequest.getSubModule().split(",");
                }
                StringBuffer buffer = new StringBuffer("java -jar " + AddressConstants.JACOCO_PATH + " report " + nowLocalJacocoExec);
                if (moduleList.length == 0) {
                    buffer.append(" --sourcefiles ./src/main/java/ ");
                    buffer.append(" --classfiles ./target/classes/com/ ");
                } else {
                    log.info("【开始计算多module覆盖率....】");
                    for (String module : moduleList) {
                        buffer.append(" --sourcefiles ./" + module + "/src/main/java/ ");
                        buffer.append(" --classfiles ./" + module + "/target/classes/com/ ");
                    }
                }
                // 执行jacoco获取exec文件时，增加Diff方法集合参数，用于后面计算增量方法覆盖率使用
                if (!StringUtils.isEmpty(diffFile)) {
                    buffer.append(" --diffFile " + diffFile);
                }
                buffer.append(" --html ./jacocoreport/ --encoding utf-8 --name " + reportName);
                int covExitCode = CmdExecutor.cmdExecutor(new String[]{"cd " + localHostRequest.getNowLocalPathProject() + "&&" + buffer.toString()}, NumberConstants.CMD_TIMEOUT);
                // 加载项目下的index.html文件
                File covFile = new File(localHostRequest.getNowLocalPathProject() + "/jacocoreport/index.html");
                if (covExitCode == 0 && covFile.isFile()) {
                    log.info("【增量覆盖率HTML文件生成成功】【地址：{}】", covFile.getAbsolutePath());
                    try {
                        // 解析index.html文件
                        Document document = Jsoup.parse(covFile.getAbsoluteFile(), "UTF-8", "");
                        // 获取字节码指令数据
                        Elements bars = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("bar");
                        // 获取未覆盖的方法、行、类、判断条件
                        Elements lineCtr1 = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("ctr1");
                        // 获取方法总数、行总数、类总数、圈复杂度
                        Elements lineCtr2 = document.getElementById("coveragetable").getElementsByTag("tfoot").first().getElementsByClass("ctr2");
                        double lineCoverage = 100;
                        double branchCoverage = 100;
                        if (document != null && bars != null) {
                            // 覆盖率报告文件行未覆盖的数量
                            double lineMolecule = Double.parseDouble(lineCtr1.get(1).text());
                            log.info("【lineMolecule结果是：{}】", lineMolecule);
                            // 覆盖率报告文件行的总数量
                            double lineDenominator = Double.parseDouble(lineCtr2.get(3).text());
                            log.info("【lineDenominator结果是：{}】", lineDenominator);
                            // 计算行覆盖率
                            lineCoverage = (lineDenominator - lineMolecule) / lineDenominator * 100;
                            log.info("【行覆盖率计算结果是：{}】", lineCoverage);
                            String[] barString = bars.get(1).text().split(" of ");
                            // 获取分支的未覆盖数
                            double branchMolecule = Double.parseDouble(barString[0]);
                            log.info("【branchMolecule结果是：{}】", branchMolecule);
                            // 获取分支的全部总数
                            double branchDenominator = Double.parseDouble(barString[1]);
                            log.info("【branchDenominator结果是：{}】", branchDenominator);
                            // 计算分支覆盖率，如果总分支数<=0的时候，意味着覆盖率报告是没有分支数据的，默认赋值为0
                            if (branchDenominator > 0.0) {
                                branchCoverage = (branchDenominator - branchMolecule) / branchDenominator * 100;
                            } else {
                                branchCoverage = 0;
                            }
                            log.info("【分支覆盖率计算结果是：{}】", branchCoverage);
                        }
                        // 将生成的覆盖率报告拷贝到，根目录下的report目录中
                        String[] cpReportCmd = new String[]{"cp -rf " + covFile.getParent() + " " + AddressConstants.REPORT_PATH + localHostRequest.getUuid() + "/"};
                        CmdExecutor.cmdExecutor(cpReportCmd, NumberConstants.CMD_TIMEOUT);
                        resultRequest.setCoverageCode(JobStatusEnum.COVERGER_RESULT_SUCCESS_MSG.getCode());
                        resultRequest.setCoverageMsg("覆盖率计算成功");
                        resultRequest.setLineCoverage(lineCoverage);
                        resultRequest.setBranchCoverage(branchCoverage);
                        resultRequest.setReportUrl(localHostRequest.getNowLocalPathProject() + "/jacocoreport/index.html");
                    } catch (Exception e) {
                        log.error("【解析jacoco报告失败】【失败原因：{}】", e.getMessage());
                        throw new RuntimeException("【覆盖率报告文件解析失败...原因：】：" + e.getMessage());
                    }
                } else {
                    // 覆盖率生成失败后考虑到是多module情况，重新针对每个module生成覆盖率，将覆盖率的报告进行合并
                    int littleExitCode = 0;
                    List<String> childReportList = new ArrayList<>();
                    if (!(moduleList.length == 0)) {
                        log.info("【多module分开计算覆盖率】【多module列表：{}】", Arrays.asList(moduleList));
                        for (String module : moduleList) {
                            StringBuffer moduleBuffer = new StringBuffer("java -jar " + AddressConstants.JACOCO_PATH + " report ./jacoco.exec");
                            moduleBuffer.append(" --sourcefiles ./" + module + "/src/main/java/");
                            moduleBuffer.append(" --classfiles ./" + module + "/target/classes/com/");
                            if (!StringUtils.isEmpty(diffFile)) {
                                moduleBuffer.append(" --diffFile " + diffFile);
                            }
                            moduleBuffer.append(" --html jacocoreport/" + module + " --encoding utf-8 --name " + reportName);
                            littleExitCode += CmdExecutor.cmdExecutor(new String[]{"cd " + localHostRequest.getNowLocalPathProject() + "&&" + moduleBuffer.toString()}, NumberConstants.CMD_TIMEOUT);
                            // 每个module生成的覆盖率报告地址
                            String moduleReport = localHostRequest.getNowLocalPathProject() + "/jacocoreport/" + module + "/index.html";
                            log.info("【Module生成的覆盖率报告地址：{}】", moduleReport);
                            if (littleExitCode == 0) {
                                // 将每个module的覆盖率报告文件加入到childReportList
                                childReportList.add(moduleReport);
                            }
                        }
                        log.info("【多module覆盖率报告数据：{}】", childReportList);
                        // 将覆盖率报告进行合并
                        if (littleExitCode == 0) {
                            // 将覆盖率的报告全部拷贝到根目录下的文件里
                            CmdExecutor.cmdExecutor(new String[]{"cd " + localHostRequest.getNowLocalPathProject() + "&&" + "cp -rf jacocoreport " + AddressConstants.REPORT_PATH + localHostRequest.getUuid() + "/"}, NumberConstants.CMD_TIMEOUT);
                            Integer[] result = MergeReportHtml.mergerHtml(childReportList, AddressConstants.REPORT_PATH + localHostRequest.getUuid() + "/index.html");
                            if (result[0] == 1) {
                                // 将图像拷贝JacocoSource路径下
                                CmdExecutor.cmdExecutor(new String[]{"cp -r " + AddressConstants.JACOCO_RESOURE_PATH + "/" + " " + AddressConstants.REPORT_PATH + localHostRequest.getUuid()}, NumberConstants.CMD_TIMEOUT);
                                resultRequest.setCoverageCode(JobStatusEnum.COVERGER_RESULT_SUCCESS_MSG.getCode());
                                resultRequest.setCoverageMsg("覆盖率计算成功");
                                resultRequest.setLineCoverage(Double.valueOf(result[2]));
                                resultRequest.setBranchCoverage(Double.valueOf(result[1]));
                                resultRequest.setReportUrl(AddressConstants.REPORT_PATH + localHostRequest.getUuid() + "/index.html");
                            } else {
                                log.error("【覆盖率生成失败...】");
                                return null;
                            }
                        } else {
                            log.error("【覆盖率报告合并失败...】");
                            return null;
                        }
                    } else {
                        log.error("【单模块覆盖率生成失败...】");
                        return null;
                    }
                }
            } else {
                log.error("【解析Jacoco.exec文件失败...】");
                return null;
            }
        } catch (Exception e) {
            log.error("【计算覆盖率失败...】【失败原因：{}】", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return resultRequest;
    }

    /**
     * 代码编译
     *
     * @param coverageReportEntity
     */
    public void cloneCodeAndCompileCode(CoverageReportEntity coverageReportEntity) {
        coverageReportEntity.setRequestStatus(JobStatusEnum.CLONING.getCode());
        Integer updateId = coverageReportDao.updateCoverageReportById(coverageReportEntity);
        log.info("【数据更新成功：{}】", updateId);
        CoverageReportDto coverageReportDto = new CoverageReportDto();
        BeanUtils.copyProperties(coverageReportEntity, coverageReportDto);
        //开始编译代码
        coverageReportEntity.setRequestStatus(JobStatusEnum.COMPILING.getCode());
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        codeCompilerExecutor.compileCode(coverageReportDto);
        BeanUtils.copyProperties(coverageReportDto, coverageReportEntity);
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        if (!JobStatusEnum.COMPILE_DONE.getCode().equals(coverageReportEntity.getRequestStatus())) {
            log.error("【{}】编译失败...【线程名称是：{}】", coverageReportEntity.getJobRecordUuid(), Thread.currentThread().getName());
        }
        // 获取Pom的Modules模块
        String pomPath = coverageReportDto.getNowLocalPathProject() + "/pom.xml";
        ArrayList<String> modulesList = MavenModuleUtil.getValidModules(pomPath);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < modulesList.size(); i++) {
            if (i < modulesList.size() - 1) {
                buffer.append(modulesList.get(i)).append(",");
            } else {
                buffer.append(modulesList.get(i));
            }
        }
        DeployInfoEntity deployInfoEntity = deployInfoEntityBuild(coverageReportEntity, buffer.toString());
        log.info("【更新部署表信息内容入参：{}】", deployInfoEntity);
        Integer updateDeployment = deployInfoDao.updateDeployInfoByUuid(deployInfoEntity);
        log.info("【更新部署表信息内容成功返回值：{}】", updateDeployment);
    }

    /**
     * 代码克隆
     */
    public void codeClone(CoverageReportEntity coverageReportEntity) {
        codeCloneExecutor.cloneCode(coverageReportEntity);
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        if (JobStatusEnum.CLONE_FAIL.getCode().equals(coverageReportEntity.getRequestStatus())) {
            log.error("【代码克隆失败...UUID：{}】【线程名称是：{}】", coverageReportEntity.getJobRecordUuid(), Thread.currentThread().getName());
        }
    }

    private CoverageReportEntity coverageReportEntityBuild(EnvCoverRequest envCoverRequest) {
        CoverageReportEntity coverageReportEntity = new CoverageReportEntity();
        coverageReportEntity.setFrom(CoverageFrom.ENV.getEnv());
        coverageReportEntity.setEnvType("");
        coverageReportEntity.setJobRecordUuid(envCoverRequest.getUuid());
        coverageReportEntity.setGitUrl(envCoverRequest.getGitUrl());
        coverageReportEntity.setNowVersion(envCoverRequest.getNowVersion());
        coverageReportEntity.setBaseVersion(envCoverRequest.getBaseVersion());
        coverageReportEntity.setType(envCoverRequest.getType());
        coverageReportEntity.setSubModule(envCoverRequest.getSubModule());
        coverageReportEntity.setLineCoverage(-1.00);
        coverageReportEntity.setBranchCoverage(-1.00);
        return coverageReportEntity;
    }

    private DeployInfoEntity deployInfoEntityBuild(CoverageReportEntity coverageReportEntity, String modules) {
        DeployInfoEntity deployInfoEntity = new DeployInfoEntity();
        deployInfoEntity.setUuid(coverageReportEntity.getJobRecordUuid());
        deployInfoEntity.setCodePath(coverageReportEntity.getNowLocalPath());
        deployInfoEntity.setChildModules(modules);
        return deployInfoEntity;
    }
}
