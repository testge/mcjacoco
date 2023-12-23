package com.example.mc_jacoco.service;

import com.example.mc_jacoco.entity.po.ProjectEntity;
import com.example.mc_jacoco.entity.vo.ProjectRequest;
import com.example.mc_jacoco.util.Result;

/**
 * @author luping
 * @date 2023/12/22 01:16
 */
public interface ProjectService {

    Result insertProject(ProjectRequest projectRequest);

    Result queryProjectData(ProjectRequest projectRequest);
}
