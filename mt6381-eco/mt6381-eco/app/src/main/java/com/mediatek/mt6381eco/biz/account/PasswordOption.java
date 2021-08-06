package com.mediatek.mt6381eco.biz.account;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mediatek.mt6381eco.R;

public class PasswordOption extends AppCompatActivity implements View.OnClickListener {
    public EditText newPassword1, newPassword2;
    public Button mCompelete;
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.password_option);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        newPassword1 = (EditText) findViewById(R.id.edt_newPd1);
        newPassword2 =(EditText)findViewById(R.id.edt_newPd2);
        mCompelete = (Button)findViewById(R.id.complete);
        mCompelete.setOnClickListener(this);
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
        if(newPassword1.getEditableText().toString().equals(newPassword2.getEditableText().toString()) && newPassword1.getEditableText().toString().equals("")){
            this.finish();
        }
    }
}
