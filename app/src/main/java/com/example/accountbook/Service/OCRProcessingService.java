package com.example.accountbook.Service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.accountbook.Entity.Record;

import java.io.InputStream;

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

    //外部通过 startService() 启动 Service 时，系统会调用此方法。
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

                if (ocrResult != null) {
                    Record record = parseOCRResult(ocrResult);
                    System.out.println(ocrResult);
                    //sendBroadcast(new Intent("OCR_RESULT_READY").putExtra("record", (CharSequence) record));
                } else {
                    sendBroadcast(new Intent("OCR_RESULT_FAILED")
                            .putExtra("error", "OCR识别返回空结果"));
                }
            } catch (Exception e) {
                Log.e(TAG, "OCR处理异常", e);
                String errorMsg = "OCR处理失败: " + e.getMessage();
                sendBroadcast(new Intent("OCR_RESULT_FAILED")
                        .putExtra("error", errorMsg));
            } finally {
                stopSelf();
            }
        }).start();
    }

    private Record parseOCRResult(String ocrResult) {
        Record record = new Record();
        // 解析逻辑...
        return record;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}