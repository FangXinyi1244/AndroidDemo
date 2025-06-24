package com.qzz.coffeeshop.pattern.singleton;

import com.qzz.coffeeshop.model.Order;
import com.qzz.coffeeshop.pattern.observer.OrderObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单管理器 - 懒汉式单例模式（带双重锁）
 */
public class OrderManager {
    private static volatile OrderManager instance;
    private List<Order> orders;
    private List<OrderObserver> observers;
    private int nextOrderId = 1;

    private OrderManager() {
        orders = new ArrayList<>();
        observers = new ArrayList<>();
    }

    /**
     * 获取单例实例 - 双重检查锁定
     */
    public static OrderManager getInstance() {
        if (instance == null) {
            synchronized (OrderManager.class) {
                if (instance == null) {
                    instance = new OrderManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加订单
     */
    public synchronized void addOrder(Order order) {
        order.setOrderId(nextOrderId++);
        orders.add(order);
        notifyObservers(order, "订单已创建");
    }

    /**
     * 更新订单状态
     */
    public synchronized void updateOrderStatus(int orderId, String status) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                order.setStatus(status);
                notifyObservers(order, "订单状态更新: " + status);
                break;
            }
        }
    }

    /**
     * 获取所有订单
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    /**
     * 添加观察者
     */
    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    /**
     * 移除观察者
     */
    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    /**
     * 通知所有观察者
     */
    private void notifyObservers(Order order, String message) {
        for (OrderObserver observer : observers) {
            observer.onOrderUpdate(order, message);
        }
    }
}

