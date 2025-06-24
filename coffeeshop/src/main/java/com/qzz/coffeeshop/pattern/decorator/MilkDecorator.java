package com.qzz.coffeeshop.pattern.decorator;

/**
 * 牛奶装饰者
 */
public class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(CoffeeComponent coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return coffee.getDescription() + " + 牛奶";
    }

    @Override
    public double getCost() {
        return coffee.getCost() + 3.0;
    }
}

