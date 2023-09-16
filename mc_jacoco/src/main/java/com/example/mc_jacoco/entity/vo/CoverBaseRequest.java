package com.example.mc_jacoco.entity.vo;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * @author luping
 * @date 2023/9/16 16:42
 */
@Data
public class CoverBaseRequest {

    /**
     * uuid是必须的，在后续查询结果时需要使用 属于唯一ID
     */
    @NotBlank(message = "uuid唯一标识不能为空")
    private String uuid;

    /**
     * git仓库地址
     */
    @NotBlank(message = "Git仓库地址不能为空")
    private String gitUrl;

    /**
     * 基准版本
     */
    @NotBlank(message = "基准版本不能为空")
    private String baseVersion;

    /**
     * 当前版本
     */
    @NotBlank(message = "当前版本不能为空")
    private String nowVersion;

    /**
     * 同一个仓库subMoudle为相对路径，出现空则代表整个仓库
     */
    private String subModule;

    /**
     * 收集覆盖率的类型
     */
    @NotBlank(message = "收集覆盖率的类型不能为空")
    @Max(value = 2)
    @Min(value = 1)
    private Integer type;
}
