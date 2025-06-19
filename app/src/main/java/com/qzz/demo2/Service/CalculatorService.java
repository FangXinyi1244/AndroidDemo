package com.qzz.demo2.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.qzz.demo2.IMyAidlInterface;

import java.util.Stack;

public class CalculatorService extends Service {
    private static final String TAG = "LogService";

    private final IMyAidlInterface.Stub binder = new IMyAidlInterface.Stub() {
        @Override
        public boolean checkExpression(String expression) {
            // 检查表达式是否有效
//            Log.d(TAG, "Checking expression: " + expression);
            return isExpressionValid(expression);
        }
        @Override
        public double calculate(String expression) {
            // 实现基础运算
            return evaluateExpression(expression);
        }
        @Override
        public double square(double number) {
            return number * number;
        }
        @Override
        public double cube(double number) {
            return number * number * number;
        }
        @Override
        public long factorial(int number) {
            if (number < 0) return 0;
            long result = 1;
            for (int i = 2; i <= number; i++) {
                result *= i;
            }
            return result;
        }
    };

    // 在Calculator类中添加新方法
    private boolean isExpressionValid(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        int parenthesesCount = 0;
        boolean lastWasOperator = true;  // 用于检查开头和连续运算符

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            // 检查括号
            if (c == '(') {
                parenthesesCount++;
            } else if (c == ')') {
                parenthesesCount--;
                if (parenthesesCount < 0) {
                    return false;  // 右括号过多
                }
            }
            // 检查字符合法性
            else if (!Character.isDigit(c) && !isOperator(c) && c != ' ' && c != '.') {
                Log.d(TAG, "Invalid character found: " + c);
                return false;  // 非法字符
            }

            // 检查运算符
            if (isOperator(c)) {
                if (lastWasOperator && c != '-') {  // 允许负号
                    return false;  // 连续运算符
                }
                lastWasOperator = true;
            } else if (Character.isDigit(c)) {
                lastWasOperator = false;
            }
        }

        // 检查最后一个字符不能是运算符
        if (expression.length() > 0) {
            char lastChar = expression.charAt(expression.length() - 1);
            if (isOperator(lastChar)) {
                return false;
            }
        }

        return parenthesesCount == 0;  // 确保括号配对
    }

    // 在 CalculatorService 中添加
    private double evaluateExpression(String expression) {
        try {
            // 简单的栈实现表达式求值
            Stack<Double> numbers = new Stack<>();
            Stack<Character> operators = new Stack<>();

            for (int i = 0; i < expression.length(); i++) {
                char c = expression.charAt(i);

                if (Character.isDigit(c)) {
                    StringBuilder num = new StringBuilder();
                    while (i < expression.length() &&
                            (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                        num.append(expression.charAt(i));
                        i++;
                    }
                    i--;
                    numbers.push(Double.parseDouble(num.toString()));
                } else if (c == '(') {
                    operators.push(c);
                } else if (c == ')') {
                    while (!operators.empty() && operators.peek() != '(') {
                        numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                    }
                    operators.pop();
                } else if (isOperator(c)) {
                    while (!operators.empty() && precedence(operators.peek()) >= precedence(c)) {
                        numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                    }
                    operators.push(c);
                }
            }

            while (!operators.empty()) {
                numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
            }

            return numbers.pop();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expression");
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '×' || c == '÷';
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '×' || op == '÷') return 2;
        return 0;
    }

    private double applyOperator(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '×': return a * b;
            case '÷':
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            default: return 0;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
