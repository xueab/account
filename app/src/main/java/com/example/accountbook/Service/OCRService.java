package com.example.accountbook.Service;

import android.content.Context;
import android.util.Log;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeBasicRequest;
import com.aliyun.ocr_api20210707.models.RecognizeBasicResponse;
import com.aliyun.ocr_api20210707.models.RecognizePaymentRecordRequest;
import com.aliyun.ocr_api20210707.models.RecognizePaymentRecordResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class OCRService {
    private static final String TAG = "OCRService";
    private Client ocrClient;
    private static OCRService instance;
    private static Context appContext;

    // 私有构造函数
    private OCRService() throws Exception {
        ocrClient = createClient();
        if (ocrClient == null) {
            throw new Exception("Failed to initialize OCR client");
        }
    }

    // 初始化方法（非必须调用）
    public static synchronized void init(Context context) {
        appContext = context.getApplicationContext();
    }

    // 获取单例（自动初始化）
    public static synchronized OCRService getInstance() throws Exception {
        if (instance == null) {
            if (appContext == null) {
                throw new IllegalStateException("Application context not set. Call OCRService.init() first or ensure your Application class is properly initialized.");
            }
            instance = new OCRService();
        }
        return instance;
    }

    // 初始化OCR客户端
    private Client createClient() throws Exception {
        try {
            Config config = new Config();
//                    .setAccessKeyId("")
//                    .setAccessKeySecret("");
            config.endpoint = "ocr-api.cn-hangzhou.aliyuncs.com";
            return new Client(config);
        } catch (Exception e) {
            Log.e(TAG, "创建OCR客户端失败", e);
            throw new Exception("Failed to create OCR client: " + e.getMessage());
        }
    }

    public String recognizeByBytes(InputStream inputStream) throws Exception {
        if (ocrClient == null) {
            throw new IllegalStateException("OCR client not initialized");
        }

        try {
            RecognizePaymentRecordRequest request = new RecognizePaymentRecordRequest()
                    .setBody(inputStream);
            RecognizePaymentRecordResponse response = ocrClient.recognizePaymentRecordWithOptions(request, new RuntimeOptions());
            return response.getBody().getData();
        } catch (TeaException error) {
            handleTeaException(error);
            throw new Exception("OCR识别错误: " + error.getMessage());
        } catch (IOException e) {
            throw new Exception("图片读取失败: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("OCR处理异常: " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "关闭输入流失败", e);
            }
        }
    }

    private void handleTeaException(TeaException error) {
        Map<String, Object> errorData = error.getData();
        String recommend = errorData != null ? (String) errorData.get("Recommend") : null;

        Log.e(TAG, "OCR识别错误: " + error.getMessage());
        if (recommend != null) {
            Log.e(TAG, "诊断地址: " + recommend);
        }
    }
}