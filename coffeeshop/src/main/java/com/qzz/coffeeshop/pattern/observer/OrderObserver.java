package com.qzz.coffeeshop.pattern.observer;

import com.qzz.coffeeshop.model.Order;

/**
 * 订单观察者接口
 */
public interface OrderObserver {
    void onOrderUpdate(Order order, String message);
}

