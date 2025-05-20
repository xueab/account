package com.example.accountbook.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.accountbook.R;

public class UserProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivAvatar;
    private TextView tvUsername, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
        setupListeners();
        loadUserData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        // 这里应该从数据库或网络加载用户数据
        // 模拟数据
        tvUsername.setText("张三");
        tvEmail.setText("zhangsan@example.com");
    }
}