package com.example.mc_jacoco.service.serviceImpl;

import com.example.mc_jacoco.constants.AddressConstants;
import com.example.mc_jacoco.constants.NumberConstants;
import com.example.mc_jacoco.dao.CoverageReportDao;
import com.example.mc_jacoco.dao.DeployInfoDao;
import com.example.mc_jacoco.entity.dto.CoverageReportDto;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.entity.po.DeployInfoEntity;
import com.example.mc_jacoco.entity.vo.EnvCoverRequest;
import com.example.mc_jacoco.enums.CoverageFrom;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.enums.ReportTypeEnum;
import com.example.mc_jacoco.executor.CmdExecutor;
import com.example.mc_jacoco.executor.CodeCloneExecutor;
import com.example.mc_jacoco.executor.CodeCompilerExecutor;
import com.example.mc_jacoco.executor.DiffMethodsExecutor;
import com.example.mc_jacoco.service.CodeCovService;
import com.example.mc_jacoco.util.DoubleUtil;
import com.example.mc_jacoco.util.LocalIpUtil;
import com.example.mc_jacoco.util.MavenModuleUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
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
    private CodeCloneExecutor codeCloneExecutor;

    @Resource
    private CodeCompilerExecutor codeCompilerExecutor;

    @Resource
    private DiffMethodsExecutor diffMethodsExecutor;

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
                log.info("【开始执行代码编译...】");
                cloneCodeAndCompileCode(coverageReportEntity);
                log.info("【计算增量方法diff集合...】");
                calculateDeployDiffMethods(coverageReportEntity);
                log.info("【开始计算代码覆盖率...】");
                calculateEnvCov(coverageReportEntity);
            }).start();
        } catch (Exception e) {
            log.error(e.getMessage());
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
        DeployInfoEntity deployInfoEntity = deployInfoDao.queryDeployInfoByUuid(uuid);
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
                // 根据UUid删除报告文件
//                CmdExecutor.cmdExecutor(new String[]{"rm -rf " + AddressConstants.REPORT_PATH + coverageReportEntity.getJobRecordUuid()}, NumberConstants.CMD_TIMEOUT);
                String[] moduleList = new String[]{};
                if (deployInfoEntity.getChildModules() != null) {
                    moduleList = deployInfoEntity.getChildModules().split(",");
                }
                StringBuffer buffer = new StringBuffer("java -jar " + AddressConstants.JACOCO_PATH + " report " + coverageReportEntity.getNowLocalPathProject() + "/jacoco.exec");
                if (moduleList.length == 0) {
                    buffer.append(" --sourcefiles ./src/main/java/ ");
                    buffer.append(" --classfiles ./target/classes/com/ ");
                } else {
                    // TODO 多module处理路径
                    for (String module : moduleList) {
                        buffer.append(" --sourcefiles ./" + module + "src/main/java/ ");
                        buffer.append(" --classfiles ./" + module + "target/classes/com/ ");
                    }
                }
                // 执行jacoco获取exec文件时，增加Diff方法集合参数，用于后面计算增量方法覆盖率使用
                if (!StringUtils.isEmpty(coverageReportEntity.getDiffMethod())) {
                    buffer.append(" --diffFile " + coverageReportEntity.getDiffMethod());
                }
                buffer.append(" --html ./jacocoreport/ --encoding utf-8 --name " + reportName + ">>" + logFile);
                int covExitCode = CmdExecutor.cmdExecutor(new String[]{"cd " + coverageReportEntity.getNowLocalPathProject() + "&&" + buffer.toString()}, NumberConstants.CMD_TIMEOUT);
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
                            System.out.println("barString");
                            System.out.println(barString[0]);
                            System.out.println(barString[1]);
                            // 获取分支的未覆盖数
                            double branchMolecule = Double.parseDouble(barString[0]);
                            log.info("【branchMolecule结果是：{}】", branchMolecule);
                            // 获取分支的全部总数
                            double branchDenominator = Double.parseDouble(barString[1]);
                            log.info("【branchDenominator结果是：{}】", branchDenominator);
                            // 计算分支覆盖率
                            if (branchDenominator > 0.0) {
                                branchCoverage = (branchDenominator - branchMolecule) / branchDenominator * 100;
                            } else {
                                branchCoverage = 100;
                            }
                            log.info("【分支覆盖率计算结果是：{}】", branchCoverage);
                        }
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
                    }
                }
            }
        } catch (Exception e) {
            log.error("【计算覆盖率失败...】【失败原因：{}】", e.getMessage());
        } finally {
            coverageReportDao.updateCoverageReportById(coverageReportEntity);
        }

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
        codeCloneExecutor.cloneCode(coverageReportEntity);
        coverageReportDao.updateCoverageReportById(coverageReportEntity);
        if (JobStatusEnum.CLONE_FAIL.getCode().equals(coverageReportEntity.getRequestStatus())) {
            log.error("【代码克隆失败...UUID：{}】【线程名称是：{}】", coverageReportEntity.getJobRecordUuid(), Thread.currentThread().getName());
            return;
        }
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
