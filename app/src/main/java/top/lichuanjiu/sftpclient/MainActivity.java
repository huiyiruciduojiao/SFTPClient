package top.lichuanjiu.sftpclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import top.lichuanjiu.sftpclient.Tools.DatabaseHelper;
import top.lichuanjiu.sftpclient.Tools.PagerAdapter;
import top.lichuanjiu.sftpclient.Tools.PickUtils;
import top.lichuanjiu.sftpclient.Tools.ResultContract;
import top.lichuanjiu.sftpclient.Tools.SFTPGroup;
import top.lichuanjiu.sftpclient.Tools.SettingConfig;
import top.lichuanjiu.sftpclient.Tools.TaskConfiguration;
import top.lichuanjiu.sftpclient.Tools.TaskExecutor;
import top.lichuanjiu.sftpclient.Tools.TaskGroup;
import top.lichuanjiu.sftpclient.View.ViewShowFile;
import top.lichuanjiu.sftpclient.View.ViewTaskList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    public static MainActivity thisMainActivity;
    public static ExecutorService executor;
    public static BlockingQueue<Runnable> taskQueue;
    public MenuItem menu_download;
    public MenuItem menu_delete;
    public MenuItem menu_rename;
    boolean isRefuse = false;
    /**
     * 文件上传对象
     */
    @SuppressWarnings("rawtypes")
    ActivityResultLauncher launcher;
    private boolean doubleBackToExitPressedOnce = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisMainActivity = MainActivity.this;

        //数据库工具初始化
        SftpClient.db = new DatabaseHelper(
                this, "SftpClient.db", null, 1).getWritableDatabase();
        //初始化设置配置对象
        SettingConfig.loadSetting(getResources().getStringArray(R.array.characterSet));
        //线程池初始化
        SftpClient myApp = (SftpClient) getApplication();
        executor = myApp.getExecutor();
        taskQueue = myApp.getTaskQueue();

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new ViewShowFile(), getString(R.string.show_file_view_title));
        pagerAdapter.addFragment(new ViewTaskList(), getString(R.string.show_taskGroup_view_title));


        // 将适配器设置给ViewPager
        viewPager.setAdapter(pagerAdapter);
        // 将ViewPager与TabLayout关联
        tabLayout.setupWithViewPager(viewPager);

        //申请权限  安卓6-安卓11申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }
        //安卓11以上申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !isRefuse) {// android 11  且 不是已经被拒绝
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1024);
            }
        }

        //创建上传按钮点击回调对象
        launcher = registerForActivityResult(new ResultContract(), result -> {
            if (result == null) {
                return;
            }
            Uri uri = result.getData();
            //将上传封装成一个任务
            TaskGroup taskGroup = createUpLoadTask(uri);
            new TaskExecutor().implement(taskGroup);
            Toast.makeText(MainActivity.this, R.string.task_add_success_tips, Toast.LENGTH_SHORT).show();

        });
    }

    @Override
    public void onBackPressed() {
        //当前不在根目录，返回键作用为上一级目录
        if (SFTPGroup.sftpUtilShow != null && !SFTPGroup.sftpUtilShow.pwd().equals("/") && !ViewShowFile.thisViewShowFile.multipleSelectionStatus) {
            Thread thread = new Thread(() -> ViewShowFile.thisViewShowFile.changPath(".."));
            thread.start();
        } else if (ViewShowFile.thisViewShowFile.multipleSelectionStatus) {
            //返回键，清除所有样式，取消多选样式
            ViewShowFile.thisViewShowFile.reSelectedStyle();
        } else if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.exit_prompt, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //绑定菜单
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        menu_download = menu.findItem(R.id.menu_download);
        menu_delete = menu.findItem(R.id.menu_delete);
        menu_rename = menu.findItem(R.id.menu_rename);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单元素点击事件
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_uploadFile:
                if(SFTPGroup.sftpUtilUpload == null) {
                    Toast.makeText(this, R.string.no_connect_host_tips,Toast.LENGTH_SHORT).show();
                }else{
                    uploadFile();
                }
                break;
            case R.id.menu_reLoad:
                //刷新
                if (ViewShowFile.sftpGroup != null) {
                    ViewShowFile.sftpGroup.login();
                } else {
                    ViewShowFile.thisViewShowFile.showHostList();
                }
                break;
            case R.id.menu_exit:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask(); // 在Android 5.0及以上版本使用finishAndRemoveTask()方法
                } else {
                    finish(); // 在Android 5.0以下版本使用finish()方法
                }
                System.exit(0); // 终止应用进程
                break;
            case R.id.menu_download:
                //创建任务组
                TaskGroup taskGroup = createDownloadTask();
                //将任务组交给任务执行器
                new TaskExecutor().implement(taskGroup);
                //恢复默认样式
                ViewShowFile.thisViewShowFile.reSelectedStyle();
                Toast.makeText(this, R.string.task_add_success_tips, Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_delete:
                deleteWarning();
                break;
            case R.id.menu_rename:
                renameInteractiveBox();
                break;
            case R.id.menu_host_list:
                startActivity(new Intent(this, HostListActivity.class));
                break;
            case R.id.menu_sponsor:
                Uri uri = Uri.parse("https://www.lichuanjiu.top/supportOur.php");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));

                break;
            case R.id.menu_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void renameInteractiveBox() {
        //获取选中的任务名
        List<QMUICommonListItemView> selectedItems = ViewShowFile.thisViewShowFile.selectedItems;
        if (selectedItems.size() != 1) {
            return;
        }
        String name = selectedItems.get(0).getText().toString();
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("重命名文件：" + name)
                .setPlaceholder(name)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", (dialog, index) -> dialog.dismiss())
                .addAction("确定", (dialog, index) -> {
                    //noinspection deprecation
                    CharSequence newFileName = builder.getEditText().getText();
                    if (!(newFileName.toString().replaceAll("\\s+", "").isEmpty())) {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                        Date date = new Date(System.currentTimeMillis());
                        //创建一个重命名任务
                        TaskConfiguration taskConfiguration = new TaskConfiguration(
                                name,
                                TaskConfiguration.TASK_TYPE_RENAME,
                                formatter.format(date),
                                TaskConfiguration.TASK_STATUS_NOT_STARTED
                        );
                        taskConfiguration.taskContent = newFileName.toString();
                        taskConfiguration.rPwd = ViewShowFile.thisViewShowFile.nowPath;
                        //开始重命名
                        new TaskExecutor().implementReNameTask(taskConfiguration);
                        //恢复样式
                        ViewShowFile.thisViewShowFile.reSelectedStyle();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "请输入新文件名", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * 删除文件时弹出警告对话框，二次确认
     */
    private void deleteWarning() {
        //获取用户选中数量
        int userSelectNum = ViewShowFile.thisViewShowFile.selectedItems.size();
        new QMUIDialog.MessageDialogBuilder(this)
                .setMessage("您已经选中了" + userSelectNum + "个文件")
                .setTitle("确认要删除吗?")
                .addAction("取消", (dialog, index) -> dialog.dismiss())
                .addAction("确认", (dialog, index) -> {
                    TaskGroup deleteTaskGroup = createDeleteTask();
                    new TaskExecutor().implementDeleteTask(deleteTaskGroup);
                    ViewShowFile.thisViewShowFile.reSelectedStyle();
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * 上传文件
     */
    private void uploadFile() {

        launcher.launch(true);
    }

    /**
     * 创建下载任务组
     */
    private TaskGroup createDownloadTask() {
        boolean isSelectFolder = false;
        //获取任务信息
        List<QMUICommonListItemView> tasks = ViewShowFile.thisViewShowFile.selectedItems;
        if (tasks.size() <= 0) {
            return null;
        }
        //创建任务组对象
        TaskGroup taskGroup = new TaskGroup();
        for (QMUICommonListItemView temp : tasks) {
            String fileMeta = temp.getDetailText().toString().split("\t")[0];
            if (fileMeta.charAt(0) == 'd') {
                isSelectFolder = true;
                continue;
            }
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            //创建单个任务对象
            TaskConfiguration taskConfiguration = new TaskConfiguration(temp.getText().toString(),
                    TaskConfiguration.TASK_TYPE_DOWNLOAD,
                    formatter.format(date),
                    TaskConfiguration.TASK_STATUS_NOT_STARTED);
            //任务初始化
            taskConfiguration.rPwd = ViewShowFile.thisViewShowFile.nowPath;//设置任务远程工作目录
            taskConfiguration.taskContent = temp.getText().toString();//设置任务工作内容
            taskConfiguration.taskSize = Integer.parseInt(temp.getDetailText().toString()
                    .split("\t")[1]);
            taskConfiguration.lPwd = SettingConfig.saveLocation;
            //将任务添加到任务组
            taskGroup.addTask(taskConfiguration);
        }
        if (isSelectFolder) {
            Toast.makeText(this, R.string.download_folder_no_tips, Toast.LENGTH_SHORT).show();
        }
        return taskGroup;
    }

    /**
     * 创建上传任务组
     *
     * @param uri 上传资源定位符
     * @return 返回任务组
     */
    private TaskGroup createUpLoadTask(Uri uri) {
        //创建任务组
        TaskGroup taskGroup = new TaskGroup();
        //获取任务信息
        //文件路径
        String mFilePath = PickUtils.getPath(getApplicationContext(), uri);
        //文件名
        String mFileName = PickUtils.getFileName(getApplicationContext(), uri);
        //任务时间
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        //创建单个任务
        TaskConfiguration taskConfiguration = new TaskConfiguration(mFileName,
                TaskConfiguration.TASK_TYPE_UPLOAD,
                formatter.format(date),
                TaskConfiguration.TASK_STATUS_NOT_STARTED);
        taskConfiguration.lPwd = mFilePath;
        taskConfiguration.rPwd = ViewShowFile.thisViewShowFile.nowPath;
        taskConfiguration.taskContent = mFileName;
        //任务初始化
        taskGroup.addTask(taskConfiguration);
        return taskGroup;
    }

    /**
     * 创建删除任务组
     *
     * @return 返回一个任务组对象
     */
    private TaskGroup createDeleteTask() {
        TaskGroup taskGroup = new TaskGroup();
        //获取选中的元素
        List<QMUICommonListItemView> tasks = ViewShowFile.thisViewShowFile.selectedItems;
        if (tasks.size() < 1) {
            return null;
        }
        for (QMUICommonListItemView temp : tasks) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            String fileMeta = temp.getDetailText().toString().split("\t")[0];
            TaskConfiguration taskConfiguration = new TaskConfiguration(
                    temp.getText().toString(),
                    TaskConfiguration.TASK_TYPE_DELETE,
                    formatter.format(date),
                    TaskConfiguration.TASK_STATUS_NOT_STARTED
            );
            taskConfiguration.rPwd = ViewShowFile.thisViewShowFile.nowPath;
            taskConfiguration.taskContent = temp.getText().toString();
            //如果选中的数据是文件夹，任务大小1
            if (fileMeta.charAt(0) == 'd') {
                taskConfiguration.taskSize = 1;
            } else {//如果选中的数据是文件，任务大小2
                taskConfiguration.taskSize = -1;
            }
            //将任务添加到任务组
            taskGroup.addTask(taskConfiguration);
        }
        return taskGroup;
    }

    /**
     * 动态申请权限 安卓6-安卓11
     */
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, R.string.authorization_prompt, Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

        } else {
            Toast.makeText(this, R.string.authorization_success_tips, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.authorization_success_tips, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.authorization_fail_tips, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 带回授权结果-安卓11以上
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 检查是否有权限
            // 授权成功
            // 授权失败
            isRefuse = !Environment.isExternalStorageManager();
        }
    }


}