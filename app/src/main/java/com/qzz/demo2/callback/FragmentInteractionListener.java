package com.qzz.demo2.callback;


import com.qzz.demo2.model.dto.Game;

public interface FragmentInteractionListener {
    void onSearchRequested(String query);
    void onLoadMoreRequested();
    void onGameSaved(Game game, OnSaveResultCallback callback);
    void onGameDeleted(long gameId, OnDeleteResultCallback callback);

    interface OnSaveResultCallback {
        void onSaveSuccess();
        void onSaveFailure(String error);
    }

    interface OnDeleteResultCallback {
        void onDeleteSuccess();
        void onDeleteFailure(String error);
    }
}
