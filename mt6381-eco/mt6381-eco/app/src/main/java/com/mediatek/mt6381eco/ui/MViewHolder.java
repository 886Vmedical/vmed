package com.mediatek.mt6381eco.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.ButterKnife;

public class MViewHolder<T> extends RecyclerView.ViewHolder {

  public MViewHolder(View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  public void bind(T data, int position) {
    itemView.setTag(position);
  }
}

