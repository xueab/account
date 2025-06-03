package com.example.accountbook.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
    private ImageView btnFood, btnTransport, btnDaily, btnSnack;
    // 当前选中的分类列表
    private Category selectedCategory;
    // 支出分类列表
    private List<Category> expenseCategories = new ArrayList<>();
    // 收入分类列表
    private List<Category> incomeCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        // 初始化
        initViews();
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
        btnFood = findViewById(R.id.btn_food);
        btnTransport = findViewById(R.id.btn_transport);
        btnDaily = findViewById(R.id.btn_daily);
        btnSnack = findViewById(R.id.btn_snack);

        // 设置默认时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        btnTime.setText(dateFormat.format(new Date()));

    }

    private void setupCategoryData() {
        // TODO 查询数据库
        // 模拟支出分类数据
        expenseCategories.add(new Category("餐饮", Category.TYPE_EXPENSE, "ic_food", 1));
        expenseCategories.add(new Category("交通", Category.TYPE_EXPENSE, "ic_traffic", 1));
        expenseCategories.add(new Category("日用", Category.TYPE_EXPENSE, "ic_dailyuse", 1));
        expenseCategories.add(new Category("零食", Category.TYPE_EXPENSE, "ic_snacks", 1));
        expenseCategories.add(new Category("娱乐", Category.TYPE_EXPENSE, "ic_game", 1));

        // 模拟收入分类数据
        incomeCategories.add(new Category("工资", Category.TYPE_INCOME, "ic_salary", 1));
        incomeCategories.add(new Category("奖金", Category.TYPE_INCOME, "ic_bonus", 1));
        incomeCategories.add(new Category("投资", Category.TYPE_INCOME, "ic_investment", 1));
        incomeCategories.add(new Category("其他", Category.TYPE_INCOME, "ic_finance", 1));

        // 默认选中第一个支出分类
        selectedCategory = expenseCategories.get(0);
        btnFood.setSelected(true);
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

            if (v == btnFood) {
                selectedCategory = currentCategories.get(0);
            } else if (v == btnTransport) {
                selectedCategory = currentCategories.get(1);
            } else if (v == btnDaily) {
                selectedCategory = currentCategories.get(2);
            } else if (v == btnSnack) {
                selectedCategory = currentCategories.get(3);
            }
        };

        btnFood.setOnClickListener(categoryListener);
        btnTransport.setOnClickListener(categoryListener);
        btnDaily.setOnClickListener(categoryListener);
        btnSnack.setOnClickListener(categoryListener);

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
        btnFood.setSelected(false);
        btnTransport.setSelected(false);
        btnDaily.setSelected(false);
        btnSnack.setSelected(false);
    }

    private void updateCategorySelection(Category category) {
        selectedCategory = category;
        resetCategoryButtons();

        if (category.getName().equals("饮食")) {
            btnFood.setSelected(true);
        } else if (category.getName().equals("出行")) {
            btnTransport.setSelected(true);
        } else if (category.getName().equals("日用")) {
            btnDaily.setSelected(true);
        } else if (category.getName().equals("零食")) {
            btnSnack.setSelected(true);
        }
    }

    private void updateCategoryUI() {
        boolean isExpense = selectedCategory.getType() == Category.TYPE_EXPENSE;
        List<Category> currentCategories = isExpense ? expenseCategories : incomeCategories;

        // 更新前4个分类按钮的显示
        if (currentCategories.size() > 0) {
            btnFood.setImageResource(getResources().getIdentifier(
                    currentCategories.get(0).getIcon(), "drawable", getPackageName()));
            ((TextView) ((ViewGroup) btnFood.getParent()).getChildAt(1))
                    .setText(currentCategories.get(0).getName());
        }

        if (currentCategories.size() > 1) {
            btnTransport.setImageResource(getResources().getIdentifier(
                    currentCategories.get(1).getIcon(), "drawable", getPackageName()));
            ((TextView) ((ViewGroup) btnTransport.getParent()).getChildAt(1))
                    .setText(currentCategories.get(1).getName());
        }

        if (currentCategories.size() > 2) {
            btnDaily.setImageResource(getResources().getIdentifier(
                    currentCategories.get(2).getIcon(), "drawable", getPackageName()));
            ((TextView) ((ViewGroup) btnDaily.getParent()).getChildAt(1))
                    .setText(currentCategories.get(2).getName());
        }

        if (currentCategories.size() > 3) {
            btnSnack.setImageResource(getResources().getIdentifier(
                    currentCategories.get(3).getIcon(), "drawable", getPackageName()));
            ((TextView) ((ViewGroup) btnSnack.getParent()).getChildAt(1))
                    .setText(currentCategories.get(3).getName());
        }

        // 重置所有按钮的选中状态
        resetCategoryButtons();

        // 高亮第一个分类按钮
        btnFood.setSelected(true);
        selectedCategory = currentCategories.get(0);
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
        btnFood.setImageResource(getResources().getIdentifier(
                selectedCategory.getIcon(), "drawable", getPackageName()));
        ((TextView) ((ViewGroup) btnFood.getParent()).getChildAt(1))
                .setText(selectedCategory.getName());

        // 重置所有按钮的选中状态
        resetCategoryButtons();

        // 高亮第一个分类按钮
        btnFood.setSelected(true);

        // 更新当前选中的分类
        this.selectedCategory = selectedCategory;
    }

    // 时间选择
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            new TimePickerDialog(this, (timeView, hour, minute) -> {
                String dateTime = String.format(Locale.getDefault(),
                        "%d-%02d-%02d %02d:%02d", year, month+1, day, hour, minute);
                btnTime.setText(dateTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
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


    //TODO 保存记录
    private void saveRecord() {
        // 获取记录类型
        int recordType = rgRecordType.getCheckedRadioButtonId() == R.id.rb_expense ?
                Record.TYPE_EXPENSE : Record.TYPE_INCOME;

        // 获取金额
        double amount = Double.parseDouble(etAmount.getText().toString());

        // 获取备注
        String remark = etRemark.getText().toString().trim();

        // 获取时间
        String dateTime = btnTime.getText().toString();

        // 创建记录对象
        Record record = new Record();
        record.setAmount(amount);
        record.setType(recordType);
        record.setCategoryId(selectedCategory.getId());
        record.setCategoryName(selectedCategory.getName());
        record.setRemark(remark);
        record.setDate(dateTime);
        record.setUserId(1); // 假设当前用户ID为1

        // TODO: 保存记录到数据库或服务器
        Toast.makeText(this, "记录已保存", Toast.LENGTH_SHORT).show();
        finish();
    }
}