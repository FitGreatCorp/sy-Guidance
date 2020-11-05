package com.fitgreat.archmvp.base.ui;

/**
 * MVPPlugin
 * Mvp presenter接口基类
 *
 * @author zixuefei
 * @since 2019/5/23 20:38
 */

public interface BasePresenter<V extends BaseView> {
    void attachView(V view);

    void detachView();
}
