package com.qzz.coffeeshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qzz.coffeeshop.R;
import com.qzz.coffeeshop.model.Order;
import com.qzz.coffeeshop.pattern.decorator.CoffeeComponent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private SimpleDateFormat dateFormat;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.textViewOrderId.setText("订单 #" + order.getOrderId());
        holder.textViewOrderStatus.setText("状态: " + order.getStatus());
        holder.textViewOrderTime.setText("时间: " + dateFormat.format(new Date(order.getTimestamp())));
        holder.textViewOrderPrice.setText(String.format("¥%.2f", order.getTotalPrice()));

        // 显示咖啡项目
        StringBuilder coffeeItems = new StringBuilder();
        for (CoffeeComponent coffee : order.getCoffeeItems()) {
            coffeeItems.append("• ").append(coffee.getDescription()).append("\n");
        }
        holder.textViewCoffeeItems.setText(coffeeItems.toString().trim());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewOrderId;
        TextView textViewOrderStatus;
        TextView textViewOrderTime;
        TextView textViewOrderPrice;
        TextView textViewCoffeeItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            textViewOrderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            textViewOrderTime = itemView.findViewById(R.id.textViewOrderTime);
            textViewOrderPrice = itemView.findViewById(R.id.textViewOrderPrice);
            textViewCoffeeItems = itemView.findViewById(R.id.textViewCoffeeItems);
        }
    }
}

