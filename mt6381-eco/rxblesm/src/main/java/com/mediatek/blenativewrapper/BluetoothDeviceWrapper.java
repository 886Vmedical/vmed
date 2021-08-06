package com.mediatek.blenativewrapper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mediatek.blenativewrapper.utils.DataConvertUtils;
import com.mediatek.blenativewrapper.utils.PlatformUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;
import timber.log.Timber;

public class BluetoothDeviceWrapper {
  @NonNull private final Context mContext;
  @NonNull private final BluetoothDevice mBluetoothDevice;
  @Nullable private BluetoothGatt mBluetoothGatt;
  @Nullable private BluetoothGattCallbackWrapper mBluetoothGattCallbackWrapper;
  private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    @Override public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      Timber.i("onConnectionStateChange status=%d(0x%02x)  newState=%d(0x%02x)", status,status,
          newState,newState);
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onConnectionStateChange(status, newState);
      }
    }

    @Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      Timber.i("onServicesDiscovered status=%d(0x%02x)", status, status);
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onServicesDiscovered(status);
      }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
        int status) {
      Timber.i("onCharacteristicRead(%s) status=%d(0x%02x) ",
          UUIDNameMapping.getDefault().nameOf(characteristic.getUuid()), status, status);

      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onCharacteristicRead(characteristic, status);
      }
    }

    @Override public void onCharacteristicWrite(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic, int status) {
      Timber.i("onCharacteristicWrite(%s) status=%d(0x%02x) ",
          UUIDNameMapping.getDefault().nameOf(characteristic.getUuid()), status, status);
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onCharacteristicWrite(characteristic, status);
      }
    }

    @Override public void onCharacteristicChanged(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic) {
      Timber.v("onCharacteristicChanged(%s) data =%s ",
          UUIDNameMapping.getDefault().nameOf(characteristic.getUuid()),
          DataConvertUtils.bytesToHex(characteristic.getValue()));
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onCharacteristicChanged(characteristic);
      }
    }

    @Override public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
        int status) {
      Timber.i("onDescriptorRead(%s - %s) status=%d(0x%02x)",
          UUIDNameMapping.getDefault().nameOf(descriptor.getCharacteristic().getUuid()),
          UUIDNameMapping.getDefault().nameOf(descriptor.getUuid()), status,status);
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onDescriptorRead(descriptor, status);
      }
    }

    @Override public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
        int status) {
      Timber.i("onDescriptorWrite(%s - %s) status=%d(0x%02x)",
          UUIDNameMapping.getDefault().nameOf(descriptor.getCharacteristic().getUuid()),
          UUIDNameMapping.getDefault().nameOf(descriptor.getUuid()), status,status);
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onDescriptorWrite(descriptor, status);
      }
    }

    @Override public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
      Timber.i("onReliableWriteCompleted status=%d(0x%02x) ", status);
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onReliableWriteCompleted(status);
      }
    }

    @Override public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
      Timber.i("onReadRemoteRssi rssi =%d status=%d(0x%02x) ", rssi, status,status);
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onReadRemoteRssi(rssi, status);
      }
    }

    @Override public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
      Timber.i("onMtuChanged mtu=%d status=%d(0x%02x) ", mtu, status,status);
      if (null != mBluetoothGattCallbackWrapper) {
        mBluetoothGattCallbackWrapper.onMtuChanged(mtu, status);
      }
    }
  };

  public BluetoothDeviceWrapper(@NonNull Context context,
      @NonNull final BluetoothDevice bluetoothDevice) {
    mContext = context;
    mBluetoothDevice = bluetoothDevice;
    mBluetoothGatt = null;
    mBluetoothGattCallbackWrapper = null;
  }

  private static Object invokeMethod(Object target, String methodName, Class<?>[] parameterClasses,
      Object[] paramterValues)
      throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException,
      InvocationTargetException {
    Class<?> clazz = target.getClass();
    Method method = clazz.getDeclaredMethod(methodName, parameterClasses);
    return method.invoke(target, paramterValues);
  }

  public String getAddress() {
    return mBluetoothDevice.getAddress();
  }

  public String getLocalName() {
    return mBluetoothDevice.getName();
  }

  public int getBondState() {
    return mBluetoothDevice.getBondState();
  }

  @SuppressLint("NewApi") public boolean createBond() {
    boolean ret = false;
    Timber.i("createBond() call.");
    if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
      try {
        ret = (Boolean) invokeMethod(mBluetoothDevice, "createBond", null, null);
      } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        e.printStackTrace();
      }
    } else {
      ret = mBluetoothDevice.createBond();
    }
    if (ret) {
      Timber.d("createBond() called. ret=true");
    } else {
      Timber.e("createBond() called. ret=false");
    }
    return ret;
  }

  public boolean cancelBondProcess() {
    boolean ret = false;
    Timber.i("cancelBondProcess() call.");
    try {
      ret = (Boolean) invokeMethod(mBluetoothDevice, "cancelBondProcess", null, null);
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    if (ret) {
      Timber.d("cancelBondProcess() called. ret=true");
    } else {
      Timber.e("cancelBondProcess() called. ret=false");
    }
    return ret;
  }

  public boolean removeBond() {
    boolean ret = false;
    Timber.i("removeBond() call.");
    try {
      ret = (Boolean) invokeMethod(mBluetoothDevice, "removeBond", null, null);
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    if (ret) {
      Timber.d("removeBond() called. ret=true");
    } else {
      Timber.e("removeBond() called. ret=false");
    }
    return ret;
  }

  public boolean isBonded() {
    return BluetoothDevice.BOND_BONDED == mBluetoothDevice.getBondState();
  }

  @SuppressLint("NewApi") public boolean setPairingConfirmation(boolean enable) {
    boolean ret = false;
    Timber.i("setPairingConfirmation(" + enable + ") call.");
    if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
      try {
        ret = (Boolean) invokeMethod(mBluetoothDevice, "setPairingConfirmation",
            new Class<?>[] { boolean.class }, new Object[] { enable });
      } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
        e.printStackTrace();
      }
    } else {
      ret = mBluetoothDevice.setPairingConfirmation(enable);
    }
    if (ret) {
      Timber.d("setPairingConfirmation() called. ret=true");
    } else {
      Timber.e("setPairingConfirmation() called. ret=false");
    }
    return ret;
  }

  @SuppressLint("NewApi") public boolean setPin(String pinCode) {
    boolean ret = false;
    byte[] pin = convertPinToBytes(pinCode);
    if (null == pin) {
      Timber.e("null == pin");
      return false;
    }

    Timber.i("setPin(" + pinCode + ") call.");
    if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT) {
      try {
        ret = (Boolean) invokeMethod(mBluetoothDevice, "setPin", new Class<?>[] { byte[].class },
            new Object[] { pin });
      } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
        e.printStackTrace();
      }
    } else {
      ret = mBluetoothDevice.setPin(pin);
    }
    if (ret) {
      Timber.d("setPin() called. ret=true");
    } else {
      Timber.e("setPin() called. ret=false");
    }
    return ret;
  }

  public boolean setPasskey(String pinCode) {
    boolean ret = false;
    Timber.i("setPasskey(" + pinCode + ") call.");
    try {
      ByteBuffer converter = ByteBuffer.allocate(4);
      converter.order(ByteOrder.nativeOrder());
      converter.putInt(Integer.parseInt(pinCode));
      byte[] pin = converter.array();
      ret = (Boolean) invokeMethod(invokeMethod(BluetoothDevice.class, "getService", null, null),
          "setPasskey",
          new Class<?>[] { BluetoothDevice.class, boolean.class, int.class, byte[].class },
          new Object[] { mBluetoothDevice, true, pin.length, pin });
    } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
    }
    if (ret) {
      Timber.d("setPasskey() called. ret=true");
    } else {
      Timber.e("setPasskey() called. ret=false");
    }
    return ret;
  }

  public byte[] convertPinToBytes(String pin) {
    byte[] ret = null;
    try {
      Class<?>[] types = {
          String.class
      };
      Object[] args = {
          pin
      };
      ret = (byte[]) invokeMethod(mBluetoothDevice, "convertPinToBytes", types, args);
    } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return ret;
  }

  public boolean hasGatt() {
    return null != mBluetoothGatt;
  }

  public boolean connectGatt(@NonNull Context context,
      BluetoothGattCallbackWrapper bluetoothGattCallbackWrapper) {
    boolean ret = false;
    if (null == bluetoothGattCallbackWrapper) {
      Timber.e("null == bluetoothGattCallbackWrapper");
      return false;
    }

    mBluetoothGattCallbackWrapper = bluetoothGattCallbackWrapper;
    if (null != mBluetoothGatt) {
      Timber.i("connect() call.");
      ret = mBluetoothGatt.connect();
      if (ret) {
        Timber.d("connect() called. ret=true");
      } else {
        Timber.e("connect() called. ret=false");
      }
    } else {
      Timber.i("connectGatt() call.");
      mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, mGattCallback);
      if (null != mBluetoothGatt) {
        ret = true;
        Timber.d("connectGatt() called. ret=Not Null");
      } else {
        Timber.e("connectGatt() called. ret=Null");
      }
    }
    return ret;
  }

  public boolean disconnectGatt() {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    Timber.i("disconnect() call.");
    mBluetoothGatt.disconnect();
    Timber.d("disconnect() called.");
    return true;
  }

  /**
   * @return BluetoothProfile.STATE_DISCONNECTED
   * BluetoothProfile.STATE_CONNECTING
   * BluetoothProfile.STATE_CONNECTED
   * BluetoothProfile.STATE_DISCONNECTING
   */
  public int getGattState() {
    BluetoothManager bluetoothManager =
        (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
    return bluetoothManager.getConnectionState(mBluetoothDevice, BluetoothProfile.GATT);
  }

  public boolean discoverServices() {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    Timber.i("discoverServices() call.");
    boolean ret = mBluetoothGatt.discoverServices();
    if (ret) {
      Timber.d("discoverServices() called. ret=true");
    } else {
      Timber.e("discoverServices() called. ret=false");
    }
    return ret;
  }

  public boolean refreshGatt() {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    boolean ret = false;
    Timber.i("refresh() call.");
    try {
      ret = (Boolean) invokeMethod(mBluetoothGatt, "refresh", null, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (ret) {
      Timber.d("refresh() called. ret=true");
    } else {
      Timber.e("refresh() called. ret=false");
    }
    return ret;
  }

  public boolean closeGatt() {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    Timber.i("close() call.");
    mBluetoothGatt.close();
    Timber.d("close() called.");
    mBluetoothGatt = null;
    mBluetoothGattCallbackWrapper = null;
    return true;
  }

  @Nullable public BluetoothGattService getService(@NonNull final UUID uuid) {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return null;
    }
    Timber.i("getService(" + uuid.toString() + ") call.");
    BluetoothGattService ret = mBluetoothGatt.getService(uuid);
    if (null != ret) {
      Timber.d("getService() called. ret=Not Null");
    } else {
      Timber.e("getService() called. ret=Null");
    }
    return mBluetoothGatt.getService(uuid);
  }

  @Nullable public List<BluetoothGattService> getServices() {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return null;
    }
    Timber.i("getServices() call.");
    List<BluetoothGattService> ret = mBluetoothGatt.getServices();
    if (null != ret) {
      if (0 == ret.size()) {
        Timber.d("getServices() called. ret.size=0");
      } else {
        Timber.d("getServices() called. ret=Not Null");
      }
    } else {
      Timber.e("getServices() called. ret=Null");
    }
    return ret;
  }

  public boolean setCharacteristicNotification(
      @NonNull final BluetoothGattCharacteristic characteristic, boolean enable) {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    Timber.i("setCharacteristicNotification(" + UUIDNameMapping.getDefault()
        .nameOf(characteristic.getUuid()) + ", " + enable + ") call.");
    boolean ret = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
    if (ret) {
      Timber.d("setCharacteristicNotification() called. ret=true");
    } else {
      Timber.e("setCharacteristicNotification() called. ret=false");
    }
    return ret;
  }

  public boolean readCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic) {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    Timber.i("readCharacteristic("
        + UUIDNameMapping.getDefault().nameOf(characteristic.getUuid())
        + ") call.");
    boolean ret = mBluetoothGatt.readCharacteristic(characteristic);
    if (ret) {
      Timber.d("readCharacteristic() called. ret=true");
    } else {
      Timber.e("readCharacteristic() called. ret=false");
    }
    return ret;
  }

  public boolean writeCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic) {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    Timber.i("writeCharacteristic("
        + UUIDNameMapping.getDefault().nameOf(characteristic.getUuid())
        + ") call.");
    boolean ret = mBluetoothGatt.writeCharacteristic(characteristic);
    if (ret) {
      Timber.d("writeCharacteristic() called. ret=true");
    } else {
      Timber.e("writeCharacteristic() called. ret=false");
    }
    return ret;
  }

  public boolean readDescriptor(@NonNull final BluetoothGattDescriptor descriptor) {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    Timber.i("readDescriptor(" + UUIDNameMapping.getDefault()
        .nameOf(descriptor.getCharacteristic().getUuid()) + ", " + UUIDNameMapping.getDefault()
        .nameOf(descriptor.getUuid()) + ") call.");
    boolean ret = mBluetoothGatt.readDescriptor(descriptor);
    if (ret) {
      Timber.d("readDescriptor() called. ret=true");
    } else {
      Timber.e("readDescriptor() called. ret=false");
    }
    return ret;
  }

  public boolean writeDescriptor(@NonNull final BluetoothGattDescriptor descriptor) {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    Timber.i("writeDescriptor(" + UUIDNameMapping.getDefault()
        .nameOf(descriptor.getCharacteristic().getUuid()) + ", " + UUIDNameMapping.getDefault()
        .nameOf(descriptor.getUuid()) + ") call.");
    boolean ret = mBluetoothGatt.writeDescriptor(descriptor);
    if (ret) {
      Timber.d("writeDescriptor() called. ret=true");
    } else {
      Timber.e("writeDescriptor() called. ret=false");
    }
    return ret;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) public int requestMtu(int mtu) {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return NativeCode.FAIL;
    }
    if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT <= 20) {
      Timber.w("for KK, only 20bytes MTU");
      return NativeCode.NOT_SUPPORT;
    } else if (android.os.Build.VERSION.SDK_INT > 20) {
      boolean isMTK = PlatformUtils.isMTK();
      if (android.os.Build.VERSION.SDK_INT <= 22 && isMTK) {
        /// M: GATT MTU Feature: MTK Phone for L
        Timber.w("MTK Phone for L, only 20bytes MTU");
        return NativeCode.NOT_SUPPORT;
      } else {
        /// M: GATT MTU Feature: non-MTK Phone for L, All Phone for M/N
        Timber.i("requestMtu(" + mtu + ") call.");
        boolean success = mBluetoothGatt.requestMtu(mtu);
        if (success) {
          Timber.d("requestMtu() called. ret=true");
          return NativeCode.SUCCESS;
        } else {
          Timber.e("requestMtu() called. ret=false");
          return NativeCode.FAIL;
        }
      }
    }
    return NativeCode.NOT_SUPPORT;
  }

  public boolean requestConnectionPriority(int connectionPriority) {
    if (null == mBluetoothGatt) {
      Timber.e("null == mBluetoothGatt");
      return false;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Timber.d("requestConnectionPriority == " + connectionPriority);
      return mBluetoothGatt.requestConnectionPriority(connectionPriority);
    } else {
      Timber.w("SdkVersion:" + Build.VERSION.SDK_INT + " < LOLLIPOP");
      return false;
    }
  }
}
