// IMyAidlInterface.aidl
package com.qzz.demo2;

// Declare any non-default types here with import statements

interface IMyAidlInterface {

    boolean checkExpression(String expression);
    double calculate(String expression);
    double square(double number);
    double cube(double number);
    long factorial(int number);
}