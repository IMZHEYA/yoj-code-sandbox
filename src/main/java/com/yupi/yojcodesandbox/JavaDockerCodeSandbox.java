package com.yupi.yojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.yupi.yojcodesandbox.model.ExecuteMessage;
import com.yupi.yojcodesandbox.model.ExecutecodeCodeRequest;
import com.yupi.yojcodesandbox.model.ExecutecodeResponse;
import com.yupi.yojcodesandbox.model.JudgeInfo;
import com.yupi.yojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Docker实现代码沙箱
 */
public class JavaDockerCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    private static final String CLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static final String SECURITY_MANAGER_CLASS_NAME = "D:\\project\\yoj-code-sandbox\\src\\main\\resources\\sercurity\\MySecurityManager.class";

    public static final Boolean FIRST_INIT = true;


    public static void main(String[] args) throws InterruptedException {
        ExecutecodeCodeRequest executecodeCodeRequest = new ExecutecodeCodeRequest();
        JavaDockerCodeSandbox javaNativeCodeSandbox = new JavaDockerCodeSandbox();
        executecodeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        //ResourceUtil可以读取resources目录下的文件
        String code = ResourceUtil.readStr("Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/SleepError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/MemoryError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/ReadFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/WriteFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/RunFileError.java", StandardCharsets.UTF_8);
        executecodeCodeRequest.setCode(code);
        executecodeCodeRequest.setLanguage("java");
        ExecutecodeResponse executecodeResponse = javaNativeCodeSandbox.executeCode(executecodeCodeRequest);
        System.out.println(executecodeResponse);

    }

    @Override
    public ExecutecodeResponse executeCode(ExecutecodeCodeRequest excodeCodeRequest){
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
            return getErrorResponse(e);
        }
        //3.创建容器，把编译后的文件上传到容器中
        // 1 下载镜像 (第一次执行拉取镜像)
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String image = "openjdk:8-alpine";
        if(FIRST_INIT){
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback(){
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.printf("下载镜像:" + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }
        //2 创建容器，直接创建一个有编译文件的容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        //withCmd 当创建的这个容器启动时，里面的命令就会执行
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L); // 限制容器的内存
        hostConfig.withCpuCount(1L); //限制容器只能使用cpu的核数
        hostConfig.setBinds(new Bind(userCodeParentPath,new Volume("/app"))); //创建容器时可以指定容器映射，作用是把本地的文件同步到容器中，可以让容器访问
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)   //获取容器输出
                .withAttachStdin(true)   //获取容器输出
                .withAttachStdout(true)  //获取容器输出
                .withTty(true) //创建交互式终端
                .exec();
        String containId = createContainerResponse.getId();
        System.out.println(createContainerResponse);
        //3 启动容器
        dockerClient.startContainerCmd(containId).exec();

        ExecutecodeResponse executecodeResponse = new ExecutecodeResponse();
        return executecodeResponse;
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
