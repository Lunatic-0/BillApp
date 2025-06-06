package com.example.bill;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.bill.databinding.FragmentAssetBinding;

public class AssetFragment extends Fragment {

    private FragmentAssetBinding binding;
    private SharedPreferences assetPrefs;
    private SharedPreferences billPrefs;
    private BroadcastReceiver updateReceiver;

    private static final String ASSET_PREF_NAME = "AssetPrefs";
    private static final String BILL_PREF_NAME = "BillPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAssetBinding.inflate(inflater, container, false);

        assetPrefs = requireContext().getSharedPreferences(ASSET_PREF_NAME, Context.MODE_PRIVATE);
        billPrefs = requireContext().getSharedPreferences(BILL_PREF_NAME, Context.MODE_PRIVATE);

        loadAssets();

        binding.btnSaveAsset.setOnClickListener(v -> saveAssets());

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadAssets();
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

    private void loadAssets() {
        float fixed = assetPrefs.getFloat("fixed", 0);
        float invest = assetPrefs.getFloat("invest", 0);
        float daily = billPrefs.getFloat("total_income", 0) - billPrefs.getFloat("total_expense", 0);

        binding.etFixedDeposit.setText(String.valueOf(fixed));
        binding.etInvest.setText(String.valueOf(invest));
        binding.etDailyAccount.setText(String.format("%.2f", daily));

        updateNetAsset(daily, invest, fixed);
    }

    private void saveAssets() {
        if (TextUtils.isEmpty(binding.etFixedDeposit.getText().toString())) {
            binding.etFixedDeposit.setError("请输入有效金额");
            return;
        }
        if (TextUtils.isEmpty(binding.etInvest.getText().toString())) {
            binding.etInvest.setError("请输入有效金额");
            return;
        }
        if (TextUtils.isEmpty(binding.etDailyAccount.getText().toString())) {
            binding.etDailyAccount.setError("请输入有效金额");
            return;
        }

        float fixed = parseFloat(binding.etFixedDeposit.getText().toString());
        float invest = parseFloat(binding.etInvest.getText().toString());
        float daily = parseFloat(binding.etDailyAccount.getText().toString());

        assetPrefs.edit()
                .putFloat("fixed", fixed)
                .putFloat("invest", invest)
                .apply();

        float currentIncome = billPrefs.getFloat("total_income", 0);
        float currentExpense = billPrefs.getFloat("total_expense", 0);
        float currentBalance = currentIncome - currentExpense;
        float incomeAdjustment = daily - currentBalance;
        billPrefs.edit()
                .putFloat("total_income", currentIncome + incomeAdjustment)
                .apply();

        updateNetAsset(daily, invest, fixed);
        notifyBillFragment();
    }

    private float parseFloat(String s) {
        if (TextUtils.isEmpty(s)) return 0;
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateNetAsset(float daily, float invest, float fixed) {
        float total = daily + invest + fixed;
        binding.tvNetAsset.setText(String.format("￥%.2f", total));
    }

    private void notifyBillFragment() {
        Intent intent = new Intent("com.example.bill.UPDATE_UI");
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }
}