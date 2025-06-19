package com.qzz.demo2.model.vo;

public class CalculatorKey {
    public static final int TYPE_NUMBER = 0;
    public static final int TYPE_OPERATOR = 1;
    public static final int TYPE_FUNCTION = 2;

    private String text;
    private int type;

    public CalculatorKey(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }
    public int getType() {
        return type;
    }

    // getter methods
}
