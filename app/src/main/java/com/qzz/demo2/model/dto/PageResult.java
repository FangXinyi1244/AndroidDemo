package com.qzz.demo2.model.dto;

import java.io.Serializable;
import java.util.List;

public class PageResult<T> implements Serializable {
    private List<T> records;
    private Long total;
    private Integer size;
    private Integer current;
    private List<String> orders;
    private Boolean searchCount;
    private Integer pages;
    // Getters and Setters
    public List<T> getRecords() {
        return records;
    }
    public void setRecords(List<T> records) {
        this.records = records;
    }
    public Long getTotal() {
        return total;
    }
    public void setTotal(Long total) {
        this.total = total;
    }
    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    public Integer getCurrent() {
        return current;
    }
    public void setCurrent(Integer current) {
        this.current = current;
    }
    public List<String> getOrders() {
        return orders;
    }
    public void setOrders(List<String> orders) {
        this.orders = orders;
    }
    public Boolean getSearchCount() {
        return searchCount;
    }
    public void setSearchCount(Boolean searchCount) {
        this.searchCount = searchCount;
    }
    public Integer getPages() {
        return pages;
    }
    public void setPages(Integer pages) {
        this.pages = pages;
    }
}
