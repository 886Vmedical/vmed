package com.mediatek.mt6381eco.biz.account.signin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.account.BaseAccountFragment;
import com.mediatek.mt6381eco.network.model.LoginRequest;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

public class SignInFragment extends BaseAccountFragment {
  @Override protected void applyActionBar(ActionBar actionBar) {
    super.applyActionBar(actionBar);
    actionBar.setTitle(R.string.sign_in);
  }

  @Override protected void initView(Bundle savedInstanceState) {
    super.initView(savedInstanceState);
    isRigster = false;
    SharedPreferences mGetSperf = getActivity().getSharedPreferences("signInDB",MODE_APPEND);
    String getAccount = mGetSperf.getString("sgAccount","");
    String getPassword = mGetSperf.getString("sgPasswd","");
    Log.d("initView","getAccount =" +getAccount);
    Log.d("initView","getPassword =" +getPassword);
    if(!getAccount.toString().equals("")
            && !getPassword.toString().equals("")){
      mEdtAccount.setText(getAccount.toString());
      mEdtPassword.setText(getPassword.toString());
    }
    mBtnContinue.setText(R.string.continue_sign_in);
  }
}
