package com.example.accountbook;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accountbook.Dao.UserDao;
import com.example.accountbook.Entity.User;
import com.example.accountbook.activity.AddRecordActivity;
import com.example.accountbook.activity.LoginActivity;
import com.example.accountbook.activity.UserProfileActivity;
import com.example.accountbook.fragment.HomeFragment;
import com.example.accountbook.fragment.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    // 用户头像按钮
    private CircleImageView btnUserProfile;
    //顶部标题
    private TextView tvTitle;
    // 底部导航栏
    private BottomNavigationView bottomNavigationView;
    // 首页统计页面
    private Fragment homeFragment, statsFragment;
    //用户id
    private Long userId;
    private UserDao userDao;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userId = getIntent().getLongExtra("USER_ID",-1);
        userDao = new UserDao(this);
        initView();
        setupBottomNavigation();
        setupListeners();
        if(userId == -1){
            Toast.makeText(this,"用户信息不存在",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        // 默认显示首页
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, homeFragment)
                .commit();
        tvTitle.setText("首页");
        //获取用户
        user = userDao.getUserById(userId);
        //加载用户头像
        loadUserAvatar(user.getAvatar() == null ? user.getAvatar() : "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当Activity从后台返回前台时，重新加载用户头像
        if (userId != -1) {
            user = userDao.getUserById(userId); // 重新获取用户数据
            loadUserAvatar(user.getAvatar());   // 重新加载头像
        }
    }

    private void loadUserAvatar(String avatarUri) {
        // 检查URI是否有效
        if (avatarUri == null || avatarUri.isEmpty()) {
            btnUserProfile.setImageResource(R.drawable.ic_default_avatar);
            return;
        }

//        // 处理文件路径的情况
//        if (avatarUri.startsWith("/")) {
//            // 本地文件路径
//            File file = new File(avatarUri);
//            if (file.exists()) {
//                btnUserProfile.setImageURI(Uri.fromFile(file));
//                return;
//            }
//        }

        // 处理content://或file:// URI
        if (avatarUri.startsWith("content://") || avatarUri.startsWith("file://")) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(),
                        Uri.parse(avatarUri)
                );
                btnUserProfile.setImageBitmap(bitmap);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 默认情况
        btnUserProfile.setImageResource(R.drawable.ic_default_avatar);
    }

    private void setupListeners() {
        // 用户头像点击事件
        btnUserProfile.setOnClickListener(v -> {
            // 当用户点击头像按钮时，启动 UserProfileActivity 活动,并传递用户id
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    private void initView(){
        // 初始化用户头像按钮
        btnUserProfile = findViewById(R.id.btn_user_profile);
        tvTitle = findViewById(R.id.tv_title);
        initFragments();
    }
    private void initFragments() {
        // 创建Bundle传递userId
        Bundle bundle = new Bundle();
        bundle.putLong("USER_ID", userId);
        homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        statsFragment = new StatsFragment();
        statsFragment.setArguments(bundle);
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
//        // 设置中间加号按钮的样式
//        // 获取加号按钮的菜单项
//        MenuItem addItem = bottomNavigationView.getMenu().findItem(R.id.nav_add);
//        //放大 1.5 倍，使其在视觉上更突出。
//        SpannableStringBuilder addTitle = new SpannableStringBuilder(addItem.getTitle());
//        addTitle.setSpan(new RelativeSizeSpan(1.5f), 0, addTitle.length(), 0);
//        // 更新加号按钮的标题
//        addItem.setTitle(addTitle);

        // 设置底部导航栏的点击事件监听器
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // 根据 ID 判断点击的是哪个按钮，并执行相应的操作
            int id = item.getItemId();
            // 点击首页按钮
            if (id == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, homeFragment)
                        .commit();
                tvTitle.setText("首页");
                return true;
            } else if (id == R.id.nav_stats) {
                // 点击统计按钮
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, statsFragment)
                        .commit();
                tvTitle.setText("统计");
                return true;
            } else if (id == R.id.nav_add) {
                // 点击加号按钮，启动 AddRecordActivity 并传递 userId
                Intent intent = new Intent(this, AddRecordActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // 移除导航栏项的点击波纹效果
        bottomNavigationView.setItemRippleColor(null);
    }

    // 处理返回键，避免退出应用,如果当前选中的不是首页（R.id.nav_home），则强制跳转到首页。
    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_home) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}