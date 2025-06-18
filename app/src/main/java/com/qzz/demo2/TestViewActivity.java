package com.qzz.demo2;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qzz.demo2.item.MessageItem;
import com.qzz.demo2.item.ToolItem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestViewActivity extends AppCompatActivity {
    private ConstraintLayout mainLayout;
    private Spinner fontSpinner;
    private Button themeButton;
    private TextView titleText;
    private EditText richTextEditor;
    private GridLayout toolGridLayout;
    private RecyclerView messageListView;

    private boolean isDarkTheme = false;
    private MessageListAdapter messageAdapter;
    private List<ToolItem> toolList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_view);

        initViews();
        initToolList();
        setupGridLayout();
        setupRecyclerView();
        setupListeners();
        loadThemePreference();
    }

    private void initViews() {
        mainLayout = findViewById(R.id.main_layout);
        fontSpinner = findViewById(R.id.font_spinner);
        themeButton = findViewById(R.id.theme_button);
        titleText = findViewById(R.id.title_text);
        richTextEditor = findViewById(R.id.rich_text_editor);
        toolGridLayout = findViewById(R.id.tool_grid_layout);
        messageListView = findViewById(R.id.message_list_view);

        setupFontSpinner();
        setupRichTextEditor();
    }

    private void setupFontSpinner() {
        String[] fonts = {"默认字体", "宋体", "黑体", "楷体"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fonts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSpinner.setAdapter(adapter);

        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFont(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRichTextEditor() {
        // 初始化富文本内容
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("欢迎使用富文本编辑器！\n");
        int start = builder.length();
        builder.append("这是粗体文字");
        builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n");
        builder.append("这是彩色文字");
        builder.setSpan(new StyleSpan(Color.BLUE), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        richTextEditor.setText(builder);
        richTextEditor.setMovementMethod(LinkMovementMethod.getInstance());

        // 设置选择监听器，实现文本高亮效果
        richTextEditor.setOnClickListener(v -> highlightSelectedText());
        richTextEditor.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                updateEditTextBackground(true);
            } else {
                updateEditTextBackground(false);
            }
        });
    }

    private void highlightSelectedText() {
        int start = richTextEditor.getSelectionStart();
        int end = richTextEditor.getSelectionEnd();

        if (start != end) {
            SpannableStringBuilder builder = new SpannableStringBuilder(richTextEditor.getText());
            // 清除之前的高亮
            BackgroundColorSpan[] spans = builder.getSpans(0, builder.length(), BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans) {
                builder.removeSpan(span);
            }
            // 添加新的高亮
            builder.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            richTextEditor.setText(builder);
            richTextEditor.setSelection(start, end);
        }
    }

    private void updateEditTextBackground(boolean focused) {
        int backgroundRes = isDarkTheme ?
                (focused ? R.drawable.edittext_border_focus_dark : R.drawable.edittext_border_dark) :
                (focused ? R.drawable.edittext_border_focus : R.drawable.edittext_border);
        richTextEditor.setBackgroundResource(backgroundRes);
    }

    // 工具列表初始化
    private void initToolList() {
        toolList = new ArrayList<>();


        // 12个工具按你的命名风格
        toolList.add(new ToolItem(R.drawable.img1, "加粗", "**文字**"));
        toolList.add(new ToolItem(R.drawable.img2, "斜体", "*文字*"));
        toolList.add(new ToolItem(R.drawable.img3, "代码", "`代码`"));
        toolList.add(new ToolItem(R.drawable.img4, "标题", "## 标题"));
        toolList.add(new ToolItem(R.drawable.img5, "有序列表", "1. 项目"));
        toolList.add(new ToolItem(R.drawable.img6, "无序列表", "- 项目"));
        toolList.add(new ToolItem(R.drawable.img7, "引用", "> 引用内容"));
        toolList.add(new ToolItem(R.drawable.img8, "表格", "| 列1 | 列2 |\n|-----|-----|\n| 内容 | 内容 |"));
        toolList.add(new ToolItem(R.drawable.img9, "提问", "问题：\n背景：\n期望："));
        toolList.add(new ToolItem(R.drawable.img10, "建议", "请为以下情况提供建议：\n"));
        toolList.add(new ToolItem(R.drawable.img11, "分隔", "---"));
        toolList.add(new ToolItem(R.drawable.img12, "清空", ""));


    }



    private void setupGridLayout() {
        toolGridLayout.removeAllViews();

        // 设置GridLayout参数：4列，行数自动计算
        int columnCount = 4;
        int rowCount = (int) Math.ceil((double) toolList.size() / columnCount);

        toolGridLayout.setColumnCount(columnCount);
        toolGridLayout.setRowCount(rowCount);

        // 动态添加工具项到GridLayout
        for (int i = 0; i < toolList.size(); i++) {
            ToolItem tool = toolList.get(i);
            View toolView = createToolView(tool, i);
            toolGridLayout.addView(toolView);
        }
    }

    private View createToolView(ToolItem tool, int position) {
        // 创建工具项的容器布局
        LinearLayout toolContainer = new LinearLayout(this);
        toolContainer.setOrientation(LinearLayout.VERTICAL);
        toolContainer.setGravity(Gravity.CENTER);
        toolContainer.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        // 设置GridLayout.LayoutParams
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        int margin = dpToPx(4);
        params.setMargins(margin, margin, margin, margin);
        params.width = 0;
        params.height = dpToPx(100);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        toolContainer.setLayoutParams(params);
        // 创建圆形图标容器
        LinearLayout iconContainer = new LinearLayout(this);
        iconContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(
                dpToPx(50), dpToPx(50));
        iconContainer.setLayoutParams(iconContainerParams);
        // 设置圆形背景和点击效果
        iconContainer.setBackground(getToolBackground());
        iconContainer.setClickable(true);
        iconContainer.setFocusable(true);
        // 根据图标类型创建不同的View
        if (tool.isPngIcon()) {
            // 使用ImageView显示PNG图标
            ImageView iconView = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                    dpToPx(32), dpToPx(32));
            iconView.setLayoutParams(iconParams);
            iconView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // 加载PNG图片
            loadPngIcon(iconView, tool);
            iconContainer.addView(iconView);
        } else {
            // 使用TextView显示文字图标
            TextView iconView = new TextView(this);
            iconView.setText(tool.getIcon());
            iconView.setTextSize(20);
            iconView.setGravity(Gravity.CENTER);
            iconContainer.addView(iconView);
        }
        // 创建名称TextView
        TextView nameView = new TextView(this);
        nameView.setText(tool.getName());
        nameView.setTextSize(10);
        nameView.setGravity(Gravity.CENTER);
        nameView.setTextColor(isDarkTheme ? Color.WHITE : Color.BLACK);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.topMargin = dpToPx(4);
        nameView.setLayoutParams(nameParams);
        // 添加子View到容器
        toolContainer.addView(iconContainer);
        toolContainer.addView(nameView);
        // 设置点击事件
        iconContainer.setOnClickListener(v -> onToolClick(tool, iconContainer));
        return toolContainer;
    }

    private void loadPngIcon(ImageView imageView, ToolItem tool) {
        try {
            if (tool.getPngResId() != 0) {
                // 从drawable资源加载
                imageView.setImageResource(tool.getPngResId());
            } else if (tool.getPngPath() != null) {
                // 从assets文件夹加载
                InputStream inputStream = getAssets().open(tool.getPngPath());
                Drawable drawable = Drawable.createFromStream(inputStream, null);
                imageView.setImageDrawable(drawable);
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 加载失败时显示默认图标
//            imageView.setImageResource(R.drawable.ic_default_tool);
        }
    }


    private Drawable getToolBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ContextCompat.getDrawable(this,
                    isDarkTheme ? R.drawable.ripple_circle_dark : R.drawable.ripple_circle);
        } else {
            return ContextCompat.getDrawable(this,
                    isDarkTheme ? R.drawable.tool_button_circle_dark : R.drawable.tool_button_circle);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager listManager = new LinearLayoutManager(this);
        messageListView.setLayoutManager(listManager);
        messageAdapter = new MessageListAdapter(getMessageList(),
                this::onMessageClick, this::onMessageDelete);
        messageListView.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        themeButton.setOnClickListener(v -> toggleTheme());
    }

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        applyTheme();
        saveThemePreference();
    }

    private void applyTheme() {
        int backgroundColor = isDarkTheme ? Color.parseColor("#2C2C2C") : Color.WHITE;
        int textColor = isDarkTheme ? Color.WHITE : Color.BLACK;

        mainLayout.setBackgroundColor(backgroundColor);
        titleText.setTextColor(textColor);
        richTextEditor.setTextColor(textColor);
        updateEditTextBackground(richTextEditor.hasFocus());

        themeButton.setText(isDarkTheme ? "浅色主题" : "深色主题");

        // 更新GridLayout中的工具项主题
        setupGridLayout();

        // 通知消息列表适配器更新
        messageAdapter.updateTheme(isDarkTheme);
    }

    private void applyFont(int fontType) {
        Typeface typeface;
        switch (fontType) {
            case 1: typeface = Typeface.SERIF; break;
            case 2: typeface = Typeface.SANS_SERIF; break;
            case 3: typeface = Typeface.MONOSPACE; break;
            default: typeface = Typeface.DEFAULT; break;
        }
        richTextEditor.setTypeface(typeface);
    }

    private void onToolClick(ToolItem tool, View clickedView) {
        // 在光标位置插入表情或文字
        int start = richTextEditor.getSelectionStart();
        int end = richTextEditor.getSelectionEnd();

        SpannableStringBuilder builder = new SpannableStringBuilder(richTextEditor.getText());
        builder.replace(start, end, tool.getText());
        richTextEditor.setText(builder);
        richTextEditor.setSelection(start + tool.getText().length());

        // 添加点击动画效果
        animateToolClick(clickedView);
    }

    private void animateToolClick(View view) {
        if (view != null) {
            view.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() ->
                            view.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(100)
                                    .setInterpolator(new OvershootInterpolator())
                                    .start()
                    )
                    .start();
        }
    }

    private void onMessageClick(MessageItem message) {
        // 高亮显示点击的消息文本
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(message.getContent());
        builder.setSpan(new BackgroundColorSpan(Color.CYAN), 0, builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        richTextEditor.setText(builder);
        richTextEditor.setSelection(builder.length());
    }

    private void onMessageDelete(int position) {
        messageAdapter.removeItem(position);
    }

    // 工具方法
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // 消息列表初始化
    private List<MessageItem> getMessageList() {
        List<MessageItem> messages = new ArrayList<>();
        // 角色设定类提示词
        messages.add(new MessageItem("你是一个专业的AI助手，请用简洁准确的语言回答问题"));
        messages.add(new MessageItem("你是一个代码专家，擅长解释编程概念和调试代码"));
        messages.add(new MessageItem("你是一个创意写作助手，帮助用户进行文案创作"));
        messages.add(new MessageItem("你是一个学习导师，善于分步骤解释复杂概念"));

        // 功能介绍类提示词
        messages.add(new MessageItem("请帮我分析这段代码的功能和潜在问题"));
        messages.add(new MessageItem("请为我生成一份详细的技术文档"));
        messages.add(new MessageItem("请帮我优化这个算法的性能"));
        messages.add(new MessageItem("请为我解释这个概念并举例说明"));

        // 使用指导类提示词
        messages.add(new MessageItem("请用markdown格式整理信息"));
        messages.add(new MessageItem("请分步骤详细说明解决方案"));
        messages.add(new MessageItem("请先总结要点，再详细展开"));
        messages.add(new MessageItem("请提供多个角度的分析"));

        // 场景应用类提示词
        messages.add(new MessageItem("帮我制定一个学习计划"));
        messages.add(new MessageItem("帮我分析这个商业案例"));
        messages.add(new MessageItem("帮我写一份项目总结报告"));
        messages.add(new MessageItem("帮我准备面试问题和答案"));
        return messages;
    }

    private void saveThemePreference() {
        SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("dark_theme", isDarkTheme).apply();
    }

    private void loadThemePreference() {
        SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean("dark_theme", false);
        applyTheme();
    }
}