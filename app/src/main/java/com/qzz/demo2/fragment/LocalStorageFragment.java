package com.qzz.demo2.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qzz.demo2.MainActivity;
import com.qzz.demo2.R;
import com.qzz.demo2.adapter.LocalStorageAdapter;
import com.qzz.demo2.callback.FragmentInteractionListener;
import com.qzz.demo2.model.dao.GameDAO;
import com.qzz.demo2.model.dto.Game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocalStorageFragment extends Fragment {
    private static final String TAG = "LocalStorageFragment";

    private RecyclerView recyclerView;
    private TextView emptyView;
    private TextView refreshInfo;
    private ProgressBar refreshProgress;

    private LocalStorageAdapter adapter;
    private List<Game> localGameList = new ArrayList<>();
    private FragmentInteractionListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            listener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_storage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        updateRefreshInfo();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.local_storage_recycler_view);
        emptyView = view.findViewById(R.id.empty_local_storage);
        refreshInfo = view.findViewById(R.id.refresh_info);
        refreshProgress = view.findViewById(R.id.refresh_progress);
    }

    private void setupRecyclerView() {
        adapter = new LocalStorageAdapter(localGameList, new LocalStorageAdapter.OnItemActionListener() {
            @Override
            public void onDeleteGame(Game game, int position) {
                deleteGameFromDatabase(game, position);
            }

            @Override
            public void onGameClick(Game game) {
                Toast.makeText(getContext(), "点击了本地游戏: " + game.getGameName(), Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void updateLocalData(List<Game> games) {
        // 首先检查Fragment是否还附加到Activity并且视图是否已创建
        if (!isAdded() || getView() == null) {
            Log.d(TAG, "视图未创建或Fragment已分离，跳过更新");
            return;
        }

        // 检查控件是否为null
        if (refreshProgress == null) {
            Log.e(TAG, "refreshProgress为null，可能视图未正确初始化");
            // 尝试重新初始化视图
            View view = getView();
            if (view != null) {
                refreshProgress = view.findViewById(R.id.refresh_progress);
            }
            // 如果仍然为null，则跳过更新
            if (refreshProgress == null) {
                Log.e(TAG, "无法初始化refreshProgress，跳过更新");
                return;
            }
        }
        // 设置进度条可见
        refreshProgress.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 再次检查Fragment是否还附加到Activity
            if (!isAdded()) {
                Log.d(TAG, "Fragment已分离，跳过UI更新");
                return;
            }

            // 更新数据
            localGameList.clear();
            if (games != null) {
                localGameList.addAll(games);
            }
            // 更新UI，都添加空值检查
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            updateEmptyView();

            if (refreshProgress != null) {
                refreshProgress.setVisibility(View.GONE);
            }

            updateRefreshInfo();
            Log.d(TAG, "本地存储数据已更新，当前显示 " + localGameList.size() + " 条数据");
        }, 500);
    }

    private void deleteGameFromDatabase(Game game, int position) {
        if (listener != null && game != null) {
            listener.onGameDeleted(game.getId(), new FragmentInteractionListener.OnDeleteResultCallback() {
                @Override
                public void onDeleteSuccess() {
                    localGameList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, localGameList.size());
                    updateEmptyView();
                    Toast.makeText(getContext(),
                            "游戏 \"" + game.getGameName() + "\" 已删除",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDeleteFailure(String error) {
                    Toast.makeText(getContext(),
                            "删除失败: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateEmptyView() {
        if (!isAdded() || getView() == null) return;

        if (emptyView == null || recyclerView == null) return;

        if (localGameList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void updateRefreshInfo() {
        if (!isAdded() || refreshInfo == null) return;

        long currentTime = System.currentTimeMillis();
        String timeInfo = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(currentTime));
        refreshInfo.setText("最后更新: " + timeInfo + " | 每5秒自动刷新，显示5条数据");
    }
}


