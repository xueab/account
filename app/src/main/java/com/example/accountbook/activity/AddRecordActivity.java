package com.example.accountbook.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RadioGroup rgRecordType;
    private LinearLayout layoutCategory;
    private EditText etAmount, etRemark;
    private Button btnSave, btnMoreCategories;
    private TextView btnSelectPhoto;
    private Button btnTime;
    // 分类按钮
    private ImageView btn1, btn2, btn3, btn4;
    // 当前选中的分类列表
    private Category selectedCategory;
    // 支出分类列表
    private List<Category> expenseCategories = new ArrayList<>();
    // 收入分类列表
    private List<Category> incomeCategories = new ArrayList<>();
    // 分类数据访问对象
    private CategoryDao categoryDao;
    private RecordDao recordDao;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        // 初始化
        initViews();
        // 初始化CategoryDao
        categoryDao = new CategoryDao(this);
        recordDao = new RecordDao(this);
        userId = getIntent().getLongExtra("USER_ID",-1);
        // 初始化数据
        setupCategoryData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rgRecordType = findViewById(R.id.rg_record_type);
        layoutCategory = findViewById(R.id.sp_category);
        etAmount = findViewById(R.id.et_amount);
        etRemark = findViewById(R.id.et_remark);
        btnSave = findViewById(R.id.btn_save);
        btnMoreCategories = findViewById(R.id.btn_more_categories);
        btnSelectPhoto = findViewById(R.id.btn_select_photo);
        btnTime = findViewById(R.id.btn_time);

        // 分类按钮
        btn1 = findViewById(R.id.btn_1);
        btn2 = findViewById(R.id.btn_2);
        btn3 = findViewById(R.id.btn_3);
        btn4 = findViewById(R.id.btn_4);

        // 设置默认时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        btnTime.setText(dateFormat.format(new Date()));

    }

    private void setupCategoryData() {
        // 从数据库获取支出分类
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

        // 如果没有数据，添加默认分类
        if (expenseCategories.isEmpty()) {
            expenseCategories.add(new Category(1, "餐饮", Category.TYPE_EXPENSE, "ic_food"));
            expenseCategories.add(new Category(2, "交通", Category.TYPE_EXPENSE, "ic_traffic"));
            expenseCategories.add(new Category(3, "日用", Category.TYPE_EXPENSE, "ic_dailyuse"));
            expenseCategories.add(new Category(4, "零食", Category.TYPE_EXPENSE, "ic_snacks"));
        }

        if (incomeCategories.isEmpty()) {
            incomeCategories.add(new Category(5, "工资", Category.TYPE_INCOME, "ic_salary"));
            incomeCategories.add(new Category(6, "奖金", Category.TYPE_INCOME, "ic_bonus"));
            incomeCategories.add(new Category(7, "投资", Category.TYPE_INCOME, "ic_investment"));
        }

        // 默认选中第一个支出分类
        if (!expenseCategories.isEmpty()) {
            selectedCategory = expenseCategories.get(0);
            btn1.setSelected(true);
            updateCategoryUI();
        }
    }

    private void setupListeners() {
        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());
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

        // 更多分类
        btnMoreCategories.setOnClickListener(v -> showMoreCategoriesDialog());

        // 时间选择
        btnTime.setOnClickListener(v -> showDateTimePicker());

        // 从相册选择
        btnSelectPhoto.setOnClickListener(v -> selectFromGallery());

        // 保存按钮点击监听
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveRecord();
            }
        });
    }

    // 重置按钮状态
    private void resetCategoryButtons() {
        btn1.setSelected(false);
        btn2.setSelected(false);
        btn3.setSelected(false);
        btn4.setSelected(false);
    }

    private void updateCategorySelection(Category category) {
        selectedCategory = category;
        resetCategoryButtons();

        if (category.getName().equals("饮食")) {
            btn1.setSelected(true);
        } else if (category.getName().equals("出行")) {
            btn2.setSelected(true);
        } else if (category.getName().equals("日用")) {
            btn3.setSelected(true);
        } else if (category.getName().equals("零食")) {
            btn4.setSelected(true);
        }
    }

    //更改图标UI
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

        // 高亮第一个分类按钮
        if (!currentCategories.isEmpty()) {
            btn1.setSelected(true);
            selectedCategory = currentCategories.get(0);
        }

        // 如果分类数量小于等于4，隐藏"更多"按钮
        btnMoreCategories.setVisibility(currentCategories.size() > 4 ? View.VISIBLE : View.GONE);
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
        // 更新第一个分类按钮的图标和文本
        btn1.setImageResource(getResources().getIdentifier(
                selectedCategory.getIcon(), "drawable", getPackageName()));
        ((TextView) ((ViewGroup) btn1.getParent()).getChildAt(1))
                .setText(selectedCategory.getName());

        // 重置所有按钮的选中状态
        resetCategoryButtons();

        // 高亮第一个分类按钮
        btn1.setSelected(true);

        // 更新当前选中的分类
        this.selectedCategory = selectedCategory;
    }

    // 时间选择
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
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



    private void selectFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    // 验证金额输入是否有效
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


    //保存记录
    private void saveRecord() {
        // 验证输入
        if (!validateInput()) {
            return;
        }

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
        record.setAmount(amount);
        record.setType(recordType);
        record.setCategoryId(selectedCategory.getId());
        record.setCategoryName(selectedCategory.getName());
        record.setRemark(remark);
        record.setDate(date);
        record.setTime(time);
        record.setUserId(userId);

        // 添加记录到数据库
        RecordDao recordDao = new RecordDao(this);
        long recordId = recordDao.addRecord(record);

        if (recordId != -1) {
            Toast.makeText(this, "记录已保存", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}