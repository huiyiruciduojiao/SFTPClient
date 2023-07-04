package top.lichuanjiu.sftpclient.Tools;

import com.jcraft.jsch.JSchException;

public interface SFTPOnLoad {
    /**
     * SFTP加载连接的回调函数

     */
    public void complete(int i);
    /**
     * SFTP 加载失败的回调行数
     */
    public void error(int i, JSchException e);
}
