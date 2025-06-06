package com.example.bill;

import java.io.Serializable;

public class Transaction implements Serializable {
    private int id;
    private String date;
    private String description; // 类别或备注
    private double amount;
    private boolean isIncome;

    // 构造函数1：完整信息
    public Transaction(String date, String description, double amount, boolean isIncome) {
        this.id = (int) System.currentTimeMillis();
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    // 构造函数2：不含日期，默认今天（你可以在外部设置）
    public Transaction(double amount, String description, boolean isIncome) {
        this((new java.text.SimpleDateFormat("yyyy-MM-dd")).format(new java.util.Date()),
                description, amount, isIncome);
    }

    // 构造函数3：明确提供日期（可选）
    public Transaction(double amount, String description, boolean isIncome, String date) {
        this(date, description, amount, isIncome);
    }

    // getter 方法
    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isIncome() {
        return isIncome;
    }

    // category 就是 description，保持兼容
    public String getCategory() {
        return description;
    }
}
