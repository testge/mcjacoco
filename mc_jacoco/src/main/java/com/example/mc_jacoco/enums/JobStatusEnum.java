package com.example.mc_jacoco.enums;

/**
 * @author luping
 * @date 2023/9/18 23:03
 */
public enum JobStatusEnum {
    NODIFF(100, "无增量"),
    INITIAL(0, "数据初始"),
    WAITING(1, "待执行"),
    CLONING(2, "下载代码中"),
    COMPILING(3, "编译中"),
    DIFF_METHODS_EXECUTING(4, "计算增量方法中"),
    ADDMODULING(5, "添加集成模块中"),
    UNITTESTEXECUTING(6, "单元测试执行中"),

    REPORTPARSING(8, "分析报告中"),

    REPORTCOPYING(9, "复制报告中"),
    CLONE_DONE(102, "下载代码成功"),
    COMPILE_DONE(103, "编译成功"),
    DIFF_METHOD_DONE(104, "计算增量方法成功"),
    ADDMODULE_DONE(105, "添加集成模块成功"),
    UNITTEST_DONE(106, "执行单元测试成功"),
    PARSEREPORT_DONE(108, "分析报告成功"),
    COPYREPORT_DONE(109, "复制报告成功"),
    CLONE_FAIL(202, "下载代码失败"),
    COMPILE_FAIL(203, "编译失败"),
    DIFF_METHOD_FAIL(204, "计算增量方法失败"),
    FAILADDMODULE(205, "添加集成自模块失败"),
    UNITTEST_FAIL(206, "执行单元测试失败"),
    FAILPARSEREPOAT(208, "分析报告失败"),
    COPYREPORT_FAIL(209, "复制报告失败"),
    SUCCESS(200, "执行成功"),
    ENVREPORT_FAIL(212, "统计功能测试增量覆盖率失败"),
    COVHTML_FAIL(213, "覆盖率报告合并失败"),
    COVERGER_RESULT_SUCCESS_MSG (1,"覆盖率信息查询成功"),
    COVERGER_RESULT_FAIL_MSG (0,"覆盖率信息查询成功"),
    ;


    private Integer code;

    private String codeMsg;

    public Integer getCode() {
        return code;
    }

    public String getCodeMsg() {
        return codeMsg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setCodeMsg(String codeMsg) {
        this.codeMsg = codeMsg;
    }

    JobStatusEnum(Integer code, String codeMsg) {
        this.code = code;
        this.codeMsg = codeMsg;
    }
}
