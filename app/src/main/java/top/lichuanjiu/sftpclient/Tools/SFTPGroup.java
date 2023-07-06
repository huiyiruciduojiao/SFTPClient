package top.lichuanjiu.sftpclient.Tools;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.jcraft.jsch.JSchException;

import top.lichuanjiu.sftpclient.MainActivity;
import top.lichuanjiu.sftpclient.R;
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

        System.out.println("连接失败");
        System.out.println(e.getMessage());
        switch (i) {
            case 1:
                sftpUtilShowStatus = false;
                String errorMessage = e.getMessage();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        assert errorMessage != null;
                        if (errorMessage.contains("connection timeout")) {
                            Toast.makeText(MainActivity.thisMainActivity, R.string.sftp_connection_timed_out,Toast.LENGTH_SHORT).show();
                        } else if (errorMessage.contains("Auth fail")) {
                            Toast.makeText(MainActivity.thisMainActivity, R.string.sftp_identity_verification_fail,Toast.LENGTH_SHORT).show();
                        } else if (errorMessage.contains("refused")) {
                            Toast.makeText(MainActivity.thisMainActivity, R.string.sftp_connection_by_refuse,Toast.LENGTH_SHORT).show();
                        } else if (errorMessage.contains("UnknownHostException")) {
                            Toast.makeText(MainActivity.thisMainActivity, R.string.sftp_unknown_host,Toast.LENGTH_SHORT).show();
                        } else if (errorMessage.contains("connection reset")) {
                            Toast.makeText(MainActivity.thisMainActivity, R.string.sftp_connection_reset,Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.thisMainActivity,MainActivity.thisMainActivity.getApplication().getString(R.string.sftp_connection_error_tips) + errorMessage,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
