package com.mediatek.mt6381eco.biz.history;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mediatek.mt6381eco.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TempRecordAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> tempRecordsList;
    private final List<String> dateRecordsList;
    private final LayoutInflater inflater;


    public TempRecordAdapter(Context context, List<String> dateRecordsList, List<String> tempRecordsList) {
        this.context = context;
        this.dateRecordsList = dateRecordsList;
        this.tempRecordsList = tempRecordsList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return tempRecordsList.size();
    }

    @Override
    public Object getItem(int position) {
        return tempRecordsList.size() == 0 ? null : tempRecordsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if(null == convertView){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.fragment_list_temp_record,null);
            viewHolder.mTxtDate = (TextView)convertView.findViewById(R.id.txt_tpdate);
            viewHolder.mTxtTpData = (TextView) convertView.findViewById(R.id.txt_tpdata);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DecimalFormat df = new DecimalFormat("######0.00");
        viewHolder.mTxtDate.setText(dateRecordsList.get(position));
        Log.d("TempRecordAdapter: ","DateR: " + dateRecordsList.get(position));
        if(isChineseLanguage()) {
            Log.d("TempRecordAdapter: ","TempRList: " + tempRecordsList);
            Log.d("TempRecordAdapter: ","TempR: " + tempRecordsList.get(position));
            double tempChinese = (Double.parseDouble(this.tempRecordsList.get(position)) - 32) / 1.8;
            String tempCH = df.format(tempChinese);
            viewHolder.mTxtTpData.setText(tempCH);
        }else{
            Log.d("TempRecordAdapter: ","TempRListOther: " + tempRecordsList);
            Log.d("TempRecordAdapter: ","TempROther: " + tempRecordsList.get(position));
            double tempOther = Double.parseDouble(this.tempRecordsList.get(position));
            String tempOTH = df.format(tempOther);
            viewHolder.mTxtTpData.setText(tempOTH);
        }
        return convertView;
    }

    public boolean isChineseLanguage() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        return "zh".equals(language);
    }

    private class ViewHolder {
        TextView mTxtTpData;
        TextView mTxtDate;
    }
}