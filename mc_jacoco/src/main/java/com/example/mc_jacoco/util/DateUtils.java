package com.example.mc_jacoco.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author luping
 * @date 2023/12/28 00:49
 */
public class DateUtils {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期转年月日时分秒时间
     * @param date
     * @return 字符串年月日时分秒
     */
    public static String dateChangeStr(Date date){
        try {
            String simpleDateFormats = simpleDateFormat.format(date);
            return simpleDateFormats;
        }catch (Exception e){
            return null;
        }
    }
}
