package com.qzz.demo2.model.dto;

import java.io.Serializable;

public class Game implements Serializable {
    private Long id;
    private String gameName;
    private String packageName;
    private String appId;
    private String icon;
    private String introduction;
    private String brief;
    private String versionName;
    private String apkUrl;
    private String tags;
    private Double score;
    private String playNumFormat;
    private String createTime;
    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getGameName() {
        return gameName;
    }
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getIntroduction() {
        return introduction;
    }
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
    public String getBrief() {
        return brief;
    }
    public void setBrief(String brief) {
        this.brief = brief;
    }
    public String getVersionName() {
        return versionName;
    }
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    public String getApkUrl() {
        return apkUrl;
    }
    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }
    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
    public Double getScore() {
        return score;
    }
    public void setScore(Double score) {
        this.score = score;
    }
    public String getPlayNumFormat() {
        return playNumFormat;
    }
    public void setPlayNumFormat(String playNumFormat) {
        this.playNumFormat = playNumFormat;
    }
    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}