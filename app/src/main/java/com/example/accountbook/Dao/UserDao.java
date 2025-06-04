package com.example.accountbook.Dao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.accountbook.Entity.User;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

import java.util.Date;

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
        values.put("avatar", "");
        long id = db.insert("user_info", null, values);
        db.close();
        return id;
    }

    // 根据手机号获取用户
    public User getUserByPhone(String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("user_info", null, "phone=?",
                    new String[]{phone}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } finally {
            // 确保无论是否发生异常，都会关闭资源
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
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
        if (rows > 0) {
            return 1; // 更新成功
        } else {
            return 0; // 没有找到匹配记录
        }
    }

    // 删除用户
    public int deleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("user_info", "id=?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rows;
    }

    //根据用户ID获取用户信息
    public User getUserById(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("user_info",
                    null,                   // 所有列
                    "id=?",                 // WHERE条件
                    new String[]{String.valueOf(userId)}, // 参数值
                    null, null, null);      // GROUP BY, HAVING, ORDER BY

            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } finally {
            // 确保资源释放
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    // 根据手机号更新用户头像
    public int updateAvatarByPhone(String phone, String avatar) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("avatar", avatar);
        values.put("update_time", "datetime('now')");
        int rows = db.update("user_info", values, "phone=?",
                new String[]{phone});
        db.close();
        return rows > 0 ? 1 : 0;
    }

    //将查询到的数据转化为user类型
    private User cursorToUser(Cursor cursor) {
        User user = new User();

        // 获取各列索引（避免硬编码列名）
        int idIndex = cursor.getColumnIndex("id");
        int phoneIndex = cursor.getColumnIndex("phone");
        int passwordIndex = cursor.getColumnIndex("password");
        int avatarIndex = cursor.getColumnIndex("avatar");
        int createTimeIndex = cursor.getColumnIndex("create_time");
        int updateTimeIndex = cursor.getColumnIndex("update_time");

        // 设置User属性（检查列是否存在）
        if (idIndex != -1) user.setId(cursor.getInt(idIndex));
        if (phoneIndex != -1) user.setPhone(cursor.getString(phoneIndex));
        if (passwordIndex != -1) user.setPassword(cursor.getString(passwordIndex));
        if (avatarIndex != -1) user.setAvatar(cursor.getString(avatarIndex));

        // 处理时间类型（假设数据库中存储的是时间戳）
        if (createTimeIndex != -1) {
            long createTime = cursor.getLong(createTimeIndex);
            user.setCreateTime(new Date(createTime));
        }
        if (updateTimeIndex != -1) {
            long updateTime = cursor.getLong(updateTimeIndex);
            user.setUpdateTime(new Date(updateTime));
        }

        return user;
    }

}