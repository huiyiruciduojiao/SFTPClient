package top.lichuanjiu.sftpclient.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import top.lichuanjiu.sftpclient.MainActivity;
import top.lichuanjiu.sftpclient.R;
import top.lichuanjiu.sftpclient.SftpClient;
import top.lichuanjiu.sftpclient.Tools.DomainUtils;
import top.lichuanjiu.sftpclient.Tools.Host;
import top.lichuanjiu.sftpclient.Tools.SFTPGroup;
import top.lichuanjiu.sftpclient.Tools.SFTPUtil;

@SuppressWarnings("deprecation")
public class ViewShowFile extends Fragment {
    private static final ReentrantLock lock = new ReentrantLock();
    /**
     * sftp 操作对象组
     */
    public static SFTPGroup sftpGroup;
    /**
     * 当前对象的静态
     */
    @SuppressLint("StaticFieldLeak")
    public static ViewShowFile thisViewShowFile;
    /**
     * 当前视图
     */
    @SuppressLint("StaticFieldLeak")
    private static View viewShowFile = null;
    /**
     * 选中文件的索引
     */
    public List<QMUICommonListItemView> selectedItems = new ArrayList<>();
    public boolean multipleSelectionStatus = false;
    public String nowPath = "./";
    public Dialog dialog;
    /**
     * 文件类型数组
     */
    String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    String[] videoExtensions = {".mp4", ".mov", ".avi", ".mkv", ".wmv"};
    String[] audioExtensions = {".mp3", ".wav", ".aac", ".flac", ".ogg"};
    String[] compressedExtensions = {".zip", ".rar", ".tar", ".gz", ".7z"};
    String[] documentExtensions = {".doc", ".docx", ".pdf", ".ppt", ".pptx"};
    String[] spreadsheetExtensions = {".xls", ".xlsx", ".csv"};
    String[] executableExtensions = {".exe", ".sh", ".bat", ".jar"};
    String[] codeExtensions = {".java", ".cpp", ".py", ".html", ".css", ".js", ".php", ".jsp"};
    String[] textExtensions = {".txt", ".xml", ".json", ".log"};
    /**
     * 加载浮层标题元素
     */
    private TextView loadViewTitle = null;
    private PopupMenu popupMenu;
    private List<Host> hostList = new ArrayList<>();
    private QMUIGroupListView mGroupListView = null;
    private Handler handler;

    /**
     * 视图创建时调用
     *
     * @return 返回视图
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_showfile, container, false);
        viewShowFile = view;
        handler = new Handler(Looper.getMainLooper());
        //调用方法，初始化页面
        initialization();
        return view;
    }

    /**
     * 页面初始化
     */
    public void initialization() {
        //给悬浮按钮绑定事件
        bindButton();
        thisViewShowFile = this;
        dialog = new Dialog(viewShowFile.getContext());
        dialog.setContentView(R.layout.view_load);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false); // 设置是否可以通过返回键取消对话框
        loadViewTitle = dialog.findViewById(R.id.load_view_title);
        showHostList();
    }

    /**
     * 默认显示主机列表，如果不存在主机显示你需要添加
     */
    @SuppressLint("NonConstantResourceId")
    public void showHostList() {
        showLoad("加载中"); // 显示加载动画
        Cursor host_list = SftpClient.db.query("HOST_LIST", null, null, null, null, null, null);
        if (mGroupListView != null) {
            mGroupListView.removeAllViews();
            mGroupListView = null;
            System.gc();
        }
        if (hostList.size() > 0) {
            hostList.clear();
        }
        TextView textView = viewShowFile.findViewById(R.id.view_show_file_host_not);
        ScrollView scrollView = viewShowFile.findViewById(R.id.scrollView4);
        mGroupListView = viewShowFile.findViewById(R.id.groupListView);
        if (host_list.moveToFirst()) {
            textView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            do {
                Host tempHost = new Host(
                        host_list.getString(1),
                        host_list.getString(2),
                        DomainUtils.toInt(host_list.getString(3)),
                        host_list.getString(4),
                        host_list.getString(5),
                        host_list.getString(6),
                        DomainUtils.toInt(host_list.getString(0)));
                hostList.add(tempHost);
            } while (host_list.moveToNext());
            for (Host host : hostList) {
                //创建元素，显示在页面中
                QMUICommonListItemView listItemView = mGroupListView.createItemView(host.getName());
                listItemView.setOrientation(QMUICommonListItemView.VERTICAL);
                listItemView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
                listItemView.setDetailText(host.getAddress() + "\t" + host.getPort() + "\t" + host.getUserName());
                listItemView.setOnClickListener(view -> {
                    ViewShowFile.sftpGroup = new SFTPGroup(host.getSftpConfig());
                    ViewShowFile.sftpGroup.login();

                });
                listItemView.setOnLongClickListener(view -> {
                    popupMenu = new PopupMenu(viewShowFile.getContext(), view);
                    popupMenu.inflate(R.menu.menu_host_list_item);
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(menuItem -> {
                        int clickId = menuItem.getItemId();
                        switch (clickId) {
                            case R.id.menu_host_attribute:

                                showHostDetails(host);
                                break;
                            case R.id.menu_host_del:
                                // showSecondaryConfirmation(host);
                                showSecondaryConfirmation(host);
                                break;
                            case R.id.menu_host_edit:
                                //  editHost(host);
                                editHost(host);
                                break;
                        }
                        return true;
                    });
                    return true; //消耗事件
                });
                mGroupListView.addView(listItemView);
            }
        } else {

            textView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }
        host_list.close();
        stopLoad();
    }

    /**
     * 显示主机的详细详细
     *
     * @param host 主机对象
     */
    @SuppressLint("SetTextI18n")
    public void showHostDetails(Host host) {
        //显示主机信息框
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(viewShowFile.getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_add_host_dialog, null); // 自定义浮层布局文件
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        Button confirm = dialogView.findViewById(R.id.view_add_host_btn_confirm);
        Button cancel = dialogView.findViewById(R.id.view_add_host_btn_cancel);
        cancel.setVisibility(View.GONE);
        confirm.setText("关闭");
        confirm.setOnClickListener(view -> dialog.dismiss());

        TextView title = dialogView.findViewById(R.id.view_add_host_title);
        title.setText("主机信息");
        //主机名
        EditText hostName = dialogView.findViewById(R.id.view_add_host_name);
        //地址
        EditText hostAddress = dialogView.findViewById(R.id.view_add_host_address);
        //端口
        EditText hostPost = dialogView.findViewById(R.id.view_add_host_port);
        //用户名
        EditText hostUserName = dialogView.findViewById(R.id.view_add_host_username);
        //密码
        EditText hostPassword = dialogView.findViewById(R.id.view_add_host_password);
        //字符集下拉框
        Spinner characterSet = dialogView.findViewById(R.id.view_add_host_characterSet);


        String[] characterSetArray = getResources().getStringArray(R.array.characterSet);
        int position = -1;
        String characterSetStr = host.getCharacterSet();
        for (int i = 0; i < characterSetArray.length; i++) {
            if (characterSetArray[i].equals(characterSetStr)) {
                position = i;
                break;
            }
        }

        hostName.setText(host.getName());
        hostAddress.setText(host.getAddress());
        hostPost.setText(host.getPort()+"");
        hostUserName.setText(host.getUserName());
        hostPassword.setText(host.getPassword());

        characterSet.setSelection(position);
        dialog.show();
    }

    /**
     * 修改主机信息
     */

    @SuppressLint("SetTextI18n")
    public void editHost(Host host) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(thisViewShowFile.getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_add_host_dialog, null); // 自定义浮层布局文件

        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        TextView title = dialogView.findViewById(R.id.view_add_host_title);
        title.setText("编辑主机");
        //主机名
        EditText hostName = dialogView.findViewById(R.id.view_add_host_name);
        //地址
        EditText hostAddress = dialogView.findViewById(R.id.view_add_host_address);
        //端口
        EditText hostPost = dialogView.findViewById(R.id.view_add_host_port);
        //用户名
        EditText hostUserName = dialogView.findViewById(R.id.view_add_host_username);
        //密码
        EditText hostPassword = dialogView.findViewById(R.id.view_add_host_password);
        //字符集下拉框
        Spinner characterSet = dialogView.findViewById(R.id.view_add_host_characterSet);


        String[] characterSetArray = getResources().getStringArray(R.array.characterSet);
        int position = -1;
        String characterSetStr = host.getCharacterSet();
        for (int i = 0; i < characterSetArray.length; i++) {
            if (characterSetArray[i].equals(characterSetStr)) {
                position = i;
                break;
            }
        }

        hostName.setText(host.getName());
        hostAddress.setText(host.getAddress());
        hostPost.setText(host.getPort()+"");
        hostUserName.setText(host.getUserName());
        hostPassword.setText(host.getPassword());
        characterSet.setSelection(position);
        Button confirm = dialogView.findViewById(R.id.view_add_host_btn_confirm);
        Button cancel = dialogView.findViewById(R.id.view_add_host_btn_cancel);
        confirm.setOnClickListener(view -> {
            //判断数据合法性
            // 获取输入字段的值
            String name = hostName.getText().toString();
            String address = hostAddress.getText().toString();
            String port = hostPost.getText().toString();
            String username = hostUserName.getText().toString();
            String password = hostPassword.getText().toString();
            String characterSetStr1 = characterSet.getSelectedItem().toString();
            //创建网络工具对象
            DomainUtils domainUtils = new DomainUtils();

            //判断主机名是否为空
            if (name.isEmpty()) {
                hostName.setError("主机名不能为空");
                return;
            }
            // 判断地址是否合法
            if (!(domainUtils.isDomainName(address) || domainUtils.isIPV4(address))) {
                // 地址不合法，给出错误提示
                hostAddress.setError("请输入有效的域名或IP地址");
                return;
            }
            // 判断端口是否合法
            boolean isValidPort = domainUtils.isPort(port);
            if (!isValidPort) {
                // 端口不合法，给出错误提示
                hostPost.setError("请输入有效的端口号");
                return;
            }
            // 判断用户名是否为空
            boolean isValidUsername = !username.isEmpty();
            if (!isValidUsername) {
                // 用户名为空，给出错误提示
                hostUserName.setError("请输入用户名");
                return;
            }
            // 判断密码是否为空
            boolean isValidPassword = !password.isEmpty();
            if (!isValidPassword) {
                // 密码为空，给出错误提示
                hostPassword.setError("请输入密码");
                return;
            }
            //数据和法性验证完成，将数据存入到数据库中
            ContentValues values = new ContentValues();
            values.put("HostName", name);
            values.put("Host", address);
            values.put("HostPort", port);
            values.put("HOstUser", username);
            values.put("HostPassword", password);
            values.put("CharacterSet", characterSetStr1);
//                long host_list = SftpClient.db.insert("Host_List", null, values);

            int host_list = SftpClient.db.update("HOST_LIST", values, "Id = ?", new String[]{host.getHostId() + ""});
            values.clear();
            if (host_list > 0) {
                Toast.makeText(viewShowFile.getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                showHostList();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    /**
     * 删除主机
     *
     * @param host 需要删除的主机
     * @return true 删除成功， false 删除失败
     */
    public boolean delHost(Host host) {
        if (host == null) {
            return false;
        }
        System.out.println(hostList.size());
        hostList.remove(host);
        System.out.println(hostList.size());
        int delete = SftpClient.db.delete("Host_List", "Id = ?", new String[]{host.getHostId() + ""});
        return delete > 0;
    }

    public void showSecondaryConfirmation(Host host) {
        new QMUIDialog.MessageDialogBuilder(viewShowFile.getContext())
                .setMessage("删除主机")
                .setTitle("确定要删除该主机吗?")
                .addAction("取消", (dialog, index) -> dialog.dismiss())
                .addAction("确认", (dialog, index) -> {
                    if (delHost(host)) {
                        Toast.makeText(viewShowFile.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                        ViewShowFile.thisViewShowFile.showHostList();
                    } else {
                        Toast.makeText(viewShowFile.getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .show();
    }

    public void showLoad(String loadTitle) {
        handler.post(() -> {
            loadViewTitle.setText(loadTitle);
            System.out.println("加载中");
            dialog.show();
        });

    }

    public void stopLoad() {
        handler.post(() -> {
            System.out.println("加载完成");
            dialog.dismiss();
        });
    }

    private void bindButton() {
        FloatingActionButton floatingActionButton = viewShowFile.findViewById(R.id.floatingActionButton);
        //悬浮按钮点击事件
        floatingActionButton.setOnClickListener(view -> openDialog());
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void upView(String[][] arr) {
        if (mGroupListView != null) {
            mGroupListView.removeAllViews();
            mGroupListView = null;
            System.gc();
        }
        mGroupListView = viewShowFile.findViewById(R.id.groupListView);
        for (String[] arr1 : arr) {
            QMUICommonListItemView itemWithDetailBelow = mGroupListView.createItemView(arr1[8]);
            itemWithDetailBelow.setOrientation(QMUICommonListItemView.VERTICAL);

            itemWithDetailBelow.setDetailText(arr1[0] + "\t" + arr1[4] + "\t" + arr1[5] + " " + arr1[6] + " " + arr1[7]);
            //设置元素点击事件
            itemWithDetailBelow.setOnClickListener(view -> {
                if (!multipleSelectionStatus) {
                    if (arr1[0].charAt(0) == 'd') {
                        Thread thread = new Thread(() -> changPath(arr1[8]));
                        thread.start();
                    }
                } else {
                    changeSelectedStyle(view);
                }
            });
            //设置元素长按事件
            itemWithDetailBelow.setOnLongClickListener(view -> {
                if (!multipleSelectionStatus) {
                    QMUICommonListItemView qmuiCommonListItemView = (QMUICommonListItemView) view;
                    if (qmuiCommonListItemView.getText().equals("..") || qmuiCommonListItemView.getText().equals(".")) {//如果选择的文件夹是..或。，不进行操作
                        return true;
                    }
                    multipleSelectionStatus = true;
                    changeSelectedStyle(view);

                }
                //return true 消耗事件，不再触发点击事件
                return true;
            });
            //判断是否是文件夹
            Drawable iconDrawable;
            if (arr1[0].charAt(0) == 'd') {
                // 设置左边的图标
                iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_file_folder_yellow);
                // 设置右箭头
                itemWithDetailBelow.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
            } else {
                //获取文件名
                String fileName = arr1[8];
                // 获取文件扩展名
                String fileExtension = getFileExtension(fileName);
                // 判断文件类型
                if (isInArray(fileExtension, imageExtensions)) {
                    // 文件是一个图片文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_image);
                } else if (isInArray(fileExtension, videoExtensions)) {
                    // 文件是一个视频文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_mov);
                } else if (isInArray(fileExtension, audioExtensions)) {
                    // 文件是一个音频文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_mp3);
                } else if (isInArray(fileExtension, compressedExtensions)) {
                    // 文件是一个压缩文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_zip);
                } else if (isInArray(fileExtension, documentExtensions)) {
                    // 文件是一个文档文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_doc);
                } else if (isInArray(fileExtension, spreadsheetExtensions)) {
                    // 文件是一个表格文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_zip);
                } else if (isInArray(fileExtension, executableExtensions)) {
                    // 文件是一个可执行文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_exe);
                } else if (isInArray(fileExtension, codeExtensions)) {
                    // 文件是一个代码文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_div);
                } else if (isInArray(fileExtension, textExtensions)) {
                    //这是一个文本文件
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_txt__1_);
                } else {
                    // 文件类型未知
                    iconDrawable = viewShowFile.getContext().getDrawable(R.drawable.ic_browser);
                }
            }
            itemWithDetailBelow.setImageDrawable(iconDrawable);

            mGroupListView.addView(itemWithDetailBelow);
        }
    }

    /**
     * 更改选中元素的样式
     */
    private void changeSelectedStyle(View view) {
        //将选中的元素转换
        QMUICommonListItemView qmuiCommonListItemView = (QMUICommonListItemView) view;
        if (qmuiCommonListItemView.getText().equals("..") || qmuiCommonListItemView.getText().equals(".")) {//如果选择的文件夹是..或。，不进行操作
            return;
        }
        //显示下载按钮
        MainActivity.thisMainActivity.menu_download.setVisible(true);
        //如果选中元素存在，移除，恢复默认样式
        if (selectedItems.contains(qmuiCommonListItemView)) {
            //修改元素样式
            qmuiCommonListItemView.setBackgroundColor(getResources().getColor(
                    com.qmuiteam.qmui.R.color.design_default_color_background));
            //从链表中移除元素
            selectedItems.remove(qmuiCommonListItemView);
            if (selectedItems.size() == 0) {
                reSelectedStyle();
                multipleSelectionStatus = false;
            }
        } else {
            //不存在，添加选中样式，将元素添加到链表中
            view.setBackgroundColor(getResources().getColor(R.color.selected_color));
            selectedItems.add(qmuiCommonListItemView);
            if (selectedItems.size() > 1) {
                MainActivity.thisMainActivity.menu_delete.setVisible(false);
                MainActivity.thisMainActivity.menu_rename.setVisible(false);
            }
        }
        if (selectedItems.size() == 1) {
            MainActivity.thisMainActivity.menu_rename.setVisible(true);
        }
        if (selectedItems.size() > 0) {
            MainActivity.thisMainActivity.menu_delete.setVisible(true);
        }

    }

    /**
     * 清除全部样式
     */
    public void reSelectedStyle() {
        if (selectedItems.size() > 0) {
            for (QMUICommonListItemView temp : selectedItems) {
                temp.setBackgroundColor(getResources().getColor(
                        com.qmuiteam.qmui.R.color.design_default_color_background));
            }
        }
        multipleSelectionStatus = false;
        MainActivity.thisMainActivity.menu_download.setVisible(false);
        MainActivity.thisMainActivity.menu_rename.setVisible(false);
        MainActivity.thisMainActivity.menu_delete.setVisible(false);
        selectedItems.clear();
    }

    /**
     * 创建显示的数据
     *
     * @param sftpUtil 传入一个sftp工具对象
     */
    public void createFolderData(SFTPUtil sftpUtil, String url) {
        try {
            TextView textView = viewShowFile.findViewById(R.id.text_showPath);
            //判断登入状态
            if (SFTPGroup.sftpUtilShow.SftpStatus()) {
                //调用sftp工具对象获取数据
                Vector<ChannelSftp.LsEntry> vector = sftpUtil.listFiles(url);

                String[][] arr = parseData(vector);

                handler.post(() -> upView(arr));
                //在TextShowPath上显示当前位置
                String showText = sftpUtil.pwd();
                if (showText.length() > 50) {
                    String truncatedText = "..." + showText.substring(showText.length() - 50);
                    handler.post(() -> textView.setText(truncatedText));

                } else {
                    handler.post(() -> textView.setText(showText));

                }

            } else {
                textView.setText("未连接服务器");
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    public void changPath(String Path) {
        lock.lock();
        showLoad("加载中");
        SFTPGroup.sftpUtilShow.cd(Path);
        nowPath = SFTPGroup.sftpUtilShow.pwd();
        System.out.println(nowPath);
        createFolderData(SFTPGroup.sftpUtilShow, nowPath);
        stopLoad();
        lock.unlock();
    }

    // 判断文件扩展名是否在指定的数组中
    private boolean isInArray(String fileExtension, String[] extensions) {
        for (String extension : extensions) {
            if (extension.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    // 获取文件扩展名
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }

    /**
     * 解析一个Vector数组
     *
     * @param vector 解析的vector数组
     * @return 返回一个有规律的二维数组，方便后续操作
     */
    @SuppressWarnings("SuspiciousListRemoveInLoop")
    private String[][] parseData(Vector<ChannelSftp.LsEntry> vector) {
        Collections.sort(vector);
        //创建一个返回的数组
        String[][] arr = new String[vector.size() - 1][];
        for (int i = 0; i < arr.length; i++) {
            if (vector.get(i).getFilename().equals(".")) {
                vector.remove(i);
            }
            String[] tempArr = vector.get(i).toString().split("\\s+");
            tempArr[8] = vector.get(i).getFilename();
            arr[i] = tempArr;
        }
        return arr;
    }

    private void openDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(thisViewShowFile.getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_add_host_dialog, null); // 自定义浮层布局文件
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        Button confirm = dialogView.findViewById(R.id.view_add_host_btn_confirm);
        Button cancel = dialogView.findViewById(R.id.view_add_host_btn_cancel);
        confirm.setOnClickListener(view -> {
            //主机名
            EditText hostName = dialogView.findViewById(R.id.view_add_host_name);
            //地址
            EditText hostAddress = dialogView.findViewById(R.id.view_add_host_address);
            //端口
            EditText hostPost = dialogView.findViewById(R.id.view_add_host_port);
            //用户名
            EditText hostUserName = dialogView.findViewById(R.id.view_add_host_username);
            //密码
            EditText hostPassword = dialogView.findViewById(R.id.view_add_host_password);
            //字符集下拉框
            Spinner characterSet = dialogView.findViewById(R.id.view_add_host_characterSet);
            //判断数据合法性
            // 获取输入字段的值
            String name = hostName.getText().toString();
            String address = hostAddress.getText().toString();
            String port = hostPost.getText().toString();
            String username = hostUserName.getText().toString();
            String password = hostPassword.getText().toString();
            String characterSetStr = characterSet.getSelectedItem().toString();
            //创建网络工具对象
            DomainUtils domainUtils = new DomainUtils();

            //判断主机名是否为空
            if (name.isEmpty()) {
                hostName.setError("主机名不能为空");
                return;
            }
            // 判断地址是否合法
            if (!(domainUtils.isDomainName(address) || domainUtils.isIPV4(address))) {
                // 地址不合法，给出错误提示
                hostAddress.setError("请输入有效的域名或IP地址");
                return;
            }
            // 判断端口是否合法
            boolean isValidPort = domainUtils.isPort(port);
            if (!isValidPort) {
                // 端口不合法，给出错误提示
                hostPost.setError("请输入有效的端口号");
                return;
            }
            // 判断用户名是否为空
            boolean isValidUsername = !username.isEmpty();
            if (!isValidUsername) {
                // 用户名为空，给出错误提示
                hostUserName.setError("请输入用户名");
                return;
            }
            // 判断密码是否为空
            boolean isValidPassword = !password.isEmpty();
            if (!isValidPassword) {
                // 密码为空，给出错误提示
                hostPassword.setError("请输入密码");
                return;
            }
            //数据和法性验证完成，将数据存入到数据库中
            ContentValues values = new ContentValues();
            values.put("HostName", name);
            values.put("Host", address);
            values.put("HostPort", port);
            values.put("HOstUser", username);
            values.put("HostPassword", password);
            values.put("CharacterSet", characterSetStr);
            long host_list = SftpClient.db.insert("Host_List", null, values);
            values.clear();
            if (host_list > 0) {
                Toast.makeText(viewShowFile.getContext(), "添加成功", Toast.LENGTH_SHORT).show();
                showHostList();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    public void reLoad() {
        changPath("./");
    }
}
