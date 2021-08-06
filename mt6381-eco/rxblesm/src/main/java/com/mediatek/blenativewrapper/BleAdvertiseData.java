

package com.mediatek.blenativewrapper;

import java.io.Serializable;
import java.util.Locale;

public class BleAdvertiseData implements Serializable {

    private static final String STRING_FORMAT = "AdvertiseData(Length=%d,Type=0x%02X)";

    private final int mLength;
    private final int mType;
    private final byte[] mData;

    BleAdvertiseData(int length, int type, byte[] data) {
        mLength = length;
        mType = type;
        mData = data.clone();
    }


    public int getLength() {
        return mLength;
    }
    public int getType() {
        return mType;
    }

    public byte[] getData() {
        return mData.clone();
    }

    @Override
    public String toString() {
        return String.format(Locale.US, STRING_FORMAT, mLength, mType);
    }
}
