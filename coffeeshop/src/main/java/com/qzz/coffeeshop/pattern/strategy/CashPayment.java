package com.qzz.coffeeshop.pattern.strategy;

import android.util.Log;

/**
 * 现金支付策略
 */
public class CashPayment implements PaymentStrategy {
    private double cashReceived;

    public CashPayment(double cashReceived) {
        this.cashReceived = cashReceived;
    }

    @Override
    public boolean processPayment(double amount) {
        Log.i("CashPayment", "正在处理现金支付...");
        Log.i("CashPayment", "应付金额: ¥" + String.format("%.2f", amount));
        Log.i("CashPayment", "收到现金: ¥" + String.format("%.2f", cashReceived));
        
        if (cashReceived >= amount) {
            double change = cashReceived - amount;
            Log.i("CashPayment", "找零: ¥" + String.format("%.2f", change));
            Log.i("CashPayment", "现金支付成功！");
            return true;
        } else {
            Log.e("CashPayment", "现金不足，支付失败！");
            return false;
        }
    }

    @Override
    public String getPaymentMethodName() {
        return "现金支付";
    }

    public double getCashReceived() {
        return cashReceived;
    }

    public void setCashReceived(double cashReceived) {
        this.cashReceived = cashReceived;
    }
}

