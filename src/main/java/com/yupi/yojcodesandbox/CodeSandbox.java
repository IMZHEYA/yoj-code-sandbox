package com.yupi.yojcodesandbox;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;
import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;

public interface CodeSandbox {
    /**
     * 执行代码
     * @param excodeCodeRequest
     * @return
     */
    ExecutecodeResponse executeCode(ExecutecodeCodeRequest excodeCodeRequest) throws InterruptedException;
}
