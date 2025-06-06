package com.example.bill;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private EditText etBudget, etAmount, etCategory;
    private TextView tvIncome, tvExpense, tvBalance, tvRemaining, tvAvgDaily, tvAvgAvailable;
    private SharedPreferences prefs;
    private double totalIncome = 0.0;
    private double totalExpense = 0.0;
    private List<Transaction> transactions = new ArrayList<>();
    private BroadcastReceiver updateReceiver;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化控件
        etBudget = view.findViewById(R.id.et_budget);
        etAmount = view.findViewById(R.id.et_amount);
        etCategory = view.findViewById(R.id.et_category);
        tvIncome = view.findViewById(R.id.tv_total_income);
        tvExpense = view.findViewById(R.id.tv_total_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvRemaining = view.findViewById(R.id.tv_remaining);
        tvAvgDaily = view.findViewById(R.id.tv_avg_daily);
        tvAvgAvailable = view.findViewById(R.id.tv_avg_available);
        Button btnAddIncome = view.findViewById(R.id.btn_add_income);
        Button btnAddExpense = view.findViewById(R.id.btn_add_expense);

        // 初始化 SharedPreferences
        prefs = requireActivity().getSharedPreferences("BillPrefs", Context.MODE_PRIVATE);
        totalIncome = prefs.getFloat("total_income", 0.0f);
        totalExpense = prefs.getFloat("total_expense", 0.0f);
        loadTransactions();

        // 设置按钮点击事件
        btnAddIncome.setOnClickListener(v -> addTransaction(true));
        btnAddExpense.setOnClickListener(v -> addTransaction(false));

        // 注册广播接收器
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                totalIncome = prefs.getFloat("total_income", 0.0f);
                totalExpense = prefs.getFloat("total_expense", 0.0f);
                loadTransactions();
                updateUI();
            }
        };
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(updateReceiver, new IntentFilter("com.example.bill.UPDATE_UI"));

        // 初始更新 UI
        updateUI();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
    }

    private void loadTransactions() {
        String json = prefs.getString("transactions", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Transaction>>(){}.getType();
            transactions = gson.fromJson(json, type);
        }
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
    }

    private void addTransaction(boolean isIncome) {
        // 获取输入
        String amountStr = etAmount.getText().toString();
        String description = etCategory.getText().toString();

        // 验证输入
        if (amountStr.isEmpty()) {
            etAmount.setError("请输入金额");
            return;
        }
        if (description.isEmpty()) {
            etCategory.setError("请输入描述");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("金额必须大于0");
                return;
            }

            // 添加 Transaction
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Transaction transaction = new Transaction(date, description, amount, isIncome);
            transactions.add(transaction);

            // 更新收入或支出
            SharedPreferences.Editor editor = prefs.edit();
            if (isIncome) {
                totalIncome += amount;
                editor.putFloat("total_income", (float) totalIncome);
            } else {
                totalExpense += amount;
                editor.putFloat("total_expense", (float) totalExpense);
            }
            Gson gson = new Gson();
            editor.putString("transactions", gson.toJson(transactions));
            editor.apply();

            // 清空输入框
            etAmount.setText("");
            etCategory.setText("");

            // 更新 UI
            updateUI();

            // 通知 BillFragment 更新
            notifyBillFragment();
        } catch (NumberFormatException e) {
            etAmount.setError("请输入有效的数字");
        }
    }

    private void updateUI() {
        // 获取预算
        String budgetStr = etBudget.getText().toString();
        double budget;
        try {
            budget = budgetStr.isEmpty() ? 0 : Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            budget = 0;
            etBudget.setError("请输入有效的预算");
        }

        // 计算日期相关数据
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 计算统计数据
        double balance = totalIncome - totalExpense;
        double remaining = budget - totalExpense;
        double avgDaily = today > 0 ? totalExpense / today : 0;
        double avgAvailable = (daysInMonth - today) > 0 ? remaining / (daysInMonth - today) : 0;

        // 更新 TextView
        tvIncome.setText(String.format("总收入：￥%.2f", totalIncome));
        tvExpense.setText(String.format("总支出：￥%.2f", totalExpense));
        tvBalance.setText(String.format("结余：￥%.2f", balance));
        tvRemaining.setText(String.format("剩余额度：￥%.2f", remaining));
        tvAvgDaily.setText(String.format("日均消费：￥%.2f", avgDaily));
        tvAvgAvailable.setText(String.format("剩余每日可用额度：￥%.2f", avgAvailable));
    }

    private void notifyBillFragment() {
        Intent intent = new Intent("com.example.bill.UPDATE_UI");
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }
}