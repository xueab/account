package com.example.accountbook.Dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "accounting_app.db";
    private static final int DATABASE_VERSION = 1;

    // 用户表创建SQL
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE user_info (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "phone TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    // 类别表创建SQL
    private static final String CREATE_CATEGORY_TABLE =
            "CREATE TABLE category (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "type INTEGER NOT NULL," +
                    "icon TEXT," +
                    "user_id INTEGER NOT NULL," +
                    "create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES user_info(id)" +
                    ");";

    // 记录表创建SQL
    private static final String CREATE_RECORD_TABLE =
            "CREATE TABLE record (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "amount REAL NOT NULL," +
                    "type INTEGER NOT NULL," +
                    "category_id INTEGER NOT NULL," +
                    "remark TEXT," +
                    "date TEXT NOT NULL," +
                    "time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "user_id INTEGER NOT NULL," +
                    "create_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "update_time TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (category_id) REFERENCES category(id)," +
                    "FOREIGN KEY (user_id) REFERENCES user_info(id)" +
                    ");";

    // 创建索引SQL
    private static final String CREATE_USER_PHONE_INDEX =
            "CREATE INDEX idx_user_phone ON user_info(phone);";
    private static final String CREATE_RECORD_USER_INDEX =
            "CREATE INDEX idx_record_user ON record(user_id);";
    private static final String CREATE_RECORD_DATE_INDEX =
            "CREATE INDEX idx_record_date ON record(date);";
    private static final String CREATE_CATEGORY_USER_INDEX =
            "CREATE INDEX idx_category_user ON category(user_id);";

    private Context context;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_CATEGORY_TABLE);
        db.execSQL(CREATE_RECORD_TABLE);
        db.execSQL(CREATE_USER_PHONE_INDEX);
        db.execSQL(CREATE_RECORD_USER_INDEX);
        db.execSQL(CREATE_RECORD_DATE_INDEX);
        db.execSQL(CREATE_CATEGORY_USER_INDEX);
        Toast.makeText(context,"创建成功",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 简单处理：删除旧表，创建新表
        db.execSQL("DROP TABLE IF EXISTS record");
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS user_info");
        onCreate(db);
    }
}