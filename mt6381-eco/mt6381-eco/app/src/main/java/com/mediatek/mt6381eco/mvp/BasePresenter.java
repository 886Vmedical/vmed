package com.mediatek.mt6381eco.mvp;

public interface BasePresenter<T extends BaseView> {
  void setView(T view);

  void destroy();
}
