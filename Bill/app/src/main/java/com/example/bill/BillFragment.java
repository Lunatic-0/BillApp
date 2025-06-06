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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bill.databinding.FragmentBillBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BillFragment extends Fragment {

    private FragmentBillBinding binding;
    private List<Transaction> billList = new ArrayList<>();
    private BillAdapter adapter;
    private SharedPreferences prefs;
    private BroadcastReceiver updateReceiver;

    private void addTransaction(boolean isIncome) {
        String amountStr = binding.etAmount.getText().toString().trim();
        String category = binding.etCategory.getText().toString().trim();

        if (amountStr.isEmpty() || category.isEmpty()) {
            return; // 简化处理：不填不添加
        }

        double amount = Double.parseDouble(amountStr);
        Transaction transaction = new Transaction(amount, category, isIncome);

        billList.add(0, transaction); // 添加到顶部
        adapter.notifyItemInserted(0);
        binding.rvBills.scrollToPosition(0);

        saveTransactions(); // 保存
        updateBalance();    // 更新余额
        notifyHomeFragment();

        // 清空输入框
        binding.etAmount.setText("");
        binding.etCategory.setText("");
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBillBinding.inflate(inflater, container, false);

        // 初始化 SharedPreferences
        prefs = requireActivity().getSharedPreferences("BillPrefs", Context.MODE_PRIVATE);

        // 初始化 RecyclerView
        binding.rvBills.setLayoutManager(new LinearLayoutManager(getContext()));
        loadTransactions();
        adapter = new BillAdapter(billList);
        binding.rvBills.setAdapter(adapter);

        // 设置长按删除
        adapter.setOnItemLongClickListener(position -> {
            Transaction removedTransaction = billList.remove(position);
            adapter.notifyItemRemoved(position);
            saveTransactions();
            updateBalance();
            notifyHomeFragment();
        });

        // 初始更新余额
        updateBalance();

        // 注册广播接收器
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

        // 添加收入按钮
        binding.btnAddIncome.setOnClickListener(v -> addTransaction(true));

// 添加支出按钮
        binding.btnAddExpense.setOnClickListener(v -> addTransaction(false));

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
        List<Transaction> listFromJson = null;
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Transaction>>() {}.getType();
            listFromJson = gson.fromJson(json, type);
        }

        if (listFromJson == null) {
            listFromJson = new ArrayList<>();
        }

        billList.clear(); // ✅ 清空原 list 引用
        billList.addAll(listFromJson); // ✅ 添加新内容（保持同一个 list 实例）
    }



    private void saveTransactions() {
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(billList);
        editor.putString("transactions", json);
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        for (Transaction t : billList) {
            if (t.isIncome()) {
                totalIncome += t.getAmount();
            } else {
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
        double balance = totalIncome - totalExpense;
        binding.tvBalance.setText(String.format("余额：￥%.2f", balance));
    }

    private void notifyHomeFragment() {
        Intent intent = new Intent("com.example.bill.UPDATE_UI");
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }
}