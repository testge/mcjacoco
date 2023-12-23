package com.example.mc_jacoco.service.serviceImpl;

import com.example.mc_jacoco.dao.ProjectDao;
import com.example.mc_jacoco.entity.po.ProjectEntity;
import com.example.mc_jacoco.entity.vo.ProjectRequest;
import com.example.mc_jacoco.service.ProjectService;
import com.example.mc_jacoco.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author luping
 * @date 2023/12/22 10:41
 */
@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {


    @Resource
    private ProjectDao projectDao;

    @Override
    public Result insertProject(ProjectRequest projectRequest) {
        log.info("【新增项目入参：{}】",projectRequest);
        ProjectEntity projectEntity = new ProjectEntity();
        BeanUtils.copyProperties(projectRequest,projectEntity);
        Integer count = projectDao.insertProject(projectEntity);
        if (count > 0){
            log.info("【项目新增成功...】");
            return Result.success();
        }else {
            return Result.fail("项目添加失败");
        }
    }

    /**
     * 查询项目数据
     * @param projectRequest
     * @return
     */
    @Override
    public Result queryProjectData(ProjectRequest projectRequest) {
        ProjectEntity projectEntity = new ProjectEntity();
        BeanUtils.copyProperties(projectRequest,projectEntity);
        log.info("【查询项目数据参数：{}】",projectEntity);
        List<ProjectEntity> list = projectDao.queryProject(projectEntity);
        log.info("【查询项目数据成功...{}条】",list.size());
        return Result.success(list);
    }
}
