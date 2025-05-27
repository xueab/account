package com.example.accountbook.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountbook.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StatsFragment extends Fragment {
    private Button btnExpense, btnIncome;

    private TextView tvWeek, tvMonth, tvYear;
    private TextView tvTitle, tvAmount, tvCount, tvMaxAmount;
    private PieChart pieChart;
    private RecyclerView rvRanking;
    private RankingAdapter rankingAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        init(view);
        setupListener();


        return view;
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
        rvRanking = view.findViewById(R.id.rv_ranking);

        rankingAdapter = new RankingAdapter();
        rvRanking.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRanking.setAdapter(rankingAdapter);

        // 初始化饼状图
        pieChart = view.findViewById(R.id.chart);


        // 默认为周数据
        btnExpense.setSelected(true);
        // 初始化数据
        selectTimeRange(tvWeek);

    }

    private void setupListener() {
        btnExpense.setOnClickListener(v -> {
            btnExpense.setSelected(true);
            btnIncome.setSelected(false);
            // 保持当前选中的时间范围
            if (tvWeek.isSelected()) {
                updateDataForRange("week", true);
            } else if (tvMonth.isSelected()) {
                updateDataForRange("month", true);
            } else {
                updateDataForRange("year", true);
            }

        });

        btnIncome.setOnClickListener(v -> {
            btnIncome.setSelected(true);
            btnExpense.setSelected(false);
            // 保持当前选中的时间范围
            if (tvWeek.isSelected()) {
                updateDataForRange("week", false);
            } else if (tvMonth.isSelected()) {
                updateDataForRange("month", false);
            } else {
                updateDataForRange("year", false);
            }

        });

        tvWeek.setOnClickListener(v -> selectTimeRange(v));
        tvMonth.setOnClickListener(v -> selectTimeRange(v));
        tvYear.setOnClickListener(v -> selectTimeRange(v));
    }

    // 更新支出数据
    private void updateExpenseDate(String date) {
        if (Objects.equals(date, "week")) {
            tvAmount.setText("427.0元");
            tvCount.setText("7笔");
            tvMaxAmount.setText("74.0元");
        } else if (Objects.equals(date, "month")) {
            tvAmount.setText("426.0元");
            tvCount.setText("4笔");
            tvMaxAmount.setText("70.0元");
        } else if (Objects.equals(date, "year")) {
            tvAmount.setText("400.0元");
            tvCount.setText("10笔");
            tvMaxAmount.setText("50.0元");
        }


    }

    // 更新收入数据
    private void updateIncomeDate(String date) {
        if (Objects.equals(date, "week")) {
            tvAmount.setText("1200.0元");
            tvCount.setText("3笔");
            tvMaxAmount.setText("800.0元");
        } else if (Objects.equals(date, "month")) {
            tvAmount.setText("10000.0元");
            tvCount.setText("4笔");
            tvMaxAmount.setText("20000.0元");
        } else if (Objects.equals(date, "year")) {
            tvAmount.setText("100000.0元");
            tvCount.setText("10笔");
            tvMaxAmount.setText("10000.0元");
        }


    }

    // 更新支出饼状图
    private void updateExpenseChart(String date) {
        if (Objects.equals(date, "week")) {
            // 创建数据
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(30f, "餐饮"));
            entries.add(new PieEntry(20f, "交通"));
            entries.add(new PieEntry(15f, "购物"));
            entries.add(new PieEntry(35f, "住房"));

            // 设置数据集
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // 设置颜色
            dataSet.setValueTextSize(16f); // 设置数值文字大小

            PieData pieData = new PieData(dataSet);
            // 设置百分比格式化器
            pieData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    // 计算百分比
                    float percentage = (value / pieData.getYValueSum()) * 100;
                    // 保留1位小数
                    return String.format(Locale.getDefault(), "%.1f%%", percentage);
                }
            });
            pieChart.setData(pieData);

            // 设置图表样式
            pieChart.getDescription().setEnabled(false); // 隐藏描述
            pieChart.setDrawHoleEnabled(true); // 显示中间的洞
            pieChart.setHoleColor(Color.WHITE); // 中间洞的颜色
            pieChart.setTransparentCircleRadius(58f); // 半透明圆环半径
            pieChart.setEntryLabelColor(Color.BLACK); // 标签文字颜色
            pieChart.setEntryLabelTextSize(16f); // 标签文字大小

            // 刷新图表
            pieChart.invalidate();
        }
    }

    // 更新收入饼状图
    private void updateIncomeChart(String date) {
        if (Objects.equals(date, "week")) {
            // 实现设置收入数据的逻辑
            // 创建数据
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(80f, "工资"));
            entries.add(new PieEntry(20f, "其他"));

            // 设置数据集
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // 设置颜色
            dataSet.setValueTextSize(16f); // 设置数值文字大小

            PieData pieData = new PieData(dataSet);
            // 设置百分比格式化器
            pieData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    // 计算百分比
                    float percentage = (value / pieData.getYValueSum()) * 100;
                    // 保留1位小数
                    return String.format(Locale.getDefault(), "%.1f%%", percentage);
                }
            });
            pieChart.setData(pieData);

            // 设置图表样式
            pieChart.getDescription().setEnabled(false); // 隐藏描述
            pieChart.setDrawHoleEnabled(true); // 显示中间的洞
            pieChart.setHoleColor(Color.WHITE); // 中间洞的颜色
            pieChart.setTransparentCircleRadius(58f); // 半透明圆环半径
            pieChart.setEntryLabelColor(Color.BLACK); // 标签文字颜色
            pieChart.setEntryLabelTextSize(16f); // 标签文字大小

            // 刷新图表
            pieChart.invalidate();
        }
    }


    // 更新排行榜数据
    private void updateRankingList(boolean isExpense, String range) {
        List<RankingItem> items = isExpense ?
                getExpenseRankingData(range) : getIncomeRankingData(range);
        rankingAdapter.updateData(items);
    }

    // 获取支出排行榜数据
    private List<RankingItem> getExpenseRankingData(String range) {
        if (range.equals("week")) {
            return Arrays.asList(
                    new RankingItem(R.drawable.ic_snacks, "零食", 1, 42.53f, 74.0f),
                    new RankingItem(R.drawable.ic_dailyuse, "日用", 1, 36.78f, 64.0f),
                    new RankingItem(R.drawable.ic_pet, "宠物", 1, 20.69f, 36.0f),
                    new RankingItem(R.drawable.ic_shop, "购物", 1, 42.53f, 74.0f),
                    new RankingItem(R.drawable.ic_fruit, "水果", 1, 36.78f, 64.0f),
                    new RankingItem(R.drawable.ic_game, "游戏", 1, 20.69f, 36.0f)
            );
        }
        return Arrays.asList(
                new RankingItem(R.drawable.ic_snacks, "零食", 1, 42.53f, 74.0f),
                new RankingItem(R.drawable.ic_shop, "日用", 1, 36.78f, 64.0f),
                new RankingItem(R.drawable.ic_pet, "宠物", 1, 20.69f, 36.0f)
        );
    }

    // 获取收入排行榜数据
    private List<RankingItem> getIncomeRankingData(String range) {
        if (range.equals("week")) {
            return Arrays.asList(
                    new RankingItem(R.drawable.ic_salary, "工资", 1, 80.0f, 8000.0f),
                    new RankingItem(R.drawable.ic_investment, "投资", 2, 20.0f, 2000.0f)
            );
        }
        return Arrays.asList(
                new RankingItem(R.drawable.ic_salary, "工资", 1, 80.0f, 8000.0f),
                new RankingItem(R.drawable.ic_investment, "投资", 2, 20.0f, 2000.0f)
        );

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
            updateDataForRange("week", isExpense);
        } else if (selectedView == tvMonth) {
            updateDataForRange("month", isExpense);
        } else if (selectedView == tvYear) {
            updateDataForRange("year", isExpense);
        }
    }

    private void updateDataForRange(String range, boolean isExpense) {
        // 更新标题
        String prefix = isExpense ? "支出" : "收入";
        switch (range) {
            case "week":
                tvTitle.setText("最近一周" + prefix + "总额");
                break;
            case "month":
                tvTitle.setText("最近一月" + prefix + "总额");
                break;
            case "year":
                tvTitle.setText("最近一年" + prefix + "总额");
                break;
        }

        // 更新金额统计（示例数据，实际应从数据库获取）
        if (isExpense) {
            updateExpenseDate(range);
            updateExpenseChart(range);
        } else {
            updateIncomeDate(range);
            updateIncomeChart(range);

        }

        // 更新排行榜
        updateRankingList(isExpense, range);
    }

}