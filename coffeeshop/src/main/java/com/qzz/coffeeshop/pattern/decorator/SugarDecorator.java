package com.qzz.coffeeshop.pattern.decorator;

/**
 * 糖装饰者
 */
public class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(CoffeeComponent coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return coffee.getDescription() + " + 糖";
    }

    @Override
    public double getCost() {
        return coffee.getCost() + 1.0;
    }
}

