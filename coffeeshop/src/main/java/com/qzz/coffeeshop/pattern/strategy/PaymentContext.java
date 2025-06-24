package com.qzz.coffeeshop.pattern.strategy;

import android.util.Log;

/**
 * 支付上下文 - 策略模式的上下文类
 */
public class PaymentContext {
    private PaymentStrategy paymentStrategy;

    public PaymentContext() {
    }

    public PaymentContext(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    /**
     * 设置支付策略
     */
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    /**
     * 执行支付
     */
    public boolean executePayment(double amount) {
        if (paymentStrategy == null) {
            Log.e("PaymentContext", "未设置支付策略！");
            return false;
        }

        Log.i("PaymentContext", "使用 " + paymentStrategy.getPaymentMethodName() + " 进行支付");
        return paymentStrategy.processPayment(amount);
    }

    /**
     * 获取当前支付方式名称
     */
    public String getCurrentPaymentMethodName() {
        if (paymentStrategy == null) {
            return "未设置支付方式";
        }
        return paymentStrategy.getPaymentMethodName();
    }
}

