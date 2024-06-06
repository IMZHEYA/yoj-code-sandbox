package com.yupi.yojcodesandbox.model;

import lombok.Data;

@Data
public class ExecuteMessage {

    private Integer exitValue;

    private String message;

    private String errorMessage;

}
