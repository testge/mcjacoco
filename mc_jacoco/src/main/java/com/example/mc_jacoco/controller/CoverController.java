package com.example.mc_jacoco.controller;

import com.example.mc_jacoco.entity.vo.EnvCoverRequest;
import com.example.mc_jacoco.entity.vo.ResultReponse;
import com.example.mc_jacoco.service.CodeCovService;
import com.example.mc_jacoco.util.Result;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
        log.info("【获取覆盖率信息入参：{}】",uuid);
        if (StringUtils.isEmpty(uuid)) {
            return Result.fail(ResultReponse.ResultReponseFailBuid("uuid不能为空"));
        } else {
            return Result.success(codeCovService.getResultEnvCover(uuid));
        }
    }
}
