package com.qzz.coffeeshop.pattern.decorator;

/**
 * 具体咖啡类 - Cappuccino
 */
public class Cappuccino implements CoffeeComponent {
    @Override
    public String getDescription() {
        return "Cappuccino";
    }

    @Override
    public double getCost() {
        return 28.0;
    }
}

