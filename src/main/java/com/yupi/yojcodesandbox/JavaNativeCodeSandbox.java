package com.yupi.yojcodesandbox;

import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;

import java.util.List;

/**
 * java原生代码沙箱
 */
public class JavaNativeCodeSandbox implements CodeSandbox {
    @Override
    public ExecutecodeResponse executeCode(ExecutecodeCodeRequest excodeCodeRequest) {
        List<String> inputList = excodeCodeRequest.getInputList();
        String code = excodeCodeRequest.getCode();
        String language = excodeCodeRequest.getLanguage();

        return null;
    }
}
