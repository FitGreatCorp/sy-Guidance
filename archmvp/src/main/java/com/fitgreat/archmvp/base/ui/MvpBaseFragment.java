package com.fitgreat.archmvp.base.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * MVPPlugin
 * Mvp fragment基类
 *
 * @author zixuefei
 * @since 2019/5/23 20:38
 */

public abstract class MvpBaseFragment<V extends BaseView, T extends BasePresenterImpl<V>> extends Fragment implements BaseView {
    public String TAG = this.getClass().getSimpleName();
    public T mPresenter;
    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView((V) this);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(getLayoutResource(), container, false);
        unbinder = ButterKnife.bind(this, root);
        initData();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
        }

        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
    }

    @Override
    public Context getContext() {
        return super.getContext();
    }

    public abstract <T> T createPresenter();

    public abstract int getLayoutResource();

    public abstract void initData();
}
