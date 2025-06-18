package com.qzz.demo2.item;

public class ToolItem {
    private String icon;        // 文字图标（表情符号等）
    private String name;        // 工具名称
    private String text;        // 插入的文本
    private int pngResId;       // PNG资源ID（新增）
    private String pngPath;     // PNG文件路径（新增）
    private boolean isPngIcon;  // 是否使用PNG图标（新增）

    // 构造函数1：使用文字图标
    public ToolItem(String icon, String name) {
        this.icon = icon;
        this.name = name;
        this.text = icon;
        this.isPngIcon = false;
    }

    // 构造函数2：使用PNG资源ID
    public ToolItem(int pngResId, String name, String text) {
        this.pngResId = pngResId;
        this.name = name;
        this.text = text;
        this.isPngIcon = true;
    }

    // 构造函数3：使用PNG文件路径
    public ToolItem(String pngPath, String name, String text, boolean fromAssets) {
        this.pngPath = pngPath;
        this.name = name;
        this.text = text;
        this.isPngIcon = true;
    }

    // Getter方法
    public String getIcon() { return icon; }
    public String getName() { return name; }
    public String getText() { return text; }
    public int getPngResId() { return pngResId; }
    public String getPngPath() { return pngPath; }
    public boolean isPngIcon() { return isPngIcon; }
}