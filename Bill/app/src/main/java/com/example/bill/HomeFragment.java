package com.example.bill;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.bill.Transaction;
import com.example.bill.databinding.FragmentHomeBinding;
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

    private FragmentHomeBinding binding;
    private SharedPreferences prefs;
    private double totalIncome = 0.0;
    private double totalExpense = 0.0;
    private List<Transaction> transactions = new ArrayList<>();
    private BroadcastReceiver updateReceiver;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // 初始化 SharedPreferences
        prefs = requireActivity().getSharedPreferences("BillPrefs", Context.MODE_PRIVATE);
        totalIncome = prefs.getFloat("total_income", 0.0f);
        totalExpense = prefs.getFloat("total_expense", 0.0f);
        loadTransactions();


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

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
        binding = null;
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



    private void updateUI() {
        String budgetStr = binding.etBudget.getText().toString();
        double budget;
        try {
            budget = budgetStr.isEmpty() ? 0 : Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            budget = 0;
            binding.etBudget.setError("请输入有效的预算");
        }

        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        double balance = totalIncome - totalExpense;
        double remaining = budget - totalExpense;
        double avgDaily = today > 0 ? totalExpense / today : 0;
        double avgAvailable = (daysInMonth - today) > 0 ? remaining / (daysInMonth - today) : 0;

        binding.tvTotalIncome.setText(String.format("总收入：￥%.2f", totalIncome));
        binding.tvTotalExpense.setText(String.format("总支出：￥%.2f", totalExpense));
        binding.tvBalance.setText(String.format("结余：￥%.2f", balance));
        binding.tvRemaining.setText(String.format("剩余额度：￥%.2f", remaining));
        binding.tvAvgDaily.setText(String.format("日均消费：￥%.2f", avgDaily));
        binding.tvAvgAvailable.setText(String.format("剩余每日可用额度：￥%.2f", avgAvailable));
    }

    private void notifyBillFragment() {
        Intent intent = new Intent("com.example.bill.UPDATE_UI");
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }
}