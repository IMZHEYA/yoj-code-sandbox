package com.yupi.yojcodesandbox.controller;

import com.yupi.yojcodesandbox.JavaDockerCodeSandbox;
import com.yupi.yojcodesandbox.JavaNativeCodeSandbox;
import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {
    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;
    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;
    //鉴权请求头
    public static final String AUTH_REQUEST_HEADER = "auth";
    //密钥
    public static final String AUTH_REQUEST_SECRET = "secretKey";
    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    /**
     * 执行代码接口
     * @param executecodeCodeRequest
     * @return
     */
    //http://localhost:8090/health
    //http://localhost:8090/executeCode
    @PostMapping("/executeCode")
    public ExecutecodeResponse executeCode(@RequestBody ExecutecodeCodeRequest executecodeCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        //基本的API认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if(!authHeader.equals(AUTH_REQUEST_SECRET)){
            response.setStatus(403);
            return null;
        }
        if(executecodeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }
        return javaNativeCodeSandbox.executeCode(executecodeCodeRequest);
    }
}