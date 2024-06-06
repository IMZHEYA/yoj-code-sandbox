package com.yupi.yojcodesandbox.utils;

import com.yupi.yojcodesandbox.model.ExecuteMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 进程工具类，执行进程并获取输出，并且使用 StringBuilder 拼接控制台输出信息
 */
public class ProcessUtils {

    public static ExecuteMessage runProcessAndGetMessage(Process runProcess,String opName) {

        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            //正常退出，我现在想获取控制台输出
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                //分批读取输入流（控制台输出）
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                //逐行读取，控制台输出信息
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutputStringBuilder.toString());
                System.out.println(compileOutputStringBuilder);
            } else {
                System.out.println(opName + "失败" + exitValue);
                //分批读取正常输出流：有的程序员会在正常输出里写一些错误日志之类的
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder compileOutputStringBuilder = new StringBuilder();
                //逐行读取，控制台输出信息
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutputStringBuilder.append(compileOutputLine);
                }
                System.out.println(compileOutputStringBuilder);
                //分批读取错误输出：
                BufferedReader errorbufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
                //逐行读取，控制台输出信息
                String errorcompileOutputLine;
                while ((errorcompileOutputLine = errorbufferedReader.readLine()) != null) {
                    errorCompileOutputStringBuilder.append(errorcompileOutputLine);
                }
                executeMessage.setErrorMessage(errorCompileOutputStringBuilder.toString());
                System.out.println(errorCompileOutputStringBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return executeMessage;
    }

}
