package top.lichuanjiu.sftpclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import top.lichuanjiu.sftpclient.Tools.SettingConfig;
import top.lichuanjiu.sftpclient.Tools.SettingUtils;

public class SettingActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_FOLDER_PICKER = 1;
    private final int BACK_BUTTON_ID = 16908332;
    private Spinner spinnerCharset;
    private EditText editTextSaveLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //初始化变量
        initializeVariable();
        //初始化页面
        initialization();
    }

    /**
     * 初始化页面
     */
    private void initialization() {
        //设置顶部菜单栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.title);//设置标题样式
            actionBar.setHomeButtonEnabled(true);//设置左上角的图标是否可以点击
            actionBar.setDisplayHomeAsUpEnabled(true);//给左上角图标的左边加上一个返回的图标
            actionBar.setDisplayShowCustomEnabled(true);// 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用
        }
        //为按钮绑定事件
        bindSaveBtnEven();
        bindSelectFolderBtnEven();
        //加载数据到页面中
        loadData();
    }

    /**
     * 将数据库中的数据加载到页面中
     */
    private void loadData() {

        SettingConfig.loadSetting(getResources().getStringArray(R.array.characterSet));
        spinnerCharset.setSelection(SettingConfig.charsetIndex);
        editTextSaveLocation.setText(SettingConfig.saveLocation);
    }

    /**
     * 初始化变量
     */
    private void initializeVariable() {
        spinnerCharset = findViewById(R.id.setting_SpinnerCharset);
        editTextSaveLocation = findViewById(R.id.setting_EditTextSaveLocation);
    }

    /**
     * 选择文件夹按钮的事件绑定
     */
    private void bindSelectFolderBtnEven() {
        Button selectFolderBtn = findViewById(R.id.setting_ButtonSelectFolder);
        selectFolderBtn.setOnClickListener(view -> openFolderPicker());
    }

    /**
     * 保存按钮的事件绑定方法
     */
    private void bindSaveBtnEven() {
        //获取布局文件中的保存按钮
        Button saveBtn = findViewById(R.id.setting_ButtonSave);
        saveBtn.setOnClickListener(view -> {
            String charset = spinnerCharset.getSelectedItem().toString();

            String saveLocation = editTextSaveLocation.getText().toString();
            // 执行保存操作
            SettingUtils.saveSetting(SettingUtils.getSETTING_KEY()[0], charset);
            SettingUtils.saveSetting(SettingUtils.getSETTING_KEY()[1], saveLocation);
            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
        });

    }

    // 打开文件选择器
    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(intent, "选择文件夹"), REQUEST_CODE_FOLDER_PICKER);
    }

    //处理文件选择器的结果
    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOLDER_PICKER && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    //uri权限持久化
                    final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);

                    editTextSaveLocation.setText(uri.toString());
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == BACK_BUTTON_ID) {//返回按钮被点击
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
