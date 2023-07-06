package top.lichuanjiu.sftpclient.Tools;

import android.net.Uri;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;

import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import top.lichuanjiu.sftpclient.MainActivity;
import top.lichuanjiu.sftpclient.R;
import top.lichuanjiu.sftpclient.SftpClient;
import top.lichuanjiu.sftpclient.View.ViewShowFile;
import top.lichuanjiu.sftpclient.View.ViewTaskList;

public class TaskExecutor {
    /**
     * 执行任务
     */
    public void implement(TaskGroup taskGroup) {
        //将任务添加到任务视图
        ViewTaskList.thisViewTaskList.createTaskList(taskGroup);
        //将任务添加到任务列队，开始执行
        SftpClient.taskQueue.add(new TaskExecutorThread(taskGroup));
    }

    public void implementDeleteTask(TaskGroup taskGroup) {
        if (taskGroup == null || taskGroup.size() == 0) {
            return;
        }
        Thread thread = new Thread(() -> {
            //显示加载图层
            ViewShowFile.thisViewShowFile.showLoad(MainActivity.thisMainActivity.getApplication().getString(R.string.load_view_deleting_text));
            for (TaskConfiguration task : taskGroup.taskConfigurations) {
                try {
                    //判断是否是文件夹
                    if (task.taskSize == 1) {
                        SFTPGroup.sftpUtilShow.rmdir(task.rPwd + "/" + task.taskContent);
                    } else {
                        SFTPGroup.sftpUtilShow.delete(task.rPwd, task.taskContent);
                    }
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }
            //终止加载图层
            ViewShowFile.thisViewShowFile.stopLoad();
            //刷新页面
            ViewShowFile.thisViewShowFile.reLoad();
        });
        thread.start();
    }

    public void implementReNameTask(TaskConfiguration task) {
        if (task == null) {
            return;
        }
        Thread thread = new Thread(() -> {
            //显示加载图层
            ViewShowFile.thisViewShowFile.showLoad(MainActivity.thisMainActivity.getApplication().getString(R.string.load_view_renaming_text));
            try {
                SFTPGroup.sftpUtilShow.rename(task.rPwd, task.taskName, task.taskContent);
            } catch (SftpException e) {
                e.printStackTrace();
            } finally {
                ViewShowFile.thisViewShowFile.stopLoad();
                ViewShowFile.thisViewShowFile.reLoad();
            }
        });
        thread.start();
    }
}

/***
 * 任务执行线程
 */
class TaskExecutorThread implements Runnable {
    private TaskGroup taskGroup;

    public TaskExecutorThread(TaskGroup taskGroup) {

        this.taskGroup = taskGroup;
    }

    @Override
    public void run() {
        //1.解析任务
        for (TaskConfiguration task : taskGroup.taskConfigurations) {
            int taskIndex = ViewTaskList.thisViewTaskList.getTaskIndex(task);
            if (task.taskType == TaskConfiguration.TASK_TYPE_DOWNLOAD) {
                //文件数据输入流
                InputStream inputStream = null;
                //获取文件输出流
                OutputStream outputStream = getOutputStream(task);
                try {
                    if (outputStream != null) {
                        //这里需要创建一个独立的sftp对象，因为一个sftp对象不能同时进行两个存在，不创建在文件下载的过程中会影响正常文件浏览
                        inputStream = SFTPGroup.sftpUtilDownload.downloadStream(task.rPwd, task.taskContent);
                        // 读取文件数据并实现进度条
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytesRead = 0;
                        long fileSize = task.taskSize;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);

                            totalBytesRead += bytesRead;
                            // 计算进度百分比
                            int progress = (int) (totalBytesRead * 1000 / fileSize);
                            // 更新进度条
                            ViewTaskList.thisViewTaskList.upProgress(taskIndex,
                                    progress);
                        }
                        ViewTaskList.thisViewTaskList.changeTaskStatus(taskIndex,
                                TaskConfiguration.TASK_STATUS_COMPLETE);
                        ViewTaskList.thisViewTaskList.upProgress(taskIndex,
                                1000);
                    } else {
                        ViewTaskList.thisViewTaskList.changeTaskStatus(taskIndex,
                                TaskConfiguration.TASK_STATUS_FAIL);
                    }
                } catch (SftpException | IOException e) {
                    ViewTaskList.thisViewTaskList.changeTaskStatus(taskIndex,
                            TaskConfiguration.TASK_STATUS_FAIL);
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (task.taskType == TaskConfiguration.TASK_TYPE_UPLOAD) {
                //上载任务
                try {
                    SFTPGroup.sftpUtilUpload.upload(task.rPwd, new String(task.lPwd.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), taskIndex);
                } catch (FileNotFoundException | SftpException e) {
                    ViewTaskList.thisViewTaskList.changeTaskStatus(taskIndex, TaskConfiguration.TASK_STATUS_FAIL);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取一个文件输出流
     *
     * @param task 传入一个任务
     * @return 返回一个输出流，如果任务中保存的文件保存位置是有效的，则返回的流是对应该位置的，
     * 如果任务中保存的默认保存位置是无效的，导致文件流创建失败，将创建一个默认下载位置的文件流，
     * 如果都创建失败了，将返回null
     */
    public OutputStream getOutputStream(TaskConfiguration task) {
        OutputStream outputStream;

        if (task.lPwd != null && !task.lPwd.isEmpty()) {//通过创建uri，使用uri创建文件流
            //创建文件保存位置Uri
            Uri uri = Uri.parse(task.lPwd);
            DocumentFile treeDocumentFile = DocumentFile.fromTreeUri(MainActivity.thisMainActivity.getApplicationContext(), uri);

            if (treeDocumentFile == null || !treeDocumentFile.isDirectory()) {
                return getOutputStream(task.taskContent);
            }
            //文件数据以二进制处理
            String mimeType = "application/octet-stream";
            //创建下载文件
            DocumentFile newFile = treeDocumentFile.createFile(mimeType, task.taskContent);
            if (newFile == null) {
                return getOutputStream(task.taskContent);
            }
            try {
                outputStream = MainActivity.thisMainActivity.getApplication().getContentResolver().openOutputStream(newFile.getUri());
            } catch (FileNotFoundException e) {
                //使用uri创建文件流失败，尝试创建Java原生File对象，获取文件默认保存位置的文件流
                outputStream = getOutputStream(task.taskContent);
            }
        } else {//通过创建Java原生文件对象，创建文件流
            outputStream = getOutputStream(task.taskContent);
        }
        return outputStream;
    }

    public OutputStream getOutputStream(String fileName) {
        OutputStream outputStream = null;

        File downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = new File(downloadsPath, fileName);
        // 判断文件是否存在
        if (outputFile.exists()) {
            // 如果文件存在，删除文件
            if (outputFile.delete()) {
                System.out.println("文件已删除");
            } else {
                outputFile = new File(downloadsPath, fileName + "(1)");
            }
        }
        try {
            outputStream = new FileOutputStream(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream;
    }
}

