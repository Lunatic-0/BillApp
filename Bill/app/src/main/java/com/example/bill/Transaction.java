package com.example.bill;

public class Transaction {
    public int id;
    public String date;
    public String description;
    public double amount;
    public boolean isIncome;

    public Transaction(String date, String description, double amount, boolean isIncome) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.isIncome = isIncome;
    }
}
