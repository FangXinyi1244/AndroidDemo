package com.qzz.coffeeshop.pattern.strategy;

import android.util.Log;

/**
 * 信用卡支付策略
 */
public class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    private String cardHolderName;

    public CreditCardPayment(String cardNumber, String cardHolderName) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
    }

    @Override
    public boolean processPayment(double amount) {
        // 模拟信用卡支付处理
        Log.i("CreditCardPayment", "正在处理信用卡支付...");
        Log.i("CreditCardPayment", "卡号: " + maskCardNumber(cardNumber));
        Log.i("CreditCardPayment", "持卡人: " + cardHolderName);
        Log.i("CreditCardPayment", "金额: ¥" + String.format("%.2f", amount));
        
        // 模拟支付处理时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 模拟支付成功（90%成功率）
        boolean success = Math.random() > 0.1;
        
        if (success) {
            Log.i("CreditCardPayment", "信用卡支付成功！");
        } else {
            Log.e("CreditCardPayment", "信用卡支付失败！");
        }
        
        return success;
    }

    @Override
    public String getPaymentMethodName() {
        return "信用卡支付";
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}

