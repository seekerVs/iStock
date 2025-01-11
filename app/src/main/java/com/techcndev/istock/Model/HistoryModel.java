package com.techcndev.istock.Model;

public class HistoryModel {
    private String date;
    private String qtyin;
    private String qtyout;
    private String balance;
    private String sales;
    private String historyid;


    public HistoryModel(String historyid, String date, String qtyin, String qtyout, String balance, String sales) {
        this.historyid = historyid;
        this.date = date;
        this.qtyin = qtyin;
        this.qtyout = qtyout;
        this.balance = balance;
        this.sales = sales;
    }

    public String getHistoryid() {
        return historyid;
    }
    public String getDate() {
        return date;
    }

    public String getQtyin() {
        return qtyin;
    }

    public String getQtyout() {
        return qtyout;
    }

    public String getBalance() {
        return balance;
    }

    public String getSales() {
        return sales;
    }

}
