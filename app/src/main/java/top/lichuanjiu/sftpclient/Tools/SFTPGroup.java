package top.lichuanjiu.sftpclient.Tools;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.jcraft.jsch.JSchException;

import top.lichuanjiu.sftpclient.MainActivity;
import top.lichuanjiu.sftpclient.View.ViewShowFile;

public class SFTPGroup implements SFTPOnLoad {

    public static SFTPUtil sftpUtilShow;
    public static SFTPUtil sftpUtilUpload;
    public static SFTPUtil sftpUtilDownload;

    private boolean sftpUtilShowStatus = false;
    private boolean sftpUtilUploadStatus = false;
    private boolean sftpUtilDownloadStatus = false;

    public SFTPGroup(SFTPConfigModel sftpConfigModel) {
        if (sftpConfigModel == null) {
            return;
        }
        sftpUtilShow = new SFTPUtil(sftpConfigModel);
        sftpUtilDownload = new SFTPUtil(sftpConfigModel);
        sftpUtilUpload = new SFTPUtil(sftpConfigModel);
    }

    public void login() {

        if (sftpUtilShow != null) {
            sftpUtilShow.logout();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sftpUtilShow.login(SFTPGroup.this, 1);
                }
            });
            thread.start();
        }
        if (sftpUtilDownload != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sftpUtilDownload.login(SFTPGroup.this, 2);
                }
            });
            thread.start();
        }
        if (sftpUtilUpload != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sftpUtilUpload.login(SFTPGroup.this, 3);
                }
            });
            thread.start();
        }
    }
    /**
     * sftp连接服务器成功的回调
     *
     * @param i
     */
    @Override
    public void complete(int i) {
        switch (i) {
            case 1:
                sftpUtilShowStatus = true;
                sftpUtilShow.cd(ViewShowFile.thisViewShowFile.nowPath);
                //初始化数据
                ViewShowFile.thisViewShowFile.createFolderData(sftpUtilShow, ViewShowFile.thisViewShowFile.nowPath);
                System.out.println(ViewShowFile.thisViewShowFile.nowPath);
                break;
            case 2:
                sftpUtilDownloadStatus = true;
                break;
            case 3:
                sftpUtilUploadStatus = true;
                break;
        }
    }

    @Override
    public void error(int i, JSchException e) {
        String errorMessage = e.getMessage();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (errorMessage.contains("connection timeout")) {
                    Toast.makeText(MainActivity.thisMainActivity, "连接超时，请检查网络连接或目标主机是否可达。",Toast.LENGTH_SHORT).show();
                } else if (errorMessage.contains("Auth fail")) {
                    Toast.makeText(MainActivity.thisMainActivity,"身份验证失败，请检查用户名和密码。",Toast.LENGTH_SHORT).show();
                } else if (errorMessage.contains("refused")) {
                    Toast.makeText(MainActivity.thisMainActivity,"连接被拒绝，请检查SSH服务是否正常运行。",Toast.LENGTH_SHORT).show();
                } else if (errorMessage.contains("UnknownHostException")) {
                    Toast.makeText(MainActivity.thisMainActivity,"未知主机，请检查主机名或网络连接。",Toast.LENGTH_SHORT).show();
                } else if (errorMessage.contains("connection reset")) {
                    Toast.makeText(MainActivity.thisMainActivity,"连接重置，请检查目标主机的SSH服务是否正常。",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.thisMainActivity,"发生了一个SSH连接错误：" + errorMessage,Toast.LENGTH_SHORT).show();
                }
            }
        });
        System.out.println("连接失败");
        System.out.println(e.getMessage());
        switch (i) {
            case 1:
                sftpUtilShowStatus = false;
                break;
            case 2:
                sftpUtilDownloadStatus = false;
                break;
            case 3:
                sftpUtilUploadStatus = false;
                break;
        }
    }
}
