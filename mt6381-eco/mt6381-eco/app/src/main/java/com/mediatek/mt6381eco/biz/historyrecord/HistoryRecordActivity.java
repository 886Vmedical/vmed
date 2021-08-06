package com.mediatek.mt6381eco.biz.historyrecord;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import com.gturedi.views.StatefulLayout;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.recorddetail.RecordDetailActivity;
import com.mediatek.mt6381eco.biz.screening.history.ScreeningListAdapter;
import com.mediatek.mt6381eco.ui.BaseActivity;
import io.reactivex.disposables.CompositeDisposable;
import java.util.ArrayList;
import javax.inject.Inject;

public class HistoryRecordActivity extends BaseActivity implements HistoryRecordContract.View {

  private static final int LIMIT = 10;
  @Inject HistoryRecordContract.Presenter mPresenter;
  @BindView(R.id.list_view) RecyclerView mListView;
  @BindView(R.id.state_layout) StatefulLayout mStateLayout;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private HistoryRecordAdapter mAdapter;
  private boolean mInLoading = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history_record);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    setView();
    uiEvent();

    firstLoad();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        finish();
        break;
      }
    }
    return true;
  }

  private void setView() {
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    mListView.setLayoutManager(layoutManager);
    mAdapter = new HistoryRecordAdapter(this, new ArrayList<>());
    mListView.setAdapter(mAdapter);
  }

  private void firstLoad() {
    mStateLayout.showLoading();
    loadNextPaging(0);
  }

  private void uiEvent() {
    mDisposables.add(mAdapter.getPositionClicks().subscribe(position -> {
      int viewType = mAdapter.getItemViewType(position);
      if (ScreeningListAdapter.TYPE_LOAD_ERROR == viewType) {
        mAdapter.resetLoadMoreState();
        mAdapter.notifyDataSetChanged();
        loadNextPaging(position);
      } else if (ScreeningListAdapter.TYPE_ITEM == viewType) {
        HistoryRecordViewItem item =
            ((HistoryRecordAdapter) mListView.getAdapter()).getItem(position);
        Intent intent = new Intent(this, RecordDetailActivity.class);
        intent.putExtra(RecordDetailActivity.TIMESTAMP, item.timestamp);
        intent.putExtra(RecordDetailActivity.MEASUREMENT_ID, item.measurementId);
        intent.putExtra(RecordDetailActivity.PROFILE_ID, item.profileId);
        startActivity(intent);
      }
    }));

    mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int lastCompletelyVisibleItemPosition =
            ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
        int viewType = mAdapter.getItemViewType(lastCompletelyVisibleItemPosition);
        if (!mInLoading && viewType == ScreeningListAdapter.TYPE_LOAD_MORE) {
          mInLoading = true;
          int offSet = lastCompletelyVisibleItemPosition;
          loadNextPaging(offSet);
        }
      }
    });
  }

  private void loadNextPaging(int offSet) {
    mDisposables.add(mPresenter.requestLoadRecords(LIMIT, offSet)
        .subscribe(this::onLoadSuccess, this::onLoadError));
  }

  private void onLoadSuccess(HistoryRecordViewItem[] recordItems) {
    uiAction(() -> {
      mInLoading = false;
      if (recordItems.length > 0) {
        mAdapter.append(recordItems);
        mAdapter.resetLoadMoreState();
      } else {
        mAdapter.noMoreData();
      }
      if (mAdapter.getDataCount() < 1) {
        mStateLayout.showEmpty();
      } else {
        mStateLayout.showContent();
      }
      mAdapter.notifyDataSetChanged();
    });
  }

  private void onLoadError(Throwable throwable) {
    uiAction(() -> {
      mInLoading = false;
      mAdapter.loadMoreError(throwable.getMessage());
      mAdapter.notifyDataSetChanged();
      if (mAdapter.getDataCount() < 1) {
        mStateLayout.showError(throwable.getMessage(), view -> firstLoad());
      }
    });
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mPresenter.destroy();
    mDisposables.clear();
  }
}
