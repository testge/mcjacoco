package com.example.mc_jacoco.entity.po;

import lombok.Data;

import java.util.Date;

/**
 * @author luping
 * @date 2023/12/2 17:21
 */
@Data
public class BaseEntity {

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
