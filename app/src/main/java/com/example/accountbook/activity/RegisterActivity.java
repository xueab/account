package com.example.accountbook.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accountbook.MainActivity;
import com.example.accountbook.R;
import com.example.accountbook.Service.UserService;
import com.example.accountbook.Utils.SmsUtil;

import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.example.accountbook.Utils.SmsUtil.isVerificationCodeValid;

public class RegisterActivity extends AppCompatActivity {

    private EditText etAccount, etVerification, etPassword, etConfirmPassword;
    private Button btnRegister, btnGetVerification;
    private TextView tvLogin,tvStrengthLevel,tvPasswordMatch;;
    private View viewStrengthLow, viewStrengthMedium, viewStrengthHigh;
    private static final int SMS_PERMISSION_CODE = 11;
    private LinearLayout layoutPasswordStrengthIndicator;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
        userService = new UserService(this);
    }

    private void initViews() {
        etAccount = findViewById(R.id.et_account);
        etVerification = findViewById(R.id.et_verification);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        btnGetVerification = findViewById(R.id.btn_get_verification);
        tvLogin = findViewById(R.id.tv_login);
        // 密码强度指示器视图
        layoutPasswordStrengthIndicator = findViewById(R.id.password_strength_indicator);
        tvStrengthLevel = findViewById(R.id.tv_strength_level);
        viewStrengthLow = findViewById(R.id.view_strength_low);
        viewStrengthMedium = findViewById(R.id.view_strength_medium);
        viewStrengthHigh = findViewById(R.id.view_strength_high);
        //密码匹配视图
        tvPasswordMatch = findViewById(R.id.tv_password_match);
    }

    private void setupListeners() {
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
            getVerificationCode(account);
        });

        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String verification = etVerification.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInputs(account, verification, password, confirmPassword)) {
                register(account, password);
            }
        });

        // 密码输入监听，实时检测密码强度
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordStrengthIndicator(s.toString());
                checkPasswordMatch();//一致性检验
            }
        });

        //确认匹配输入监听
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkPasswordMatch();//一致性检验
            }
        });
    }

    // 更新密码强度指示器
    private void updatePasswordStrengthIndicator(String password) {
        if (TextUtils.isEmpty(password)) {
            layoutPasswordStrengthIndicator.setVisibility(View.GONE);
            return;
        }
        int strength = calculatePasswordStrength(password);
        layoutPasswordStrengthIndicator.setVisibility(View.VISIBLE);
        switch (strength) {
            case 0: // 弱
                tvStrengthLevel.setText("低");
                tvStrengthLevel.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                viewStrengthLow.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                viewStrengthMedium.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                viewStrengthHigh.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                break;
            case 1: // 中
                tvStrengthLevel.setText("中");
                tvStrengthLevel.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                viewStrengthLow.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                viewStrengthMedium.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                viewStrengthHigh.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                break;
            case 2: // 强
                tvStrengthLevel.setText("高");
                tvStrengthLevel.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                viewStrengthLow.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                viewStrengthMedium.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                viewStrengthHigh.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
        }
    }

    // 计算密码强度 (0:弱, 1:中, 2:强)
    private int calculatePasswordStrength(String password) {
        int strength = 0;

        // 长度至少6位
        if (password.length() < 6) return 0;

        // 包含数字
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        // 包含小写字母
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        // 包含大写字母
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        // 包含特殊字符
        boolean hasSpecial = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();

        // 计算满足的条件数量
        int conditionsMet = 0;
        if (hasDigit) conditionsMet++;
        if (hasLower) conditionsMet++;
        if (hasUpper) conditionsMet++;
        if (hasSpecial) conditionsMet++;

        // 根据条件数量确定强度
        if (password.length() >= 8 && conditionsMet >= 3) {
            strength = 2; // 强
        } else if (password.length() >= 6 && conditionsMet >= 2) {
            strength = 1; // 中
        } else {
            strength = 0; // 弱
        }

        return strength;
    }

    // 检查两次密码是否一致的方法
    private void checkPasswordMatch() {
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            tvPasswordMatch.setVisibility(View.GONE);
            return;
        }

        if (password.equals(confirmPassword)) {
            tvPasswordMatch.setVisibility(View.GONE);
        } else {
            tvPasswordMatch.setVisibility(View.VISIBLE);
        }
    }
    private boolean validateInputs(String account, String verification,
                                   String password, String confirmPassword) {
        if (account.isEmpty()) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(account.length()!= 11){
            Toast.makeText(this, "账号格式不合规", Toast.LENGTH_SHORT).show();
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

        return isVerificationCodeValid(this, account, verification);
    }

    private void getVerificationCode(String account) {
        // 获取验证码
        SmsUtil.sendVerificationCode(this, account);
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


    //注册功能
    private void register(String account, String password) {
        userService.register(account,password);
    }
}