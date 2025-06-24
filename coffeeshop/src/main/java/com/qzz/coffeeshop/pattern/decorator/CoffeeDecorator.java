package com.qzz.coffeeshop.pattern.decorator;

/**
 * 抽象装饰者类
 */
public abstract class CoffeeDecorator implements CoffeeComponent {
    protected CoffeeComponent coffee;

    public CoffeeDecorator(CoffeeComponent coffee) {
        this.coffee = coffee;
    }

    @Override
    public String getDescription() {
        return coffee.getDescription();
    }

    @Override
    public double getCost() {
        return coffee.getCost();
    }
}

