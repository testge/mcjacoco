package com.example.mc_jacoco.util;

import com.example.mc_jacoco.constants.AddressConstants;
import com.example.mc_jacoco.entity.po.CoverageReportEntity;
import com.example.mc_jacoco.enums.JobStatusEnum;
import com.example.mc_jacoco.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author luping
 * @date 2023/10/10 23:36
 */

@Slf4j
public class JDiffFiles {

    public static HashMap<String, String> diffMethodsListNew(CoverageReportEntity coverageReport) {
        HashMap<String, String> stringHashMap = new HashMap<>();
        try {
            File newCode = new File(coverageReport.getNowLocalPath());
            File oldCode = new File(coverageReport.getBaseLocalPath());
            Git newGit;
            Git oldGit;
            Repository newRepository;
            Repository oldRepository;
            newGit = Git.open(newCode);
            newRepository = newGit.getRepository();
            oldGit = Git.open(oldCode);
            oldRepository = oldGit.getRepository();
            ObjectId nowId = newRepository.resolve(coverageReport.getNowVersion());
            ObjectId baseId = oldRepository.resolve(coverageReport.getBaseVersion());
            AbstractTreeIterator nowAbstract = prepareTreeParser(newRepository, nowId);
            AbstractTreeIterator oldbstract = prepareTreeParser(oldRepository, baseId);
            List<DiffEntry> diffEntries = newGit.diff().setOldTree(oldbstract).setNewTree(nowAbstract).setShowNameAndStatusOnly(true).call();
            for (DiffEntry diff : diffEntries) {
                if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
                    continue;
                }
                if (diff.getNewPath().indexOf(coverageReport.getSubModule()) < 0) {
                    continue;
                }
                if (diff.getNewPath().indexOf("src/test/java") == 0) {
                    continue;
                }
                if (diff.getNewPath().endsWith(".java")) {
                    String nowClassPath = diff.getNewPath();
                    if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
                        log.info("【存在新增源文件：{}】", nowClassPath);
                        stringHashMap.put(nowClassPath.replace(".java", ""), "true");
                    } else if (diff.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                        log.info("【存在修改源文件：{}】", nowClassPath);
                        MethodParserUtil methodParserUtil = new MethodParserUtil();
                        // 获取原基础java文件的方法名、方法入参子节点
                        HashMap<String, String> baseMMap = methodParserUtil.parseMethodsMd5(oldGit.getRepository().getDirectory().getParent() + "/" + nowClassPath);
                        // 获取当前分支java文件的方法名、方法入参子节点
                        HashMap<String, String> newMMap = methodParserUtil.parseMethodsMd5(newGit.getRepository().getDirectory().getParent() + "/" + nowClassPath);
                        HashMap<String, String> methodsMap = diffMethods(baseMMap, newMMap);
                        log.info("【基准分支与对比分支的差异化方法是：{}】", methodsMap);
                        if (!methodsMap.isEmpty()) {
                            StringBuffer buffer = new StringBuffer();
                            for (String st : methodsMap.values()) {
                                buffer.append(st).append("#");
                            }
                            // 将原java文件与差异的方法进行关联
                            stringHashMap.put(nowClassPath.replace(".java", ""), buffer.toString());
                        }
                    }
                }
            }
            // 出现空表示原分支与新分支没有差异点
            if (stringHashMap.isEmpty()) {
                coverageReport.setLineCoverage(Double.parseDouble("100"));
                coverageReport.setBranchCoverage(Double.parseDouble("100"));
                coverageReport.setRequestStatus(ResultEnum.SUCCESS.getCode());
                coverageReport.setReportUrl(AddressConstants.NO_CODE_COVERGET_REPORT);
                // 删除目录的文件
                FileUtils.cleanDirectory(new File(coverageReport.getNowLocalPath()));
                FileUtils.cleanDirectory(new File(coverageReport.getBaseLocalPath()));
                coverageReport.setErrMsg("没有增量代码...");
            } else {
                coverageReport.setRequestStatus(JobStatusEnum.DIFF_METHOD_DONE.getCode());
            }
        } catch (IOException e) {
            log.error("【读取文件出现失败...失败原因：{}】", e.getMessage());
            coverageReport.setRequestStatus(JobStatusEnum.DIFF_METHOD_FAIL.getCode());
        } catch (GitAPIException e) {
            log.error("【Git操作失败...失败原因是：{}】", e.getMessage());
            coverageReport.setRequestStatus(JobStatusEnum.DIFF_METHOD_FAIL.getCode());
        }
        return stringHashMap;
    }

    public static HashMap<String, String> diffMethodsListForEnv(String basePath, String nowPath, String baseVersion, String nowVersion) {
        log.info("【计算增量方法入参：{}--{}--{}--{}】", basePath, nowPath, baseVersion, nowVersion);
        HashMap<String, String> hashMap = new HashMap<>();
        try {
            File baseFile = new File(basePath);
            File nowFile = new File(nowPath);
            Git baseGit;
            Git nowGit;
            Repository baseRepository;
            Repository nowRepository;
            baseGit = Git.open(baseFile);
            nowGit = Git.open(nowFile);
            // getRepository返回的是git存储库路径：Repository[/Users/luping/app/mcs_jacoco/clonecode/101000000210022/main/.git]
            baseRepository = baseGit.getRepository();
            nowRepository = nowGit.getRepository();
            // resolve是可以根据分支名解析出对象id，入参可以是分支名或者指定ID，默认是获取该存储库中的分支的最新的提交Log，
            // 并将Log通过SHA-1的加密算法返回
            ObjectId baseObjectId = baseRepository.resolve(baseVersion);
            ObjectId nowObjectId = nowRepository.resolve(nowVersion);
            AbstractTreeIterator baseTree = prepareTreeParser(baseRepository, baseObjectId);
            AbstractTreeIterator nowTree = prepareTreeParser(nowRepository, nowObjectId);
            List<DiffEntry> diffEntryList = nowGit.diff().setOldTree(baseTree).setNewTree(nowTree).setShowNameAndStatusOnly(true).call();
            for (DiffEntry diffEntry : diffEntryList) {
                // 文件是删除类型，直接忽略
                if (diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                    continue;
                }
                if (diffEntry.getNewPath().contains("src/test/java")) {
                    continue;
                }
                if (diffEntry.getNewPath().endsWith(".java")) {
                    String nowLocalPath = diffEntry.getNewPath();
                    log.info("【Java文件路径：{}】", nowLocalPath);
                    if (diffEntry.getChangeType() == DiffEntry.ChangeType.ADD) {
                        hashMap.put(nowLocalPath.replace(".java", ""), "true");
                    }
                    if (diffEntry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                        MethodParserUtil methodParserUtil = new MethodParserUtil();
                        HashMap<String, String> baseHashMap = methodParserUtil.parseMethodsMd5(basePath + nowLocalPath);
                        HashMap<String, String> nowHashMap = methodParserUtil.parseMethodsMd5(basePath + nowLocalPath);
                        HashMap<String, String> diffMethod = diffMethods(baseHashMap, nowHashMap);
                        if (diffMethod.isEmpty()) {
                            continue;
                        } else {
                            StringBuffer buffer = new StringBuffer();
                            for (String st : diffMethod.values()) {
                                buffer.append(st).append("#");
                            }
                            hashMap.put(nowLocalPath.replace(".java", ""), buffer.toString());
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            log.error("【读取文件出现失败...失败原因：{}】", ioe.getMessage());
        } catch (GitAPIException e) {
            log.error("【Git操作失败...失败原因是：{}】", e.getMessage());
        }
        return hashMap;
    }


    public static AbstractTreeIterator prepareTreeParser(Repository repository, AnyObjectId objectId) throws IOException {
        try {
            // RevWalk可以获取版本库中历史的提交记录
            RevWalk walk = new RevWalk(repository);
            RevTree tree;
            // parseTree是解析给定的版本记录对象ID
            tree = walk.parseTree(objectId);
            CanonicalTreeParser TreeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                TreeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return TreeParser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算原分支与基础分支的差异方法
     *
     * @param baseMap
     * @param nowMap
     * @return
     */
    private static HashMap<String, String> diffMethods(HashMap<String, String> baseMap, HashMap<String, String> nowMap) {
        HashMap<String, String> resMap = new HashMap<>();
        for (String key : nowMap.keySet()) {
            if (!baseMap.containsKey(key)) {
                resMap.put(key, nowMap.get(key));
            }
        }
        return resMap;
    }
}
