package com.example.accountbook.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountbook.Dao.CategoryDao;
import com.example.accountbook.Dao.RecordDao;
import com.example.accountbook.Entity.Record;
import com.example.accountbook.R;
import com.example.accountbook.Service.OCRProcessingService;
import com.example.accountbook.Utils.DateUtils;
import com.example.accountbook.activity.EditRecordActivity;
import com.example.accountbook.activity.LoginActivity;
import com.example.accountbook.adapter.RecordAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements RecordAdapter.OnRecordClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_EDIT_RECORD = 2;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
    private static final SimpleDateFormat DAY_OF_WEEK_FORMAT = new SimpleDateFormat("E", Locale.getDefault());
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d", Locale.getDefault());

    private TextView tvMonthTitle;
    private HorizontalScrollView scrollDatePicker;
    private LinearLayout layoutDateContainer;
    private TextView tvTotalExpense;
    private TextView tvTotalIncome;
    private RecyclerView rvRecords;
    private View layoutEmpty;
    private Calendar selectedCalendar;
    private RecordAdapter recordAdapter;
    private List<Record> recordList = new ArrayList<>();
    private RecordDao recordDao;
    private CategoryDao categoryDao;
    private int currentUserId = 1; // 假设当前用户ID为1，实际应从登录信息获取
    private long userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordDao = new RecordDao(requireContext());
        categoryDao = new CategoryDao(requireContext());
        selectedCalendar = Calendar.getInstance();
        if (getArguments() != null) {
            userId = getArguments().getLong("USER_ID", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();
        setupDateSelector();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecordsForSelectedDate();
    }

    private void initViews(View view) {
        tvMonthTitle = view.findViewById(R.id.tv_month_title);
        scrollDatePicker = view.findViewById(R.id.scroll_date_picker);
        layoutDateContainer = view.findViewById(R.id.layout_date_container);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        rvRecords = view.findViewById(R.id.rv_records);
        layoutEmpty = view.findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        recordAdapter = new RecordAdapter(recordList, this);
        rvRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecords.setAdapter(recordAdapter);
    }

    private void setupDateSelector() {
        updateDateSelector();

        // 设置滚动监听，确保选中日期居中
        scrollDatePicker.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            scrollToSelectedDate();
        });
    }

    private void updateDateSelector() {
        layoutDateContainer.removeAllViews();

        // 设置月份标题
        tvMonthTitle.setText(MONTH_FORMAT.format(selectedCalendar.getTime()));

        // 添加7个日期项（当前选中日期的前3天和后3天）
        Calendar tempCalendar = (Calendar) selectedCalendar.clone();
        tempCalendar.add(Calendar.DAY_OF_MONTH, -3); // 从选中日期的前3天开始,下方开始处理的第一项

        for (int i = 0; i < 7; i++) {
            View dateItem = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_date_picker, layoutDateContainer, false);

            TextView tvDayOfWeek = dateItem.findViewById(R.id.tv_day_of_week);
            TextView tvDay = dateItem.findViewById(R.id.tv_day);
            View indicator = dateItem.findViewById(R.id.indicator);

            // 设置日期文本
            tvDayOfWeek.setText(DAY_OF_WEEK_FORMAT.format(tempCalendar.getTime()));
            tvDay.setText(DAY_FORMAT.format(tempCalendar.getTime()));

            // 高亮选中日期
            boolean isSelected = isSameDay(tempCalendar, selectedCalendar);
            tvDayOfWeek.setTextColor(ContextCompat.getColor(getContext(),
                    isSelected ? R.color.colorPrimary : R.color.colorSurfaceVariant));
            tvDay.setTextColor(ContextCompat.getColor(getContext(),
                    isSelected ? R.color.colorPrimary : R.color.colorSurface));
            indicator.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

            // 设置点击事件
            final Calendar clickCalendar = (Calendar) tempCalendar.clone();
            dateItem.setOnClickListener(v -> {//点击后自动切换到中间的原因:七项全部重新加载,当前日期在中间
                selectedCalendar = clickCalendar;
                updateDateSelector();
                loadRecordsForSelectedDate();
            });

            // 设置长按事件
            dateItem.setOnLongClickListener(v -> {
                showDatePickerDialog();
                return true;
            });

            layoutDateContainer.addView(dateItem);
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void scrollToSelectedDate() {
        // 计算选中日期的位置并滚动到中间
        int scrollTo = (layoutDateContainer.getWidth() - scrollDatePicker.getWidth()) / 2;
        scrollDatePicker.smoothScrollTo(scrollTo, 0);
    }

    private void showDatePickerDialog() {
        // 创建日期选择器对话框
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // 用户选择日期后的回调
                    Calendar newCalendar = Calendar.getInstance();
                    newCalendar.set(year, month, dayOfMonth);

                    // 更新选中日期
                    selectedCalendar = newCalendar;

                    // 重新加载日期选择器和数据
                    updateDateSelector();
                    loadRecordsForSelectedDate();

                    // 确保滚动到选中日期
                    scrollToSelectedDate();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // 显示对话框
        datePickerDialog.show();
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void loadRecordsForSelectedDate() {
        String dateStr = DATE_FORMAT.format(selectedCalendar.getTime());

        // 使用RecordDao获取数据
        Cursor cursor = recordDao.getRecordsByDate(currentUserId, dateStr);
        recordList.clear();

        try {
            while (cursor != null && cursor.moveToNext()) {
                Record record = new Record();
                record.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                record.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                record.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
                record.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                record.setRemark(cursor.getString(cursor.getColumnIndexOrThrow("remark")));
                record.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));

                //根据类别id获取类别名称和编号
                Cursor categoryCursor = categoryDao.getCategoryCursorById(record.getCategoryId());
                try {
                    if (categoryCursor != null && categoryCursor.moveToFirst()) {
                        record.setCategoryName(categoryCursor.getString(
                                categoryCursor.getColumnIndexOrThrow("name")));
                        record.setCategoryIcon(categoryCursor.getString(
                                categoryCursor.getColumnIndexOrThrow("icon")));
                    }
                } finally {
                    if (categoryCursor != null) {
                        categoryCursor.close();
                    }
                }
                try {
                    record.setTime(cursor.getString(cursor.getColumnIndexOrThrow("time")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                recordList.add(record);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        boolean isEmpty = recordList.isEmpty();

        rvRecords.setVisibility(isEmpty ? View.GONE : View.VISIBLE); // 列表视图
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE); // 空状态视图

        recordAdapter.notifyDataSetChanged();
        updateTotals();
    }

    private void updateTotals() {
        double totalExpense = 0;
        double totalIncome = 0;

        for (Record record : recordList) {
            if (record.getType() == Record.TYPE_EXPENSE) {
                totalExpense += record.getAmount();
            } else {
                totalIncome += record.getAmount();
            }
        }

        tvTotalExpense.setText(String.format("¥%.2f", totalExpense));
        tvTotalIncome.setText(String.format("¥%.2f", totalIncome));
    }

    @Override
    public void onRecordClick(int position) {
        Record record = recordList.get(position);
        Intent intent = new Intent(getActivity(), EditRecordActivity.class);
        intent.putExtra("record_id", record.getId());
        intent.putExtra("USER_ID", userId);
        startActivityForResult(intent, REQUEST_EDIT_RECORD);
    }

//    //从EditRecordActivity返回时触发
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQUEST_EDIT_RECORD && resultCode == Activity.RESULT_OK) {
//            // 记录被修改，刷新列表
//            loadRecordsForSelectedDate();
//        }
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recordDao != null) {
            // 清理资源
        }
    }
}