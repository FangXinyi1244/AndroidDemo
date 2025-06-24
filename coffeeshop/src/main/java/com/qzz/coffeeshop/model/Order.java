package com.qzz.coffeeshop.model;

import com.qzz.coffeeshop.pattern.decorator.CoffeeComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单实体类
 */
public class Order {
    private int orderId;
    private List<CoffeeComponent> coffeeItems;
    private double totalPrice;
    private String status;
    private long timestamp;

    public Order() {
        this.coffeeItems = new ArrayList<>();
        this.status = "待处理";
        this.timestamp = System.currentTimeMillis();
        this.totalPrice = 0.0;
    }

    public void addCoffeeItem(CoffeeComponent coffee) {
        coffeeItems.add(coffee);
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        totalPrice = 0.0;
        for (CoffeeComponent coffee : coffeeItems) {
            totalPrice += coffee.getCost();
        }
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public List<CoffeeComponent> getCoffeeItems() {
        return coffeeItems;
    }

    public void setCoffeeItems(List<CoffeeComponent> coffeeItems) {
        this.coffeeItems = coffeeItems;
        calculateTotalPrice();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("订单 #").append(orderId).append("\n");
        sb.append("状态: ").append(status).append("\n");
        sb.append("商品:\n");
        for (CoffeeComponent coffee : coffeeItems) {
            sb.append("- ").append(coffee.getDescription()).append(" ¥").append(coffee.getCost()).append("\n");
        }
        sb.append("总价: ¥").append(String.format("%.2f", totalPrice));
        return sb.toString();
    }
}

