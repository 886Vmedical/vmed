 package com.mediatek.mt6381eco.biz.startup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.account.createAccount.CreateAccountFragment;
import com.mediatek.mt6381eco.biz.account.signin.SignInFragment;
import com.mediatek.mt6381eco.biz.flavor.IFlavorUtils;
import com.mediatek.mt6381eco.biz.home.HomeActivity;
import com.mediatek.mt6381eco.biz.profile.ProfileActivity;
import com.mediatek.mt6381eco.biz.splash.VmedSplashActivity;
import com.mediatek.mt6381eco.ui.BaseActivity;
import com.mediatek.mt6381eco.ui.ContainerActivity;
import com.mediatek.mt6381eco.ui.URLSpanNoUnderline;
import com.mediatek.mt6381eco.ui.interfaces.GuestPage;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class StartupActivity extends BaseActivity implements StartupContract.View, GuestPage {
  private static final int REQUEST_SIGN_IN = 1;
  private static final int REQUEST_CREATE_ACCOUNT = 2;
  private static final int REQUEST_PROFILE = 3;
  @Inject IFlavorUtils mNavigation;
  @Inject StartupContract.Presenter mPresenter;

  @BindView(R.id.txt_policy) TextView mTxtPolicy;
  @BindView(R.id.txt_guest) TextView mTxtGuest;

  private static final int BAIDU_READ_PHONE_STATE = 100;//定位权限请求
  private static final int PRIVATE_CODE = 1315;//开启GPS权限
  private LocationManager lm;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    URLSpanNoUnderline.setTo(mTxtPolicy);
    mTxtPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    showGPSContacts();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_SIGN_IN:
      case REQUEST_CREATE_ACCOUNT: {
        if (resultCode == Activity.RESULT_OK) {
          mPresenter.navToNext();
        }
        break;
      }
      case REQUEST_PROFILE: {
        if (resultCode == Activity.RESULT_OK) {
          navToHome();
        }else {
          mPresenter.profileInvalid();
        }
        break;
      }
      case PRIVATE_CODE:
        showGPSContacts();
        break;
    }
  }

  @Override public void navToHome() {
    startActivity(new Intent(this, HomeActivity.class));
    finish();
  }

  @Override public void requireProfile() {
    startActivityForResult(new Intent(this, ProfileActivity.class), REQUEST_PROFILE);
  }

  @OnClick(R.id.btn_sign_in) void onBtnSignInClick() {
    startActivityForResult(ContainerActivity.makeIntent(this, SignInFragment.class),
        REQUEST_SIGN_IN);
  }

  @OnClick(R.id.btn_create_an_account) void onBtnCreateAnAccountClick() {
    startActivityForResult(ContainerActivity.makeIntent(this, CreateAccountFragment.class),
        REQUEST_CREATE_ACCOUNT);
  }

  //add this for guest SB
  @OnClick(R.id.txt_guest) void onTxtGuestClickforSB() {
    //isguest is true;
    mPresenter.requestGuestForSB(true);
  }

  public void showGPSContacts() {
    AtomicBoolean isLoactionEnable = new AtomicBoolean(false);
    //得到系统的位置服务，判断GPS是否激活
    lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    if (ok) {//开了定位服务
      if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED) {// 没有权限，申请权限。
          ActivityCompat.requestPermissions(StartupActivity.this, LOCATIONGPS, BAIDU_READ_PHONE_STATE);
          isLoactionEnable.set(true);
        } else {
          initLocationOption();//有权限，进行相应的处理
        }
      } else {
        initLocationOption();//有权限，进行相应的处理
      }

    } else {
      Intent intent = new Intent();
      intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      intent.setData(Uri.fromParts("package", "com.mediatek.mt6381eco.bt.prod", null));
      startActivityForResult(intent, PRIVATE_CODE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case BAIDU_READ_PHONE_STATE:
        //如果用户取消，permissions可能为null.
        if (grantResults[0] == PERMISSION_GRANTED && grantResults.length > 0) { //有权限
          // 获取到权限，作相应处理
          initLocationOption();//有权限，进行相应的处理

        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)
                || !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
          MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
          builder.title(R.string.attention)
                  .content(R.string.permission_user_attention)
                  .positiveText(R.string.go_to_setting)
                  .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                      Intent intent = new Intent();
                      intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                      intent.setData(Uri.fromParts("package", "com.mediatek.mt6381eco.bt.prod", null));
                      startActivity(intent);
                    }
                  });
          builder.show();
        } else {
          /*无权限*/
          ActivityCompat.requestPermissions(this, LOCATIONGPS, BAIDU_READ_PHONE_STATE);
        }
        break;
      default:
        break;
    }
  }

  private void initLocationOption() {
    // 获取位置管理服务
    LocationManager locationManager;
    String serviceName = LOCATION_SERVICE;
    locationManager = (LocationManager) this.getSystemService(serviceName);
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
    ) {
      return;
    }

  }

  public static final String[] LOCATIONGPS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE,
  };
}
