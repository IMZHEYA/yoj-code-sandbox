package com.yupi.yojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * java原生代码沙箱 （模版方法模式 新版）
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {
    @Override
    public  ExecutecodeResponse executeCode(ExecutecodeCodeRequest excodeCodeRequest) {
        return super.executeCode(excodeCodeRequest);
    }
}
