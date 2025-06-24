package com.qzz.coffeeshop.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.qzz.coffeeshop.R;
import com.qzz.coffeeshop.adapter.CoffeeAdapter;
import com.qzz.coffeeshop.model.Coffee;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCoffeeMenu;
    private CoffeeAdapter coffeeAdapter;
    private List<Coffee> coffeeList;
    private Button buttonViewOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewCoffeeMenu = findViewById(R.id.recyclerViewCoffeeMenu);
        buttonViewOrder = findViewById(R.id.buttonViewOrder);

        coffeeList = new ArrayList<>();
        // 示例咖啡数据
        coffeeList.add(new Coffee("Espresso", 20.0));
        coffeeList.add(new Coffee("Latte", 25.0));
        coffeeList.add(new Coffee("Cappuccino", 28.0));
        coffeeList.add(new Coffee("Mocha", 30.0));

        coffeeAdapter = new CoffeeAdapter(this, coffeeList);
        recyclerViewCoffeeMenu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCoffeeMenu.setAdapter(coffeeAdapter);

        buttonViewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OrderDetailActivity.class);
                startActivity(intent);
            }
        });
    }
}

