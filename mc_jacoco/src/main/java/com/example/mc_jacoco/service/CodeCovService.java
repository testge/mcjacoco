package com.example.mc_jacoco.service;

import com.example.mc_jacoco.entity.vo.EnvCoverRequest;

/**
 * @author luping
 * @date 2023/9/18 22:02
 */
public interface CodeCovService {

    /**
     * 采集覆盖率
     * @param envCoverRequest
     */
    void triggerEnvCov(EnvCoverRequest envCoverRequest);
}
