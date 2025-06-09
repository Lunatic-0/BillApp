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
import com.example.bill.databinding.FragmentHomeBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.util.Log;
import android.widget.ScrollView;
import android.view.inputmethod.EditorInfo;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences prefs;
    private double totalIncome = 0.0;
    private double totalExpense = 0.0;
    private List<Transaction> transactions = new ArrayList<>();
    private BroadcastReceiver updateReceiver;

    private static final String BILL_PREF_NAME = "BillPrefs";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        prefs = requireActivity().getSharedPreferences(BILL_PREF_NAME, Context.MODE_PRIVATE);
        totalIncome = prefs.getFloat("total_income", 0.0f);
        totalExpense = prefs.getFloat("total_expense", 0.0f);
        loadTransactions();
        loadBudget();

        binding.btnSaveBudget.setOnClickListener(v -> saveBudget());

        // 确保 EditText 获得焦点时滚动到可见区域
        setupEditTextFocus();

        // 处理键盘“完成”键
        binding.etBudget.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveBudget();
                return true;
            }
            return false;
        });

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                totalIncome = prefs.getFloat("total_income", 0.0f);
                totalExpense = prefs.getFloat("total_expense", 0.0f);
                loadTransactions();
                loadBudget();
                updateUI();
            }
        };

        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(updateReceiver, new IntentFilter("com.example.bill.UPDATE_UI"));
        updateUI();

        return binding.getRoot();
    }

    private void setupEditTextFocus() {
        binding.etBudget.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollToView(binding.etBudget);
            }
        });
    }

    private void scrollToView(View view) {
        ScrollView scrollView = (ScrollView) binding.getRoot();
        scrollView.post(() -> {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            scrollView.smoothScrollTo(0, location[1] - 100); // 留出顶部空间
        });
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
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Transaction>>(){}.getType();
                transactions = gson.fromJson(json, type);
            } catch (Exception e) {
                Log.e("HomeFragment", "Failed to parse transactions", e);
                transactions = new ArrayList<>();
            }
        }
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
    }

    private void loadBudget() {
        float budget = prefs.getFloat("budget", 0.0f);
        if (budget > 0) {
            binding.etBudget.setText(String.format("%.2f", budget));
        }
        binding.tvMonth.setText(new SimpleDateFormat("yyyy年M月", Locale.getDefault()).format(Calendar.getInstance().getTime()));
    }

    private void saveBudget() {
        String budgetStr = binding.etBudget.getText().toString().trim();
        float budget = 0.0f;
        if (!budgetStr.isEmpty()) {
            try {
                budget = Float.parseFloat(budgetStr);
                if (budget < 0) {
                    binding.etBudget.setError("预算不能为负");
                    return;
                }
            } catch (NumberFormatException e) {
                binding.etBudget.setError("请输入有效数字");
                return;
            }
        }
        prefs.edit().putFloat("budget", budget).apply();
        updateUI();
        notifyOtherFragments();
    }

    private void updateUI() {
        String budgetStr = binding.etBudget.getText().toString();
        double budget = 0;
     

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

    private void notifyOtherFragments() {
        Intent intent = new Intent("com.example.bill.UPDATE_UI");
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }
}