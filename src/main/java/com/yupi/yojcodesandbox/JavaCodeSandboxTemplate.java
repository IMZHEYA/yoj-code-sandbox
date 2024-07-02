package com.yupi.yojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.yojcodesandbox.model.ExecuteMessage;
import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;
import com.yupi.yojcodesandbox.model.JudgeInfo;
import com.yupi.yojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 模版方法抽象类
 */
@Slf4j
public abstract  class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    private static final String CLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static final String SECURITY_MANAGER_CLASS_NAME = "D:\\project\\yoj-code-sandbox\\src\\main\\resources\\sercurity\\MySecurityManager.class";

    /**
     * //1.把用户提交的代码保存到文件中
     * @param code  用户提交的代码
     * @return  userCodeFile 存放用户代码的文件
     */
    public File saveCodeToFile(String code){
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
        return userCodeFile;
    }

    /**
     *  //2.编译程序代码
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){

        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        try {
            Process process = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(process, "编译");
            System.out.println(executeMessage);
            return executeMessage;
        } catch (IOException e) {
            throw new RuntimeException("编译代码错误：" + e);
        }
    }

    /**
     * 3.执行程序
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList){
        //输出信息列表
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        for(String inputArgs:inputList){
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=MySecurityManager Main %s", userCodeParentPath, SECURITY_MANAGER_CLASS_NAME, inputArgs);
            try {
                //执行命令
                Process process = Runtime.getRuntime().exec(runCmd);
                new Thread(() -> {
                    try {
                        Thread.sleep(500l);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("超时了，中断");
                    process.destroy();
                }).start();
                //获取控制台输出
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(process, "执行");
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                throw new RuntimeException("执行程序错误: " + e);
            }
        }
        return executeMessageList;

    }

    /**
     * 4. 收集整理输出结果
     * @param executeMessageList
     * @return
     */
    public ExecutecodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList){
        ExecutecodeResponse executecodeResponse = new ExecutecodeResponse();
        List<String> outputList = new ArrayList<>();
        Long maxTime = 0L;
        for(ExecuteMessage executeMessage : executeMessageList){
            //只要有一个程序超时，就判断为超时
            Long time = executeMessage.getTime();
            if(time != null){
                maxTime = Math.max(time,maxTime);
            }
            //有的执行用例执行时出现错误，响应信息直接设为用户提交代码错误的信息，且响应状态设为错误,中断循环
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

    public boolean deleteFile(File userCodeFile){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        if(userCodeFile.getParentFile() != null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        //文件为空，直接返回真
        return true;
    }

    @Override
    public ExecutecodeResponse executeCode(ExecutecodeCodeRequest excodeCodeRequest) {
        List<String> inputList = excodeCodeRequest.getInputList();
        inputList = Arrays.asList("1 2","3 4");
        String code = excodeCodeRequest.getCode();
        String language = excodeCodeRequest.getLanguage();
        //1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);
        //2. 编译代码，得到 class 文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileFileExecuteMessage);
        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);
        //4.整理输出
        ExecutecodeResponse outputResponse = getOutputResponse(executeMessageList);
        //5.文件清理
        boolean b = deleteFile(userCodeFile);
        if(!b){
            log.error("deleteFile error, userCodeFilePath = {}",userCodeFile.getAbsolutePath());
        }
        return outputResponse;
    }

    /**
     * 返回异常信息对象
     * @param e
     * @return
     */
    private ExecutecodeResponse getErrorResponse(Throwable e){
        ExecutecodeResponse executecodeResponse = new ExecutecodeResponse();
        executecodeResponse.setOutputList(new ArrayList<>());
        executecodeResponse.setMessage(e.getMessage());
        //表示代码沙箱错误
        executecodeResponse.setStatus(2);
        executecodeResponse.setJudgeInfo(new JudgeInfo());

        return executecodeResponse;
    }

}
