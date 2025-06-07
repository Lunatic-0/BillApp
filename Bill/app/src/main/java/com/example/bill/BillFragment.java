package com.example.bill;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bill.databinding.FragmentBillBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BillFragment extends Fragment {

    private FragmentBillBinding binding;
    private List<Transaction> billList = new ArrayList<>();
    private BillAdapter adapter;
    private SharedPreferences prefs;
    private BroadcastReceiver updateReceiver;

    private static final String BILL_PREF_NAME = "BillPrefs";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBillBinding.inflate(inflater, container, false);

        prefs = requireActivity().getSharedPreferences(BILL_PREF_NAME, Context.MODE_PRIVATE);

        binding.rvBills.setLayoutManager(new LinearLayoutManager(getContext()));
        loadTransactions();
        adapter = new BillAdapter(billList);
        binding.rvBills.setAdapter(adapter);

        adapter.setOnItemLongClickListener(position -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("删除提示")
                    .setMessage("确定要删除本条记录吗?")
                    .setPositiveButton("确定", (dialog, which) -> {
                        billList.remove(position);
                        adapter.notifyItemRemoved(position);
                        saveTransactions();
                        updateBalance();
                        notifyHomeFragment();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        binding.btnAddIncome.setOnClickListener(v -> addTransaction(true));
        binding.btnAddExpense.setOnClickListener(v -> addTransaction(false));

        updateBalance();

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadTransactions();
                adapter.notifyDataSetChanged();
                updateBalance();
            }
        };
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(updateReceiver, new IntentFilter("com.example.bill.UPDATE_UI"));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);
        binding = null;
    }

    private void addTransaction(boolean isIncome) {
        String amountStr = binding.etAmount.getText().toString().trim();
        String category = binding.etCategory.getText().toString().trim();

        if (amountStr.isEmpty()) {
            binding.etAmount.setError("请输入金额");
            return;
        }
        if (category.isEmpty()) {
            binding.etCategory.setError("请输入备注");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                binding.etAmount.setError("输入金额不能小于0");
                return;
            }

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Transaction transaction = new Transaction(date, category, amount, isIncome);

            if (!isIncome) {
                float budget = prefs.getFloat("budget", 0.0f);
                double totalExpense = prefs.getFloat("total_expense", 0.0f);
                if (budget > 0 && (totalExpense + amount) > budget) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("预算提醒")
                            .setMessage("这笔支出超出了你的预算，是否要继续记录?")
                            .setPositiveButton("确定", (dialog, which) -> {
                                billList.add(0, transaction);
                                adapter.notifyItemInserted(0);
                                binding.rvBills.scrollToPosition(0);
                                saveTransactions();
                                updateBalance();
                                notifyHomeFragment();
                                binding.etAmount.setText("");
                                binding.etCategory.setText("");
                            })
                            .setNegativeButton("取消", null)
                            .show();
                    return;
                }
            }

            billList.add(0, transaction);
            adapter.notifyItemInserted(0);
            binding.rvBills.scrollToPosition(0);
            saveTransactions();
            updateBalance();
            notifyHomeFragment();

            binding.etAmount.setText("");
            binding.etCategory.setText("");
        } catch (NumberFormatException e) {
            binding.etAmount.setError("Please enter a valid number");
        }
    }

    private void loadTransactions() {
        String json = prefs.getString("transactions", null);
        List<Transaction> listFromJson = null;
        if (json != null) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Transaction>>(){}.getType();
                listFromJson = gson.fromJson(json, type);
            } catch (Exception e) {
                Log.e("BillFragment", "Failed to parse transactions", e);
                listFromJson = new ArrayList<>();
            }
        }

        if (listFromJson == null) {
            listFromJson = new ArrayList<>();
        }

        billList.clear();
        billList.addAll(listFromJson);
    }

    private void saveTransactions() {
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(billList);
        editor.putString("transactions", json);
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        for (Transaction t : billList) {
            if (t != null && t.isIncome()) {
                totalIncome += t.getAmount();
            } else if (t != null) {
                totalExpense += t.getAmount();
            }
        }
        editor.putFloat("total_income", (float) totalIncome);
        editor.putFloat("total_expense", (float) totalExpense);
        editor.apply();
    }

    private void updateBalance() {
        double totalIncome = prefs.getFloat("total_income", 0.0f);
        double totalExpense = prefs.getFloat("total_expense", 0.0f);
        double previousBalance = prefs.getFloat("previous_balance", 0.0f);
        double monthlyBalance = totalIncome - totalExpense;
        double totalBalance = previousBalance + monthlyBalance;
        binding.tvBalance.setText(String.format("总余额: ￥%.2f", totalBalance));
        binding.tvRemaining.setText(String.format("本月余额: ￥%.2f", monthlyBalance));
    }

    private void notifyHomeFragment() {
        Intent intent = new Intent("com.example.bill.UPDATE_UI");
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }
}