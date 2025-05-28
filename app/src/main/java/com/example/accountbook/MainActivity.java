package com.example.accountbook;


import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.example.accountbook.activity.AddRecordActivity;
import com.example.accountbook.activity.LoginActivity;
import com.example.accountbook.activity.UserProfileActivity;
import com.example.accountbook.fragment.HomeFragment;
import com.example.accountbook.fragment.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    // 用户头像按钮
    private ImageButton btnUserProfile;
    // 底部导航栏
    private BottomNavigationView bottomNavigationView;
    // 首页 统计页面
    private Fragment homeFragment, statsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setupBottomNavigation();
        setupListeners();

        // 默认显示首页
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, homeFragment)
                .commit();
    }

    private void setupListeners() {
        // 用户头像点击事件
        btnUserProfile.setOnClickListener(v -> {
            // 当用户点击头像按钮时，启动 UserProfileActivity 活动
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        });
    }


    private void initView(){
        // 初始化用户头像按钮
        btnUserProfile = findViewById(R.id.btn_user_profile);
        initFragments();
    }
    private void initFragments() {
        homeFragment = new HomeFragment();
        statsFragment = new StatsFragment();
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 设置中间加号按钮的样式
        // 获取加号按钮的菜单项
        MenuItem addItem = bottomNavigationView.getMenu().findItem(R.id.nav_add);
        // 创建可变字符串并设置样式
        SpannableStringBuilder addTitle = new SpannableStringBuilder(addItem.getTitle());
        addTitle.setSpan(new RelativeSizeSpan(1.5f), 0, addTitle.length(), 0);
        // 更新加号按钮的标题
        addItem.setTitle(addTitle);

        // 设置底部导航栏的点击事件监听器
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // 根据 ID 判断点击的是哪个按钮，并执行相应的操作
            int id = item.getItemId();
            // 点击首页按钮
            if (id == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, homeFragment)
                        .commit();
                return true;
            } else if (id == R.id.nav_stats) {
                // 点击统计按钮
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, statsFragment)
                        .commit();
                return true;
            } else if (id == R.id.nav_add) {
                // 点击加号按钮
                startActivity(new Intent(this, AddRecordActivity.class));
                return true;
            }
            return false;
        });

        // 移除导航栏项的点击波纹效果
        bottomNavigationView.setItemRippleColor(null);
    }

    // 处理返回键，避免退出应用
    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_home) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}