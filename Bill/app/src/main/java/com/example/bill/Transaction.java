package com.example.bill;

import java.io.Serializable;

public class Transaction implements Serializable {
    private int id;
    private String date;
    private String description;
    private double amount;
    private boolean isIncome;

    public Transaction(String date, String description, double amount, boolean isIncome) {
        this.id = (int) System.currentTimeMillis(); // 使用时间戳生成唯一 ID
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isIncome = isIncome;
    }

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
}