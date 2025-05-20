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

    private ImageButton btnUserProfile;
    private BottomNavigationView bottomNavigationView;
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
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        });
    }


    private void initView(){
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
        MenuItem addItem = bottomNavigationView.getMenu().findItem(R.id.nav_add);
        SpannableStringBuilder addTitle = new SpannableStringBuilder(addItem.getTitle());
        addTitle.setSpan(new RelativeSizeSpan(1.5f), 0, addTitle.length(), 0);
        addItem.setTitle(addTitle);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, homeFragment)
                        .commit();
                return true;
            } else if (id == R.id.nav_stats) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, statsFragment)
                        .commit();
                return true;
            } else if (id == R.id.nav_add) {
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