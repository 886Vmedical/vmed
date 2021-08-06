package com.mediatek.mt6381eco.biz.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.mt6381eco.R;

import java.util.Timer;
import java.util.TimerTask;

public class ModelPowerBroadCastReceiver extends BroadcastReceiver {
    private static final String ACTION_MODEL_POWER_RECEIVER = "com.mediatek.mt6381eco.biz.home.ACTION_MODEL_POWER_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_MODEL_POWER_RECEIVER)) {
            int powerData = intent.getIntExtra("pData",0);
            Log.d("ModelPowerBroadCastReceiver","ModelPowerBroadCastReceiver--Data：" + powerData);
            if (powerData <= 15) {
                //Toast.makeText(context, R.string.power_low_warning, Toast.LENGTH_LONG).show();

                String message = "<font color='#FF0000'>"+ context.getString(R.string.power_low_warning) +"</font>";
                Toast toast =  Toast.makeText(context, Html.fromHtml(message), Toast.LENGTH_LONG);
                LinearLayout linearLayout = (LinearLayout) toast.getView();
                toast.getView().setBackgroundColor(Color.parseColor("#BCEE68"));
                TextView messageTextView = (TextView) linearLayout.getChildAt(0);
                messageTextView.setTextSize(16);
                toast.setGravity(Gravity.CENTER,0,0);
                showMyToast(toast,3*1000);
            }
        }
    }

    //krestin add to make long toast time start
    public void showMyToast(final Toast toast, final int cnt) {
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
        }, cnt );
    }
    //krestin add to make long toast time end
}
