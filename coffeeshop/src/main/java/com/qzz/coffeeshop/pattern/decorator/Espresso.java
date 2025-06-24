package com.qzz.coffeeshop.pattern.decorator;

/**
 * 具体咖啡类 - Espresso
 */
public class Espresso implements CoffeeComponent {
    @Override
    public String getDescription() {
        return "Espresso";
    }

    @Override
    public double getCost() {
        return 20.0;
    }
}

