package com.qzz.demo2.fragment;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.os.Bundle;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.qzz.demo2.MainActivity;
import com.qzz.demo2.R;
import com.qzz.demo2.adapter.SearchResultsAdapter;
import com.qzz.demo2.callback.FragmentInteractionListener;
import com.qzz.demo2.model.dao.GameDAO;
import com.qzz.demo2.model.dto.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResultsFragment extends Fragment {
    private static final String TAG = "SearchResultsFragment";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar loadMoreProgress;
    private TextView emptyView;

    private SearchResultsAdapter adapter;
    private List<Game> gameList = new ArrayList<>();
    private FragmentInteractionListener listener;
    private boolean isLoading = false;
    private String currentQuery = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 使用接口方式进行通信
        if (context instanceof FragmentInteractionListener) {
            listener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // 避免内存泄漏
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.search_results_recycler_view);
        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        emptyView = view.findViewById(R.id.empty_view);
    }

    private void setupRecyclerView() {
        adapter = new SearchResultsAdapter(gameList, new SearchResultsAdapter.OnItemActionListener() {
            @Override
            public void onSaveGame(Game game, int position) {
                saveGameToDatabase(game, position);
            }

            @Override
            public void onGameClick(Game game) {
                Toast.makeText(getContext(), "点击了游戏: " + game.getGameName(), Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 添加滚动监听，实现上拉加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && !isLoading && !currentQuery.isEmpty()) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null) {
                        int visibleItemCount = lm.getChildCount();
                        int totalItemCount = lm.getItemCount();
                        int pastVisibleItems = lm.findFirstVisibleItemPosition();

                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 5) {
                            loadMoreResults();
                        }
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!currentQuery.isEmpty() && listener != null) {
                listener.onSearchRequested(currentQuery);
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void updateSearchResults(List<Game> games) {
        gameList.clear();
        if (games != null) {
            gameList.addAll(games);
        }

        adapter.notifyDataSetChanged();
        updateEmptyView();
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
    }

    public void appendSearchResults(List<Game> games) {
        if (games != null && !games.isEmpty()) {
            int insertPosition = gameList.size();
            gameList.addAll(games);
            adapter.notifyItemRangeInserted(insertPosition, games.size());
        }

        loadMoreProgress.setVisibility(View.GONE);
        isLoading = false;
    }

    public void clearResults() {
        currentQuery = "";
        gameList.clear();
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    public void setCurrentQuery(String query) {
        this.currentQuery = query;
    }

    private void loadMoreResults() {
        if (!isLoading && listener != null) {
            isLoading = true;
            loadMoreProgress.setVisibility(View.VISIBLE);
            listener.onLoadMoreRequested();
        }
    }

    private void saveGameToDatabase(Game game, int position) {
        if (listener != null && game != null) {
            listener.onGameSaved(game, new FragmentInteractionListener.OnSaveResultCallback() {
                @Override
                public void onSaveSuccess() {
                    adapter.markGameAsSaved(position);
                    Toast.makeText(getContext(),
                            "游戏 \"" + game.getGameName() + "\" 已保存到本地",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSaveFailure(String error) {
                    Toast.makeText(getContext(),
                            "保存失败: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateEmptyView() {
        if (gameList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}


