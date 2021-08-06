package com.mediatek.mt6381eco.biz.screening;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.measure.MeasureActivity;
import com.mediatek.mt6381eco.biz.screening.history.HistoryFragment;
import com.mediatek.mt6381eco.ui.BaseActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScreeningActivity extends BaseActivity {
  @BindView(R.id.view_pager) ViewPager mViewPager;
  @BindView(R.id.navigation) BottomNavigationView mNavigationView;
  private Fragment[] mFragments;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_screening);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    mFragments = new Fragment[] { new HistoryFragment(),new EducationFragment() };
    bindNavigationViewPager();
  }

  private void bindNavigationViewPager() {
    mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
      @Override public Fragment getItem(int position) {
        return mFragments[position];
      }

      @Override public int getCount() {
        return mFragments.length;
      }
    });

    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override public void onPageSelected(int position) {
        mNavigationView.setSelectedItemId(mNavigationView.getMenu().getItem(position).getItemId());
      }

      @Override public void onPageScrollStateChanged(int state) {

      }
    });
    mNavigationView.setOnNavigationItemSelectedListener(item -> {
      mViewPager.setCurrentItem(item.getOrder());
      return true;
    });
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case android.R.id.home:{
        finish();
        break;
      }
    }
    return super.onOptionsItemSelected(item);

  }
}
