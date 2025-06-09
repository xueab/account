package com.example.accountbook.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.example.accountbook.Service.UserService;
import com.example.accountbook.Utils.SmsUtil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText etAccount, etPassword, etVerification;
    private Button btnLogin, btnGetVerification;
    private CheckBox cbRemember;
    private TextView tvSwitchLogin, tvRegister;
    private LinearLayout layoutPassword, layoutVerification;

    private boolean isPasswordLogin = true;
    private static final int SMS_PERMISSION_CODE = 10;

    private SharedPreferences sharedPreferences;
    private UserService userService;
    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        loadSavedCredentials();
        setupListeners();
        userService = new UserService(this);
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
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

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
            if(account.length()!= 11){
                Toast.makeText(this, "账号格式不合规", Toast.LENGTH_SHORT).show();
                return;
            }
            // 检查权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE);
                return;
            }
            // 获取验证码逻辑
            getVerificationCode(account);
        });
    }

    //切换登陆方式
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

    //密码登陆校验
    private void loginWithPassword(String account, String password) {
        Long userId = userService.loginWithPassword(account,password);
        if(userId != -1){
            saveCredentials(account, password, cbRemember.isChecked(),layoutPassword.getVisibility());
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            this.startActivity(intent);
            finish();
        }
    }

    //验证码登陆校验
    private void loginWithVerification(String account, String verification) {
        long userId = userService.loginWithVerification(account,verification);
        if(userId != -1){
            saveCredentials(account, "", false,layoutPassword.getVisibility());
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            this.startActivity(intent);
            finish();
        }
    }

    //加载保存的信息
    private void loadSavedCredentials() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean remember = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        String savedAccount = sharedPreferences.getString(KEY_ACCOUNT, "");
        String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
        etAccount.setText(savedAccount);
        etPassword.setText(savedPassword);
        cbRemember.setChecked(remember);
    }

    //记住密码
    private void saveCredentials(String account, String password, boolean remember,int layoutPassword) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (remember && layoutPassword == View.VISIBLE) {
            editor.putString(KEY_ACCOUNT, account);
            editor.putString(KEY_PASSWORD, password);
            editor.putBoolean(KEY_REMEMBER, remember);
        } else if (layoutPassword == View.VISIBLE) {
            editor.putString(KEY_ACCOUNT, account);
            editor.putString(KEY_PASSWORD, "");
            editor.putBoolean(KEY_REMEMBER, false);
        } else if(layoutPassword == View.GONE){
            editor.putString(KEY_ACCOUNT, account);
            editor.putString(KEY_PASSWORD, "");
            editor.putBoolean(KEY_REMEMBER, false);
        } else {
            editor.clear();  // 如果不记住密码，清除所有保存的数据
        }
        editor.apply();
    }

    // 获取验证码
    private void getVerificationCode(String account) {
        SmsUtil.sendVerificationCode(this, account);
        Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
        startCountDown();
    }

    private void startCountDown() {
        //创建倒计时器
        new CountDownTimer(60000, 1000) {
            //倒计时过程回调
            public void onTick(long millisUntilFinished) {
                btnGetVerification.setText(millisUntilFinished / 1000 + "秒后重试");
                btnGetVerification.setEnabled(false);
            }
            //倒计时过程回调
            public void onFinish() {
                btnGetVerification.setText("获取验证码");
                btnGetVerification.setEnabled(true);
            }
        }.start();
    }
}