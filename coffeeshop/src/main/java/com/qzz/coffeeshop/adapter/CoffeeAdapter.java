
package com.qzz.coffeeshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qzz.coffeeshop.R;
import com.qzz.coffeeshop.activity.CoffeeCustomizationActivity;
import com.qzz.coffeeshop.model.Coffee;

import java.util.List;

public class CoffeeAdapter extends RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder> {

    private Context context;
    private List<Coffee> coffeeList;

    public CoffeeAdapter(Context context, List<Coffee> coffeeList) {
        this.context = context;
        this.coffeeList = coffeeList;
    }

    @NonNull
    @Override
    public CoffeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_coffee, parent, false);
        return new CoffeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoffeeViewHolder holder, int position) {
        Coffee coffee = coffeeList.get(position);
        holder.coffeeName.setText(coffee.getName());
        holder.coffeePrice.setText(String.format("¥%.2f", coffee.getPrice()));

        holder.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到咖啡定制页面
                Intent intent = new Intent(context, CoffeeCustomizationActivity.class);
                intent.putExtra("coffee_type", coffee.getName().toLowerCase());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coffeeList.size();
    }

    public static class CoffeeViewHolder extends RecyclerView.ViewHolder {
        TextView coffeeName;
        TextView coffeePrice;
        Button buttonAdd;

        public CoffeeViewHolder(@NonNull View itemView) {
            super(itemView);
            coffeeName = itemView.findViewById(R.id.textViewCoffeeName);
            coffeePrice = itemView.findViewById(R.id.textViewCoffeePrice);
            buttonAdd = itemView.findViewById(R.id.buttonAddCoffee);
        }
    }
}

