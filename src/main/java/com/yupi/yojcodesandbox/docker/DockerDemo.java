package com.yupi.yojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.util.List;

public class DockerDemo {
    public static void main(String[] args) throws InterruptedException {
        // 1 下载镜像
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String image = "nginx:latest";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback(){
            @Override
            public void onNext(PullResponseItem item) {
                System.out.printf("下载镜像:" + item.getStatus());
                super.onNext(item);
            }
        };
        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
        //2 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        //withCmd 当创建的这个容器启动时，里面的命令就会执行
        CreateContainerResponse createContainerResponse = containerCmd.withCmd("echo", "Hello Docker")
                .exec();
        String containId = createContainerResponse.getId();
        System.out.println(createContainerResponse);
        //3 查看容器状态
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.withShowAll(true).exec();
        for(Container container : containerList){
            System.out.println(container);
        }
        //4 启动容器
        dockerClient.startContainerCmd(containId).exec();
        //5 查看日志
        LogContainerResultCallback logContainerResultCallback = new LogContainerResultCallback(){
            @Override
            public void onNext(Frame item) {
                System.out.println(item.getStreamType());  //返回日志帧的流类型，表示日志消息的来源
                System.out.println("日志：" + new String(item.getPayload()));//返回日志帧的实际日志内容，返回类型为字节数组
                super.onNext(item);
            }
        };
        //阻塞等待日志输出
        dockerClient.logContainerCmd(containId)
                .withStdErr(true)   //包含标准错误输出
                .withStdOut(true)   //包含标准输出
                .exec(logContainerResultCallback)
                .awaitCompletion();

        //6 强制删除容器
//        dockerClient.removeContainerCmd(containId).withForce(true).exec();
    }

}
