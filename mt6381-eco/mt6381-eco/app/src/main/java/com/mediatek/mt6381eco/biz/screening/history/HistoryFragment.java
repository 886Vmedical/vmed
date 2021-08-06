package com.mediatek.mt6381eco.biz.screening.history;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import com.gturedi.views.StatefulLayout;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.recorddetail.RecordDetailActivity;
import com.mediatek.mt6381eco.dagger.Injectable;
import com.mediatek.mt6381eco.ui.BaseFragment;
import com.mediatek.mt6381eco.ui.ContextUtils;

import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import javax.inject.Inject;

import static android.content.Context.MODE_APPEND;

public class HistoryFragment extends BaseFragment implements HistoryContract.View, Injectable {
  private static final String END_COUNT = "END_COUNT";
  private static final int LIMIT = 10;
  @BindView(R.id.list_view) RecyclerView mListView;
  @BindView(R.id.state_layout) StatefulLayout mStateLayout;
  @Inject HistoryContract.Presenter mPresenter;
  @Inject HistoryViewModel mViewModel;
  private ScreeningListAdapter mAdapter;
  private boolean mInLoading;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private int mEndCount = Integer.MAX_VALUE;

  //EditText
  private TextView mTempData;
  private TextView mSpo2Data;
  private TextView mBrvData;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_screening_history, container, false);
    mTempData = (TextView) view.findViewById(R.id.temp_cdata);
    mSpo2Data = (TextView)view.findViewById(R.id.spo2_cdata);
    mBrvData = (TextView)view.findViewById(R.id.brv_cdata);
    getCovidPreferenceData();

    return view;
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);


  }

  @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    if(savedInstanceState!= null) {
      mEndCount = savedInstanceState.getInt(END_COUNT, Integer.MAX_VALUE);
    }
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(END_COUNT, mEndCount);
  }

  @Override protected void initView(Bundle savedInstanceState) {
    RecyclerView.LayoutManager layoutManager =
        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

    mListView.setLayoutManager(layoutManager);
    mAdapter = new ScreeningListAdapter(getContext());
    mListView.setAdapter(mAdapter);
    mViewModel.dataList.observe(this, resource -> {

      switch (resource.status) {
        case LOADING: {
          mAdapter.resetLoadMoreState();
          mAdapter.setDate(resource.data);
          if (mAdapter.getDataCount() == 0) {
            mStateLayout.showLoading();
          } else {
            mStateLayout.showContent();
          }
          break;
        }
        case SUCCESS: {
          mInLoading = false;
          int lastCount = mAdapter.getDataCount();
          mAdapter.setDate(resource.data);
          if (mAdapter.getDataCount() > lastCount && mAdapter.getDataCount()  < mEndCount) {
            mAdapter.resetLoadMoreState();
          } else {
            mAdapter.noMoreData();
            mEndCount = lastCount;
          }
          if (mAdapter.getDataCount() < 1) {
            mStateLayout.showEmpty();
//            mStateLayout.setVisibility(View.GONE);
////            LayoutInflater inflater = getLayoutInflater();
////            View mView = inflater.inflate(R.layout.offline_history_record,null);
////            mStateLayout.addView(mView);
          } else {
            mStateLayout.showContent();
          }
          break;
        }
        case ERROR: {
          mInLoading = false;
          mAdapter.loadMoreError(ContextUtils.getErrorMessage(resource.throwable));
          mAdapter.notifyDataSetChanged();
          if (mAdapter.getDataCount() < 1) {
            mStateLayout.showError(ContextUtils.getErrorMessage(resource.throwable), view1 -> firstLoad());
          }
          break;
        }
      }
    });
    mDisposables.add(mAdapter.getPositionClicks().subscribe(position -> {
      int viewType = mAdapter.getItemViewType(position);
      if (ScreeningListAdapter.TYPE_LOAD_ERROR == viewType) {
        mAdapter.resetLoadMoreState();
        mAdapter.notifyDataSetChanged();
        loadNextPaging(position);
      } else if (ScreeningListAdapter.TYPE_ITEM == viewType) {
        HistoryViewItem item = ((ScreeningListAdapter) mListView.getAdapter()).getItem(position);
        Intent intent = new Intent(this.getActivity(), RecordDetailActivity.class);
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


  private void firstLoad() {
    loadNextPaging(0);
  }


  private void loadNextPaging(int offSet) {
    mPresenter.loadHistory(LIMIT, offSet);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mPresenter.destroy();
    mDisposables.clear();
  }

  public boolean isChineseLanguage() {
    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();
    return "zh".equals(language);
  }

  private void getCovidPreferenceData() {
    SharedPreferences sp1 = getContext().getSharedPreferences("vemdtemp", MODE_APPEND);
    String mStrDataTemp = sp1.getString("temperature", getString(R.string.default_no_measure));
    if((mStrDataTemp.equals(getString(R.string.default_no_measure)))){
      mTempData.setText(R.string.default_no_measure);
    }else{
      double mDataTemp = Double.parseDouble(mStrDataTemp);
      double mFTemp = Double.parseDouble(String.valueOf(mDataTemp));
      double mCTemp = Double.parseDouble(String.valueOf((mFTemp - 32)/1.8));
      Log.d("ScreeningActivity","mDataTemp: " + mDataTemp);
      Log.d("ScreeningActivity","mTempData: " + mTempData);
      if(isChineseLanguage()) {
       mTempData.setText(String.format("%.2f", mCTemp) + " °C");
       if(mCTemp > 38.0) {
        mTempData.setTextColor(Color.RED);
       }
      }else {
      mTempData.setText(String.format("%.2f", mDataTemp) + " °F");
      if(mFTemp > 100.0) {
        mTempData.setTextColor(Color.RED);
      }
     }
    }
    SharedPreferences sp2 = getContext().getSharedPreferences("vemddata", MODE_APPEND);
    String mDataSpo2= sp2.getString("spo2", getString(R.string.default_no_measure));
    String mDataBrv = sp2.getString("brv", getString(R.string.default_no_measure));
    Log.d("ScreeningActivity","mDataBrv: " + mDataBrv);
    Log.d("ScreeningActivity","mDataSpo2: " + mDataSpo2);

    if(mDataSpo2.equals(getString(R.string.default_no_measure))){
      mSpo2Data.setText(R.string.default_no_measure);
    }else {
      mSpo2Data.setText(mDataSpo2 + "%");
      int mHFSpo2 = Integer.parseInt(mDataSpo2);
      if(mHFSpo2 < 87){
        mSpo2Data.setTextColor(Color.RED);
      }
    }

    if(mDataBrv.equals(getString(R.string.default_no_measure))){
      mBrvData.setText(R.string.default_no_measure);
    }else{
      mBrvData.setText(mDataBrv);
      int mHFBrv = Integer.parseInt(mDataBrv);
      if(mHFBrv < 12 || mHFBrv > 24){
        mBrvData.setTextColor(Color.RED);
      }
    }
  }
  //krestin add to get covid data from measure when people complete test end

}