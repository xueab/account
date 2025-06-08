package com.example.accountbook.Service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.accountbook.Entity.Record;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRProcessingService extends Service {
    private static final String TAG = "OCRProcessingService";
    private OCRService ocrService;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // 确保在Application类中调用了OCRService.init()
            ocrService = OCRService.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "初始化OCR服务失败", e);
            sendBroadcast(new Intent("OCR_RESULT_FAILED")
                    .putExtra("error", "OCR服务初始化失败: " + e.getMessage()));
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            String imageUriString = intent.getStringExtra("image_uri");
            if (imageUriString != null) {
                processImageFromUri(Uri.parse(imageUriString));
            } else {
                Log.e(TAG, "未接收到有效的图片URI");
                sendBroadcast(new Intent("OCR_RESULT_FAILED")
                        .putExtra("error", "未接收到有效的图片URI"));
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    private void processImageFromUri(Uri imageUri) {
        new Thread(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
                if (inputStream == null) {
                    throw new Exception("无法从URI获取输入流");
                }
                if (ocrService == null) {
                    throw new Exception("OCR服务未正确初始化");
                }

                String ocrResult = ocrService.recognizeByBytes(inputStream);
                Log.d(TAG, "OCR识别结果: " + ocrResult);

                if (ocrResult != null && !ocrResult.isEmpty()) {
                    // 直接传递原始JSON字符串
                    Intent successIntent = new Intent("OCR_RESULT_READY");
                    successIntent.putExtra("ocr_json", ocrResult);
                    successIntent.setPackage(getPackageName());  // 添加这行从隐式广播转为显式广播
                    sendBroadcast(successIntent);
                    Log.d(TAG, "OCR结果广播已发送");
                } else {
                    Intent errorIntent = new Intent("OCR_RESULT_FAILED");
                    errorIntent.putExtra("error", "OCR识别返回空结果");
                    errorIntent.setPackage(getPackageName());  // 添加包名转为显式广播
                    sendBroadcast(errorIntent);
                    Log.d(TAG, "OCR失败广播已发送");
                }
            } catch (Exception e) {
                Log.e(TAG, "OCR处理异常", e);
                String errorMsg = "OCR处理失败";
                Intent errorIntent = new Intent("OCR_RESULT_FAILED");
                errorIntent.putExtra("error", errorMsg);
                errorIntent.setPackage(getPackageName());  // 添加包名转为显式广播
                sendBroadcast(errorIntent);
                Log.d(TAG, "OCR失败广播已发送");
            } finally {
                // 确保广播发送完成后再停止服务
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    stopSelf();
                }, 1000); // 延迟1秒停止服务
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}