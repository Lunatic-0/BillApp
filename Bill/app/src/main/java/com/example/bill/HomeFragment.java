package com.example.bill;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private EditText etBudget;
    private TextView tvIncome, tvExpense, tvBalance, tvRemaining, tvAvgDaily, tvAvgAvailable;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        etBudget = view.findViewById(R.id.et_budget);
        tvIncome = view.findViewById(R.id.tv_total_income);
        tvExpense = view.findViewById(R.id.tv_total_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvRemaining = view.findViewById(R.id.tv_remaining);
        tvAvgDaily = view.findViewById(R.id.tv_avg_daily);
        tvAvgAvailable = view.findViewById(R.id.tv_avg_available);

        double income = 5000;
        double expense = 3200;
        double budget = 4000;

        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        double remaining = budget - expense;
        double avgDaily = expense / today;
        double avgAvailable = (daysInMonth - today) > 0 ? remaining / (daysInMonth - today) : 0;

        tvIncome.setText("总收入：￥" + income);
        tvExpense.setText("总支出：￥" + expense);
        tvBalance.setText("结余：￥" + (income - expense));
        tvRemaining.setText("剩余额度：￥" + String.format("%.2f", remaining));
        tvAvgDaily.setText("日均消费：￥" + String.format("%.2f", avgDaily));
        tvAvgAvailable.setText("剩余每日可用额度：￥" + String.format("%.2f", avgAvailable));

        return view;
    }
}
