package com.example.mc_jacoco.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.extern.slf4j.Slf4j;

import java.beans.MethodDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author luping
 * @date 2023/10/11 21:54
 */
@Slf4j
public class MethodParserUtil {

    protected HashMap<String, String> methodMd5Map;

    /**
     * 解析方法
     *
     * @param fileName 接收代码源文件路径
     * @return
     */
    public  HashMap<String, String> parseMethodsMd5(String fileName) {
        log.info("【解析Java文件入参：{}】",fileName);
        methodMd5Map = new HashMap<>();
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            CompilationUnit cu = JavaParser.parse(inputStream);
            List<Comment> compilation = cu.getAllContainedComments();
            // 获取代码中的行注释
            List<Comment> unwantedComments = compilation
                    .stream()
                    .filter(p -> !p.getCommentedNode().isPresent() || p instanceof LineComment)
                    .collect(Collectors.toList());
            // 删除代码中的行注释
            unwantedComments.forEach(Node::remove);
            cu.accept(new MethodMd5Visitor(), null);
            return methodMd5Map;
        } catch (FileNotFoundException e) {
            log.error("【解析方法出错...原因是：{}】", e.getMessage());
            return new HashMap<>();
        }
    }

    private class MethodMd5Visitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {

            // 获取方法参数
            NodeList<Parameter> parameters = n.getParameters();
            // 获取全部的方法名+参数？？？
            StringBuffer buffer = new StringBuffer(n.getNameAsString());
            for (Parameter parameter : parameters) {
                if (parameter.getType().getChildNodes().size() > 0) {
                    buffer.append(",").append(parameter.getType().getChildNodes().get(0).toString());
                }
            }
            String md5 = getMd5Value(n.toString());
            methodMd5Map.put(md5, buffer.toString());
            super.visit(n, arg);
        }
    }

    public static String getMd5Value(String dataStr) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(dataStr.getBytes(StandardCharsets.UTF_8));
            byte[] s = md.digest();
            int num;
            for (int i = 0; i < s.length; i++) {
                num = s[i];
                if (num < 0) {
                    num += 256;
                } else if (num < 16) {
                    stringBuffer.append(0);
                }
                stringBuffer.append(Integer.toHexString(num));
            }
        } catch (Exception e) {
            log.error("【MD5算法生成失败...原因是:{}】", e.getMessage());
        }
        return stringBuffer.toString();
    }
}
