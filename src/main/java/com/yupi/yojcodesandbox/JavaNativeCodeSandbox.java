package com.yupi.yojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.yojcodesandbox.model.ExecuteMessage;
import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;
import com.yupi.yojcodesandbox.model.JudgeInfo;
import com.yupi.yojcodesandbox.utils.ProcessUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        executecodeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
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
        inputList = Arrays.asList("1 2","3 4");
        String code = excodeCodeRequest.getCode();
        String language = excodeCodeRequest.getLanguage();
        //1.把用户提交的代码保存到文件中
        //获取当前用户工作目录 D:\project\yoj-code-sandbox
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        //没有全局代码目录就新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        //把和用户的代码隔离存放
        //用户代码父目录
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + CLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        //2.编译程序代码
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        try {
            Process process = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(process, "编译");
            System.out.println(executeMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //3.执行程序
        //输出信息列表
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for(String inputArgs:inputList){
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                //执行命令
                Process process = Runtime.getRuntime().exec(runCmd);
                //获取控制台输出
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(process, "执行");
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //4.整理输出
        ExecutecodeResponse executecodeResponse = new ExecutecodeResponse();
        List<String> outputList = new ArrayList<>();
        Long maxTime = 0L;
        for(ExecuteMessage executeMessage : executeMessageList){
            //只要有一个程序超时，就判断为超时
            Long time = executeMessage.getTime();
            if(time != null){
                maxTime = Math.max(time,maxTime);
            }
            //有的执行用例执行时出现错误，响应信息直接设为错误信息，且响应状态设为错误,中断循环
            if(StrUtil.isNotBlank(executeMessage.getErrorMessage())){
                executecodeResponse.setMessage(executeMessage.getErrorMessage());
                executecodeResponse.setStatus(3);
                break;
            }
            //将输出用例添加到列表
            outputList.add(executeMessage.getMessage());
        }
        executecodeResponse.setOutputList(outputList);
        //每条都正常输出了，正常运行完成,状态设置为1
        if(outputList.size() == executeMessageList.size()){
            executecodeResponse.setStatus(1);
        }
        JudgeInfo judgeInfo = new JudgeInfo();
//        judgeInfo.setMessage();   judgeInfo 的信息在判题过程中设置
        judgeInfo.setTime(maxTime);
//        judgeInfo.setMemory();
        executecodeResponse.setJudgeInfo(judgeInfo);

        return executecodeResponse;
    }
}
