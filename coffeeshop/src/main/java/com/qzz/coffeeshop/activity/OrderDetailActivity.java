package com.qzz.coffeeshop.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qzz.coffeeshop.R;
import com.qzz.coffeeshop.adapter.OrderAdapter;
import com.qzz.coffeeshop.model.Order;
import com.qzz.coffeeshop.pattern.singleton.OrderManager;
import com.qzz.coffeeshop.pattern.strategy.CashPayment;
import com.qzz.coffeeshop.pattern.strategy.CreditCardPayment;
import com.qzz.coffeeshop.pattern.strategy.MobilePayment;
import com.qzz.coffeeshop.pattern.strategy.PaymentContext;

import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private TextView textViewTotalAmount;
    private Button buttonPayment;
    private Button buttonBackToMenu;

    private OrderManager orderManager;
    private OrderAdapter orderAdapter;
    private List<Order> orders;
    private PaymentContext paymentContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        initData();
        setupListeners();
    }

    private void initViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        buttonPayment = findViewById(R.id.buttonPayment);
        buttonBackToMenu = findViewById(R.id.buttonBackToMenu);
    }

    private void initData() {
        orderManager = OrderManager.getInstance();
        orders = orderManager.getAllOrders();
        paymentContext = new PaymentContext();

        orderAdapter = new OrderAdapter(this, orders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(orderAdapter);

        updateTotalAmount();
    }

    private void setupListeners() {
        buttonPayment.setOnClickListener(v -> showPaymentOptions());
        buttonBackToMenu.setOnClickListener(v -> finish());
    }

    private void updateTotalAmount() {
        double totalAmount = 0.0;
        for (Order order : orders) {
            totalAmount += order.getTotalPrice();
        }
        textViewTotalAmount.setText(String.format("总金额: ¥%.2f", totalAmount));
    }

    private void showPaymentOptions() {
        if (orders.isEmpty()) {
            Toast.makeText(this, "没有订单需要支付", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] paymentOptions = {"信用卡支付", "现金支付", "微信支付", "支付宝支付"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择支付方式");
        builder.setItems(paymentOptions, (dialog, which) -> {
            double totalAmount = getTotalAmount();

            switch (which) {
                case 0: // 信用卡支付
                    paymentContext.setPaymentStrategy(
                            new CreditCardPayment("1234567890123456", "张三"));
                    break;
                case 1: // 现金支付
                    paymentContext.setPaymentStrategy(
                            new CashPayment(totalAmount + 10)); // 假设给了多10元现金
                    break;
                case 2: // 微信支付
                    paymentContext.setPaymentStrategy(
                            new MobilePayment("微信", "wx_user_123456"));
                    break;
                case 3: // 支付宝支付
                    paymentContext.setPaymentStrategy(
                            new MobilePayment("支付宝", "alipay_user_789"));
                    break;
            }

            processPayment(totalAmount);
        });

        builder.show();
    }

    private void processPayment(double amount) {
        // 在后台线程处理支付
        new Thread(() -> {
            boolean success = paymentContext.executePayment(amount);

            // 在主线程更新UI
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "支付成功！", Toast.LENGTH_LONG).show();

                    // 更新所有订单状态为已支付
                    for (Order order : orders) {
                        orderManager.updateOrderStatus(order.getOrderId(), "已支付");
                    }

                    // 模拟订单处理流程
                    simulateOrderProcessing();

                } else {
                    Toast.makeText(this, "支付失败，请重试", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void simulateOrderProcessing() {
        // 模拟订单处理流程
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                runOnUiThread(() -> {
                    for (Order order : orders) {
                        orderManager.updateOrderStatus(order.getOrderId(), "制作中");
                    }
                });

                Thread.sleep(5000);
                runOnUiThread(() -> {
                    for (Order order : orders) {
                        orderManager.updateOrderStatus(order.getOrderId(), "制作完成");
                    }
                    Toast.makeText(this, "您的咖啡已制作完成，请取餐！", Toast.LENGTH_LONG).show();
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private double getTotalAmount() {
        double totalAmount = 0.0;
        for (Order order : orders) {
            totalAmount += order.getTotalPrice();
        }
        return totalAmount;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新订单列表
        orders.clear();
        orders.addAll(orderManager.getAllOrders());
        orderAdapter.notifyDataSetChanged();
        updateTotalAmount();
    }
}

