package com.mediatek.mt6381eco.biz.connect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.mediatek.blenativewrapper.DiscoverPeripheral;
import com.mediatek.mt6381eco.R;
import java.util.ArrayList;

public class PeripheralAdapter extends BaseAdapter {

  private ArrayList<DiscoverPeripheral> mDataList = new ArrayList<>();

  public PeripheralAdapter(){
  }

  public void setList(ArrayList<DiscoverPeripheral> data) {
    mDataList = data;
    notifyDataSetChanged();
  }

  @Override public int getCount() {
    return mDataList.size();
  }

  @Override public DiscoverPeripheral getItem(int position) {
    return mDataList.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    TextView textView = (TextView) convertView;
    if (textView == null) {
      textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_1, parent, false);
    }
    textView.setText(getItem(position).getLocalName());
    return textView;
  }
}
