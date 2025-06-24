package com.qzz.coffeeshop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.qzz.coffeeshop.R;
import com.qzz.coffeeshop.model.Order;
import com.qzz.coffeeshop.pattern.decorator.CoffeeComponent;
import com.qzz.coffeeshop.pattern.decorator.MilkDecorator;
import com.qzz.coffeeshop.pattern.decorator.SugarDecorator;
import com.qzz.coffeeshop.pattern.factory.CoffeeFactory;
import com.qzz.coffeeshop.pattern.singleton.OrderManager;
import com.qzz.coffeeshop.pattern.observer.CustomerObserver;
import com.qzz.coffeeshop.pattern.observer.KitchenObserver;

public class CoffeeCustomizationActivity extends AppCompatActivity {

    private TextView textViewCoffeeName;
    private TextView textViewBasePrice;
    private TextView textViewTotalPrice;
    private CheckBox checkBoxMilk;
    private CheckBox checkBoxSugar;
    private Button buttonAddToOrder;
    private Button buttonBackToMenu;

    private String coffeeType;
    private CoffeeComponent baseCoffee;
    private OrderManager orderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_customization);

        initViews();
        initData();
        setupListeners();
    }

    private void initViews() {
        textViewCoffeeName = findViewById(R.id.textViewCoffeeName);
        textViewBasePrice = findViewById(R.id.textViewBasePrice);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        checkBoxMilk = findViewById(R.id.checkBoxMilk);
        checkBoxSugar = findViewById(R.id.checkBoxSugar);
        buttonAddToOrder = findViewById(R.id.buttonAddToOrder);
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu);
    }

    private void initData() {
        // 获取传递的咖啡类型
        coffeeType = getIntent().getStringExtra("coffee_type");
        if (coffeeType == null) {
            coffeeType = "espresso";
        }

        // 使用工厂模式创建咖啡
        baseCoffee = CoffeeFactory.createCoffee(coffeeType);

        // 获取订单管理器单例
        orderManager = OrderManager.getInstance();

        // 初始化观察者（如果还没有添加的话）
        if (orderManager.getAllOrders().isEmpty()) {
            orderManager.addObserver(new CustomerObserver("张三"));
            orderManager.addObserver(new KitchenObserver("主厨房"));
        }

        updateUI();
    }

    private void setupListeners() {
        checkBoxMilk.setOnCheckedChangeListener((buttonView, isChecked) -> updateUI());
        checkBoxSugar.setOnCheckedChangeListener((buttonView, isChecked) -> updateUI());

        buttonAddToOrder.setOnClickListener(v -> addToOrder());
        buttonBackToMenu.setOnClickListener(v -> finish());
    }

    private void updateUI() {
        textViewCoffeeName.setText(baseCoffee.getDescription());
        textViewBasePrice.setText(String.format("基础价格: ¥%.2f", baseCoffee.getCost()));

        // 使用装饰者模式计算总价
        CoffeeComponent decoratedCoffee = baseCoffee;
        if (checkBoxMilk.isChecked()) {
            decoratedCoffee = new MilkDecorator(decoratedCoffee);
        }
        if (checkBoxSugar.isChecked()) {
            decoratedCoffee = new SugarDecorator(decoratedCoffee);
        }

        textViewTotalPrice.setText(String.format("总价: ¥%.2f", decoratedCoffee.getCost()));
    }

    private void addToOrder() {
        // 使用装饰者模式创建最终的咖啡
        CoffeeComponent finalCoffee = baseCoffee;
        if (checkBoxMilk.isChecked()) {
            finalCoffee = new MilkDecorator(finalCoffee);
        }
        if (checkBoxSugar.isChecked()) {
            finalCoffee = new SugarDecorator(finalCoffee);
        }

        // 创建订单并添加咖啡
        Order order = new Order();
        order.addCoffeeItem(finalCoffee);

        // 使用单例模式的订单管理器添加订单
        orderManager.addOrder(order);

        Toast.makeText(this, "已添加到订单: " + finalCoffee.getDescription(), Toast.LENGTH_SHORT).show();
        finish();
    }
}

