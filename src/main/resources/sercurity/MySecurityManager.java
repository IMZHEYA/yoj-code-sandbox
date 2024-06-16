import java.security.Permission;

/**
 * 禁用所有权限安全管理器
 */
public class MySecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
//        super.checkPermission(perm);  放行
    }

    //限制读权限
    @Override
    public void checkRead(String file) {
        throw new SecurityException("checkRead 权限异常：" + file);
    }

    //限制写权限
    @Override
    public void checkWrite(String file) {
        throw new SecurityException("checkWrite 权限异常：" + file);
    }

    //限制执行文件权限
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec 权限异常：" + cmd);
    }

    //限制网络连接权限
    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("checkConnect 权限异常：" + host + ":" + port);
    }
}
