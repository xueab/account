package com.example.accountbook.Entity;

import java.util.Date;

// 分类
public class Category {
    public static final int TYPE_EXPENSE = 0;  // 支出类型
    public static final int TYPE_INCOME = 1;   // 收入类型

    private long id;
    private String name;   // 分类名称
    private int type;  // 0:支出, 1:收入
    private String icon;   // icon 资源名
    private int userId;
    private Date createTime;
    private Date updateTime;

    public Category() {
    }

    public Category(String name, int type, String icon, int userId) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.userId = userId;
    }

    public Category(long id, String name, int type, String icon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }


    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", icon='" + icon + '\'' +
                ", userId=" + userId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}