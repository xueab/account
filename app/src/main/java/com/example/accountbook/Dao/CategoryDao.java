package com.example.accountbook.Dao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.accountbook.Entity.Category;

public class CategoryDao {
    private DatabaseHelper dbHelper;

    public CategoryDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 添加类别
    public long addCategory(String name, int type, String icon, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("type", type);
        values.put("icon", icon);
        values.put("user_id", userId);
        long id = db.insert("category", null, values);
        db.close();
        return id;
    }

    // 获取用户的所有类别
    public Cursor getCategoriesByUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query("category", null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, "name ASC");
    }

    // 获取特定类型的类别
    public Cursor getCategoriesByType(int type) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query("category", null, "type=?",
                new String[]{String.valueOf(type)},
                null, null, "name ASC");
    }

    //获取指定类别id的代码
    public Cursor getCategoryCursorById(long categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(
                "category",
                null,
                "id=?",
                new String[]{String.valueOf(categoryId)},
                null, null, null
        );
    }

    // 更新类别
    public int updateCategory(long categoryId, String name, String icon) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("icon", icon);
        values.put("update_time", "datetime('now')");
        int rows = db.update("category", values, "id=?",
                new String[]{String.valueOf(categoryId)});
        db.close();
        return rows;
    }

    // 删除类别
    public int deleteCategory(long categoryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("category", "id=?",
                new String[]{String.valueOf(categoryId)});
        db.close();
        return rows;
    }
}