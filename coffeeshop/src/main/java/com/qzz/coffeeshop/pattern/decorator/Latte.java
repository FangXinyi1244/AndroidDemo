package com.qzz.coffeeshop.pattern.decorator;

/**
 * 具体咖啡类 - Latte
 */
public class Latte implements CoffeeComponent {
    @Override
    public String getDescription() {
        return "Latte";
    }

    @Override
    public double getCost() {
        return 25.0;
    }
}

