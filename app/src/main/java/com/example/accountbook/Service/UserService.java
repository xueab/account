package com.example.accountbook.Service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.accountbook.Dao.UserDao;
import com.example.accountbook.Entity.User;
import com.example.accountbook.MainActivity;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

import static com.example.accountbook.Utils.SmsUtil.isVerificationCodeValid;

public class UserService {
    private UserDao userDao;
    private Context context;

    public UserService(Context context) {
        this.context = context;
        this.userDao = new UserDao(context);
    }

    /**
     * 用户注册逻辑
     * @param phone 手机号
     * @param password 明文密码
     * @return 注册成功返回用户ID，失败返回-1
     */
    public long register(String phone, String password) {
        // 1. 检查手机号是否已存在
        if (userDao.getUserByPhone(phone) != null) {
            Toast.makeText(context, "该手机号已注册", Toast.LENGTH_SHORT).show();
            return -1;
        }
        // 2. SM3加密
        String encryptedPassword = sm3Hash(password);
        // 3. 添加用户到数据库
        long userId = userDao.addUser(phone, encryptedPassword);
        if (userId == -1) {
            Toast.makeText(context, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            return -1;
        }
        // 4. 注册成功处理
        Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show();
        // 5. 跳转到主页面并传递用户ID
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("USER_ID", userId);
        context.startActivity(intent);

        return userId;
    }

    /**
     * 密码登录校验
     * @param phone 手机号
     * @param password 用户输入的密码
     * @return 用户Id
     */
    public long loginWithPassword(String phone, String password) {
        if(phone.length()!= 11){
            Toast.makeText(context, "账号格式不合规", Toast.LENGTH_SHORT).show();
            return -1;
        }
        // 1. 检查手机号是否存在
        User user = userDao.getUserByPhone(phone);
        if (user == null) {
            Toast.makeText(context, "手机号未注册", Toast.LENGTH_SHORT).show();
            return -1;
        }

        // 2. SM3加密用户输入的密码
        String encryptedInput = sm3Hash(password);

        // 3. 比对数据库中的密码哈希值
        if (encryptedInput.equals(user.getPassword())) {
            return user.getId();
        } else {
            Toast.makeText(context, "账号或密码错误", Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    /**
     * 验证码登录校验
     * @param inputPhone 手机号
     * @param inputCode 用户输入的验证码
     * @return 是否登录成功
     */
    public long loginWithVerification(String inputPhone, String inputCode) {
        if(inputPhone.length()!= 11){
            Toast.makeText(context, "账号格式不合规", Toast.LENGTH_SHORT).show();
            return -1;
        }

        // 1. 检验验证码
        if(!isVerificationCodeValid(context,inputPhone,inputCode)){
            Toast.makeText(context, "验证码错误或已失效", Toast.LENGTH_SHORT).show();
            return -1;
        }

        // 2. 检验手机号是否注册
        User user = userDao.getUserByPhone(inputPhone);
        if (user == null) {
            Toast.makeText(context, "手机号未注册", Toast.LENGTH_SHORT).show();
            return -1;
        }

        return user.getId();
    }

    //SM3加密
    private String sm3Hash(String input) {
        SM3Digest digest = new SM3Digest();
        byte[] inputBytes = input.getBytes();
        digest.update(inputBytes, 0, inputBytes.length);

        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);

        return Hex.toHexString(hash);
    }

    /**
     * 修改密码逻辑
     * @param phone 手机号
     * @param newPassword 新密码(明文)
     * @return 修改是否成功
     */
    public boolean changePassword(String phone, String newPassword) {
        // 1. 检查手机号格式
        if (phone.length() != 11) {
            Toast.makeText(context, "账号格式不合规", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. 检查手机号是否已注册
        User user = userDao.getUserByPhone(phone);
        if (user == null) {
            Toast.makeText(context, "该手机号未注册", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 3. SM3加密新密码
        String encryptedPassword = sm3Hash(newPassword);

        // 4. 更新数据库中的密码
        return userDao.updatePassword(phone, encryptedPassword) == 1;
    }
}