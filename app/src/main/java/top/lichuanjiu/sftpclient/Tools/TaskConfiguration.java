package top.lichuanjiu.sftpclient.Tools;

public class TaskConfiguration {
    /**
     * 任务类型，上传
     */
    public static final int TASK_TYPE_UPLOAD = 0;
    /**
     * 任务类型，下载
     */
    public static final int TASK_TYPE_DOWNLOAD = 1;
    /**
     * 任务类型，删除
     */
    public static final int TASK_TYPE_DELETE = 2;
    /**
     * 任务类型，重命名
     */
    public static final int TASK_TYPE_RENAME = 3;
    /**
     * 任务状态未开始
     */
    public static final int TASK_STATUS_NOT_STARTED = 0;
    /**
     * 任务状态进行中
     */
    public static final int TASK_STATUS_IN_PROGRESS = 1;
    /**
     * 任务状态完成
     */
    public static final int TASK_STATUS_COMPLETE = 2;
    /**
     * 任务状态，失败
     */
    public static final int TASK_STATUS_FAIL = -1;
    public String taskName;
    public int taskType;
    public String taskTime;
    public int taskStatus;
    /**
     * 任务本地工作目录
     */
    public String lPwd;
    /**
     * 任务远程工作目录
     */
    public String rPwd;
    /**
     * 任务内容
     */
    public String taskContent;
    /***
     * 任务大小
     */
    public long taskSize;

    public TaskConfiguration(String taskName, int taskType, String taskTime, int taskStatus) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.taskTime = taskTime;
        this.taskStatus = taskStatus;
    }
}
