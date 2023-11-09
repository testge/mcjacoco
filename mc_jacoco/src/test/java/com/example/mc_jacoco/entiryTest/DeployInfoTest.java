package com.example.mc_jacoco.entiryTest;

import com.example.mc_jacoco.dao.DiffDeployInfoDao;
import com.example.mc_jacoco.entity.po.DeployInfoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author luping
 * @date 2023/11/8 23:20
 */
@SpringBootTest
public class DeployInfoTest {

    @Resource
    private DiffDeployInfoDao diffDeployInfoDao;

    @Test
    void queryDeployInfo() {
        String uuid = "101000000210011";
        DeployInfoEntity deployInfo = diffDeployInfoDao.queryInfoById(uuid);
        System.out.println(deployInfo);
    }
}
