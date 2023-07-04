package top.lichuanjiu.sftpclient.Tools;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

import top.lichuanjiu.sftpclient.View.ViewShowFile;

public class SFTPUtil {

    private final Logger log = LoggerFactory.getLogger(SFTPUtil.class);

    private ChannelSftp sftp;

    private Session session;

    /**
     * FTP 登录用户名
     */
    private String username;
    /**
     * FTP 登录密码
     */
    private String password;
    /**
     * 私钥
     */
    private String privateKey;
    /**
     * FTP 服务器地址IP地址
     */
    private String host;
    /**
     * FTP 端口
     */
    private int port;

    /**
     * 建议使用配置构参方式，可拓展性比较强
     *
     */
    public SFTPUtil(SFTPConfigModel sftpConfigModel) {
        this.username = sftpConfigModel.getUserName();
        this.password = sftpConfigModel.getPassWord();
        this.privateKey = sftpConfigModel.getPrivateKey();
        this.host = sftpConfigModel.getHost();
        this.port = sftpConfigModel.getPort();
    }

    /**
     * 连接sftp服务器
     *
     */
    public void login(SFTPGroup sftpGroup, int i) {
        ViewShowFile.thisViewShowFile.showLoad("加载中");
        try {
            JSch jsch = new JSch();
            if (privateKey != null) {
                jsch.addIdentity(privateKey);// 设置私钥
                log.info("sftp connect,path of private key file：{}", privateKey);
            }
            log.info("sftp connect by host:{} username:{}", host, username);

            session = jsch.getSession(username, host, port);
            log.info("Session is build");
            if (password != null) {
                session.setPassword(password);
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);

            session.connect();
            log.info("Session is connected");
            Channel channel = session.openChannel("sftp");
            channel.connect();
            log.info("channel is connected");

            sftp = (ChannelSftp) channel;
            System.out.println(sftp);
            log.info(String.format("sftp server host:[%s] port:[%s] is connect successfull", host, port));
            //连接sftp成功执行回调函数
            sftpGroup.complete(i);
        } catch (JSchException e) {
            //执行连接sftp失败的回调函数
            sftpGroup.error(i, e);
        } finally {
            ViewShowFile.thisViewShowFile.stopLoad();
        }
    }

    /**
     * 关闭连接 server
     */
    public void logout() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
                log.info("sftp is closed already");
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
                log.info("sshSession is closed already");
            }
        }
    }

    /**
     * 将输入流的数据上传到sftp作为文件
     *
     * @param directory    上传到该目录
     * @param sftpFileName sftp端文件名
     * @param input        输入流
     */
    public void upload(String directory, String sftpFileName, InputStream input, long size, int taskIndex) throws SftpException {
        try {
            sftp.cd(directory);
        } catch (SftpException e) {
            log.warn("directory is not exist");
            sftp.mkdir(directory);
            sftp.cd(directory);
        }
        UpLoadTaskProgressMonitor progressMonitor = new UpLoadTaskProgressMonitor(size, taskIndex);
        sftp.put(input, sftpFileName, progressMonitor);
        log.info("file:{} is upload successful", sftpFileName);
    }

    /**
     * 上传单个文件
     *
     * @param directory  上传到sftp目录
     * @param uploadFile 要上传的文件,包括路径
     */
    public void upload(String directory, String uploadFile, int taskIndex) throws FileNotFoundException, SftpException {
        File file = new File(uploadFile);
        upload(directory, file.getName(), new FileInputStream(file), file.length(), taskIndex);
    }

    public InputStream downloadStream(String directory, String downloadFile) throws SftpException, IOException {
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        return sftp.get(downloadFile);
    }

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    public void delete(String directory, String deleteFile) throws SftpException {
        if (isExist(directory, deleteFile)) {
            sftp.cd(directory);
            sftp.rm(deleteFile);
            log.info("file:{} is delete successful", deleteFile);
        } else {
            log.info("file:{} is delete failure,because of file is not exist", deleteFile);
        }
    }

    public void rmdir(String deleteDir) throws SftpException {
        Vector<ChannelSftp.LsEntry> fileList = listFiles(deleteDir);
        for (ChannelSftp.LsEntry entry : fileList) {
            String filename = entry.getFilename();
            if (!filename.equals(".") && !filename.equals("..")) {
                String filePath = deleteDir + "/" + filename;
                if (entry.getAttrs().isDir()) {
                    rmdir( filePath); // 递归删除子文件夹
                } else {
                    sftp.rm(filePath); // 删除文件
                }
            }
        }
        sftp.rmdir(deleteDir); // 删除空文件夹
    }

    /**
     * 列出目录下的文件
     *
     * @param directory 要列出的目录

     */
    public Vector<ChannelSftp.LsEntry> listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

    /**
     * 判断是否存在该文件
     *
     * @param directory 路径
     * @param fileName  文件名称
     */
    public boolean isExist(String directory, String fileName) {
        return isExist(directory + "/" + fileName);
    }


    /**
     * 判断是否存在该文件
     * 文件不存在的话会抛 SftpException，e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE
     *
     * @param path 文件(绝对路径)
     */
    public boolean isExist(String path) {
        boolean flag = false;
        try {
            sftp.stat(path);
            flag = true;
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                log.info("path:{} is not exist", path);
            }
        }
        return flag;
    }

    /**
     * 修改文件名
     *
     * @param path    路径
     * @param oldName 原始名称
     * @param newName 修改名称
     */
    public boolean rename(String path, String oldName, String newName) throws SftpException {
        boolean flag = false;
        try {
            System.out.println(path + "/" + oldName + "------->" + path + "/" + newName);
            sftp.rename(path + "/" + oldName, path + "/" + newName);
            flag = true;
            log.info("old file name:[{}] rename to new file name:[{}] successful", oldName, newName);
        } catch (SftpException e) {
            log.warn("old file name:[{}] is not exist or new file name:[{}] is exist", oldName, newName);
        }
        return flag;
    }

    /**
     * 获取sftp的状态
     *
     * @return true 连接成功
     * false 连接失败
     */
    public boolean SftpStatus() {
        return this.sftp != null;
    }

    /**
     * 获取当前路径
     *
     * @return 当前路径
     */
    public String pwd() {
        if (this.sftp == null) {
            return "./";
        }
        try {
            return this.sftp.pwd();
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return "./";
    }

    public void cd(String path) {
        if (this.sftp == null) {
            return;
        }
        try {
            this.sftp.cd(path);
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
}

