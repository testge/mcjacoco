package com.example.mc_jacoco.dao;

import com.example.mc_jacoco.entity.po.ProjectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luping
 * @date 2023/12/22 10:42
 */
@Mapper
public interface ProjectDao {

    Integer insertProject(@Param("projectEntity") ProjectEntity projectEntity);

    List<ProjectEntity> queryProject(@Param("projectEntity") ProjectEntity projectEntity);

    ProjectEntity queryByProject(@Param("projectEntity") ProjectEntity projectEntity);
}
