package com.example.accountbook.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountbook.Dao.CategoryDao;
import com.example.accountbook.Dao.RecordDao;
import com.example.accountbook.Entity.Category;
import com.example.accountbook.Entity.Record;
import com.example.accountbook.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class StatsFragment extends Fragment {
    private Button btnExpense, btnIncome;

    private TextView tvWeek, tvMonth, tvYear,rank;
    private TextView tvTitle, tvAmount, tvCount, tvMaxAmount;
    private PieChart pieChart;
    private RecyclerView rvRanking;
    private RankingAdapter rankingAdapter;
    private long userId;
    private RecordDao recordDao;
    private CategoryDao categoryDao;
    private List<Category> expenseCategories = new ArrayList<>();  // 支出分类
    private List<Category> incomeCategories = new ArrayList<>();  // 收入分类
    private List<Record> filteredRecords = new ArrayList<>();     // 当前筛选后的记录
    private PopupWindow popupWindow;//悬浮窗


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        recordDao = new RecordDao(requireContext());
        categoryDao = new CategoryDao(requireContext());
        init(view);
        setupListener();
        if (getArguments() != null) {
            userId = getArguments().getLong("USER_ID", -1);
        }

        // 使用AsyncTask或线程池异步加载数据
        new LoadDataTask().execute("week", true);
        return view;
    }

    private class LoadDataTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {//后台线程只执行耗时操作,不能用来更新UI
            String range = (String) params[0];
            boolean isExpense = (boolean) params[1];
            updateDataForRange(range, isExpense);
            return null;
        }
    }

    private void init(View view) {
        // 初始化按钮
        btnExpense = view.findViewById(R.id.btn_expense);
        btnIncome = view.findViewById(R.id.btn_income);

        // 初始化总的支出/收入内容
        tvWeek = view.findViewById(R.id.tv_week);
        tvMonth = view.findViewById(R.id.tv_month);
        tvYear = view.findViewById(R.id.tv_year);

        tvTitle = view.findViewById(R.id.tv_title);
        tvAmount = view.findViewById(R.id.tv_amount);
        tvCount = view.findViewById(R.id.tv_count);
        tvMaxAmount = view.findViewById(R.id.tv_max_amount);

        // 初始化排行榜
        rank = view.findViewById(R.id.rank);
        rvRanking = view.findViewById(R.id.rv_ranking);

        rankingAdapter = new RankingAdapter();
        rvRanking.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRanking.setAdapter(rankingAdapter);

        // 初始化饼状图
        pieChart = view.findViewById(R.id.chart);

        // 默认为周支出数据
        btnExpense.setSelected(true);
        tvWeek.setSelected(true);
        Cursor expenseCursor = categoryDao.getCategoriesByType(Category.TYPE_EXPENSE);
        if (expenseCursor != null) {
            while (expenseCursor.moveToNext()) {
                long id = expenseCursor.getLong(expenseCursor.getColumnIndexOrThrow("id"));
                String name = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("name"));
                String icon = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("icon"));
                expenseCategories.add(new Category(id, name, Category.TYPE_EXPENSE, icon));
            }
            expenseCursor.close();
        }
        // 从数据库获取收入分类
        Cursor incomeCursor = categoryDao.getCategoriesByType(Category.TYPE_INCOME);
        if (incomeCursor != null) {
            while (incomeCursor.moveToNext()) {
                long id = incomeCursor.getLong(incomeCursor.getColumnIndexOrThrow("id"));
                String name = incomeCursor.getString(incomeCursor.getColumnIndexOrThrow("name"));
                String icon = incomeCursor.getString(incomeCursor.getColumnIndexOrThrow("icon"));
                incomeCategories.add(new Category(id, name, Category.TYPE_INCOME, icon));
            }
            incomeCursor.close();
        }

        initPopupWindow();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPopupWindow() {
        if (popupWindow == null) {
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.popup_pie_chart_info, null);

            popupWindow = new PopupWindow(
                    view,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            // 设置悬浮窗动画和背景
            popupWindow.setAnimationStyle(R.style.PopupAnimation);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);


            popupWindow.setOnDismissListener(() -> {
                pieChart.highlightValues(null);
            });

            // 允许外部点击关闭
            popupWindow.setTouchInterceptor((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    clearHighlightAndPopup();
                }
                return false;
            });
        }
    }

    private void setupListener() {
        btnExpense.setOnClickListener(v -> {
            btnExpense.setSelected(true);
            btnIncome.setSelected(false);
            // 保持当前选中的时间范围
            if (tvWeek.isSelected()) {
                new LoadDataTask().execute("week", true);
            } else if (tvMonth.isSelected()) {
                new LoadDataTask().execute("month", true);
            } else {
                new LoadDataTask().execute("year", true);
            }
            rank.setText("总支出排行榜");
        });

        btnIncome.setOnClickListener(v -> {
            btnIncome.setSelected(true);
            btnExpense.setSelected(false);
            // 保持当前选中的时间范围
            if (tvWeek.isSelected()) {
                new LoadDataTask().execute("week", false);
            } else if (tvMonth.isSelected()) {
                new LoadDataTask().execute("month", false);
            } else {
                new LoadDataTask().execute("year", false);
            }
            rank.setText("总收入排行榜");
        });

        tvWeek.setOnClickListener(v -> selectTimeRange(v));
        tvMonth.setOnClickListener(v -> selectTimeRange(v));
        tvYear.setOnClickListener(v -> selectTimeRange(v));
    }


    // 排行榜数据模型
    private static class RankingItem {
        // 图标
        private final int iconRes;
        // 类别
        private final String categoryName;
        // 数量
        private final int count;
        // 占比
        private final float percentage;
        // 金额
        private final float amount;

        private RankingItem(int iconRes, String categoryName, int count, float percentage, float amount) {
            this.iconRes = iconRes;
            this.categoryName = categoryName;
            this.count = count;
            this.percentage = percentage;
            this.amount = amount;
        }

        public int getIconRes() {
            return iconRes;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public int getCount() {
            return count;
        }

        public float getPercentage() {
            return percentage;
        }

        public float getAmount() {
            return amount;
        }

    }

    // RecyclerView Adapter
    private static class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {
        private List<RankingItem> items = new ArrayList<>();

        public void updateData(List<RankingItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ranking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RankingItem item = items.get(position);
            holder.ivIcon.setImageResource(item.getIconRes());
            holder.tvName.setText(item.getCategoryName());
            holder.tvCount.setText(item.getCount() + "笔");
            holder.tvPercent.setText(String.format("%.1f%%", item.getPercentage()));
            holder.tvAmount.setText(String.format("%.1f", item.getAmount()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvName, tvCount, tvPercent, tvAmount;

            ViewHolder(View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_icon);
                tvName = itemView.findViewById(R.id.tv_name);
                tvCount = itemView.findViewById(R.id.tv_count);
                tvPercent = itemView.findViewById(R.id.tv_percent);
                tvAmount = itemView.findViewById(R.id.tv_amount);
            }
        }
    }

    // 根据周, 月, 年显示支出/收入数据
    private void selectTimeRange(View selectedView) {
        // 重置所有按钮状态
        tvWeek.setSelected(false);
        tvMonth.setSelected(false);
        tvYear.setSelected(false);

        // 设置当前选中按钮
        selectedView.setSelected(true);
        // 获取当前是支出还是收入模式
        boolean isExpense = btnExpense.isSelected();

        // 根据选择更新数据
        if (selectedView == tvWeek) {
            new LoadDataTask().execute("week", isExpense);
        } else if (selectedView == tvMonth) {
            new LoadDataTask().execute("month", isExpense);
        } else if (selectedView == tvYear) {
            new LoadDataTask().execute("year", isExpense);
        }
    }

    private void updateDataForRange(String range, boolean isExpense) {
        // 1. 获取时间范围
        String[] dates = getDateRange(range);

        // 2. 查询数据库
        Cursor cursor = recordDao.getRecordsByDateRange(
                userId,
                btnExpense.isSelected() ? 0 : 1,
                dates[0],
                dates[1]
        );

        filteredRecords.clear();
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
                filteredRecords.add(record);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // 3. 计算统计数据
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                calculateAndDisplayStats(range, isExpense);//切换回主线程更新UI,更新UI必须在主线程进行
            }
        });
    }

    //获取指定时间范围的起始和结束日期
    private String[] getDateRange(String range) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        // 结束日期默认为今天
        String endDate = sdf.format(calendar.getTime());

        // 根据范围计算起始日期
        switch (range) {
            case "week":
                calendar.add(Calendar.DAY_OF_YEAR, -6); // 最近7天（包含今天）
                break;
            case "month":
                calendar.add(Calendar.DAY_OF_YEAR, -29); // 最近30天（包含今天）
                break;
            case "year":
                calendar.add(Calendar.DAY_OF_YEAR, -364); // 最近365天（包含今天）
                break;
            default:
                // 默认返回最近一周
                calendar.add(Calendar.DAY_OF_YEAR, -6);
        }

        String startDate = sdf.format(calendar.getTime());
        return new String[]{startDate, endDate};
    }

    private void calculateAndDisplayStats(String range, boolean isExpense) {
        // 更新标题
        tvTitle.setText(isExpense ? "总支出" : "总收入");

        // 计算统计数据
        double totalAmount = 0;
        int recordCount = filteredRecords.size();
        double maxAmount = 0;

        // 按类别分组统计
        Map<Long, CategoryStats> categoryStatsMap = new HashMap<>();

        for (Record record : filteredRecords) {
            double amount = record.getAmount();
            totalAmount += amount;

            // 更新最大金额
            if (amount > maxAmount) {
                maxAmount = amount;
            }

            // 按类别统计
            long categoryId = record.getCategoryId();
            CategoryStats stats = categoryStatsMap.get(categoryId);
            if (stats == null) {
                stats = new CategoryStats();
                stats.categoryId = categoryId;
                stats.categoryName = record.getCategoryName();
                stats.iconRes = getResources().getIdentifier(record.getCategoryIcon(), "drawable", requireContext().getPackageName());
                categoryStatsMap.put(categoryId, stats);
            }
            stats.totalAmount += amount;
            stats.count++;
        }

        // 更新UI显示
        tvAmount.setText(String.format(Locale.getDefault(), "%.1f元", totalAmount));
        tvCount.setText(String.format(Locale.getDefault(), "%d笔", recordCount));
        tvMaxAmount.setText(String.format(Locale.getDefault(), "%.1f元", maxAmount));

        // 更新饼图
        updateChart(range, isExpense, categoryStatsMap);

        // 更新排行榜
        updateRankingList(isExpense, range, categoryStatsMap);
    }

    // 辅助类，用于存储类别统计信息
    private static class CategoryStats {
        long  categoryId;
        String categoryName;
        int iconRes;
        double totalAmount;
        int count;
    }

    // 更新饼图
    private void updateChart(String range, boolean isExpense, Map<Long, CategoryStats> categoryStatsMap) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // 转换统计数据为饼图条目
        for (CategoryStats stats : categoryStatsMap.values()) {
            if (stats.totalAmount > 0) {
                entries.add(new PieEntry((float) stats.totalAmount, stats.categoryName));
            }
        }

        if (entries.isEmpty()) {
            // 如果没有数据，显示提示
            pieChart.clear();
            pieChart.setNoDataText("暂无数据");
            pieChart.invalidate();
            return;
        }

        int[] customColors = new int[] {
                // 基础彩虹色（7种）
                Color.rgb(255, 0, 0),      // 红
                Color.rgb(255, 127, 0),    // 橙
                Color.rgb(255, 255, 0),    // 黄
                Color.rgb(0, 255, 0),      // 绿
                Color.rgb(0, 0, 255),      // 蓝
                Color.rgb(75, 0, 130),     // 靛
                Color.rgb(148, 0, 211),    // 紫

                // 扩展色（13种）
                Color.rgb(255, 99, 132),   // 粉红
                Color.rgb(54, 162, 235),   // 天蓝
                Color.rgb(255, 206, 86),   // 浅黄
                Color.rgb(75, 192, 192),   // 青绿
                Color.rgb(153, 102, 255),  // 薰衣草紫
                Color.rgb(255, 159, 64),   // 橙黄
                Color.rgb(199, 199, 199),  // 浅灰
                Color.rgb(83, 102, 255),   // 钴蓝
                Color.rgb(255, 102, 0),    // 深橙
                Color.rgb(50, 205, 50),    // 酸橙绿
                Color.rgb(220, 20, 60),    // 猩红
                Color.rgb(138, 43, 226),   // 蓝紫
                Color.rgb(0, 139, 139)     // 深青
        };
        // 设置数据集
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(customColors);
        // dataSet.setValueTextSize(16f);
        dataSet.setDrawValues(false); // 不显示数值
        dataSet.setValueTextSize(0f);

        PieData pieData = new PieData(dataSet);
//        pieData.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                float percentage = (value / pieData.getYValueSum()) * 100;
//                return String.format(Locale.getDefault(), "%.1f%%", percentage);
//            }
//        });

        pieChart.setDrawEntryLabels(false); // 不显示分类标签
        pieChart.setUsePercentValues(false); // 不使用百分比显示
        // pieChart.getLegend().setEnabled(false);  // 不显示图例


        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(16f);
        pieChart.invalidate();

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);

        legend.setMaxSizePercent(0.4f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setYEntrySpace(10f);  // 垂直间距
        legend.setXEntrySpace(20f); // 水平间距
        legend.setTextSize(12f);
        legend.setWordWrapEnabled(true);  // 允许换行

        pieChart.setExtraOffsets(30f, 10f, 30f, 10f);  // 左、上、右、下边距
        pieChart.setMinOffset(20f);  // 减少饼图与边缘的间距

        // 点击监听
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    pieChart.highlightValue(h);//高亮显示
                    PieEntry pieEntry = (PieEntry) e;
                    float percentage = (pieEntry.getValue() / pieData.getYValueSum()) * 100;

                    // 绑定数据
                    View popupView = popupWindow.getContentView();
                    TextView tvCategory = popupView.findViewById(R.id.tv_category);
                    TextView tvPercentage = popupView.findViewById(R.id.tv_percentage);

                    tvCategory.setText(pieEntry.getLabel());
                    tvPercentage.setText(String.format(Locale.getDefault(), "占比: %.1f%%", percentage));

                    // 获取饼图中心点坐标
                    MPPointF center = pieChart.getCenter();

                    // 获取当前高亮扇形的角度（弧度）
                    float angle = pieChart.getAngleForPoint(h.getX(), h.getY());

                    // 计算扇形半径（减去内圆半径）
                    float radius = pieChart.getRadius() * 0.7f; // 0.7f 控制悬浮窗显示在扇形中部

                    // 通过三角函数计算扇形中心点坐标
                    float x = center.x + (float) (radius * Math.cos(Math.toRadians(angle)));
                    float y = center.y + (float) (radius * Math.sin(Math.toRadians(angle)));

                    // 显示悬浮窗（居中于扇形）
                    popupWindow.showAsDropDown(
                            pieChart,
                            (int) (x - popupWindow.getWidth() / 2),
                            (int) (y - 700)
                    );
                }
            }

            @Override
            public void onNothingSelected() {
                clearHighlightAndPopup(); // 统一清理
            }

        });

        pieChart.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                clearHighlightAndPopup(); // 点击图表任意位置时清理
            }
            return false;
        });

        pieChart.invalidate();
        pieChart.requestLayout();
    }

    private void clearHighlightAndPopup() {
        pieChart.highlightValues(null); // 强制取消高亮
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    // 更新排行榜
    private void updateRankingList(boolean isExpense, String range, Map<Long, CategoryStats> categoryStatsMap) {
        // 转换统计数据为排行榜条目
        List<RankingItem> rankingItems = new ArrayList<>();
        double totalAmount = 0;

        // 计算总金额
        for (CategoryStats stats : categoryStatsMap.values()) {
            totalAmount += stats.totalAmount;
        }

        // 创建排行榜条目
        for (CategoryStats stats : categoryStatsMap.values()) {
            if (stats.totalAmount > 0) {
                float percentage = (float) (stats.totalAmount / totalAmount * 100);
                rankingItems.add(new RankingItem(
                        stats.iconRes,
                        stats.categoryName,
                        stats.count,
                        percentage,
                        (float) stats.totalAmount
                ));
            }
        }

        // 按金额降序排序
        Collections.sort(rankingItems, (o1, o2) -> Float.compare(o2.getAmount(), o1.getAmount()));

        // 更新适配器
        rankingAdapter.updateData(rankingItems);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 获取当前是支出还是收入模式
        boolean isExpense = btnExpense.isSelected();
        if(tvWeek.isSelected()){
            new LoadDataTask().execute("week", isExpense);
        } else if (tvMonth.isSelected()) {
            new LoadDataTask().execute("month", isExpense);
        } else if (tvYear.isSelected()) {
            new LoadDataTask().execute("year", isExpense);
        }
    }

}
