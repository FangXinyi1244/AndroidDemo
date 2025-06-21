package com.qzz.compenenttest.model;

public class ImageItem {
    private String imageUrl;
    private String id;
    private int width;
    private int height;

    public ImageItem(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ImageItem(String imageUrl, String id, int width, int height) {
        this.imageUrl = imageUrl;
        this.id = id;
        this.width = width;
        this.height = height;
    }

    // Getters and setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}
