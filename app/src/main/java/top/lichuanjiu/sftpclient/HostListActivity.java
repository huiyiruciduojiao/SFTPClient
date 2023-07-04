package top.lichuanjiu.sftpclient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.ArrayList;
import java.util.List;

import top.lichuanjiu.sftpclient.Tools.DomainUtils;
import top.lichuanjiu.sftpclient.Tools.Host;
import top.lichuanjiu.sftpclient.Tools.SFTPGroup;
import top.lichuanjiu.sftpclient.View.ViewShowFile;

public class HostListActivity extends AppCompatActivity {
    //当前对象
    public static HostListActivity hostListActivity;
    private QMUIGroupListView groupListView;
    private List<Host> hostList = new ArrayList<>();
    private PopupMenu popupMenu;

    @SuppressLint("Range")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_list);
        hostListActivity = this;
        initializeData();

    }

    private void initializeData() {
        if (groupListView != null) {
            groupListView.removeAllViews();
            groupListView = null;
            System.gc();
        }
        if (hostList.size() > 0) {
            hostList.clear();
        }
        Cursor host_list = SftpClient.db.query("HOST_LIST", null, null, null, null, null, null);
        groupListView = findViewById(R.id.host_list_groupListView);
        if (host_list.moveToFirst()) {
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
                QMUICommonListItemView listItemView = groupListView.createItemView(host.getName());
                listItemView.setOrientation(QMUICommonListItemView.VERTICAL);
                listItemView.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);
                listItemView.setDetailText(host.getAddress() + "\t" + host.getPort() + "\t" + host.getUserName());
                listItemView.setOnClickListener(view -> {
                    //创建一个host对象，并通过host对象获得sftpConfig对象
                    //        System.out.println(host_list.getString(1));
                    ViewShowFile.thisViewShowFile.nowPath = "./";
                    ViewShowFile.sftpGroup = new SFTPGroup(host.getSftpConfig());
                    ViewShowFile.sftpGroup.login();
                    finish();
                });
                listItemView.setOnLongClickListener(view -> {
                    popupMenu = new PopupMenu(HostListActivity.this, view);
                    popupMenu.inflate(R.menu.menu_host_list_item);
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(menuItem -> {
                        int clickId = menuItem.getItemId();
                        switch (clickId) {
                            case R.id.menu_host_attribute:
                                showHostDetails(host);
                                break;
                            case R.id.menu_host_del:
                                showSecondaryConfirmation(host);
                                break;
                            case R.id.menu_host_edit:
                                editHost(host);
                                break;
                        }
                        return true;
                    });
                    return true; //消耗事件
                });
                groupListView.addView(listItemView);
            }
        } else {
            TextView textView = findViewById(R.id.text_host_list_show_not);
            textView.setVisibility(View.VISIBLE);
        }
        host_list.close();
    }

    /**
     * 显示主机的详细详细
     *
     * @param host 主机对象
     */
    public void showHostDetails(Host host) {
        //显示主机信息框
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HostListActivity.this);

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
        hostPost.setText(host.getPort());
        hostUserName.setText(host.getUserName());
        hostPassword.setText(host.getPassword());

        characterSet.setSelection(position);
        dialog.show();
    }

    /**
     * 修改主机信息
     */

    public void editHost(Host host) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HostListActivity.this);

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
        hostPost.setText(host.getPort());
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

            int host_list = SftpClient.db.update("HOST_LIST", values, "Id = ?", new String[]{host.getHostId() + ""});
            values.clear();
            if (host_list > 0) {
                Toast.makeText(HostListActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                initializeData();
                ViewShowFile.thisViewShowFile.showHostList();
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
        new QMUIDialog.MessageDialogBuilder(this)
                .setMessage("删除主机")
                .setTitle("确定要删除该主机吗?")
                .addAction("取消", (dialog, index) -> dialog.dismiss())
                .addAction("确认", (dialog, index) -> {
                    if (delHost(host)) {
                        Toast.makeText(HostListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        initializeData();
                        ViewShowFile.thisViewShowFile.showHostList();
                    } else {
                        Toast.makeText(HostListActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .show();
    }

}
