package com.yupi.yojcodesandbox;

import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;

/**
 * java原生代码沙箱 （模版方法模式 新版）
 */
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {
    @Override
    public ExecutecodeResponse executeCode(ExecutecodeCodeRequest excodeCodeRequest) {
        return super.executeCode(excodeCodeRequest);
    }
}
