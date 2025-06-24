package com.qzz.coffeeshop.pattern.factory;

import com.qzz.coffeeshop.pattern.decorator.CoffeeComponent;
import com.qzz.coffeeshop.pattern.decorator.Espresso;
import com.qzz.coffeeshop.pattern.decorator.Latte;
import com.qzz.coffeeshop.pattern.decorator.Cappuccino;

/**
 * 咖啡工厂 - 工厂模式
 */
public class CoffeeFactory {
    
    public enum CoffeeType {
        ESPRESSO, LATTE, CAPPUCCINO
    }
    
    public static CoffeeComponent createCoffee(CoffeeType type) {
        switch (type) {
            case ESPRESSO:
                return new Espresso();
            case LATTE:
                return new Latte();
            case CAPPUCCINO:
                return new Cappuccino();
            default:
                throw new IllegalArgumentException("未知的咖啡类型: " + type);
        }
    }
    
    public static CoffeeComponent createCoffee(String coffeeTypeName) {
        switch (coffeeTypeName.toLowerCase()) {
            case "espresso":
                return new Espresso();
            case "latte":
                return new Latte();
            case "cappuccino":
                return new Cappuccino();
            default:
                throw new IllegalArgumentException("未知的咖啡类型: " + coffeeTypeName);
        }
    }
}

