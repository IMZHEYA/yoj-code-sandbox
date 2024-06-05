package com.yupi.yojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutecodeCodeRequest {
    //输入用例
    private List<String> inputList;

    //提交代码
    private String code;

    //提交语言
    private String language;
}
