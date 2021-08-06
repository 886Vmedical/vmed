package com.mediatek.mt6381eco.mvp;

public interface BaseView {
  void startLoading(Object ...args);
  void stopLoading();
  void showError(Throwable throwable);
}
