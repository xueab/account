package com.example.accountbook.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accountbook.Dao.CategoryDao;
import com.example.accountbook.Dao.RecordDao;
import com.example.accountbook.Entity.Category;
import com.example.accountbook.Entity.Record;
import com.example.accountbook.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditRecordActivity extends AppCompatActivity {

    private ImageButton btnBack, btnDelete;
    private RadioGroup rgRecordType;
    private EditText etAmount, etRemark;
    private Button btnSave, btnTime,btnMoreCategories;
    private ImageView btn1, btn2, btn3, btn4;

    private Category selectedCategory;
    private List<Category> expenseCategories = new ArrayList<>();
    private List<Category> incomeCategories = new ArrayList<>();
    private CategoryDao categoryDao;
    private RecordDao recordDao;
    private int recordId;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_record); // 使用新的布局文件

        // 初始化视图
        initViews();

        // 初始化DAO
        categoryDao = new CategoryDao(this);
        recordDao = new RecordDao(this);

        // 获取传递过来的记录ID和用户ID
        recordId = getIntent().getIntExtra("record_id", -1);
        userId = getIntent().getLongExtra("USER_ID", -1);

        // 加载分类数据
        setupCategoryData();

        // 加载记录数据
        loadRecordData();

        // 设置监听器
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnDelete = findViewById(R.id.btn_delete);
        rgRecordType = findViewById(R.id.rg_record_type);
        etAmount = findViewById(R.id.et_amount);
        etRemark = findViewById(R.id.et_remark);
        btnSave = findViewById(R.id.btn_save);
        btnTime = findViewById(R.id.btn_time);
        btnMoreCategories = findViewById(R.id.btn_more_categories);
        // 分类按钮
        btn1 = findViewById(R.id.btn_1);
        btn2 = findViewById(R.id.btn_2);
        btn3 = findViewById(R.id.btn_3);
        btn4 = findViewById(R.id.btn_4);
    }

    private void setupCategoryData() {
            // 从数据库获取支出分类
            Cursor expenseCursor = categoryDao.getCategoriesByType(Category.TYPE_EXPENSE);
            List<Category> tempExpenseCategories = new ArrayList<>();
            if (expenseCursor != null) {
                while (expenseCursor.moveToNext()) {
                    long id = expenseCursor.getLong(expenseCursor.getColumnIndexOrThrow("id"));
                    String name = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("name"));
                    String icon = expenseCursor.getString(expenseCursor.getColumnIndexOrThrow("icon"));
                    tempExpenseCategories.add(new Category(id, name, Category.TYPE_EXPENSE, icon));
                }
                expenseCursor.close();
            }

            // 从数据库获取收入分类
            Cursor incomeCursor = categoryDao.getCategoriesByType(Category.TYPE_INCOME);
            List<Category> tempIncomeCategories = new ArrayList<>();
            if (incomeCursor != null) {
                while (incomeCursor.moveToNext()) {
                    long id = incomeCursor.getLong(incomeCursor.getColumnIndexOrThrow("id"));
                    String name = incomeCursor.getString(incomeCursor.getColumnIndexOrThrow("name"));
                    String icon = incomeCursor.getString(incomeCursor.getColumnIndexOrThrow("icon"));
                    tempIncomeCategories.add(new Category(id, name, Category.TYPE_INCOME, icon));
                }
                incomeCursor.close();
            }

            // 如果没有数据，添加默认分类
            if (tempExpenseCategories.isEmpty()) {
                tempExpenseCategories.add(new Category(1, "餐饮", Category.TYPE_EXPENSE, "ic_food"));
                tempExpenseCategories.add(new Category(2, "交通", Category.TYPE_EXPENSE, "ic_traffic"));
                tempExpenseCategories.add(new Category(3, "日用", Category.TYPE_EXPENSE, "ic_dailyuse"));
                tempExpenseCategories.add(new Category(4, "零食", Category.TYPE_EXPENSE, "ic_snacks"));
            }

            if (tempIncomeCategories.isEmpty()) {
                tempIncomeCategories.add(new Category(5, "工资", Category.TYPE_INCOME, "ic_salary"));
                tempIncomeCategories.add(new Category(6, "奖金", Category.TYPE_INCOME, "ic_bonus"));
                tempIncomeCategories.add(new Category(7, "投资", Category.TYPE_INCOME, "ic_investment"));
            }


            expenseCategories.clear();
            expenseCategories.addAll(tempExpenseCategories);
            incomeCategories.clear();
            incomeCategories.addAll(tempIncomeCategories);
            // 加载记录数据
            loadRecordData();
    }

    private void loadRecordData() {
        if (recordId == -1) {
            Toast.makeText(this, "记录ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 从数据库获取记录
        Cursor cursor = recordDao.getRecordById(recordId);
        if (cursor != null && cursor.moveToFirst()) {
            // 设置记录类型
            int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
            rgRecordType.check(type == Record.TYPE_EXPENSE ? R.id.rb_expense : R.id.rb_income);

            // 设置金额
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            etAmount.setText(String.valueOf(amount));

            // 设置备注
            String remark = cursor.getString(cursor.getColumnIndexOrThrow("remark"));
            etRemark.setText(remark);

            // 设置时间
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            btnTime.setText(date + " " + time);

            // 设置分类
            long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"));
            selectedCategory = findCategoryById(categoryId);
            updateCategoryUI();

            cursor.close();
        } else {
            Toast.makeText(this, "记录不存在", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private Category findCategoryById(long categoryId) {
        // 先在支出分类中查找
        for (Category category : expenseCategories) {
            if (category.getId() == categoryId) {
                return category;
            }
        }

        // 再在收入分类中查找
        for (Category category : incomeCategories) {
            if (category.getId() == categoryId) {
                return category;
            }
        }

        // 如果没找到，返回第一个分类
        return rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense ?
                expenseCategories.get(0) : incomeCategories.get(0);
    }

    private void updateCategoryUI() {
        boolean isExpense = rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense;
        List<Category> currentCategories = isExpense ? expenseCategories : incomeCategories;

        // 获取所有分类按钮的父布局
        ViewGroup btn1Parent = (ViewGroup) btn1.getParent();
        ViewGroup btn2Parent = (ViewGroup) btn2.getParent();
        ViewGroup btn3Parent = (ViewGroup) btn3.getParent();
        ViewGroup btn4Parent = (ViewGroup) btn4.getParent();

        // 默认隐藏所有分类按钮
        btn1Parent.setVisibility(View.GONE);
        btn2Parent.setVisibility(View.GONE);
        btn3Parent.setVisibility(View.GONE);
        btn4Parent.setVisibility(View.GONE);

        // 检查选中的分类是否在前四个中
        boolean isSelectedInFirstFour = currentCategories.indexOf(selectedCategory) >= 0
                && currentCategories.indexOf(selectedCategory) < 4;

        // 如果选中的分类不在前四个，替换第一个分类为选中的分类
        if (!isSelectedInFirstFour && selectedCategory != null) {
            // 临时存储前四个分类
//            List<Category> firstFour = new ArrayList<>();
//            if (currentCategories.size() > 0) firstFour.add(currentCategories.get(0));
//            if (currentCategories.size() > 1) firstFour.add(currentCategories.get(1));
//            if (currentCategories.size() > 2) firstFour.add(currentCategories.get(2));
//            if (currentCategories.size() > 3) firstFour.add(currentCategories.get(3));

            // 将选中的分类放到第一个位置
            currentCategories.remove(selectedCategory);
            currentCategories.add(0, selectedCategory);

//            // 恢复其他三个分类
//            for (Category category : firstFour) {
//                if (category != null && !category.equals(selectedCategory) && currentCategories.size() < 4) {
//                    currentCategories.add(category);
//                }
//            }
        }

        // 根据分类数量显示对应的按钮
        if (currentCategories.size() > 0) {
            btn1Parent.setVisibility(View.VISIBLE);
            btn1.setImageResource(getResources().getIdentifier(
                    currentCategories.get(0).getIcon(), "drawable", getPackageName()));
            ((TextView) btn1Parent.getChildAt(1))
                    .setText(currentCategories.get(0).getName());
        }

        if (currentCategories.size() > 1) {
            btn2Parent.setVisibility(View.VISIBLE);
            btn2.setImageResource(getResources().getIdentifier(
                    currentCategories.get(1).getIcon(), "drawable", getPackageName()));
            ((TextView) btn2Parent.getChildAt(1))
                    .setText(currentCategories.get(1).getName());
        }

        if (currentCategories.size() > 2) {
            btn3Parent.setVisibility(View.VISIBLE);
            btn3.setImageResource(getResources().getIdentifier(
                    currentCategories.get(2).getIcon(), "drawable", getPackageName()));
            ((TextView) btn3Parent.getChildAt(1))
                    .setText(currentCategories.get(2).getName());
        }

        if (currentCategories.size() > 3) {
            btn4Parent.setVisibility(View.VISIBLE);
            btn4.setImageResource(getResources().getIdentifier(
                    currentCategories.get(3).getIcon(), "drawable", getPackageName()));
            ((TextView) btn4Parent.getChildAt(1))
                    .setText(currentCategories.get(3).getName());
        }

        // 重置所有按钮的选中状态
        resetCategoryButtons();

        // 高亮选中的分类按钮
        if (selectedCategory != null) {
            highlightSelectedCategory();
        }

        // 如果分类数量小于等于4，隐藏"更多"按钮
        btnMoreCategories.setVisibility(currentCategories.size() > 4 ? View.VISIBLE : View.GONE);
    }

    private void resetCategoryButtons() {
        btn1.setSelected(false);
        btn2.setSelected(false);
        btn3.setSelected(false);
        btn4.setSelected(false);
    }

    private void highlightSelectedCategory() {
        boolean isExpense = rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense;
        List<Category> currentCategories = isExpense ? expenseCategories : incomeCategories;

        int index = currentCategories.indexOf(selectedCategory);
        if (index >= 0 && index < 4) {
            switch (index) {
                case 0:
                    btn1.setSelected(true);
                    break;
                case 1:
                    btn2.setSelected(true);
                    break;
                case 2:
                    btn3.setSelected(true);
                    break;
                case 3:
                    btn4.setSelected(true);
                    break;
            }
        }
    }

    private void setupListeners() {
        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());

        // 删除按钮点击事件
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());

        // 更多分类
        btnMoreCategories.setOnClickListener(v -> showMoreCategoriesDialog());

        // 记录类型切换监听
        rgRecordType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_expense) {
                selectedCategory = expenseCategories.get(0);
            } else {
                selectedCategory = incomeCategories.get(0);
            }
            updateCategoryUI();
        });

        // 分类按钮点击
        View.OnClickListener categoryListener = v -> {
            resetCategoryButtons();
            v.setSelected(true);

            boolean isExpense = rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense;
            List<Category> currentCategories = isExpense ? expenseCategories : incomeCategories;

            if (v == btn1 && !currentCategories.isEmpty()) {
                selectedCategory = currentCategories.get(0);
            } else if (v == btn2 && currentCategories.size() > 1) {
                selectedCategory = currentCategories.get(1);
            } else if (v == btn3 && currentCategories.size() > 2) {
                selectedCategory = currentCategories.get(2);
            } else if (v == btn4 && currentCategories.size() > 3) {
                selectedCategory = currentCategories.get(3);
            }
        };

        btn1.setOnClickListener(categoryListener);
        btn2.setOnClickListener(categoryListener);
        btn3.setOnClickListener(categoryListener);
        btn4.setOnClickListener(categoryListener);

        // 时间选择
        btnTime.setOnClickListener(v -> showDateTimePicker());

        // 保存按钮点击监听
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                updateRecord();
            }
        });
    }

    // 图标选择
    private void showMoreCategoriesDialog() {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.category_selector, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // 获取当前记录类型(支出/收入)
        boolean isExpense = rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense;
        List<Category> categories = isExpense ? expenseCategories : incomeCategories;

        // 获取GridView
        GridView gridView = dialogView.findViewById(R.id.grid_categories);

        // 创建适配器
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this, R.layout.item_icon, categories) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_icon, parent, false);
                }

                Category category = getItem(position);

                ImageView iconView = convertView.findViewById(R.id.iv_category_icon);
                TextView nameView = convertView.findViewById(R.id.tv_category_name);

                // 设置图标和名称
                iconView.setImageResource(getResources().getIdentifier(
                        category.getIcon(), "drawable", getPackageName()));
                nameView.setText(category.getName());

                // 如果当前选中，设置选中状态
                iconView.setSelected(category.equals(selectedCategory));

                return convertView;
            }
        };

        gridView.setAdapter(adapter);

        // 设置图标点击事件
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Category selected = categories.get(position);
            selectedCategory = selected;

            // 更新主界面第一个分类图标为选中的分类
            updateFirstCategoryIcon(selected);

            dialog.dismiss();
        });

        dialog.show();
    }


    private void updateFirstCategoryIcon(Category selectedCategory) {
        boolean isExpense = rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense;
        List<Category> currentCategories = isExpense ? expenseCategories : incomeCategories;

        // 确保选中的分类在列表中
        if (!currentCategories.contains(selectedCategory)) {
            currentCategories.add(0, selectedCategory);
        }

        // 如果选中的分类不在第一个位置，调整位置
        if (currentCategories.indexOf(selectedCategory) != 0) {
            currentCategories.remove(selectedCategory);
            currentCategories.add(0, selectedCategory);
        }

        // 更新UI
        updateCategoryUI();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除记录")
                .setMessage("确定要删除这条记录吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteRecord())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteRecord() {
        boolean success = recordDao.deleteRecord(recordId) > 0;

        if (success) {
            Toast.makeText(this, "记录已删除", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            // 解析当前显示的时间
            String[] dateTimeParts = btnTime.getText().toString().split(" ");
            String[] dateParts = dateTimeParts[0].split("-");
            String[] timeParts = dateTimeParts[1].split(":");
            calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }

        new DatePickerDialog(this, (view, year, month, day) -> {
            // 保存日期部分
            String selectedDate = String.format(Locale.getDefault(),
                    "%d-%02d-%02d", year, month + 1, day);

            new TimePickerDialog(this, (timeView, hour, minute) -> {
                // 保存时间部分
                String selectedTime = String.format(Locale.getDefault(),
                        "%02d:%02d", hour, minute);

                // 更新按钮显示
                btnTime.setText(selectedDate + " " + selectedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
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

    private void updateRecord() {
        // 获取记录类型
        int recordType = rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense ?
                Record.TYPE_EXPENSE : Record.TYPE_INCOME;

        // 获取金额
        double amount = Double.parseDouble(etAmount.getText().toString());

        // 获取备注
        String remark = etRemark.getText().toString().trim();

        // 解析日期和时间
        String[] dateTimeParts = btnTime.getText().toString().split(" ");
        String date = dateTimeParts[0];  // yyyy-MM-dd
        String time = dateTimeParts.length > 1 ? dateTimeParts[1] : "00:00"; // HH:mm

        // 创建记录对象
        Record record = new Record();
        record.setId(recordId);
        record.setAmount(amount);
        record.setType(recordType);
        record.setCategoryId(selectedCategory.getId());
        record.setCategoryName(selectedCategory.getName());
        record.setRemark(remark);
        record.setDate(date);
        record.setTime(time);
        record.setUserId(userId);

        // 更新记录到数据库
        boolean success = recordDao.updateRecord(record);

        if (success) {
            Toast.makeText(this, "记录已更新", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "更新失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}