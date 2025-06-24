package com.qzz.coffeeshop.pattern.observer;

import android.util.Log;
import com.qzz.coffeeshop.model.Order;

/**
 * 客户观察者 - 接收订单状态更新
 */
public class CustomerObserver implements OrderObserver {
    private String customerName;

    public CustomerObserver(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public void onOrderUpdate(Order order, String message) {
        String notification = String.format("[客户 %s] 订单 #%d 更新: %s", 
                customerName, order.getOrderId(), message);
        Log.i("CustomerObserver", notification);
        
        // 在实际应用中，这里可以发送推送通知给客户
        // 或者更新UI界面显示订单状态
    }

    public String getCustomerName() {
        return customerName;
    }
}

