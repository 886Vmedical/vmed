package com.mediatek.mt6381eco.biz.screening.history;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.utils.MTextUtils;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ScreeningListAdapter extends RecyclerView.Adapter<ScreeningListAdapter.MViewHolder>
    implements View.OnClickListener {
  public static final int TYPE_ITEM = 0;
  public static final int TYPE_LOAD_MORE = 1;
  public static final int TYPE_LOAD_ERROR = 2;
  public static final int TYPE_LOAD_END = 3;

  private final PublishSubject<Integer> mOnClickSubject = PublishSubject.create();
  private final LayoutInflater mInflater;
  private final Context mContext;
  private int mLoadMoreState = TYPE_LOAD_MORE;

  private ArrayList<HistoryViewItem> mData = new ArrayList<>();
  private String mErrorMessage;

  public ScreeningListAdapter(Context context) {
    mInflater = LayoutInflater.from(context);
    mContext = context;
  }

  public void setDate(ArrayList<HistoryViewItem> data) {
    mData = data;
    notifyDataSetChanged();
  }

  public Observable<Integer> getPositionClicks() {
    return mOnClickSubject.debounce(400, TimeUnit.MILLISECONDS);
  }

  @Override
  public ScreeningListAdapter.MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final ScreeningListAdapter.MViewHolder viewHolder;
    switch (viewType) {
      case TYPE_LOAD_MORE: {
        viewHolder =
            new MLoadMoreViewHolder(mInflater.inflate(R.layout.list_item_load_more, parent, false));
        break;
      }
      case TYPE_LOAD_ERROR: {
        viewHolder = new MLoadErrorViewHolder(
            mInflater.inflate(R.layout.list_item_load_error, parent, false));
        break;
      }
      case TYPE_LOAD_END: {
        viewHolder =
            new MViewHolder(mInflater.inflate(R.layout.list_item_load_end, parent, false)) {
            };
        break;
      }
      default:
        viewHolder = new MItemViewHolder(
            mInflater.inflate(R.layout.list_item_screening_history, parent, false));
        break;
    }
    viewHolder.itemView.setOnClickListener(this);
    return viewHolder;
  }

  @Override public void onBindViewHolder(MViewHolder holder, int position) {
    if (position < getDataCount() && holder instanceof MItemViewHolder) {
      holder.bind(mData.get(position), position);
    } else if (holder instanceof MLoadErrorViewHolder) {
      holder.bind(mErrorMessage, position);
    }
    holder.itemView.setTag(position);
  }

  @Override public int getItemViewType(int position) {
    if (position < getDataCount()) {
      return TYPE_ITEM;
    }
    return mLoadMoreState;
  }

  @Override public int getItemCount() {
    return getDataCount() + getAdditionItem();
  }

  public HistoryViewItem getItem(int position) {
    return mData.get(position);
  }

  public int getDataCount() {
    return mData.size();
  }

  public int getAdditionItem() {
    return 1;
  }

  public void loadMoreError(String message) {
    mErrorMessage = message;
    mLoadMoreState = TYPE_LOAD_ERROR;
  }

  public void noMoreData() {
    mLoadMoreState = TYPE_LOAD_END;
  }

  public void resetLoadMoreState() {
    mLoadMoreState = TYPE_LOAD_MORE;
  }

  public void append(HistoryViewItem[] items) {
    for (HistoryViewItem item : items) {
      mData.add(item);
    }
  }

  @Override public void onClick(View view) {
    mOnClickSubject.onNext((Integer) view.getTag());
  }

  public class MViewHolder<T> extends RecyclerView.ViewHolder {

    public MViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void bind(T data, int position) {
      itemView.setTag(position);
    }
  }

  public class MItemViewHolder extends MViewHolder<HistoryViewItem> {
    @BindView(R.id.txt_risk) TextView txtRisk;
    @BindView(R.id.txt_timestamp) TextView txtTimestamp;

    public MItemViewHolder(View itemView) {
      super(itemView);
    }

    @Override public void bind(HistoryViewItem item, int position) {
      super.bind(item, position);
      txtTimestamp.setText(MTextUtils.formatDateTime(item.timestamp));
      final int[] iconIds =
          new int[] { R.drawable.img_risk_no, R.drawable.img_risk_low, R.drawable.img_risk_high };
      int iconId = iconIds[item.riskLevel];
      txtRisk.setText(
          BizUtils.getHeartRateRiskText(mContext, item.riskLevel, item.riskProbability));
      Drawable img = ContextCompat.getDrawable(mContext, iconId);
      txtRisk.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }
  }

  private class MLoadMoreViewHolder extends MViewHolder<Void> {
    public MLoadMoreViewHolder(View itemView) {
      super(itemView);
    }
  }

  public class MLoadErrorViewHolder extends MViewHolder<String> {
    @BindView(R.id.txt_message) TextView mTxtMessage;

    public MLoadErrorViewHolder(View itemView) {
      super(itemView);
    }

    @Override public void bind(String message, int position) {
      super.bind(message, position);
      mTxtMessage.setText(mContext.getString(R.string.screening_error_formatter, message));
    }
  }
}
