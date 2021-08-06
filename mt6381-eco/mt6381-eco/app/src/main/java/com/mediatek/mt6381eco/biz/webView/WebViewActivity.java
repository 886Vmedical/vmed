package com.mediatek.mt6381eco.biz.webView;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.ui.interfaces.GuestPage;
import timber.log.Timber;

public class WebViewActivity extends AppCompatActivity implements GuestPage {
  @BindView(R.id.web_view) WebView mWebView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    if(savedInstanceState != null){
      mWebView.restoreState(savedInstanceState);
    }else {
      loadUrl(getIntent().getData());
    }

  }

  private void loadUrl(Uri data) {
    String url = data.getEncodedSchemeSpecificPart();
    mWebView.loadUrl("file:" + url);
    Timber.i("LoadUrl:%s", url);
    mWebView.setBackgroundColor(Color.TRANSPARENT);
    getSupportActionBar().setTitle(R.string.loading);
    mWebView.setWebViewClient(new WebViewClient() {
      @Override public void onPageFinished(WebView view, String url) {
        getSupportActionBar().setTitle(view.getTitle());
      }
    });
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case android.R.id.home:{
        finish();
        break;
      }
      default:
        return false;
    }
    return true;
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mWebView.saveState(outState);
  }
}
