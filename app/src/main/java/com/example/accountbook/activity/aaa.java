//package com.example.accountbook.activity;
//
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.example.accountbook.Entity.Record;
//import com.example.accountbook.R;
//import com.example.accountbook.Service.OCRProcessingService;
//
//import java.io.IOException;
//
//public class aaa extends Fragment {
//
//    /*    定义一个请求码用于在startActivityForResult()
//        和onActivityResult()之间进行唯一标识，以便区分不同的Intent请求和返回结果。*/
//    private static final int PICK_IMAGE_REQUEST = 1;
//
//    Button button;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_home, container, false);
//
//        button = view.findViewById(R.id.btn_test);
//        button.setOnClickListener(v -> openGallery());
//
//        return view;
//    }
//
//    private void openGallery() {
//        System.out.println("3333333333333333333333333333333");
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
//            try {
//                Uri selectedImageUri = data.getData();
//                // 启动OCR服务处理图片
//                Intent serviceIntent = new Intent(getActivity(), OCRProcessingService.class);
//                serviceIntent.putExtra("image_uri", selectedImageUri.toString());
//                requireActivity().startService(serviceIntent);
//
//                // 注册广播接收器
//                registerOCRResultReceiver();
//
//                Toast.makeText(getActivity(), "正在识别票据...", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(getActivity(), "图片处理失败", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void registerOCRResultReceiver() {
//        BroadcastReceiver receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if ("OCR_RESULT_READY".equals(intent.getAction())) {
//                    Record record = intent.getParcelableExtra("record");
//                    Toast.makeText(getActivity(),
//                            "识别成功: " + record.getAmount() + "元",
//                            Toast.LENGTH_LONG).show();
//                    // 可以在这里更新UI或做其他处理
//                } else if ("OCR_RESULT_FAILED".equals(intent.getAction())) {
//                    String error = intent.getStringExtra("error");
//                    Toast.makeText(getActivity(),
//                            "识别失败: " + error,
//                            Toast.LENGTH_LONG).show();
//                }
//                // 注销接收器
//                try {
//                    requireActivity().unregisterReceiver(this);
//                } catch (IllegalArgumentException e) {
//                    // 接收器未注册，忽略
//                }
//            }
//        };
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("OCR_RESULT_READY");
//        filter.addAction("OCR_RESULT_FAILED");
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            // Android 13 (API level 33) 及以上版本
//            requireActivity().registerReceiver(
//                    receiver,
//                    filter,
//                    Context.RECEIVER_NOT_EXPORTED
//            );
//        }
//    }
//}
