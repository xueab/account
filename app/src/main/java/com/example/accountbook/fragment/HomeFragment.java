package com.example.accountbook.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountbook.Entity.Record;
import com.example.accountbook.R;

import java.util.ArrayList;
import java.util.List;

//TODO 首页页面
public class HomeFragment extends Fragment {

    private RecyclerView rvRecords;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvRecords = view.findViewById(R.id.rv_records);
        rvRecords.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}