package top.lichuanjiu.sftpclient.Tools;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;

import top.lichuanjiu.sftpclient.SftpClient;

public class SettingUtils {
    private static final String TABLE_SETTINGS = "SETTING";
    private static final String COLUMN_KEY = "Key";
    private static final String COLUMN_VALUE = "Value";

    private static final String[] SETTING_KEY = new String[]{"charset", "save_location"};
    /**
     * 保存设置
     *
     * @param key   设置名
     * @param value 设置值
     */
    public static void saveSetting(String key, String value) {
        ContentValues values = new ContentValues();
        if (getSetting(key) == null) {
            values.put(COLUMN_KEY, key);
            values.put(COLUMN_VALUE, value);
            SftpClient.db.insert(TABLE_SETTINGS, null, values);
        } else {
            values.put(COLUMN_VALUE, value);
            SftpClient.db.update(TABLE_SETTINGS, values, COLUMN_KEY + " = ?", new String[]{key});
        }
    }
    @SuppressLint("Range")
    public static String getSetting(String key) {
        String[] columns = {COLUMN_VALUE};
        String selection = COLUMN_KEY + "=?";
        String[] selectionArgs = {key};
        Cursor cursor = SftpClient.db.query(TABLE_SETTINGS, columns, selection, selectionArgs, null, null, null);
        String value = null;
        if (cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));
        }
        cursor.close();
        return value;
    }

    public static String[] getSETTING_KEY() {
        return SETTING_KEY;
    }
}
