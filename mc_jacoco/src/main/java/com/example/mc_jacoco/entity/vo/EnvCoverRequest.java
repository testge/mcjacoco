package com.example.mc_jacoco.entity.vo;

import lombok.Data;

/**
 * @author luping
 * @date 2023/9/16 17:03
 */
@Data
public class EnvCoverRequest extends CoverBaseRequest {

    /**
     * 环境地址（Host）
     */
    private String address;

    /**
     * 环境地址端口
     */
    private String port;

}
