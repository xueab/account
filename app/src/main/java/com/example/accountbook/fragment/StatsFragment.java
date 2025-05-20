package com.example.accountbook.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.accountbook.R;
import java.util.ArrayList;
import java.util.List;

//TODO 统计页面
public class StatsFragment extends Fragment {

    private TextView tvSummary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        tvSummary = view.findViewById(R.id.tv_summary);

        updateSummary();

        return view;
    }

    private void updateSummary() {
        tvSummary.setText("本月总收入: ¥5000.00\n本月总支出: ¥1200.00");
    }
}