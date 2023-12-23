package com.example.mc_jacoco.controller;

import com.example.mc_jacoco.entity.po.ProjectEntity;
import com.example.mc_jacoco.entity.vo.ProjectRequest;
import com.example.mc_jacoco.service.ProjectService;
import com.example.mc_jacoco.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author luping
 * @date 2023/12/22 01:05
 */
@Slf4j
@RestController
@RequestMapping("/mc/project/v1")
public class ProjectController {

    @Resource
    private ProjectService projectService;

    /**
     * 项目新增接口
     *
     * @param projectEntity
     * @return
     */
    @RequestMapping(value = "/projectSave", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result projectSave(@RequestBody @Validated ProjectRequest projectRequest) {
        return projectService.insertProject(projectRequest);
    }

    /**
     * 项目列表查询接口
     * @return
     */
    @RequestMapping(value = "/projectList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result projectList(@RequestBody ProjectRequest projectRequest) {
        log.info("【项目列表查询接口入参：{}】",projectRequest);
        return projectService.queryProjectData(projectRequest);
    }
}
