package top.lichuanjiu.sftpclient.Tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_DATABASE_HOST_LIST = "CREATE TABLE HOST_LIST (\n" +
            "    Id           INTEGER   PRIMARY KEY AUTOINCREMENT,\n" +
            "    HostName     TEXT (50) NOT NULL,\n" +
            "    Host         TEXT (50) NOT NULL,\n" +
            "    HostPort     INTEGER   NOT NULL,\n" +
            "    HostUser     TEXT (50) NOT NULL,\n" +
            "    HostPassword TEXT (50) NOT NULL,\n" +
            "    CharacterSet TEXT (50) NOT NULL\n" +
            ");\n";
    private static final String CREATE_TABLE_SETTING = "CREATE TABLE SETTING (\n" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    'Key' TEXT,\n" +
            "    Value TEXT,\n" +
            "    UNIQUE ('Key', Value)" +
            ");\n";

    private Context context;

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DATABASE_HOST_LIST);
        sqLiteDatabase.execSQL(CREATE_TABLE_SETTING);
        Toast.makeText(context, "数据库创建成功", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
