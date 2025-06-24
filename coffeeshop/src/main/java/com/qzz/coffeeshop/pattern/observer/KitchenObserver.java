package com.qzz.coffeeshop.pattern.observer;

import android.util.Log;
import com.qzz.coffeeshop.model.Order;

/**
 * 厨房观察者 - 接收订单状态更新
 */
public class KitchenObserver implements OrderObserver {
    private String kitchenName;

    public KitchenObserver(String kitchenName) {
        this.kitchenName = kitchenName;
    }

    @Override
    public void onOrderUpdate(Order order, String message) {
        String notification = String.format("[厨房 %s] 订单 #%d 更新: %s", 
                kitchenName, order.getOrderId(), message);
        Log.i("KitchenObserver", notification);
        
        // 在实际应用中，这里可以：
        // 1. 自动开始制作咖啡
        // 2. 更新厨房显示屏
        // 3. 发送通知给厨房工作人员
        
        if (message.contains("订单已创建")) {
            // 模拟自动开始制作
            simulateOrderProcessing(order);
        }
    }

    private void simulateOrderProcessing(Order order) {
        // 模拟订单处理流程
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 模拟制作时间
                // 更新订单状态为制作中
                // 注意：这里应该通过OrderManager来更新状态
                Log.i("KitchenObserver", "开始制作订单 #" + order.getOrderId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getKitchenName() {
        return kitchenName;
    }
}

