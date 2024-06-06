package com.yupi.yojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * java原生代码沙箱
 */
public class JavaNativeCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    private static final String CLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static void main(String[] args) {
        ExecutecodeCodeRequest executecodeCodeRequest = new ExecutecodeCodeRequest();
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        executecodeCodeRequest.setInputList(Arrays.asList("1 2","3 4"));
        //ResourceUtil可以读取resources目录下的文件
        String code = ResourceUtil.readStr("Main.java", StandardCharsets.UTF_8);
        executecodeCodeRequest.setCode(code);
        executecodeCodeRequest.setLanguage("java");
        ExecutecodeResponse executecodeResponse = javaNativeCodeSandbox.executeCode(executecodeCodeRequest);
        System.out.println(executecodeResponse);

    }

    @Override
    public ExecutecodeResponse executeCode(ExecutecodeCodeRequest excodeCodeRequest) {
        List<String> inputList = excodeCodeRequest.getInputList();
        String code = excodeCodeRequest.getCode();
        String language = excodeCodeRequest.getLanguage();
        //1.把用户提交的代码保存到文件中
        //获取当前用户工作目录 D:\project\yoj-code-sandbox
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        //没有全局代码目录就新建
        if(!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }

        //把和用户的代码隔离存放
        //用户代码父目录
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + CLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        //2.编译程序代码
        String compileCmd = String.format("javac -encoding utf-8 %s",userCodeFile.getAbsoluteFile());
        try {
            Process process = Runtime.getRuntime().exec(compileCmd);
            int exitValue  = process.waitFor();
            //正常退出，我现在想获取控制台输出
            if(exitValue == 0){
                System.out.println("编译成功");
                //分批读取输入流（控制台输出）
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                //逐行读取，控制台输出信息
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null){
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                System.out.println(compileOutputStringBuilder);
            }else{
                System.out.println("编译失败" + exitValue);
                //分批读取正常输出流：有的程序员会在正常输出里写一些错误日志之类的
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                //逐行读取，控制台输出信息
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null){
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                System.out.println(compileOutputStringBuilder);
                //分批读取错误输出：
                BufferedReader errorbufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
                //逐行读取，控制台输出信息
                String errorcompileOutputLine;
                while ((errorcompileOutputLine = errorbufferedReader.readLine()) != null){
                    errorCompileOutputStringBuilder.append(errorcompileOutputLine);
                }
                System.out.println(errorCompileOutputStringBuilder);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
