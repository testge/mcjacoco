package com.example.mc_jacoco.util;

import com.example.mc_jacoco.template.CovIndexHtmlTemPlate;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author luping
 * @date 2023/11/3 00:17
 */
@Slf4j
public class MergeReportHtml {

    /**
     * 合并Html报告
     *
     * @param listFiles module集合
     * @param filePath  根路径的报告地址
     * @return
     */
    public static Integer[] mergerHtml(ArrayList<String> listFiles, String filePath) {
        Integer[] result = new Integer[3];
        result[0] = 0;
        result[1] = -1;
        result[2] = -1;
        try {
            Document docSchema = Jsoup.parse(CovIndexHtmlTemPlate.HTML_TEMPLATE);
            Integer[] array = new Integer[15];
            array[0] = 0;
            array[1] = 0;
            array[2] = 0;
            array[3] = 0;
            array[4] = 0;
            array[5] = 0;
            array[6] = 0;
            array[7] = 0;
            array[8] = 0;
            array[9] = 0;
            array[10] = 0;
            array[11] = 0;
            array[12] = 0;
            array[13] = 0;
            array[14] = 0;
            Element element = docSchema.getElementsByTag("table").first();
            for (String fileName : listFiles) {
                File file = new File(fileName);
                String moduleName = new File(file.getParent()).getName();
                Document docc = Jsoup.parse(new File(fileName), "UTF-8", "");
                Document doc = Jsoup.parse(docc.toString().replace("<a href=\"", "<a href=\"" + moduleName + "/"));
                // 覆盖率报告文件中tbody标签为空，表示没有覆盖率数据
                if (doc.getElementsByTag("tbody") == null) {
                    continue;
                }
                // 获取每个报告的tr标签，tr标签包含：字节码信息、分支信息、行信息
                Elements trs = doc.getElementsByTag("tbody").first().getElementsByTag("tr");
                for (Element ele : trs) {
                    element.getElementsByTag("tbody").first().append(ele.html());
                }
                // 获取每个模块的Total覆盖率总数据加入到array中
                // [Total, 373, of, 480, 22%, 66, of, 66, 0%, 55, 68, 9, 33, 22, 35, 0, 6]
                String[] cov = doc.getElementsByTag("tfoot").first().child(0).text().split(" ");
                // 字节码未覆盖的指令
                array[1] = array[1] + Integer.parseInt(cov[1].replace(",", ""));
                // 字节码全部的指令
                array[2] = array[2] + Integer.parseInt(cov[3].replace(",", ""));
                //array[3] = array[3] + Integer.parseInt(a[4].replace("%", ""));
                // 未覆盖的分支数
                array[4] = array[4] + Integer.parseInt(cov[5].replace(",", ""));
                // 总分支数
                array[5] = array[5] + Integer.parseInt(cov[7].replace(",", ""));
                //array[6] = array[6] + Integer.parseInt(a[8].replace("%", ""));
                // 未覆盖的代码行
                array[7] = array[7] + Integer.parseInt(cov[9].replaceAll(",", ""));
                // 圈复杂度
                array[8] = array[8] + Integer.parseInt(cov[10].replace(",", ""));
                // 未覆盖的代码行
                array[9] = array[9] + Integer.parseInt(cov[11].replace(",", ""));
                // 代码行总数
                array[10] = array[10] + Integer.parseInt(cov[12].replace(",", ""));
                // 未覆盖的方法数
                array[11] = array[11] + Integer.parseInt(cov[13].replace(",", ""));
                // 方法总数
                array[12] = array[12] + Integer.parseInt(cov[14].replace(",", ""));
                // 未覆盖的类数
                array[13] = array[13] + Integer.parseInt(cov[15].replace(",", ""));
                // 总类数
                array[14] = array[14] + Integer.parseInt(cov[16].replace(",", ""));
            }
            // 字节码全部的指令等于0，则将未覆盖的字节码指令+全部字节码指令设置为1
            if (array[2] == 0) {
                array[1] = 1;
                array[2] = 2;
            }
            // 总分支数等于0，将未覆盖的分支数+全部分支数设置为1
            if (array[5] == 0) {
                array[4] = 1;
                array[5] = 1;
            }
            // 代码总行数等于0，将未覆盖的代码行数+全部代码行数设置为1
            if (array[10] == 0) {
                array[9] = 1;
                array[10] = 1;
            }
            String tfootTemplate = covHtmlTfootTemplate(array);
            element.getElementsByTag("tfoot").first().append(tfootTemplate);
            FileWriter writer = new FileWriter(filePath);
            writer.write(element.toString());
            writer.flush();
            // 表示覆盖率生成成功
            result[0] = 1;
            // 分支覆盖率
            result[1] = (array[5]-array[4])*100/array[5];
            // 行覆盖率
            result[2] = (array[10]-array[9])*100/array[10];
        } catch (IOException e) {
            log.error("【覆盖率报告和合并失败...】");
            log.error("【失败原因是：{}】",e.getMessage());
        }
        return result;
    }

    private static String covHtmlTfootTemplate(Integer[] array) {
        String tfoot = "         <tr>\n" +
                "                <td>Total</td>\n" +
                "                <td class=\"bar\">" + array[1] + " of " + array[2] + "</td>\n" +
                "                <td class=\"ctr2\">" + (array[2] - array[1]) * 100 / array[2] + "%</td>\n" +
                "                <td class=\"bar\">" + array[4] + " of " + array[5] + "</td>\n" +
                "                <td class=\"ctr2\">" + (array[5] - array[4]) * 100 / array[5] + "%</td>\n" +
                "                <td class=\"ctr1\">" + array[7] + "</td>\n" +
                "                <td class=\"ctr2\">" + array[8] + "</td>\n" +
                "                <td class=\"ctr1\">" + array[9] + "</td>\n" +
                "                <td class=\"ctr2\">" + array[10] + "</td>\n" +
                "                <td class=\"ctr1\">" + array[11] + "</td>\n" +
                "                <td class=\"ctr2\">" + array[12] + "</td>\n" +
                "                <td class=\"ctr1\">" + array[13] + "</td>\n" +
                "                <td class=\"ctr2\">" + array[14] + "</td>\n" +
                "            </tr>\n";
        return tfoot;
    }
}
