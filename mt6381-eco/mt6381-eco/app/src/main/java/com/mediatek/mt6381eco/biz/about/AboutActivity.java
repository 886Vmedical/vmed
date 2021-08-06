package com.mediatek.mt6381eco.biz.about;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mediatek.jni.mt6381.Utils;
import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.ui.URLSpanNoUnderline;
import java.util.Locale;

public class AboutActivity extends AppCompatActivity {
  @BindView(R.id.txt_version) TextView mTxtVersion;
  @BindView(R.id.txt_policy) TextView mTxtPolicy;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

// delete by herman for gitsha
		/*mTxtVersion.setText(
        String.format(Locale.getDefault(), getString(R.string.version) + " %s-%s,%d,%d,%d,%d",
            BuildConfig.VERSION_NAME, BuildConfig.GIT_SHA, Utils.ohrmGetVersion(),
            Utils.spo2GetVersion(), Utils.bpAlgGetVersion(), Utils.HRVGetVersion()));*/

    mTxtVersion.setText(
            String.format(Locale.getDefault(), getString(R.string.version) + " %s", BuildConfig.VERSION_NAME));

    URLSpanNoUnderline.setTo(mTxtPolicy);
    mTxtPolicy.setMovementMethod(LinkMovementMethod.getInstance());
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
}
