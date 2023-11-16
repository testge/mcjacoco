package com.example.mc_jacoco.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author luping
 * @date 2023/11/15 23:10
 */
@Data
public class LocalHostRequest {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 唯一标识Id
     */
    @NotNull(message = "uuid不能为空")
    private String uuid;

    /**
     * 机器地址
     */
    @NotNull(message = "机器IP不能为空")
    private String address;

    /**
     * 机器端口
     */
    @NotNull(message = "机器IP的端口信息不能为空")
    private String port;

    /**
     * 子模块目录
     */
    private String subModule;

//    @NotEmpty(message = "classFilePath不能为空")
    private String classFilePath;

    /**
     * base代码存储路径
     */
    @NotEmpty(message = "基础代码路径不能为空")
    private String baseLocalPath;
    /**
     * 目标代码存储路径
     */
    @NotEmpty(message = "目标代码路径不能为空")
    private String nowLocalPath;

    @NotBlank(message = "baseVersion不能为空")
    private String baseVersion;

    @NotBlank(message = "nowVersion不能为空")
    private String nowVersion;

    private String baseLocalPathProject;

    private String nowLocalPathProject;

}
