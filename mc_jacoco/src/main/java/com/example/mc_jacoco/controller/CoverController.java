package com.example.mc_jacoco.controller;

import com.example.mc_jacoco.entity.vo.*;
import com.example.mc_jacoco.service.CodeCovService;
import com.example.mc_jacoco.util.Result;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author luping
 * @date 2023/9/16 17:06
 */
@Log4j2
@RestController
@RequestMapping("/mc/cover/v1")
public class CoverController {

    @Resource
    private CodeCovService codeCovService;

    /**
     * 指定环境覆盖率收集
     *
     * @param envCoverRequest 前端调用入参
     * @return
     */
    @RequestMapping(value = "/tiggerEnvCov", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result tiggerEnvCov(@RequestBody @Validated EnvCoverRequest envCoverRequest) {
        log.info("【指定环境覆盖率采集入参：{}】", envCoverRequest);
        codeCovService.triggerEnvCov(envCoverRequest);
        return Result.success();
    }

    /**
     * 获取覆盖率信息
     *
     * @param uuid
     * @return
     */
    @RequestMapping(value = "/getEnvCover", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result<ResultReponse> getEnvCoverInfo(@Param("uuid") String uuid) {
        log.info("【获取覆盖率信息入参：{}】", uuid);
        if (StringUtils.isEmpty(uuid)) {
            return Result.fail(ResultReponse.ResultReponseFailBuid("uuid不能为空"));
        } else {
            return Result.success(codeCovService.getResultEnvCover(uuid));
        }
    }

    /**
     * 手动获取env增量代码覆盖率，代码部署和覆盖率服务在同一机器上，可直接读取本机源码和本机class文件
     *
     * @param localHostRequest
     * @return
     */
    @RequestMapping(value = "/getEnvCoverResult", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result<ResultReponse> getEnvLocalCoverResult(@RequestBody @Valid LocalHostRequest localHostRequest) {
        log.info("【手工触发计算覆盖率...】");
        return Result.success(codeCovService.getLocalCoverResult(localHostRequest));
    }

    /**
     * 单元测试覆盖率收集
     *
     * @param untiCoverRequest
     * @return
     */
    @RequestMapping(value = "/tiggerUntiCov", method = RequestMethod.POST)
    @ResponseBody
    public Result tiggerUntiCoverger(@RequestBody @Valid UntiCoverRequest untiCoverRequest) {
        log.info("【触发单元测试覆盖率...】");
        return codeCovService.triggerUnitCov(untiCoverRequest);
    }

    /**
     * 查询单元测试覆盖率报告或状态
     */
    @RequestMapping(value = "/resultUntiCov", method = RequestMethod.POST)
    @ResponseBody
    public Result<ResultReponse> getUntiReponseResult(@Param("uuid") String uuid) {
        log.info("【查询单元测试覆盖率报告或状态入参：{}】", uuid);
        if (TextUtils.isEmpty(uuid)) {
            return Result.fail(ResultReponse.ResultReponseFailBuid("uuid不可为空，请检查"));
        }
        return Result.success(codeCovService.getResultEnvCover(uuid));
    }

    /**
     * 根据项目查询覆盖率数据
     */
    @RequestMapping(value = "/queryCoverageReport", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result queryCoverageReport(@RequestBody ProjectRequest projectRequest) {
        log.info("【查询覆盖率报告入参：{}】", projectRequest);
        // 端上给传的是空时写入默认值
        if (StringUtils.isBlank(projectRequest.getProjectName()) &&
                StringUtils.isBlank(projectRequest.getGitUrl()) &&
                StringUtils.isBlank(projectRequest.getNowVersion())) {
            projectRequest.setProjectName("studyProject");
        }
        return Result.success(codeCovService.coverageReportList(projectRequest));
    }

    /**
     * 根据覆盖率报告UUID查询
     */
    @RequestMapping(value = "/queryCoverageReportByUuid", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result queryCoverageReport(@RequestParam("uuid") String uuid) {
        log.info("【根据覆盖率报告UUID查询入参：{}】", uuid);
        // 端上给传的是空时写入默认值
        if (StringUtils.isBlank(uuid)){
            return Result.fail();
        }
        return Result.success(codeCovService.coverageReportEntity(uuid));
    }
}
