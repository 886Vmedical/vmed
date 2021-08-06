package com.mediatek.mt6381eco.biz.historyrecord;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.ui.LoadMoreAdapter;
import com.mediatek.mt6381eco.ui.MViewHolder;
import com.mediatek.mt6381eco.utils.MTextUtils;
import java.util.ArrayList;

public class HistoryRecordAdapter extends LoadMoreAdapter<HistoryRecordViewItem> {

  public HistoryRecordAdapter(Context context, ArrayList<HistoryRecordViewItem> data) {
    super(context, data);
  }

  @NonNull protected MViewHolder getItemViewHolder(ViewGroup parent) {
    return new MItemViewHolder(mInflater.inflate(R.layout.list_item_history_record, parent, false));
  }

  @Override public void onBindViewHolder(MViewHolder holder, int position) {
    super.onBindViewHolder(holder, position);
    if (holder instanceof MItemViewHolder) {
      ((MItemViewHolder) holder).bind(mData.get(position), position);
    }
  }

  public class MItemViewHolder extends MViewHolder {
    @BindView(R.id.txt_date) TextView mTxtDate;
    @BindView(R.id.txt_time) TextView mTxtTime;

    public MItemViewHolder(View view) {
      super(view);
    }

    public void bind(HistoryRecordViewItem item, int position) {
      super.bind(item, position);
      mTxtDate.setText(MTextUtils.formatDate(item.timestamp));
      mTxtTime.setText(MTextUtils.formatTime(item.timestamp));
    }
  }
}
