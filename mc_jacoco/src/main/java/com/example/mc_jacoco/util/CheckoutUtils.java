package com.example.mc_jacoco.util;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.enums.ReportTypeEnum;

/**
 * @author luping
 * @date 2023/12/4 23:32
 */
public class CheckoutUtils {

    /**
     * 统一
     * @param coverageReportEntity
     * @return
     */
    public static boolean commitIdIsValid(CoverageReportEntity coverageReportEntity) {
        if (ReportTypeEnum.DIFF.equals(coverageReportEntity.getType()) && !coverageReportEntity.getBaseVersion().equals(coverageReportEntity.getNowVersion())) {
            return true;
        }else {
            return false;
        }
    }
}
