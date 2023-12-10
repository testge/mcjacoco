package com.example.mc_jacoco.executor;

import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.entity.po.ModuleInfoEntity;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.util.BufferReaderUtils;
import com.example.mc_jacoco.util.MavenModuleUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author luping
 * @date 2023/12/5 23:38
 */
@Slf4j
@Component
public class MavenModuleAddExecutor {

    /**
     * 添加Maven集成模块信息
     */
    public void addMavenModule(CoverageReportEntity coverageReport) {
        log.info("【添加Maven集成模块入参:{}】",coverageReport);
        try {
            // 父目录的pom路径
            String pomxml = coverageReport.getNowLocalPathProject() + "/pom.xml";
            File file = new File(pomxml);
            if (!file.exists()) {
                coverageReport.setRequestStatus(JobStatusEnum.FAILADDMODULE.getCode());
                coverageReport.setErrMsg(JobStatusEnum.FAILADDMODULE.getCodeMsg());
                return;
            }
            // 设置lombok设置
            File lombokFile = new File(coverageReport.getNowLocalPathProject() + "/lombok.config");
            FileWriter lombokfileWriter = new FileWriter(lombokFile);
            if (lombokFile.exists()) {
                lombokfileWriter.write("lombok.addLombokGeneratedAnnotation = true");
                lombokfileWriter.flush();
                lombokfileWriter.close();
            }
            // 获取所有子模块的Pom路径
            ArrayList<String> childPomPathList = getChildPomPath(pomxml);
            if (childPomPathList.size() <= 1) {
                coverageReport.setRequestStatus(JobStatusEnum.ADDMODULE_DONE.getCode());
                coverageReport.setReportUrl(coverageReport.getNowLocalPathProject() + "/target/site/jacoco/index.html");
            }
            StringBuffer buffer = new StringBuffer();
            ModuleInfoEntity moduleInfo = getModuleInfo(pomxml);
            StringBuffer str = getDependencyBuild(pomxml, moduleInfo, buffer);
            if (StringUtils.isBlank(str)) {
                coverageReport.setRequestStatus(JobStatusEnum.ADDMODULE_DONE.getCode());
                coverageReport.setReportUrl(coverageReport.getNowLocalPathProject() + "/target/site/jacoco/index.html");
            }
            String pomStr = BufferReaderUtils.reader(pomxml).toString();
            // 在父pom中增加jacocomodule模块
            pomStr = pomStr.replace("<modules>", "<modules>\n<module>jacocomodule</module>");
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(pomStr);
            fileWriter.flush();
            fileWriter.close();
            // 构建jacocomodule模块
            StringBuffer stringBuffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                    "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
            stringBuffer.append("<modelVersion>4.0.0</modelVersion>\n");
            stringBuffer.append("<parent>\n");
            stringBuffer.append("<artifactId>" + moduleInfo.getArtifactId() + "</artifactId>\n");
            stringBuffer.append("<groupId>" + moduleInfo.getGroupId() + "</groupId>\n");
            stringBuffer.append("<version>" + moduleInfo.getVersion() + "</version>\n");
            stringBuffer.append("</parent>\n");
            stringBuffer.append("<groupId>" + moduleInfo.getGroupId() + "</groupId>\n");
            stringBuffer.append("<artifactId>jacocomodule</artifactId>\n");
            stringBuffer.append("<version>" + moduleInfo.getVersion() + "</version>\n");
            stringBuffer.append("<dependencies>\n");
            stringBuffer.append(str.toString());
            stringBuffer.append("\n</dependencies>\n" +
                    "<build>\n" +
                    "        <plugins>\n" +
                    "            <plugin>\n" +
                    "                <groupId>org.jacoco</groupId>\n" +
                    "                <artifactId>jacoco-maven-plugin</artifactId>\n" +
                    "                <version>1.0.2-SNAPSHOT</version>\n" +
                    "                <executions>\n" +
                    "                    <execution>\n" +
                    "                        <id>report-aggregate</id>\n" +
                    "                        <phase>compile</phase>\n" +
                    "                        <goals>\n" +
                    "                            <goal>report-aggregate</goal>\n" +
                    "                        </goals>\n" +
                    "                    </execution>\n" +
                    "                </executions>\n" +
                    "            </plugin>\n" +
                    "        </plugins>\n" +
                    "    </build>\n" +
                    "</project>");
            File coverageModule = new File(coverageReport.getNowLocalPathProject() + "/jacocomodule");
            File coveragePomFile = new File(coverageReport.getNowLocalPathProject() + "/jacocomodule/pom.xml");
            coverageModule.mkdirs();
            coveragePomFile.createNewFile();
            FileWriter fileWriter1 = new FileWriter(coveragePomFile);
            fileWriter1.write(stringBuffer.toString());
            fileWriter1.flush();
            fileWriter1.close();
            coverageReport.setReportFile(coverageReport.getNowLocalPathProject()+"/jacocomodule/target/site/jacoco-aggregate/index.html");
            coverageReport.setRequestStatus(JobStatusEnum.ADDMODULE_DONE.getCode());
            return;
        } catch (IOException e) {
            log.error("【集成模块添加异常...异常原因：{}】", e.getMessage());
            coverageReport.setRequestStatus(JobStatusEnum.ADDMODULE_DONE.getCode());
            coverageReport.setErrMsg("集成模块添加异常...原因是:" + e.getMessage());
            coverageReport.setReportUrl(coverageReport.getNowLocalPathProject() + "/target/site/jacoco/index.html");
        }
    }

    public static ArrayList<String> getChildPomPath(String pom) {
        File file = new File(pom).getParentFile();
        ArrayList<String> list = new ArrayList<>();
        if (!file.exists()) {
            return list;
        }
        File[] listFiles = file.listFiles();
        for (File files : listFiles) {
            if (files.isDirectory()) {
                File[] childFiles = files.listFiles();
                for (File filess : childFiles) {
                    if (filess.getName().contains("pom.xml")) {
                        list.add(filess.getAbsolutePath());
                    }
                }
            }
        }
        log.info("【获取子模块pom文件信息:{}条...内容是:{}】", list.size(), list);
        return list;
    }

    public static ModuleInfoEntity getModuleInfo(String pomPath) {
        log.info("【获取Pom模块信息：{}】", pomPath);
        ModuleInfoEntity moduleInfo = new ModuleInfoEntity();
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(pomPath);
            Element root = document.getRootElement();
            Element parent = root.element("parent");
            if (parent != null) {
                if (parent.element("version") != null) {
                    moduleInfo.setParentVersion(parent.elementText("version"));
                    moduleInfo.setVersion(moduleInfo.getParentVersion());
                }
                if (parent.element("groupId") != null) {
                    moduleInfo.setParentGroupId(parent.elementText("groupId"));
                    moduleInfo.setGroupId(moduleInfo.getParentGroupId());
                }
                if (parent.element("artifactId") != null) {
                    moduleInfo.setParentArtifactId(parent.elementText("artifactId"));
                    moduleInfo.setArtifactId(moduleInfo.getParentArtifactId());
                }
            }
            if (root.element("properties") != null) {
                moduleInfo.setProperties(root.element("properties"));
            }
            if (root.element("packaging") != null) {
                moduleInfo.setPackaging(root.elementText("packaging"));
            }
            Iterator<Element> iterator = root.elementIterator();
            while (iterator.hasNext()) {
                Element ele = iterator.next();
                if (ele.getName().equals("version")) {
                    moduleInfo.setVersion(ele.getText());
                } else if (ele.getName().equals("groupId")) {
                    moduleInfo.setGroupId(ele.getText());
                } else if (ele.getName().equals("artifactId")) {
                    moduleInfo.setArtifactId(ele.getText());
                }
            }
            if (!StringUtils.isBlank(moduleInfo.getVersion()) &&
                    !StringUtils.isBlank(moduleInfo.getArtifactId()) &&
                    !StringUtils.isBlank(moduleInfo.getGroupId())) {
                moduleInfo.setFlag(true);
            }
        } catch (Exception e) {
            log.error("【获取Pom模块信息失败...失败原因：{}】", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        log.info("【获取Pom模块信息内容：{}】", moduleInfo);
        return moduleInfo;
    }

    /**
     * 获取构建依赖
     *
     * @param pom
     * @param moduleInfo
     * @param stringBuf
     * @return
     */
    public static StringBuffer getDependencyBuild(String pom, ModuleInfoEntity moduleInfo, StringBuffer stringBuf) {
        log.info("【获取构建依赖入参:{}-{}-{}】",pom,moduleInfo,stringBuf);
        // 父POM的Packaging是有值且Packaging类型 = pom，所以父Pom不会进入这个判断
        // 在下面的else会找子Pom并组成ModuleInfo实体，同时关联父实体会进入下面的判断
        if (moduleInfo.getPackaging() == null || moduleInfo.getPackaging().equals("jar")) {
            if (moduleInfo.getArtifactId() != null) {
                String groupId = getModuleGroupId(moduleInfo);
                String version = getModuleVersion(moduleInfo);
                StringBuffer sb = new StringBuffer("<dependency>\n");
                sb.append("<artifactId>" + moduleInfo.getArtifactId() + "</artifactId>\n");
                if (!StringUtils.isBlank(groupId)) {
                    sb.append("<groupId>" + groupId + "</groupId>\n");
                }
                if (!StringUtils.isBlank(version)) {
                    sb.append("<version>" + version + "</version>\n");
                }
                sb.append("</dependency>\n");
                stringBuf.append(sb.toString());
            }
        } else {
            ArrayList<String> modulesList = MavenModuleUtil.getValidModules(pom);
            for (int i = 0; i < modulesList.size(); i++) {
                // 子pom路径
                String childPom = new File(pom).getParent() + "/" + modulesList.get(i) + "/pom.xml";
                ModuleInfoEntity moduleInfoChild = getModuleInfo(childPom);
                if (moduleInfoChild.isFlag()) {
                    // 将父实体插入子实体中
                    moduleInfoChild.setParent(moduleInfo);
                    moduleInfoChild.setFlag(true);
                    // 递归根据子pom找依赖
                    stringBuf = getDependencyBuild(childPom, moduleInfoChild, stringBuf);
                }
            }
        }
        return stringBuf;
    }

    /**
     * 获取模块组ID
     *
     * @param moduleInfo
     * @return
     */
    public static String getModuleGroupId(ModuleInfoEntity moduleInfo) {
        String groupId = moduleInfo.getGroupId();
        if (groupId == null) {
            if (moduleInfo.getParent() != null) {
                // 子pom文件会执行这里
                groupId = getModuleGroupId(moduleInfo.getParent());
            }
        }
        log.info("【获取模块组ID:{}】",groupId);
        return groupId;
    }

    /**
     * 获取模块版本
     *
     * @return
     */
    public static String getModuleVersion(ModuleInfoEntity moduleInfo) {
        String version = moduleInfo.getVersion();
        // 子POM文件中引入的父POM版本，是写死的则不会进入下面的校验
        if (version != null && version.contains("$")) {
            ModuleInfoEntity moduleInfo1 = moduleInfo;
            String versionName = version.replace("$", "").replace("{", "").replace("}", "");
            while (moduleInfo1.getParent() != null) {
                Element properties = moduleInfo1.getParent().getProperties();
                if (properties != null && properties.element(version) != null) {
                    version = properties.elementText(versionName);
                    return version;
                } else {
                    moduleInfo1 = moduleInfo1.getParent();
                }
            }
        } else if (version == null) {
            // 子POM文件中引入的父POM版本，是写死的会进入下面的校验，直接用父POm的版本即可
            version = moduleInfo.getParentVersion();
        }
        log.info("【获取版本:{}】",version);
        return version;
    }

    public static void main(String[] args) {
        CoverageReportEntity coverageReportEntity = new CoverageReportEntity();
        coverageReportEntity.setNowLocalPathProject("/Users/luping/app/mcs_jacoco/clonecode/110555555551001/feature_newtest02/studyproject");
        MavenModuleAddExecutor mavenModuleAddExecutor = new MavenModuleAddExecutor();
        mavenModuleAddExecutor.addMavenModule(coverageReportEntity);

        //        System.out.println(MavenModuleAddExecutor.getChildPomPath(pom));
    }
}
