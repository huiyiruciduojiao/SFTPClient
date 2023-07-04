package top.lichuanjiu.sftpclient.Tools;

import top.lichuanjiu.sftpclient.R;

public class SettingConfig {
    public static String charset;
    public static String saveLocation;
    public static int charsetIndex;
    public static void loadSetting(String[] characterSetArray) {

        SettingConfig.charset = SettingUtils.getSetting(SettingUtils.getSETTING_KEY()[0]);
        SettingConfig.saveLocation = SettingUtils.getSetting(SettingUtils.getSETTING_KEY()[1]);
        int position = 0;
        for (int i = 0; i < characterSetArray.length; i++) {
            if (characterSetArray[i].equals(charset)) {
                position = i;
                break;
            }
        }
        SettingConfig.charsetIndex = position;
    }
}
