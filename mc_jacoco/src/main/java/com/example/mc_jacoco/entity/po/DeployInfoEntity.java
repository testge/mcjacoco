package com.example.mc_jacoco.entity.po;

import lombok.Data;

import java.util.Date;

/**
 * @author luping
 * @date 2023/10/10 00:30
 */

@Data
public class DeployInfoEntity {

    private Integer id;

    /**
     * 唯一uuid
     */
    private String uuid;

    /**
     * 部署地址
     */
    private String address;

    /**
     * 部署端口
     */
    private Integer port;

    /**
     * 部署分支代码路径
     */
    private String codePath;

    /**
     * 项目多module
     */
    private String childModules;

    private Date createTime;

    private Date updateTime;
}
