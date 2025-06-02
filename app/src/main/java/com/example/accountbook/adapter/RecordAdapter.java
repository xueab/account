package com.example.accountbook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountbook.Entity.Record;
import com.example.accountbook.R;
import com.example.accountbook.Utils.DateUtils;
import com.example.accountbook.Utils.ResourceUtil;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private List<Record> recordList;
    private OnRecordClickListener listener;

    public interface OnRecordClickListener {
        void onRecordClick(int position);
    }

    public RecordAdapter(List<Record> recordList, OnRecordClickListener listener) {
        this.recordList = recordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record record = recordList.get(position);

        // 设置分类图标和名称
        if (record.getCategoryIcon() != null) {
            int iconRes = ResourceUtil.getDrawableResourceId(
                    holder.itemView.getContext(),
                    record.getCategoryIcon()
            );
            holder.ivCategoryIcon.setImageResource(iconRes);
        }

        holder.tvCategoryName.setText(record.getCategoryName());

        // 设置金额和颜色
        holder.tvAmount.setText(String.format("¥%.2f", record.getAmount()));
        int colorRes = record.getType() == Record.TYPE_EXPENSE ?
                R.color.colorExpense : R.color.colorIncome;
        holder.tvAmount.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes)
        );

        // 设置时间和备注
        if (record.getTime() != null) {
            holder.tvTime.setText(DateUtils.formatTime(record.getTime()));
        }
        holder.tvRemark.setText(record.getRemark());

        holder.itemView.setOnClickListener(v -> listener.onRecordClick(position));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public void updateRecords(List<Record> newRecords) {
        recordList.clear();
        recordList.addAll(newRecords);
        notifyDataSetChanged();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvAmount;
        TextView tvTime;
        TextView tvRemark;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvRemark = itemView.findViewById(R.id.tv_remark);
        }
    }
}