package com.mediatek.mt6381eco.biz.account.createAccount;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.account.BaseAccountFragment;

public class CreateAccountFragment extends BaseAccountFragment {

  @Override protected void applyActionBar(ActionBar actionBar) {
    super.applyActionBar(actionBar);
    actionBar.setTitle(R.string.create_an_account);
    actionBar.setDisplayHomeAsUpEnabled(true);
    setHasOptionsMenu(true);
  }

  @Override protected void initView(Bundle savedInstanceState) {
    super.initView(savedInstanceState);
    isRigster = true;
    mBtnContinue.setText(R.string.get_started);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case android.R.id.home:{
        getActivity().finish();
        break;
      }
    }
    return super.onOptionsItemSelected(item);

  }
}
