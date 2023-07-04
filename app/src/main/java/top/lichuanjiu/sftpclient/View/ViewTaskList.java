package top.lichuanjiu.sftpclient.View;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qmuiteam.qmui.widget.QMUIProgressBar;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.ArrayList;
import java.util.List;

import top.lichuanjiu.sftpclient.R;
import top.lichuanjiu.sftpclient.Tools.TaskConfiguration;
import top.lichuanjiu.sftpclient.Tools.TaskGroup;

public class ViewTaskList extends Fragment {
    /**
     * 当前对象引用
     */
    @SuppressLint("StaticFieldLeak")
    public static ViewTaskList thisViewTaskList;
    /**
     * 当前视图
     */
    @SuppressLint("StaticFieldLeak")
    public static View viewTaskList;
    /**
     * 任务列表
     */
    public List<QMUICommonListItemView> taskViewList = new ArrayList<>();
    /**
     * 任务进度列表
     */
    public List<QMUIProgressBar> TaskProgressList = new ArrayList<>();
    /**
     * 任务列表
     */
    public List<TaskConfiguration> taskConfigurations = new ArrayList<>();
    private QMUIGroupListView qmuiGroupListView;

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_tasklist, container, false);
        viewTaskList = view;
        thisViewTaskList = this;
        initialization();

        return view;
    }

    public void initialization() {
        qmuiGroupListView = viewTaskList.findViewById(R.id.taskGroupListView);

    }

    public void createTaskList(TaskGroup taskGroup) {
        for (TaskConfiguration task : taskGroup.taskConfigurations) {
            // 创建任务布局
            LinearLayout container = new LinearLayout(viewTaskList.getContext());
            container.setOrientation(LinearLayout.VERTICAL);

            //创建任务元素
            QMUICommonListItemView qmuiCommonListItemView = qmuiGroupListView.createItemView(task.taskName);
            qmuiCommonListItemView.setOrientation(QMUICommonListItemView.VERTICAL);
            //根据任务类型创建图片
            Drawable iconDrawable = null;
            if (task.taskType == TaskConfiguration.TASK_TYPE_UPLOAD) {
                iconDrawable = viewTaskList.getContext().getDrawable(R.drawable.ic_upload);
            } else if (task.taskType == TaskConfiguration.TASK_TYPE_DOWNLOAD) {
                iconDrawable = viewTaskList.getContext().getDrawable(R.drawable.ic_download);
            }
            //为元素添加图片
            qmuiCommonListItemView.setImageDrawable(iconDrawable);
            qmuiCommonListItemView.setDetailText(task.taskTime + "\t" + task.taskContent);
            container.addView(qmuiCommonListItemView); // 添加任务元素

            // 创建任务进度条
            QMUIProgressBar progressBar = new QMUIProgressBar(viewTaskList.getContext());
            progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 5));
            progressBar.setMaxValue(1000);
            progressBar.setProgress(0);

            //将任务元素以及任务进度条添加到对应的列表中
            taskViewList.add(qmuiCommonListItemView);
            TaskProgressList.add(progressBar);
            taskConfigurations.add(task);

            container.addView(progressBar); // 添加任务进度条

            qmuiGroupListView.addView(container);
        }
    }

    /**
     * 获取任务的下标
     */
    public int getTaskIndex(TaskConfiguration taskConfiguration) {
        if (taskConfigurations.contains(taskConfiguration)) {
            return taskConfigurations.indexOf(taskConfiguration);
        }
        return 0;
    }

    /**
     * 更新任务进度
     */
    public void upProgress(int index, int value) {
        TaskProgressList.get(index).setProgress(value);
    }

    public void changeTaskStatus(int index, int Status) {
        QMUICommonListItemView commonListItemView = taskViewList.get(index);
        Drawable iconDrawable = null;
        int color = -1;
        switch (Status) {
            case TaskConfiguration.TASK_STATUS_COMPLETE:
                //任务完成
                color = getResources().getColor(R.color.green);
                iconDrawable = viewTaskList.getContext().getDrawable(R.drawable.ic_complete);
                break;
            case TaskConfiguration.TASK_STATUS_FAIL:
                //任务失败
                color = getResources().getColor(R.color.red);
                iconDrawable = viewTaskList.getContext().getDrawable(R.drawable.ic_fail);
                break;
            case TaskConfiguration.TASK_STATUS_NOT_STARTED:
                iconDrawable = viewTaskList.getContext().getDrawable(R.drawable.ic_suspend);
                //任务未开始
                break;
            case TaskConfiguration.TASK_STATUS_IN_PROGRESS:
                //任务进行中
                break;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        int finalColor = color;
        Drawable finalIconDrawable = iconDrawable;
        handler.post(() -> {
            commonListItemView.getTextView().setTextColor(finalColor);
            commonListItemView.setImageDrawable(finalIconDrawable);
        });

    }
}
