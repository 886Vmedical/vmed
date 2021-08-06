package com.mediatek.mt6381eco;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.mediatek.jni.mt6381.Utils;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mt6381eco.biz.utlis.SensorDataAligner;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class) public class JniSoUnitTest {
    SensorDataAligner mSensorDataAligner = new SensorDataAligner();
    private int[][] mBuffer;
    private int mBufferIndex;
    private int receiveCount;
    protected int mHRVChecked = 0;
    protected int mSPChecked = 0;
    protected int mSPO2Checked = 0;

    @Test public void testSo() throws IOException {
        Context appContext =  InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = appContext.getAssets();
        String[] fileNames  = assetManager.list("raw_data");
        for(String fileName :fileNames){
            System.out.println(fileName);
            InputStream inputStream = assetManager.open("raw_data/" + fileName);
            try{
                checkRawData(inputStream);
            }finally {
                inputStream.close();
            }
        }
    }

    private void checkRawData(InputStream inputStream) throws IOException {
        mHRVChecked = 0;
        mSPChecked = 0;
        mSPO2Checked = 0;
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream));
        Utils.bpAlgInit();
        Utils.checkQualityInit(1);
        Utils.spo2Init();
        mSensorDataAligner.init(new int[]{ SensorData.DATA_TYPE_EKG, SensorData.DATA_TYPE_PPG1,SensorData.DATA_TYPE_PPG2});

        mBuffer = new int[3][12];
        mBufferIndex = 0;
        receiveCount = 0;
        String line;
        while((line = reader.readLine()) != null){
            parseLine(line);
        }
        assertEquals(mHRVChecked, 2);
        assertEquals(mSPChecked ,1);
        assertEquals(mSPO2Checked, 1);
    }

    private void parseLine(String line) {
        String[] ss = line.split(",");
        try {
            int type = Integer.parseInt(ss[0]);
            switch (type) {
                case 1000: {
                    parseProfile(ss);
                    break;
                }
                case 1010:{
                    parseCalibration(ss);
                    break;
                }
                case 5:
                case 9:
                case 10:{
                    parseData(ss);
                    break;
                }
                case 80:{
                    checkSpo2(ss);
                    break;
                }
                case 81:{
                    checkBp(ss);
                    break;
                }
                case 82:{
                    checkHrv(ss);
                    break;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Skip:" + ss[0]);
        }

    }

    private void checkBp(String[] ss) {
        int sbp1 = Integer.parseInt(ss[3]);
        int dbpl = Integer.parseInt(ss[4]);
        int sbp2 = Utils.bpAlgGetSbp();
        int dbp2 = Utils.bpAlgGetDbp();
        assertEquals(sbp1, sbp2);
        assertEquals(dbpl, dbp2);
        ++ mSPChecked;
    }

    private void checkHrv(String[] ss) {
        int fi1 = Integer.parseInt(ss[7]);
        int pi1 = Integer.parseInt(ss[8]);
        int fi2 = Utils.bpAlgGetFatigueIndex();
        int pi2 = Utils.bpAlgGetPressureIndex();
        assertEquals(fi1, fi2);
        assertEquals(pi1, pi2);
        ++mHRVChecked;
    }

    private void checkSpo2(String[] ss) {
        int bpm1 = Integer.parseInt(ss[3]);
        int spo21 = Integer.parseInt(ss[4]);
        int bpm2 = Utils.spo2GetBpm();
        int spo22 = Utils.spo2GetSpO2();
        assertEquals(bpm1, bpm2);
        assertEquals(spo21, spo22);
        ++ mSPO2Checked;
    }

    private void parseData(String[] ss) {
        int type = getType(Integer.parseInt(ss[0]));
        int sn = Integer.parseInt(ss[1]);
        for(int i =0;i < 12; ++i){
            int value = Integer.parseInt(ss[i +2]);
            int[] ret = mSensorDataAligner.align(type, sn ++, value);
            if(ret != null){
                mBuffer[0][mBufferIndex] = ret[0];
                mBuffer[1][mBufferIndex] = ret[1];
                mBuffer[2][mBufferIndex] = ret[2];
                mBufferIndex++;
                if (mBufferIndex == 12) {
                    int quality =
                            Utils.checkQuality(mBuffer[1], mBufferIndex, mBuffer[2], mBufferIndex, mBuffer[0],
                                    mBufferIndex, 1, 1);

                    receiveCount += 12;
                    mBufferIndex = 0;
                }
            }
        }
    }

    public void parseProfile(String[] ss) {
        int age = Integer.parseInt(ss[4]);
        int height = Integer.parseInt(ss[5]);
        int weight = Integer.parseInt(ss[6]);
        int gender = ss[3].equals("Female")? 2:1;
        Utils.bpAlgSetUserInfo(age, gender,height,weight, 0);
    }

    private void parseCalibration(String[] ss){
        int[] mCalibrationArray = new int[12];
        for(int i =0;i < mCalibrationArray.length; ++i){
            mCalibrationArray[i] = Integer.parseInt(ss[i +2]);
        }
        int[] soArray = new int[18];
        soArray[0] = mCalibrationArray[0];
        soArray[2] = mCalibrationArray[9];
        soArray[1] = mCalibrationArray[1];
        soArray[3] = mCalibrationArray[10];
        for (int i = 6; i <= 11; i++) {
            soArray[i] = mCalibrationArray[i - 3];
        }
        Utils.bpAlgSetCalibrationData(soArray, soArray.length);
    }


    private int getType(int i) {
        switch (i){
            case 5: return SensorData.DATA_TYPE_EKG;
            case 9: return SensorData.DATA_TYPE_PPG1;
            case 10: return SensorData.DATA_TYPE_PPG2;
        }
        return i;
    }
}
