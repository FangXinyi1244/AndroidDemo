package com.qzz.demo2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qzz.demo2.Service.CalculatorService;
import com.qzz.demo2.adapter.CalculatorKeyboardAdapter;
import com.qzz.demo2.model.vo.CalculatorKey;

import java.util.ArrayList;
import java.util.List;

public class ServiceActivity extends AppCompatActivity {

    private TextView tvHistory;
    private TextView tvResult;
    private RecyclerView rvKeyboard;
    private StringBuilder currentExpression = new StringBuilder();
    private IMyAidlInterface calculatorService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            calculatorService = IMyAidlInterface.Stub.asInterface(service);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            calculatorService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        // 初始化视图
        initViews();
        // 绑定服务
        initBindService();
    }
    private void initViews() {
        tvHistory = findViewById(R.id.tv_history);
        tvResult = findViewById(R.id.tv_result);
        rvKeyboard = findViewById(R.id.rv_keyboard);

        // 设置RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // 可以根据position返回不同的span size
                return 1; // 默认每个item占1个位置
            }
        });
        rvKeyboard.setLayoutManager(layoutManager);

        List<CalculatorKey> keys = createKeys();
        CalculatorKeyboardAdapter adapter = new CalculatorKeyboardAdapter(keys,
                key -> onKeyClick(key));
        rvKeyboard.setAdapter(adapter);
    }

    private List<CalculatorKey> createKeys() {
        List<CalculatorKey> keys = new ArrayList<>();

        // 第一行：功能键
        keys.add(new CalculatorKey("AC", CalculatorKey.TYPE_FUNCTION));
        keys.add(new CalculatorKey("⌫", CalculatorKey.TYPE_FUNCTION)); // 删除键
        keys.add(new CalculatorKey("=", CalculatorKey.TYPE_FUNCTION));
        keys.add(new CalculatorKey("+", CalculatorKey.TYPE_OPERATOR));

        // 数字键 7-9
        keys.add(new CalculatorKey("7", CalculatorKey.TYPE_NUMBER));
        keys.add(new CalculatorKey("8", CalculatorKey.TYPE_NUMBER));
        keys.add(new CalculatorKey("9", CalculatorKey.TYPE_NUMBER));
        // 运算符
        keys.add(new CalculatorKey("÷", CalculatorKey.TYPE_OPERATOR));

        // 数字键 4-6
        keys.add(new CalculatorKey("4", CalculatorKey.TYPE_NUMBER));
        keys.add(new CalculatorKey("5", CalculatorKey.TYPE_NUMBER));
        keys.add(new CalculatorKey("6", CalculatorKey.TYPE_NUMBER));
        // 运算符
        keys.add(new CalculatorKey("×", CalculatorKey.TYPE_OPERATOR));

        // 数字键 1-3
        keys.add(new CalculatorKey("1", CalculatorKey.TYPE_NUMBER));
        keys.add(new CalculatorKey("2", CalculatorKey.TYPE_NUMBER));
        keys.add(new CalculatorKey("3", CalculatorKey.TYPE_NUMBER));
        // 运算符
        keys.add(new CalculatorKey("-", CalculatorKey.TYPE_OPERATOR));

        // 底部行
        keys.add(new CalculatorKey(".", CalculatorKey.TYPE_NUMBER));
        keys.add(new CalculatorKey("0", CalculatorKey.TYPE_NUMBER));
        keys.add(new CalculatorKey("(", CalculatorKey.TYPE_FUNCTION));
        keys.add(new CalculatorKey(")", CalculatorKey.TYPE_FUNCTION));
        // 运算符


        // 最后一行：进阶运算
        keys.add(new CalculatorKey("x²", CalculatorKey.TYPE_FUNCTION));
        keys.add(new CalculatorKey("x³", CalculatorKey.TYPE_FUNCTION));
        // 可以添加更多进阶运算键

        return keys;
    }

    private void onKeyClick(CalculatorKey key) {
        switch (key.getType()) {
            case CalculatorKey.TYPE_NUMBER:
            case CalculatorKey.TYPE_OPERATOR:
                appendExpression(key.getText());
                break;

            case CalculatorKey.TYPE_FUNCTION:
                handleFunction(key.getText());
                break;
        }
    }
    private void appendExpression(String text) {
        currentExpression.append(text);
        tvResult.setText(currentExpression.toString());
    }
    private void handleFunction(String function) {
        switch (function) {
            case "⌫":
                if (currentExpression.length() > 0) {
                    currentExpression.deleteCharAt(currentExpression.length() - 1);
                    tvResult.setText(currentExpression.toString());
                }
                break;
            case "AC":
                clearExpression();
                break;
            case "=":
                calculateResult();
                break;
            case "x²":
                calculateSquare();
                break;
            case "x³":
                calculateCube();
                break;
            default:
                appendExpression(function);
        }
    }
    private void calculateSquare() {
        if (currentExpression.length() == 0) {
            Toast.makeText(this, "请先输入数字", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // 尝试将当前表达式转换为数字
            double number = Double.parseDouble(currentExpression.toString());

            // 检查Service是否已绑定
            if (calculatorService != null) {
                // 调用Service进行计算
                double result = calculatorService.square(number);

                // 更新历史记录
                tvHistory.setText(number + "²" + " = ");

                // 更新结果显示
                tvResult.setText(String.valueOf(result));

                // 更新当前表达式为计算结果
                currentExpression.setLength(0);
                currentExpression.append(result);
            } else {
                Toast.makeText(this, "计算服务未连接", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "无效的数字格式", Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Toast.makeText(this, "计算服务异常", Toast.LENGTH_SHORT).show();
        }
    }
    private void calculateCube() {
        if (currentExpression.length() == 0) {
            Toast.makeText(this, "请先输入数字", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // 尝试将当前表达式转换为数字
            double number = Double.parseDouble(currentExpression.toString());

            // 检查Service是否已绑定
            if (calculatorService != null) {
                // 调用Service进行计算
                double result = calculatorService.cube(number);

                // 更新历史记录
                tvHistory.setText(number + "³" + " = ");

                // 更新结果显示
                tvResult.setText(String.valueOf(result));

                // 更新当前表达式为计算结果
                currentExpression.setLength(0);
                currentExpression.append(result);
            } else {
                Toast.makeText(this, "计算服务未连接", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "无效的数字格式", Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Toast.makeText(this, "计算服务异常", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearExpression() {
        currentExpression.setLength(0);
        tvResult.setText("0");
        tvHistory.setText("");
    }
    private void calculateResult() {
        try {

            if (!calculatorService.checkExpression(currentExpression.toString())) {
                Toast.makeText(this, "表达式错误", Toast.LENGTH_SHORT).show();
            }else{
                double result = calculatorService.calculate(currentExpression.toString());
                tvHistory.setText(currentExpression.toString());
                tvResult.setText(String.valueOf(result));
                currentExpression.setLength(0);
                currentExpression.append(result);
            }

        } catch (RemoteException e) {
            Toast.makeText(this, "计算错误", Toast.LENGTH_SHORT).show();
        }
    }
    private void initBindService() {
        Intent intent = new Intent(this, CalculatorService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}