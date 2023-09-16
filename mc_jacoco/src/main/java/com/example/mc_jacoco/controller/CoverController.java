package com.example.mc_jacoco.controller;

import com.example.mc_jacoco.entity.vo.EnvCoverRequest;
import com.example.mc_jacoco.util.Result;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author luping
 * @date 2023/9/16 17:06
 */
@Log4j2
@RestController
@RequestMapping("/mc/cover/v1")
public class CoverController {


    /**
     * 收集环境覆盖率
     *
     * @param envCoverRequest 前端调用入参
     * @return
     */
    @RequestMapping(value = "/tiggerEnvCov", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result tiggerEnvCov(@RequestBody @Validated EnvCoverRequest envCoverRequest) {
        log.info("【触发收集覆盖率】【入参】：【{}】", envCoverRequest);
        return Result.success();
    }
}
