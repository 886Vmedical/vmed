package com.mediatek.mt6381.ble;

import android.util.Log;
import android.util.SparseArray;

import com.mediatek.blenativewrapper.BLEDataReceiver;
import com.mediatek.blenativewrapper.rxbus.RxBus;
import com.mediatek.blenativewrapper.utils.DataConvertUtils;
import com.mediatek.mt6381.ble.command.AskTemperatureCommand;
import com.mediatek.mt6381.ble.command.MeasurementCommand;
import com.mediatek.mt6381.ble.data.CommandResponse;
import com.mediatek.mt6381.ble.data.SystemInformationData;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mtk6381.data_parsing.DataParsing;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class MT6381RawDataParserForTemp implements BLEDataReceiver {

  //默认温度0℃  32℉
  public static double  doubletempData = 0;

  @Override public void receive(UUID uuid, byte[] data, int length) {
    //处理温度数据
    Log.d("ParserForTemp","data: " + data);
    Log.d("ParserForTemp","uuid: " + uuid);
    Log.d("ParserForTemp","data.length: " + data.length);
    /*Log.d("ParserForTemp","data[0]: " + data[0]);
    Log.d("ParserForTemp","data[1]: " + data[1]);

    Log.d("ParserForTemp","(int)data[0]: " + (int)data[0]);
    Log.d("ParserForTemp","(int)data[1]: " + (int)data[1]);*/

    //1.把负数变正
    //2.把十进制整数转化为16进制的整数
    //3.把16进制的整数转化成字符串调换位置
    //4.把16进制的字符串转化为10进制的整数
    //5.加上小数点变成浮点数

    /*//方法一：
    int data0;
    int data1;

    if(data[0] < 0){
       data0 = (int)data[0] + 256;//170： -86 to hex :-86 + 256 =170=0xAA // -79  to hex: -79 + 256 =177=0XB1
    }else{
       data0 = (int)data[0];
    }
    if(data[1] < 0){
       data1 = (int)data[1] + 256;
    }else{
       data1 = (int)data[1];//85： to hex: 0x55   // 10 to hex:  0x0A
    }

    //Log.d("ParserForTemp","data0: " + data0);
    //Log.d("ParserForTemp","data1: " + data1);//10
    String hexdata0 = Integer.toHexString(data0);//"AA"
    String hexdata1 = Integer.toHexString(data1);//"55"
    //Log.d("ParserForTemp","hexdata0: " + hexdata0);
    //Log.d("ParserForTemp","hexdata1: " + hexdata1);//A  should be 0A
    String hexdata = hexdata1 + hexdata0;
    //Log.d("ParserForTemp","hexdata: " + hexdata);//"55AA"  21930
    //方法一 end*/

    //方法二：
    String temphexdata2 = DataConvertUtils.bytesToHex(data);
    Log.d("ParserForTemp","temphexdata2: " + temphexdata2);
    //调换位置
    String hexdata2 = temphexdata2.substring(3,5) + temphexdata2.substring(0,2);
    Log.d("ParserForTemp","hexdata2: " + hexdata2);
    //end

    //16进制转10进制
    int decdata = Integer.valueOf(hexdata2, 16);
    Log.d("ParserForTemp","decdata: " + decdata);
    //10进制转浮点数
    doubletempData = (double)decdata/100;//26.00 //219.3  Not 219.30
    Log.d("ParserForTemp","doubletempData: " + doubletempData);
  }


  @Override public void reset() {

  }


  @Override public void destroy() {

  }
}
