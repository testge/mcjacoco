package com.example.mc_jacoco.entity.vo;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author luping
 * @date 2023/12/23 21:37
 */
@Data
public class ProjectRequest{

    private Integer projectId;
    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    /**
     * 仓库地址
     */
    @NotBlank(message = "Git仓库地址不能为空")
    private String gitUrl;

    /**
     * 基准分支
     */
    @NotBlank(message = "基准版本不能为空")
    private String baseVersion;

    /**
     * 新分支 又叫 对比分支
     */
    @NotBlank(message = "当前版本不能为空")
    private String nowVersion;

    /**
     * 采集类型
     */
    @NotNull(message = "收集覆盖率的类型不能为空")
    @Max(value = 2)
    @Min(value = 1)
    private Integer type;

    /**
     * 环境地址（Host）
     */
    @NotNull(message = "环境地址不能为空")
    private String address;

    /**
     * 环境地址端口
     */
    @NotNull(message = "环境端口不能为空")
    private String port;

    @Override
    public String toString() {
        return "ProjectEntity{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", gitUrl='" + gitUrl + '\'' +
                ", baseVersion='" + baseVersion + '\'' +
                ", nowVersion='" + nowVersion + '\'' +
                ", type=" + type +
                ", address='" + address + '\'' +
                ", port='" + port + '\'' +
                "} " + super.toString();
    }
}
