package com.qzz.coffeeshop.pattern.strategy;

import android.util.Log;

/**
 * 移动支付策略（微信/支付宝）
 */
public class MobilePayment implements PaymentStrategy {
    private String paymentApp;
    private String accountId;

    public MobilePayment(String paymentApp, String accountId) {
        this.paymentApp = paymentApp;
        this.accountId = accountId;
    }

    @Override
    public boolean processPayment(double amount) {
        Log.i("MobilePayment", "正在处理" + paymentApp + "支付...");
        Log.i("MobilePayment", "账户: " + maskAccountId(accountId));
        Log.i("MobilePayment", "金额: ¥" + String.format("%.2f", amount));
        
        // 模拟移动支付处理时间
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 模拟支付成功（95%成功率）
        boolean success = Math.random() > 0.05;
        
        if (success) {
            Log.i("MobilePayment", paymentApp + "支付成功！");
        } else {
            Log.e("MobilePayment", paymentApp + "支付失败！");
        }
        
        return success;
    }

    @Override
    public String getPaymentMethodName() {
        return paymentApp + "支付";
    }

    private String maskAccountId(String accountId) {
        if (accountId.length() < 4) {
            return accountId;
        }
        return accountId.substring(0, 3) + "****" + accountId.substring(accountId.length() - 3);
    }
}

