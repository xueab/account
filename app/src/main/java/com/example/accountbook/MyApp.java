package com.example.accountbook;

import android.app.Application;

import com.example.accountbook.Service.OCRService;

//集中管理
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OCRService.init(this);
    }
}