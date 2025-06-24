package com.qzz.coffeeshop.pattern.decorator;

/**
 * 咖啡接口 - 装饰者模式的组件接口
 */
public interface CoffeeComponent {
    String getDescription();
    double getCost();
}

