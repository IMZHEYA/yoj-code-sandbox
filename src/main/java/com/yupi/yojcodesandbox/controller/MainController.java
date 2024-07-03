package com.yupi.yojcodesandbox.controller;

import com.yupi.yojcodesandbox.JavaNativeCodeSandbox;
import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/")
public class MainController {
    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    /**
     * 执行代码接口
     * @param executecodeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    public ExecutecodeResponse executeCode(@RequestBody ExecutecodeCodeRequest executecodeCodeRequest) {
        if(executecodeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }
        return javaNativeCodeSandbox.executeCode(executecodeCodeRequest);
    }
}