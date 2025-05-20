package com.example.accountbook.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accountbook.MainActivity;
import com.example.accountbook.R;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etAccount, etVerification, etPassword, etConfirmPassword;
    private Button btnRegister, btnGetVerification;
    private ImageButton btnBack;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etAccount = findViewById(R.id.et_account);
        etVerification = findViewById(R.id.et_verification);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        btnGetVerification = findViewById(R.id.btn_get_verification);
        btnBack = findViewById(R.id.btn_back);
        tvLogin = findViewById(R.id.tv_login);
    }

    private void setupListeners() {
        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());

        // 去登录文本点击事件
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // 获取验证码按钮点击事件
        btnGetVerification.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            if (account.isEmpty()) {
                Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
                return;
            }
            getVerificationCode(account);
        });

        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String verification = etVerification.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInputs(account, verification, password, confirmPassword)) {
                register(account, password, verification);
            }
        });
    }

    private boolean validateInputs(String account, String verification,
                                   String password, String confirmPassword) {
        if (account.isEmpty()) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (verification.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    //TODO 获取验证码
    private void getVerificationCode(String account) {
        // 模拟获取验证码
        Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();

        // 并启动倒计时
        startCountDown();
    }

    private void startCountDown() {
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnGetVerification.setText(millisUntilFinished / 1000 + "秒后重试");
                btnGetVerification.setEnabled(false);
            }

            public void onFinish() {
                btnGetVerification.setText("获取验证码");
                btnGetVerification.setEnabled(true);
            }
        }.start();
    }

    //TODO 注册逻辑
    private void register(String account, String password, String verification) {
        // 模拟注册成功
        Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();

        // 实际开发中这里应该调用注册API
        // 注册成功后跳转到主页面
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}