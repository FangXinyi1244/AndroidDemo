package com.qzz.demo2;

// MainActivity.java
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity implements FragmentCallback {

    private ViewPager viewPager;
    private TextView tabHome, tabCategory, tabProfile;
    private TextView tvTitle;
    private MainPagerAdapter pagerAdapter;

    private TextView[] tabViews;
    private String[] tabTitles = {"我的应用 - 首页", "我的应用 - 分类", "我的应用 - 我的"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewPager();
        setupBottomNavigation();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        tabHome = findViewById(R.id.tab_home);
        tabCategory = findViewById(R.id.tab_category);
        tabProfile = findViewById(R.id.tab_profile);
        tvTitle = findViewById(R.id.tv_title);

        tabViews = new TextView[]{tabHome, tabCategory, tabProfile};
    }

    private void setupViewPager() {
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // 设置ViewPager滑动监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 页面滚动时的回调
            }

            @Override
            public void onPageSelected(int position) {
                // 页面选中时同步底部导航状态
                updateBottomNavigation(position);
                updateTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 页面滚动状态改变时的回调
            }
        });

        // 初始化状态
        updateBottomNavigation(0);
        updateTitle(0);
    }

    private void setupBottomNavigation() {
        // 底部导航点击监听
        tabHome.setOnClickListener(v -> switchToPage(0));
        tabCategory.setOnClickListener(v -> switchToPage(1));
        tabProfile.setOnClickListener(v -> switchToPage(2));
    }

    private void switchToPage(int position) {
        if (viewPager.getCurrentItem() != position) {
            viewPager.setCurrentItem(position, true);
        }
    }

    private void updateBottomNavigation(int position) {
        // 重置所有标签状态
        for (TextView tab : tabViews) {
            tab.setAlpha(0.6f);
            tab.setScaleX(1.0f);
            tab.setScaleY(1.0f);
        }

        // 高亮选中的标签
        tabViews[position].setAlpha(1.0f);
        tabViews[position].setScaleX(1.1f);
        tabViews[position].setScaleY(1.1f);
    }

    private void updateTitle(int position) {
        tvTitle.setText(tabTitles[position]);
    }

    // 辅助方法：根据位置查找Fragment
    private Fragment findFragmentByPosition(int position) {
        String tag = "android:switcher:" + R.id.view_pager + ":" + position;
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    private void jumpToNextFragmentWithMessage(String fromTag, String message) {
        // 获取当前Fragment位置
        int currentPosition = viewPager.getCurrentItem();

        // 计算下一个Fragment位置（循环跳转：0->1->2->0）
        int nextPosition = (currentPosition + 1) % 3;

        // 跳转到下一个Fragment
        viewPager.setCurrentItem(nextPosition, true);

        // 延迟传递消息，确保Fragment切换完成
        viewPager.postDelayed(() -> {
            sendMessageToTargetFragment(nextPosition, fromTag, message);
        }, 300); // 300ms延迟确保页面切换完成
    }
    private void sendMessageToTargetFragment(int position, String fromTag, String message) {
        // 通过ViewPager的tag机制找到目标Fragment
        String fragmentTag = "android:switcher:" + R.id.view_pager + ":" + position;
        Fragment targetFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

        if (targetFragment != null) {
            // 根据Fragment类型调用相应的接收方法
            if (targetFragment instanceof HomeFragment) {
                ((HomeFragment) targetFragment).receiveMessageFromPrevious(fromTag, message);
            } else if (targetFragment instanceof CategoryFragment) {
                ((CategoryFragment) targetFragment).receiveMessageFromPrevious(fromTag, message);
            } else if (targetFragment instanceof ProfileFragment) {
                ((ProfileFragment) targetFragment).receiveMessageFromPrevious(fromTag, message);
            }
        }
    }

    // 实现FragmentCallback接口
    @Override
    public void onFragmentMessage(String tag, String message) {
        Toast.makeText(this, "收到来自" + tag + "的消息: " + message, Toast.LENGTH_SHORT).show();
        jumpToNextFragmentWithMessage(tag, message);
    }

    @Override
    public void onFragmentDataChanged(String tag, Object data) {
        // 处理Fragment数据变化
        Toast.makeText(this, tag + "数据已更新", Toast.LENGTH_SHORT).show();
    }
}
