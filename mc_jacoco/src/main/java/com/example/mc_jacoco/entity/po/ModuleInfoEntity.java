package com.example.mc_jacoco.entity.po;

import lombok.Data;
import org.dom4j.Element;

/**
 * @author luping
 * @date 2023/12/6 23:34
 */
@Data
public class ModuleInfoEntity {

    /**
     * 标记，groupId，artifactId，version，都存在flag才是true
     */
    private boolean flag;

    /**
     * 父版本
     */
    private String parentVersion;

    /**
     * 父组ID
     */
    private String parentGroupId;

    private String parentArtifactId;

    private ModuleInfoEntity parent;

    /**
     * 子制品ID
     */

    private String artifactId;

    /**
     * 子版本
     */
    private String version;

    /**
     * 子组ID
     */
    private String groupId;

    /**
     * 记录属性,父pom的properties可能存储了version信息
     */
    private Element properties;

    /**
     * 包类型,如果是pom类型需要递归查找，只要找jar类型的
     */
    private String packaging;

}
