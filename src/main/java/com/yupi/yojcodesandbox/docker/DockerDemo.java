package com.yupi.yojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;

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

    }

}