package com.qzz.tagcloud.adapter;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class TagAdapter {
    private List<DataSetObserver> mObservers = new ArrayList<>();

    public abstract int getCount();
    public abstract View getView(int position, View convertView, ViewGroup parent);

    /**
     * 注册数据集观察者
     * @param observer 观察者对象
     */
    public void registerDataSetObserver(DataSetObserver observer) {
        if (observer != null && !mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    /**
     * 注销数据集观察者
     * @param observer 观察者对象
     */
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            mObservers.remove(observer);
        }
    }

    /**
     * 通知数据集已更改
     */
    public void notifyDataSetChanged() {
        // 创建副本避免并发修改异常
        for (DataSetObserver observer : new ArrayList<>(mObservers)) {
            observer.onChanged();
        }
    }

    /**
     * 通知数据集失效
     */
    public void notifyDataSetInvalidated() {
        // 创建副本避免并发修改异常
        for (DataSetObserver observer : new ArrayList<>(mObservers)) {
            observer.onInvalidated();
        }
    }

    /**
     * 获取指定位置的数据项
     * 子类可重写此方法提供具体的数据项
     * @param position 位置索引
     * @return 数据项，默认返回null
     */
    public Object getItem(int position) {
        return null;
    }

    /**
     * 获取指定位置的数据项ID
     * 子类可重写此方法提供具体的ID
     * @param position 位置索引
     * @return 数据项ID，默认返回position
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * 判断数据集是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return getCount() == 0;
    }

    /**
     * 清理资源
     * 在适配器不再使用时调用
     */
    public void clear() {
        mObservers.clear();
    }
}
