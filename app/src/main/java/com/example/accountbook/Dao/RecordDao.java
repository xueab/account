package com.example.accountbook.Dao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class RecordDao {
    private DatabaseHelper dbHelper;

    public RecordDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 添加记录
    public long addRecord(double amount, int type, long categoryId,
                          String remark, String date, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("type", type);
        values.put("category_id", categoryId);
        values.put("remark", remark);
        values.put("date", date);
        values.put("user_id", userId);
        long id = db.insert("record", null, values);
        db.close();
        return id;
    }

    // 获取用户的所有记录
    public Cursor getRecordsByUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query("record", null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, "date DESC, time DESC");
    }

    // 获取特定日期的记录
    public Cursor getRecordsByDate(long userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query("record", null, "user_id=? AND date=?",
                new String[]{String.valueOf(userId), date},
                null, null, "time DESC");
    }

    // 获取日期范围内的记录
    public Cursor getRecordsByDateRange(long userId, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query("record", null,
                "user_id=? AND date BETWEEN ? AND ?",
                new String[]{String.valueOf(userId), startDate, endDate},
                null, null, "date DESC, time DESC");
    }

    // 更新记录
    public int updateRecord(long recordId, double amount, long categoryId,
                            String remark, String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("category_id", categoryId);
        values.put("remark", remark);
        values.put("date", date);
        values.put("update_time", "datetime('now')");
        int rows = db.update("record", values, "id=?",
                new String[]{String.valueOf(recordId)});
        db.close();
        return rows;
    }

    // 删除记录
    public int deleteRecord(long recordId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("record", "id=?",
                new String[]{String.valueOf(recordId)});
        db.close();
        return rows;
    }

    // 获取某月的收支统计
    public Cursor getMonthlySummary(long userId, String yearMonth) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT type, SUM(amount) as total " +
                "FROM record " +
                "WHERE user_id=? AND strftime('%Y-%m', date)=? " +
                "GROUP BY type";
        return db.rawQuery(sql, new String[]{String.valueOf(userId), yearMonth});
    }

    // 获取某月的分类统计
    public Cursor getCategorySummary(long userId, String yearMonth, int type) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT c.name, SUM(r.amount) as total " +
                "FROM record r JOIN category c ON r.category_id=c.id " +
                "WHERE r.user_id=? AND strftime('%Y-%m', r.date)=? AND r.type=? " +
                "GROUP BY c.name " +
                "ORDER BY total DESC";
        return db.rawQuery(sql, new String[]{String.valueOf(userId), yearMonth, String.valueOf(type)});
    }
}