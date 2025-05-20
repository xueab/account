package com.example.accountbook.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.accountbook.Entity.Category;
import com.example.accountbook.Entity.Record;
import com.example.accountbook.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RadioGroup rgRecordType;
    private Spinner spCategory;
    private EditText etAmount, etRemark;
    private Button btnSave;

    private List<Category> expenseCategories = new ArrayList<>();
    private List<Category> incomeCategories = new ArrayList<>();
    private ArrayAdapter<Category> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        initViews();
        setupCategoryData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rgRecordType = findViewById(R.id.rg_record_type);
        spCategory = findViewById(R.id.sp_category);
        etAmount = findViewById(R.id.et_amount);
        etRemark = findViewById(R.id.et_remark);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupCategoryData() {
        // 模拟支出分类数据
        expenseCategories.add(new Category("餐饮", Category.TYPE_EXPENSE, "ic_food", 1));
        expenseCategories.add(new Category("交通", Category.TYPE_EXPENSE, "ic_transport", 1));
        expenseCategories.add(new Category("购物", Category.TYPE_EXPENSE, "ic_shopping", 1));
        expenseCategories.add(new Category("娱乐", Category.TYPE_EXPENSE, "ic_entertainment", 1));

        // 模拟收入分类数据
        incomeCategories.add(new Category("工资", Category.TYPE_INCOME, "ic_salary", 1));
        incomeCategories.add(new Category("奖金", Category.TYPE_INCOME, "ic_bonus", 1));
        incomeCategories.add(new Category("投资", Category.TYPE_INCOME, "ic_investment", 1));

        // 默认显示支出分类
        updateCategorySpinner(Record.TYPE_EXPENSE);
    }

    private void setupListeners() {
        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());
        // 记录类型切换监听
        rgRecordType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_expense) {
                updateCategorySpinner(Record.TYPE_EXPENSE);
            } else {
                updateCategorySpinner(Record.TYPE_INCOME);
            }
        });

        // 保存按钮点击监听
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveRecord();
            }
        });
    }

    private void updateCategorySpinner(int recordType) {
        List<Category> categories = recordType == Record.TYPE_EXPENSE ? expenseCategories : incomeCategories;

        categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);
    }

    private boolean validateInput() {
        if (etAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double amount = Double.parseDouble(etAmount.getText().toString());
            if (amount <= 0) {
                Toast.makeText(this, "金额必须大于0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的金额", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    //TODO 保存记录
    private void saveRecord() {
        // 获取记录类型
        int recordType = rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense ?
                Record.TYPE_EXPENSE : Record.TYPE_INCOME;

        // 获取分类
        Category selectedCategory = (Category) spCategory.getSelectedItem();

        // 获取金额
        double amount = Double.parseDouble(etAmount.getText().toString());

        // 获取备注
        String remark = etRemark.getText().toString().trim();

        // 获取当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = dateFormat.format(new Date());

        // 创建记录对象
        Record record = new Record();
        record.setAmount(amount);
        record.setType(recordType);
        record.setCategoryId(selectedCategory.getId());
        record.setCategoryName(selectedCategory.getName());
        record.setRemark(remark);
        record.setDate(date);
        record.setUserId(1); // 假设当前用户ID为1
    }
}