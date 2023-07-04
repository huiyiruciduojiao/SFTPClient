package top.lichuanjiu.sftpclient.Tools;

import java.util.ArrayList;
import java.util.List;

public class TaskGroup {

    public List<TaskConfiguration> taskConfigurations =new ArrayList<>();

    public TaskGroup() {

    }

    /**
     * 添加任务
     *
     */
    public void addTask(TaskConfiguration taskConfiguration) {
        if (!taskConfigurations.contains(taskConfiguration)) {
            taskConfigurations.add(taskConfiguration);
        }
    }
    /**
     * 移除任务
     *
     */
    public void removeTask(TaskConfiguration taskConfiguration) {
        taskConfigurations.remove(taskConfiguration);
    }
    /**
     * 任务大小
     *
     */
    public int size() {
        return taskConfigurations.size();
    }
    /**
     * 获取任务索引
     *
     * @param taskConfiguration 传入一个任务
     * @return 返回一个int索引,如果任务存在，返回索引，不存在返回-1
     */
    public int getTaskIndex(TaskConfiguration taskConfiguration) {
        if (taskConfigurations.contains(taskConfiguration)) {
            return taskConfigurations.indexOf(taskConfiguration);
        } else {
            return -1;
        }
    }
    /**
     * 获取任务
     * @param index 传入一个任务索引
     * @return 如果任务存在，放回任务，不存在放回null
     */
    public TaskConfiguration getTask(int index) {
        return taskConfigurations.get(index);
    }
}
