package com.example.bill;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bill.AssetFragment;
import com.example.bill.BillFragment;
import com.example.bill.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // 默认选中首页
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new HomeFragment())
                .commit();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_bill) {
            selectedFragment = new BillFragment();
        } else if (itemId == R.id.nav_asset) {
            selectedFragment = new AssetFragment();
        }

        if (selectedFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, selectedFragment);
            transaction.commit();
            return true;
        }
        return false;
    }
}
