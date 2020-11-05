package com.fitgreat.archmvp.base.ui;

/**
 * MVPPlugin
 * Mvp presenter接口基类实现类
 *
 * @author zixuefei
 * @since 2019/5/23 20:38
 */

public class BasePresenterImpl<V extends BaseView> implements BasePresenter<V> {
    protected String TAG = getClass().getSimpleName();
    protected V mView;

    @Override
    public void attachView(V view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }
}
