package com.mediatek.mt6381eco.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import com.mediatek.mt6381eco.R;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;

public abstract class LoadMoreAdapter<T> extends RecyclerView.Adapter<MViewHolder>
    implements View.OnClickListener {
  public static final int TYPE_ITEM = 0;
  public static final int TYPE_LOAD_MORE = 1;
  public static final int TYPE_LOAD_ERROR = 2;
  public static final int TYPE_LOAD_END = 3;
  protected final LayoutInflater mInflater;
  private final PublishSubject<Integer> mOnClickSubject = PublishSubject.create();
  private final Context mContext;
  protected ArrayList<T> mData;
  private int mLoadMoreState = TYPE_LOAD_MORE;
  private String mErrorMessage;

  public LoadMoreAdapter(Context context, ArrayList<T> data) {
    mContext = context;
    mData = data;
    mInflater = LayoutInflater.from(context);
  }

  public void append(T[] items) {
    for (T item : items) {
      mData.add(item);
    }
  }

  @Override public void onClick(View view) {
    mOnClickSubject.onNext((Integer) view.getTag());
  }

  public void loadMoreError(String message) {
    mErrorMessage = message;
    mLoadMoreState = TYPE_LOAD_ERROR;
  }

  @Override public int getItemViewType(int position) {
    if (position < getDataCount()) {
      return TYPE_ITEM;
    } else {
      return mLoadMoreState;
    }
  }

  public int getAdditionItem() {
    return 1;
  }

  public void noMoreData() {
    mLoadMoreState = TYPE_LOAD_END;
  }

  public T getItem(int position) {
    return mData.get(position);
  }

  @Override public int getItemCount() {
    return getDataCount() + getAdditionItem();
  }

  public int getDataCount() {
    return mData.size();
  }

  @Override public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final MViewHolder mViewHolder;
    switch (viewType) {
      case TYPE_LOAD_MORE: {
        mViewHolder =
            new MLoadMoreViewHolder(mInflater.inflate(R.layout.list_item_load_more, parent, false));
        break;
      }
      case TYPE_LOAD_ERROR: {
        mViewHolder = new MLoadErrorViewHolder(
            mInflater.inflate(R.layout.list_item_load_error, parent, false));
        break;
      }
      case TYPE_LOAD_END: {
        mViewHolder =
            new MViewHolder(mInflater.inflate(R.layout.list_item_load_end, parent, false));
        break;
      }
      default: {
        mViewHolder = getItemViewHolder(parent);
        break;
      }
    }
    mViewHolder.itemView.setOnClickListener(this);
    return mViewHolder;
  }

  protected abstract MViewHolder getItemViewHolder(ViewGroup parent);

  public void resetLoadMoreState() {
    mLoadMoreState = TYPE_LOAD_MORE;
  }

  public PublishSubject<Integer> getPositionClicks() {
    return mOnClickSubject;
  }

  @Override public void onBindViewHolder(MViewHolder holder, int position) {
    if (holder instanceof MLoadErrorViewHolder) {
      holder.bind(mErrorMessage, position);
    }
    holder.itemView.setTag(position);
  }

  private class MLoadMoreViewHolder extends MViewHolder<Void> {
    public MLoadMoreViewHolder(View itemView) {
      super(itemView);
    }
  }

  public class MLoadErrorViewHolder<T> extends MViewHolder<String> {
    @BindView(R.id.txt_message) TextView mTxtMessage;

    public MLoadErrorViewHolder(View itemView) {
      super(itemView);
    }

    @Override public void bind(String message, int position) {
      super.bind(message, position);
      mTxtMessage.setText(mContext.getString(R.string.load_more_error, message));
    }
  }
}
