package com.example.accountbook.Dao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDao {
    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 添加用户
    public long addUser(String phone, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phone", phone);
        values.put("password", password);
        long id = db.insert("user_info", null, values);
        db.close();
        return id;
    }

    // 根据手机号获取用户
    public Cursor getUserByPhone(String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query("user_info", null, "phone=?",
                new String[]{phone}, null, null, null);
    }

    // 验证用户登陆
    public boolean validateUser(String phone, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("user_info", new String[]{"id"},
                "phone=? AND password=?",
                new String[]{phone, password}, null, null, null);
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isValid;
    }

    // 更新用户密码
    public int updatePassword(String phone, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        values.put("update_time", "datetime('now')");
        int rows = db.update("user_info", values, "phone=?",
                new String[]{phone});
        db.close();
        return rows;
    }

    // 删除用户
    public int deleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("user_info", "id=?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rows;
    }
}