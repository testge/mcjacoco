package com.example.mc_jacoco.handler;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author luping
 * @date 2023/9/26 22:06
 */
@Slf4j
@Component
public class GitHandler {

    @Value(value = "git.username")
    private String gitUserName;

    @Value(value = "git.password")
    private String gitPwd;

    public static Git gitInit(String dir){
        Git git = null;
        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(Paths.get(dir, ".git").toFile())
                    .build();
            git = new Git(repository);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return git;
    }

    public Git cloneRepository(String gitUrl, String codePath, String commitId) throws GitAPIException {
        log.info("【获取Git开始执行代码下载,入参：{},{},{}】",gitUrl,codePath,commitId);
        Git git = gitInit(codePath)
                .cloneRepository()
                .setURI(gitUrl)
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUserName, gitPwd))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("294402584@qq.com", "456.caocao"))
                .setGitDir(new File(codePath))
                .setBranch(commitId)
                .call();
        // 切换到当前版本分支
//        checkoutBranch(git, commitId);
        log.info("【获取Git开始执行代码下载成功...】");
        return git;
    }

    private Ref checkoutBranch(Git git, String branch) throws GitAPIException {
        log.info("【切换分支执行中...】");
        Ref ref = git.checkout().setName(branch).call();
        log.info("【获取Git开始执行分支切换成功...】");
        return ref;
    }


    private static SshSessionFactory gitSSh(){
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory(){
            // 配置 默认即可
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }
            // 加载ssh-keygen生成的秘钥路径，该操作要与git服务器上的公钥进行匹配
            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch sch = super.createDefaultJSch(fs);
                // 输入秘钥路径
                sch.addIdentity("/Users/luping/.ssh/id_rsa");
                return sch;
            }
        };
        return sshSessionFactory;
    }

    public static void main(String[] args) throws GitAPIException {
        GitHandler git = new GitHandler();
        git.cloneRepository("git@github.com:lupingp/jacoco_study_project_test.git","/Users/luping/apps/mcs_jacoco/clonecode/11111111111110018/newTest01","feature/newTest01");
//        git.cloneRepository("git@github.com:lupingp/jacoco_study_project_test.git","/Users/luping/app/mcs_jacoco/clonecode/11111111111110017/newTest01","38be239020eab0aa2b4d17445511602e91d40060");
    }
}
