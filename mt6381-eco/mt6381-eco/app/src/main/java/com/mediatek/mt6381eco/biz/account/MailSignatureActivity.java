package com.mediatek.mt6381eco.biz.account;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mediatek.mt6381eco.R;

public class MailSignatureActivity extends AppCompatActivity implements View.OnClickListener {
    public EditText mailAccount, mailPassword, mVerification;
    public Button mNext, mVerButton;

    private TimeCount time;
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_mail_signature);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mailAccount = (EditText) findViewById(R.id.edt_maccount);
        mailPassword =(EditText)findViewById(R.id.edt_mpassword);
        mVerification =(EditText)findViewById(R.id.verif_code);
        mNext = (Button)findViewById(R.id.btn_next);

        time = new TimeCount(60000, 1000);
        mVerButton = (Button)findViewById(R.id.send_verification);
        mVerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                time.start();
            }
        });
        mNext.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
      startActivity(new Intent(this,PasswordOption.class));
    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mVerButton.setBackgroundColor(Color.parseColor("#B6B6D8"));
            mVerButton.setClickable(false);
            mVerButton.setText("("+millisUntilFinished / 1000 +")" + getString(R.string.ver_second));
        }

        @Override
        public void onFinish() {
            mVerButton.setText(R.string.ver_timer_text);
            mVerButton.setClickable(true);
            mVerButton.setBackgroundColor(Color.parseColor("#4EB84A"));

        }
    }

}

