package top.lichuanjiu.sftpclient.Tools;

import com.jcraft.jsch.SftpProgressMonitor;

import top.lichuanjiu.sftpclient.View.ViewTaskList;

public class UpLoadTaskProgressMonitor implements SftpProgressMonitor {
    //任务总大小
    private long taskSize;
    //当前传输的大小
    private long totalSize = 0;
    //任务下标
    private int taskIndex;

    public UpLoadTaskProgressMonitor(long taskSize, int taskIndex) {
        this.taskSize = taskSize;
        this.taskIndex = taskIndex;
    }

    /**
     * 任务开始时传输
     */
    @Override
    public void init(int op, String src, String dest, long max) {
    }

    /**
     * 任务每传输玩一个数据块时调用
     *
     * @param count 本次传输数据块大小
     * @return true 继续传输，false 暂停传输
     */
    @Override
    public boolean count(long count) {
        totalSize += count;
        int progress = (int) (totalSize * 1000 / taskSize);
        ViewTaskList.thisViewTaskList.upProgress(taskIndex, progress);
        return true;
    }

    /**
     * 任务完成
     */
    @Override
    public void end() {
        ViewTaskList.thisViewTaskList.changeTaskStatus(taskIndex, TaskConfiguration.TASK_STATUS_COMPLETE);
        ViewTaskList.thisViewTaskList.upProgress(taskIndex, 1000);
    }
}
