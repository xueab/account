package com.example.accountbook.Utils;

import android.content.Context;

public class ResourceUtil {
    public static int getDrawableResourceId(Context context, String name) {
        return context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName()
        );
    }

    public static String getCategoryName(String categoryName) {
        // 如果需要，可以在这里添加默认分类名称处理逻辑
        return categoryName != null ? categoryName : "未分类";
    }
}