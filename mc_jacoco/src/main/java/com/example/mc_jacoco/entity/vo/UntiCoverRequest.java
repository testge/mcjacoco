package com.example.mc_jacoco.entity.vo;

import lombok.Data;

/**
 * @author luping
 * @date 2023/11/18 15:55
 */
@Data
public class UntiCoverRequest extends CoverBaseRequest{

    /**
     * profile，只有单元测试需要，在命令行会加-Pstable、-Ptest等
     */
    private String envType;
}
