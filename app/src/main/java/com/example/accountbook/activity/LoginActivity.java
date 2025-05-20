package com.example.accountbook.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accountbook.MainActivity;
import com.example.accountbook.R;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etAccount, etPassword, etVerification;
    private Button btnLogin, btnGetVerification;
    private CheckBox cbRemember;
    private TextView tvSwitchLogin, tvRegister;
    private LinearLayout layoutPassword, layoutVerification;

    private boolean isPasswordLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        etVerification = findViewById(R.id.et_verification);
        btnLogin = findViewById(R.id.btn_login);
        btnGetVerification = findViewById(R.id.btn_get_verification);
        cbRemember = findViewById(R.id.cb_remember);
        tvSwitchLogin = findViewById(R.id.tv_switch_login);
        tvRegister = findViewById(R.id.tv_register);
        layoutPassword = findViewById(R.id.layout_password);
        layoutVerification = findViewById(R.id.layout_verification);
    }

    private void setupListeners() {
        tvSwitchLogin.setOnClickListener(v -> switchLoginMethod());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            if (account.isEmpty()) {
                Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isPasswordLogin) {
                String password = etPassword.getText().toString().trim();
                if (password.isEmpty()) {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 执行密码登录逻辑
                loginWithPassword(account, password);
            } else {
                String verification = etVerification.getText().toString().trim();
                if (verification.isEmpty()) {
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 执行验证码登录逻辑
                loginWithVerification(account, verification);
            }
        });

        btnGetVerification.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            if (account.isEmpty()) {
                Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
                return;
            }
            // 获取验证码逻辑
            getVerificationCode(account);
        });
    }

    private void switchLoginMethod() {
        isPasswordLogin = !isPasswordLogin;
        if (isPasswordLogin) {
            layoutPassword.setVisibility(View.VISIBLE);
            layoutVerification.setVisibility(View.GONE);
            tvSwitchLogin.setText("其他方式登陆");
        } else {
            layoutPassword.setVisibility(View.GONE);
            layoutVerification.setVisibility(View.VISIBLE);
            tvSwitchLogin.setText("密码登陆");
        }
    }

    //TODO 登陆密码校验
    private void loginWithPassword(String account, String password) {
        // 模拟登录成功
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    //TODO 验证码登陆校验
    private void loginWithVerification(String account, String verification) {
        // 模拟登录成功
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    //TODO 获取验证码
    //
    private void getVerificationCode(String account) {
        // 模拟获取验证码
        Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();

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

    //TODO 记住密码
}