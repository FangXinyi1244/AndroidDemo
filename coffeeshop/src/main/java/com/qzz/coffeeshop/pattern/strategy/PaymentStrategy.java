package com.qzz.coffeeshop.pattern.strategy;

/**
 * 支付策略接口
 */
public interface PaymentStrategy {
    boolean processPayment(double amount);
    String getPaymentMethodName();
}

