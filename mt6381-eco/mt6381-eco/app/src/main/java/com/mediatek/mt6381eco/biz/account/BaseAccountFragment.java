package com.mediatek.mt6381eco.biz.account;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.reactivex.Completable;

import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.about.AboutActivity;
import com.mediatek.mt6381eco.biz.home.HomeActivity;
import com.mediatek.mt6381eco.dagger.Injectable;
import com.mediatek.mt6381eco.network.model.LoginRequest;
import com.mediatek.mt6381eco.ui.BaseFragment;
import com.mediatek.mt6381eco.ui.interfaces.GuestPage;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

public class BaseAccountFragment extends BaseFragment
    implements AccountContract.View, Injectable, Validator.ValidationListener,GuestPage {

  @Length(min = 4) @BindView(R.id.edt_account) protected EditText mEdtAccount;
  @Length(min = 8) @BindView(R.id.edt_password) protected EditText mEdtPassword;
  @BindView(R.id.btn_continue) protected Button mBtnContinue;
  @BindView(R.id.pd_found) public TextView mPdFound;
  @BindView(R.id.pd_reset) public TextView mPdReset;
  @Inject AccountContract.Presenter mPresenter;
  @Inject AccountViewModel mViewModel;
  private Validator mValidator;
  public static boolean isRigster = false;

  @Override protected void applyActionBar(ActionBar actionBar) {
    super.applyActionBar(actionBar);
    actionBar.setDisplayHomeAsUpEnabled(true);
    setHasOptionsMenu(true);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    mValidator = null;
  }

  @Override protected void initView(Bundle savedInstanceState) {

    mBtnContinue.setText(R.string.continue_sign_in);
    mValidator = new Validator(this);
    mValidator.setValidationListener(this);

    mViewModel.loginRequest.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          startLoading(getString(R.string.waiting));
          break;
        }
        case SUCCESS: {

          SharedPreferences mSgFlag = getActivity().getSharedPreferences("signInDB", MODE_APPEND);
          SharedPreferences.Editor mSgEditor = mSgFlag.edit();
          mSgEditor.putString("sgAccount", mEdtAccount.getText().toString());
          mSgEditor.putString("sgPasswd", mEdtPassword.getText().toString());
          mSgEditor.commit();

          navToNext();
          stopLoading();
          break;
        }
        case ERROR: {
          stopLoading();
          showError(resource.throwable);
          break;
        }
      }
    });
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    if(!BuildConfig.BUILD_TYPE.equals("debug")) {
      mValidator.validate();
    }
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_sign_in, container, false);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        getActivity().finish();
        break;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @OnTextChanged({ R.id.edt_account, R.id.edt_password }) void onTextChanged() {
    if(!BuildConfig.BUILD_TYPE.equals("debug")) {
      mValidator.validate();
    }
  }

  @OnClick(R.id.btn_continue) void OnBtnContinueClick() {
      mPresenter.requestAccount(mEdtAccount.getText().toString(), mEdtPassword.getText().toString());
  }
  @OnClick(R.id.pd_found) void OnPdFoundClick() {
    startActivity(new Intent(getActivity(), MailSignatureActivity.class));
  }
  @OnClick(R.id.pd_reset) void OnPdResetClick() {
    startActivity(new Intent(getActivity(), MailSignatureActivity.class));
  }


  @Override public void navToNext() {
    getActivity().setResult(Activity.RESULT_OK);
    getActivity().finish();
  }

  @Override public void onValidationSucceeded() {
    mBtnContinue.setEnabled(true);
  }

  @Override public void onValidationFailed(List<ValidationError> errors) {
    mBtnContinue.setEnabled(false);
  }

  public void showCreateAccountToast(final Toast toast, final int cnt) {
    final Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        toast.show();
      }
    }, 0, 3000);
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        toast.cancel();
        timer.cancel();
      }
    }, cnt);
  }
}
