package com.yupi.yojcodesandbox.model;

import lombok.Data;

@Data
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 程序执行消耗时间
     */
    private Long time;

    /**
     * 程序执行消耗内存
     */
    private Long memory;
}
