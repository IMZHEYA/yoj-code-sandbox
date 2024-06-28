package com.yupi.yojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.core.DockerClientBuilder;

public class DockerDemo {
    public static void main(String[] args) {
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        PingCmd pingCmd = dockerClient.pingCmd();
        pingCmd.exec();
    }
}
