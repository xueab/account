package com.example.accountbook.Entity;

import java.time.LocalDateTime;
import java.util.Date;

public class Record {
    public static final int TYPE_EXPENSE = 0;  // 支出类型
    public static final int TYPE_INCOME = 1;   // 收入类型

    private int id;
    private double amount;
    private int type;  // 0:支出, 1:收入
    private int categoryId;
    private String remark;
    private String date;  // 格式: yyyy-MM-dd
    private Date time;
    private int userId;
    private Date createTime;
    private Date updateTime;

    // 非数据库字段，用于显示
    private String categoryName;
    private String categoryIcon;

    public Record() {
    }

    public Record(double amount, int type, int categoryId, String remark, String date, int userId) {
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.remark = remark;
        this.date = date;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", amount=" + amount +
                ", type=" + type +
                ", categoryId=" + categoryId +
                ", remark='" + remark + '\'' +
                ", date='" + date + '\'' +
                ", time=" + time +
                ", userId=" + userId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", categoryName='" + categoryName + '\'' +
                ", categoryIcon='" + categoryIcon + '\'' +
                '}';
    }
}