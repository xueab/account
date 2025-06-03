package com.example.accountbook.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import com.example.accountbook.Dao.UserDao;
import com.example.accountbook.Entity.User;
import com.example.accountbook.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private Long userId;
    private CircleImageView ivAvatar;
    private TextView tvPhone;
    private static final int PICK_IMAGE_REQUEST_1 = 12;//从相册选取图片
    private static final int PERMISSION_REQUEST_CODE = 100; // 可以是任意唯一整数值
    private UserDao userDao;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // 从Intent获取用户ID
        userId = getIntent().getLongExtra("USER_ID",-1);
        if (userId == -1) {
            Toast.makeText(this, "用户信息获取失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userDao = new UserDao(this);
        // 初始化视图
        initViews();
        // 加载用户数据
        loadUserData();
        // 设置点击事件
        setupClickListeners();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        tvPhone = findViewById(R.id.tv_phone);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    //加载用户数据
    private void loadUserData() {
        user = userDao.getUserById(userId);
        if(user == null){
            Toast.makeText(this,"用户不存在",Toast.LENGTH_LONG).show();
            return;
        }
        tvPhone.setText(user.getPhone());
        // 加载头像
        loadAvatar();
    }

    private void loadAvatar() {
        if(user.getAvatar() == null){
            //显示默认头像
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            return;
        }
        // 显示用户头像
        loadUserAvatar(user.getAvatar());
    }

    private void loadUserAvatar(String avatarUri) {
        // 检查URI是否有效
        if (avatarUri == null || avatarUri.isEmpty()) {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            return;
        }

        // 处理文件路径的情况
        if (avatarUri.startsWith("/")) {
            // 本地文件路径
            File file = new File(avatarUri);
            if (file.exists()) {
                ivAvatar.setImageURI(Uri.fromFile(file));
                return;
            }
        }

        // 处理content://或file:// URI
        if (avatarUri.startsWith("content://") || avatarUri.startsWith("file://")) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(),
                        Uri.parse(avatarUri)
                );
                ivAvatar.setImageBitmap(bitmap);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 默认情况
        ivAvatar.setImageResource(R.drawable.ic_default_avatar);
    }

    private void setupClickListeners() {
        // 更改头像
        findViewById(R.id.tv_change_avatar).setOnClickListener(v -> {
            // 打开相册或相机选择头像
            openImagePicker();
        });

        // 修改密码
        findViewById(R.id.layout_change_password).setOnClickListener(v -> {
            // 跳转到修改密码页面
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // 退出登录
        findViewById(R.id.layout_logout).setOnClickListener(v -> {
            // 显示确认对话框
            showLogoutConfirmationDialog();
        });
    }

    private void openImagePicker() {

        // 使用系统相册选择图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST_1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // 1. 显示选择的图片
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(),
                            selectedImageUri
                    );
                    ivAvatar.setImageBitmap(bitmap);

                    // 2. 保存头像到本地存储
                    String savedAvatarPath = saveAvatarToLocal(bitmap);

                    // 3. 更新用户对象的头像URI
                    user.setAvatar(savedAvatarPath);

                    // 4. 更新数据库中的头像信息
                    updateUserAvatarInDatabase(savedAvatarPath);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String saveAvatarToLocal(Bitmap bitmap) {
        // 创建头像保存目录（如果不存在）
        File avatarDir = new File(getFilesDir(), "avatars");
        if (!avatarDir.exists()) {
            avatarDir.mkdirs();
        }

        // 创建唯一的头像文件名
        String fileName = "avatar_" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
        File avatarFile = new File(avatarDir, fileName);

        // 保存Bitmap到文件
        try (FileOutputStream out = new FileOutputStream(avatarFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            // 返回file://格式的URI
            return Uri.fromFile(avatarFile).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateUserAvatarInDatabase(String avatarPath) {
        // 使用UserDao更新数据库
        UserDao userDao = new UserDao(this);
        int result = userDao.updateAvatarByPhone(user.getPhone(), avatarPath);

        if (result > 0) {
            Toast.makeText(this, "头像更新成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "头像更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认退出")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 执行退出登录操作
                    performLogout();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void performLogout() {
        // 跳转到登录页面
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);//清除任务栈中的所有Activity
        startActivity(intent);
        finish();
    }
}