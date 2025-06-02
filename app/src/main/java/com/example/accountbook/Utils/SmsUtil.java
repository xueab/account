package com.example.accountbook.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class SmsUtil {
    public static void sendVerificationCode(Context context, String phoneNumber) {
        String code = generateRandomCode(); // 生成6位随机码

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    "您的验证码是: " + code + "，有效期10分钟",
                    null,
                    null);
            // 保存验证码和过期时间
            saveVerificationCode(context, phoneNumber,code);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private static String generateRandomCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    //保存验证码
    private static void saveVerificationCode(Context context, String phone, String code) {
        SharedPreferences prefs = context.getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("phone",phone)
                .putString("sms_code", code)
                .putLong("code_expiry", System.currentTimeMillis() + 600000) // 10分钟有效期
                .apply();
    }

    //检验验证码是否正确
    public static boolean isVerificationCodeValid(Context context, String inputPhone , String inputCode) {
        // 获取SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);

        // 获取存储的验证码和过期时间
        String phone = prefs.getString("phone",null);
        String storedCode = prefs.getString("sms_code", null);
        long expiryTime = prefs.getLong("code_expiry", 0);

        //检验手机号是否匹配
        if(phone == null || !phone.equals(inputPhone)){
            Toast.makeText(context,"验证码错误或已过期",Toast.LENGTH_SHORT).show();
            return false;
        }
        // 检查验证码是否存在
        if (storedCode == null) {
            Toast.makeText(context,"验证码错误或已过期",Toast.LENGTH_SHORT).show();
            return false;
        }

        // 检查验证码是否过期（当前时间是否超过过期时间）
        if (System.currentTimeMillis() > expiryTime) {
            // 验证码已过期，可以清除存储的数据
            prefs.edit()
                    .remove("phone")
                    .remove("sms_code")
                    .remove("code_expiry")
                    .apply();
            Toast.makeText(context,"验证码错误或已过期",Toast.LENGTH_SHORT).show();
            return false;
        }

        //            prefs.edit()
        //                    .remove("phone")
        //                    .remove("sms_code")
        //                    .remove("code_expiry")
        //                    .apply();
        // 比较输入的验证码和存储的验证码是否一致
        return storedCode.equals(inputCode);
    }

}