package com.example.mc_jacoco.entity.po;

import lombok.Data;

/**
 * @author luping
 * @date 2023/10/10 00:30
 */

@Data
public class DeployInfoEntity {

    private String uuid;

    private String address;

    private int port;

    private String codePath;

    private String childModules;
}
