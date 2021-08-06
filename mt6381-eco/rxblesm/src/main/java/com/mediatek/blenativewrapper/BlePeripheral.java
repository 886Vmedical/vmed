package com.mediatek.blenativewrapper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mediatek.blenativewrapper.commnication.BaseCommunicationItem;
import com.mediatek.blenativewrapper.commnication.ReadCharacteristicCommunication;
import com.mediatek.blenativewrapper.commnication.SetNotificationEnabledCommunication;
import com.mediatek.blenativewrapper.commnication.WriteCharacteristicCommunicationItem;
import com.mediatek.blenativewrapper.exceptions.ChangePeripheralException;
import com.mediatek.blenativewrapper.exceptions.CommunicateException;
import com.mediatek.blenativewrapper.exceptions.ConnectException;
import com.mediatek.blenativewrapper.exceptions.DisconnectException;
import com.mediatek.blenativewrapper.sm.State;
import com.mediatek.blenativewrapper.sm.StateMachine;
import com.mediatek.blenativewrapper.utils.DataConvertUtils;
import com.mediatek.blenativewrapper.utils.IteratorUtils;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

import static com.mediatek.blenativewrapper.BlePeripheralSettings.MTU_SIZE_DEFAULT;

public class BlePeripheral {

  @NonNull private final BlePeripheralSM mBlePeripheralSM;

  public BlePeripheral(@NonNull final Context context,
      @NonNull final DiscoverPeripheral discoverPeripheral, @Nullable final Looper looper) {
    this(context, discoverPeripheral, new Bundle(), looper);
  }

  public BlePeripheral(@NonNull final Context context,
      @NonNull final DiscoverPeripheral discoverPeripheral, @NonNull final Bundle setting,
      @Nullable final Looper looper) {
    this(context, setting, looper);
    changeDevice(discoverPeripheral, new ActionCallbackListener() {
      @Override public void onComplete() {
        Timber.i("BlePeripheral:init done");
      }

      @Override public void onError(Throwable throwable) {
        Timber.e(throwable, "BlePeripheral:init error");
      }
    });
  }

  public BlePeripheral(@NonNull final Context context, @NonNull final Bundle setting,
      @Nullable final Looper looper) {

    BlePeripheralSettings blePeripheralSettings = new BlePeripheralSettings();
    blePeripheralSettings.setParameter(setting);
    mBlePeripheralSM = new BlePeripheralSM(context, blePeripheralSettings, looper);
  }

  public void destroy() {
    Timber.i("BlePerpheral.destory");
    mBlePeripheralSM.sendMessage(BlePeripheralSM.EVT_DESTROY);
  }

  @NonNull public String getAddress() {
    if (mBlePeripheralSM.mBluetoothDeviceWrapper != null) {
      return mBlePeripheralSM.mBluetoothDeviceWrapper.getAddress();
    }
    return "NULL";
  }

  public int getMtuSize(){
    return mBlePeripheralSM.mCommunicatingState.mMtuSize;
  }

  @NonNull public String getLocalName() {
    return mBlePeripheralSM.mBluetoothDeviceWrapper.getLocalName();
  }

  @NonNull public BlePeripheralSettings getSettings() {
    return mBlePeripheralSM.mBlePeripheralSettings;
  }

  public StateInfo getStateInfo() {
    return mBlePeripheralSM.getStateInfo();
  }

  public boolean connect(@NonNull ActionReceiver actionReceiver,
      @NonNull ActionCallbackListener connectionListener) {
    return connect(actionReceiver, connectionListener, null);
  }

  public boolean connect(@NonNull ActionReceiver actionReceiver,
      @NonNull ActionCallbackListener connectionListener,
      @Nullable StateInfo.StateMonitor stateMonitor) {
    Timber.d("[connect] Address:" + getAddress());
    final Object[] objects = { actionReceiver, connectionListener, stateMonitor };
    mBlePeripheralSM.sendMessage(BlePeripheralSM.EVT_CONNECT, objects);
    return true;
  }

  public boolean disconnect(@NonNull ActionCallbackListener disconnectionListener) {
    Timber.d("[disconnect]Address:" + getAddress());
    final Object[] objects = { disconnectionListener };
    mBlePeripheralSM.sendMessage(BlePeripheralSM.EVT_DISCONNECT, objects);
    return true;
  }

  public void requestConnectionPriority(int connectionPriority,
      ActionCallbackListener actionCallbackListener) {
    final Object[] objects = { connectionPriority, actionCallbackListener };
    mBlePeripheralSM.sendMessage(BlePeripheralSM.EVT_REQUEST_CONNECTION_PRIORITY, objects);
  }

  @Nullable public List<BluetoothGattService> getServices() {
    return mBlePeripheralSM.getServices();
  }

  @Nullable public BluetoothGattService getService(@NonNull final UUID serviceUUID) {
    return mBlePeripheralSM.getService(serviceUUID);
  }

  @Nullable
  public BluetoothGattCharacteristic getCharacteristic(@NonNull final BluetoothGattService service,
      @NonNull final UUID characteristicUUID) {

    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
    if (null == characteristics) {
      Timber.e("[getCharacteristic]null == characteristics");
      return null;
    }
    if (0 == characteristics.size()) {
      Timber.e("[getCharacteristic]0 == characteristics.size()");
      return null;
    }

    BluetoothGattCharacteristic ret = null;
    for (BluetoothGattCharacteristic characteristic : characteristics) {
      if (characteristicUUID.equals(characteristic.getUuid())) {
        ret = characteristic;
        break;
      }
    }

    return ret;
  }

  @Nullable
  public BluetoothGattCharacteristic getCharacteristic(@NonNull final UUID characteristicUUID) {

    List<BluetoothGattService> services = mBlePeripheralSM.getServices();
    if (null == services) {
      Timber.e("[getCharacteristic]null == services");

      return null;
    }
    if (0 == services.size()) {
      Timber.e("[getCharacteristic]0 == services.size()");
      return null;
    }

    BluetoothGattCharacteristic ret = null;
    for (BluetoothGattService service : services) {
      ret = getCharacteristic(service, characteristicUUID);
      if (null != ret) {
        break;
      }
    }

    return ret;
  }

  public void writeCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic,
      byte[] data, @NonNull final CommunicateListener communicateListener) {
    writeCharacteristic(characteristic, false, data, communicateListener);
  }

  public void writeCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic,
      boolean noResponse, byte[] data, @NonNull final CommunicateListener communicateListener) {
    if(data.length < 30){
      Timber.d("[writeCharacteristic]Address:%s data:%s", getAddress() , DataConvertUtils.bytesToHex(data));
    }else {
      Timber.d("[writeCharacteristic]Address:%s data:%d", getAddress() , data.length);
    }

    final Object[] objects = {
        new WriteCharacteristicCommunicationItem(characteristic, noResponse, data),
        communicateListener
    };
    mBlePeripheralSM.sendMessage(BlePeripheralSM.EVT_COMMUNICATION_REQ, objects);
  }

  public void readCharacteristic(@NonNull final BluetoothGattCharacteristic characteristic,
      @NonNull final CommunicateListener communicateListener) {
    Timber.d("[readCharacteristic]Address:" + getAddress());

    final Object[] objects = {
        new ReadCharacteristicCommunication(characteristic), communicateListener
    };
    mBlePeripheralSM.sendMessage(BlePeripheralSM.EVT_COMMUNICATION_REQ, objects);
  }

  public void setNotificationEnabled(@NonNull final BluetoothGattCharacteristic characteristic,
      boolean enable, @NonNull final CommunicateListener communicateListener) {

    SetNotificationEnabledCommunication setNotificationEnabledCommunication =
        new SetNotificationEnabledCommunication(characteristic, enable);
    final Object[] objects = { setNotificationEnabledCommunication, communicateListener };
    mBlePeripheralSM.sendMessage(BlePeripheralSM.EVT_COMMUNICATION_REQ, objects);
  }

  public void changeDevice(DiscoverPeripheral discoverPeripheral,
      ActionCallbackListener callbackListener) {
    Timber.i("changeDevice:%s", discoverPeripheral.getAddress());
    mBlePeripheralSM.sendMessage(BlePeripheralSM.EVT_CHANGE_PERIPHERAL,
        new Object[] { discoverPeripheral.getBluetoothDevice(), callbackListener });
  }

  public interface CommunicateListener extends EventListener {
    void onComplete(int gattStatus);

    void onError(Throwable throwable);
  }

  public interface ActionCallbackListener {
    void onComplete();

    void onError(Throwable throwable);
  }

  public interface ActionReceiver {
    void didDisconnection(@NonNull String address);

    void onCharacteristicChanged(@NonNull UUID uuid, byte[] data, int len);
  }

  private static class BlePeripheralSM extends StateMachine {

    private static final int EVT_BASE = BlePrivateConstants.BLE_PERIPHERAL_EVT_BASE;
    private static final int LOCAL_EVT_BASE = BlePrivateConstants.LOCAL_EVT_BASE;

    private static final int EVT_CONNECT = EVT_BASE + 0x0001;
    private static final int EVT_DISCONNECT = EVT_BASE + 0x0002;
    private static final int EVT_COMMUNICATION_REQ = EVT_BASE + 0x0003;
    private static final int EVT_REQUEST_CONNECTION_PRIORITY = EVT_BASE + 0x0004;
    private static final int EVT_CHANGE_PERIPHERAL = EVT_BASE + 0x0005;

    private static final int EVT_DESTROY = EVT_BASE + 0xffff;

    private static final int EVT_GATT_CONNECTED = EVT_BASE + 0x1001;
    private static final int EVT_GATT_DISCONNECTED = EVT_BASE + 0x1002;
    private static final int EVT_DISCOVER_SERVICE_SUCCESS = EVT_BASE + 0x1004;
    private static final int EVT_DISCOVER_SERVICE_FAILURE = EVT_BASE + 0x1005;
    private static final int EVT_ON_CHARACTERISTIC_CHANGED = EVT_BASE + 0x1006;
    private static final int EVT_COMMUNICATION_RES = EVT_BASE + 0x1007;
    private static final int EVT_ON_MTU_CHANGED = EVT_BASE + 0x1008;
    private static final int EVT_REQUEST_MTU_FAILURE = EVT_BASE + 0x1009;

    @NonNull private final Context mContext;
    @NonNull private final BlePeripheralSettings mBlePeripheralSettings;
    @NonNull private final BleReceiver mBleReceiver;
    @NonNull private final StateInfo mStateInfo;
    @NonNull private final BluetoothGattCallbackWrapper mGattCallbackWrapper =
        new BluetoothGattCallbackWrapper() {
          @Override public void onConnectionStateChange(final int status, final int newState) {
            if (BluetoothProfile.STATE_CONNECTED == newState) {
              sendMessage(EVT_GATT_CONNECTED, status);
            } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {
              sendMessage(EVT_GATT_DISCONNECTED, status);
            }
          }

          @Override public void onServicesDiscovered(final int status) {
            if (GattStatusCode.GATT_SUCCESS == status) {
              sendMessage(EVT_DISCOVER_SERVICE_SUCCESS);
            } else {
              final Object[] objects = { status };
              sendMessage(EVT_DISCOVER_SERVICE_FAILURE, objects);
            }
          }

          @Override
          public void onCharacteristicRead(final BluetoothGattCharacteristic characteristic,
              final int status) {
            final Object[] objects =
                { CommunicationResType.OnCharacteristicRead, characteristic, status };
            sendMessage(EVT_COMMUNICATION_RES, objects);
            byte[] data = characteristic.getValue();
            sendMessage(EVT_ON_CHARACTERISTIC_CHANGED,
                new Object[] { characteristic, data, data.length });
          }

          @Override
          public void onCharacteristicWrite(final BluetoothGattCharacteristic characteristic,
              final int status) {
            final Object[] objects =
                { CommunicationResType.OnCharacteristicWrite, characteristic, status };
            sendMessage(EVT_COMMUNICATION_RES, objects);
          }

          @Override
          public void onCharacteristicChanged(final BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            final Object[] objects = { characteristic, data, data.length };
            sendMessage(EVT_ON_CHARACTERISTIC_CHANGED, objects);
          }

          @Override
          public void onDescriptorRead(BluetoothGattDescriptor descriptor, final int status) {
            final Object[] objects = { CommunicationResType.OnDescriptorRead, descriptor, status };
            sendMessage(EVT_COMMUNICATION_RES, objects);
          }

          @Override public void onDescriptorWrite(final BluetoothGattDescriptor descriptor,
              final int status) {
            final Object[] objects = { CommunicationResType.OnDescriptorWrite, descriptor, status };
            sendMessage(EVT_COMMUNICATION_RES, objects);
          }

          @Override public void onMtuChanged(int mtu, int status) {
            final Object[] objects = { mtu, status };
            if (GattStatusCode.GATT_SUCCESS == status) {
              sendMessage(EVT_ON_MTU_CHANGED, objects);
            } else {
              sendMessage(EVT_REQUEST_MTU_FAILURE, objects);
            }
          }
        };
    private final State mDefaultState = new DefaultState();
    private final State mEmptyState = new EmptyState();
    private final State mDeadObjectState = new DeadObjectState();
    private final State mBluetoothOffState = new BluetoothOffState();
    private final State mBluetoothOnState = new BluetoothOnState();
    private final State mDisconnectedState = new DisconnectedState();
    private final State mConnectingState = new ConnectingState();
    private final State mConnectedState = new ConnectedState();
    private final State mDisconnectingState = new DisconnectingState();
    private final State mConnectStartingState = new ConnectStartingState();
    private final State mPairingState = new PairingState();
    private final State mGattConnectingState = new GattConnectingState();
    private final State mRequestMtuState = new RequestMTUState();
    private final State mServiceDiscoveringState = new ServiceDiscoveringState();
    private final State mConnectCleanupState = new ConnectCleanupState();
    private final State mCommunicationReadyState = new CommunicationReadyState();
    private final CommunicatingState mCommunicatingState = new CommunicatingState();
    private final State mConnectionFailedState = new ConnectionFailedState();
    private BluetoothDeviceWrapper mBluetoothDeviceWrapper;
    private ActionReceiver mActionReceiver;
    private ActionCallbackListener mConnectionListener;
    private ActionCallbackListener mDisconnectionListener;
    private int mConnectRetryCount;
    private boolean mIsShowPairingDialog;

    BlePeripheralSM(@NonNull final Context context,
        @NonNull final BlePeripheralSettings blePeripheralSettings, @Nullable final Looper looper) {
      super("BleNativeWrapper", looper);

      mContext = context;
      mBlePeripheralSettings = blePeripheralSettings;
      mBleReceiver = new BleReceiver(mContext, getHandler());
      mStateInfo = new StateInfo();
      mStateInfo.setBondState(StateInfo.BondState.Unknown, false);

      addState(mDefaultState);
      addState(mBluetoothOnState, mDefaultState);
      addState(mEmptyState, mDefaultState);
      addState(mDisconnectedState, mBluetoothOnState);
      addState(mConnectingState, mBluetoothOnState);
      addState(mConnectedState, mBluetoothOnState);
      addState(mDisconnectingState, mBluetoothOnState);
      addState(mConnectStartingState, mConnectingState);
      addState(mPairingState, mConnectingState);
      addState(mGattConnectingState, mConnectingState);
      addState(mRequestMtuState, mConnectingState);
      addState(mServiceDiscoveringState, mConnectingState);
      addState(mConnectCleanupState, mConnectingState);
      addState(mCommunicationReadyState, mConnectedState);
      addState(mCommunicatingState, mConnectedState);
      addState(mConnectionFailedState, mDisconnectedState);
      addState(mDeadObjectState, mDefaultState);
      addState(mBluetoothOffState, mDefaultState);
      setInitialState(mEmptyState);
      start();
    }

    private static boolean isBluetoothOn(Context context) {
      BluetoothManager bluetoothManager =
          (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
      return bluetoothManager.getAdapter().isEnabled();
    }

    private void setDevice(@NonNull BluetoothDevice bluetoothDevice,
        ActionCallbackListener actionCallbackListener) {
      mBluetoothDeviceWrapper = new BluetoothDeviceWrapper(mContext, bluetoothDevice);
      mBleReceiver.setAddressFilter(mBluetoothDeviceWrapper.getAddress());
      mStateInfo.setBondState(mBluetoothDeviceWrapper.getBondState(), false);
      actionCallbackListener.onComplete();
    }

    @NonNull public StateInfo.ConnectionState getConnectionState() {
      return mStateInfo.getConnectionState();
    }

    @NonNull public StateInfo getStateInfo() {
      return mStateInfo;
    }

    @Nullable public BluetoothGattService getService(@NonNull final UUID uuid) {
      if (StateInfo.ConnectionState.Unknown == getConnectionState()) {
        Timber.e("[getService]Unknown state.");
        return null;
      }
      if (StateInfo.ConnectionState.Connected != getConnectionState()) {
        Timber.e("[getService]Not connected.");
        return null;
      }
      return mBluetoothDeviceWrapper.getService(uuid);
    }

    @Nullable public List<BluetoothGattService> getServices() {
      if (StateInfo.ConnectionState.Unknown == getConnectionState()) {
        Timber.e("[getService]Unknown state.");
        return null;
      }
      if (StateInfo.ConnectionState.Connected != getConnectionState()) {
        Timber.e("[getService]Not connected.");
        return null;
      }
      return mBluetoothDeviceWrapper.getServices();
    }

    private void onCommunicationRequestError(Object[] requestObjects, ErrorCode errorCode) {
      CommunicateListener communicateListener = (CommunicateListener) requestObjects[1];
      communicateListener.onError(new CommunicateException("errorCode:" + errorCode.name()));
    }

    private void assistPairingDialogIfNeeded() {
      if (mBlePeripheralSettings.AssistPairingDialog) {
        // Show pairing dialog mandatorily.
        // The app calls start discovery and cancel interface so that app will show pairing dialog each time
        // based on specification that Android O/S shows the dialog when the app pairs with a device
        // within 60 seconds after cancel discovery.
        BluetoothManager bluetoothManager =
            (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothManager.getAdapter().startDiscovery();
        bluetoothManager.getAdapter().cancelDiscovery();
      }
    }

    private void autoPairingIfNeeded(int variant) {
      if (!mBlePeripheralSettings.EnableAutoPairing) {
        return;
      }
      switch (variant) {
        case BlePrivateConstants.PAIRING_VARIANT_PIN:
        case BlePrivateConstants.PAIRING_VARIANT_PIN_16_DIGITS:
          if (null != mBlePeripheralSettings.AutoPairingPinCode
              && !mBlePeripheralSettings.AutoPairingPinCode.isEmpty()) {
            mBluetoothDeviceWrapper.setPin(mBlePeripheralSettings.AutoPairingPinCode);
          }
          break;
        case BlePrivateConstants.PAIRING_VARIANT_PASSKEY:
          if (null != mBlePeripheralSettings.AutoPairingPinCode
              && !mBlePeripheralSettings.AutoPairingPinCode.isEmpty()) {
            mBluetoothDeviceWrapper.setPasskey(mBlePeripheralSettings.AutoPairingPinCode);
          }
          break;
        case BlePrivateConstants.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
          mBluetoothDeviceWrapper.setPairingConfirmation(true);
          break;
        case BlePrivateConstants.PAIRING_VARIANT_CONSENT:
          mBluetoothDeviceWrapper.setPairingConfirmation(true);
          break;
        case BlePrivateConstants.PAIRING_VARIANT_DISPLAY_PASSKEY:
          break;
        case BlePrivateConstants.PAIRING_VARIANT_DISPLAY_PIN:
          break;
        case BlePrivateConstants.PAIRING_VARIANT_OOB_CONSENT:
          break;
        default:
          break;
      }
    }

    public enum CommunicationResType {
      OnCharacteristicWrite, OnCharacteristicRead, OnDescriptorWrite, OnDescriptorRead
    }

    private class DefaultState extends State {
      @Override public void enter(@Nullable Object[] transferObjects) {
        super.enter(transferObjects);
        mBleReceiver.registerReceiver();
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_DESTROY: {
            Object[] transferObjects = { StateInfo.Reason.DestroyRequest };
            transitionTo(mDeadObjectState, transferObjects);
            break;
          }
          case EVT_REQUEST_CONNECTION_PRIORITY: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener actionCallbackListener = (ActionCallbackListener) objects[1];
            actionCallbackListener.onError(
                new CommunicateException("badState:" + mStateInfo.getDetailedState().name()));
            break;
          }
          case EVT_DISCONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener disconnectionListener = (ActionCallbackListener) objects[0];
            disconnectionListener.onError(
                new DisconnectException("errorState: " + getCurrentState().getName()));
            break;
          }
          case EVT_COMMUNICATION_REQ: {
            onCommunicationRequestError((Object[]) msg.obj, ErrorCode.BadState);
            break;
          }
        }
        return StateMachine.HANDLED;
      }
    }

    private class EmptyState extends State {
      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_CHANGE_PERIPHERAL: {
            final Object[] objects = (Object[]) msg.obj;
            BluetoothDevice bluetoothDevice = (BluetoothDevice) objects[0];
            ActionCallbackListener actionCallbackListener = (ActionCallbackListener) objects[1];
            setDevice(bluetoothDevice, actionCallbackListener);
            if (BluetoothProfile.STATE_DISCONNECTED != mBluetoothDeviceWrapper.getGattState()) {
              Timber.w("[processMessage]This peripheral is connected by the other module.");
            }
            if (isBluetoothOn(mContext)) {
              transitionTo(mDisconnectedState);
            } else {
              Timber.w("[processMessage]Bluetooth off.");
              transitionTo(mBluetoothOffState);
            }
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }
    }

    private class DeadObjectState extends State {
      @Override public void enter(Object[] transferObjects) {
        mBleReceiver.unregisterReceiver();
        if (mBluetoothDeviceWrapper != null && mBluetoothDeviceWrapper.hasGatt()) {
          mBluetoothDeviceWrapper.closeGatt();
        }
        if (null != transferObjects && transferObjects[0] instanceof ErrorCode) {
          ErrorCode errorCode = (ErrorCode) transferObjects[0];
          if (null != mConnectionListener) {
            mConnectionListener.onError(new ConnectException("errorCode:" + errorCode.name()));
            mConnectionListener = null;
          }
          if (null != mDisconnectionListener) {
            mDisconnectionListener.onError(
                new DisconnectException("errorCode:" + errorCode.name()));
            mDisconnectionListener = null;
          }
        }
        if (null != mActionReceiver) {
          mActionReceiver.didDisconnection(mBluetoothDeviceWrapper.getAddress());
        }
        mActionReceiver = null;
        mStateInfo.setDetailedState(StateInfo.DetailedState.Dead, null, true);
        mStateInfo.setBondState(StateInfo.BondState.Unknown, false);
        mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Unknown, false);
        mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Unknown, false);
        if (transferObjects != null && transferObjects[0] instanceof StateInfo.Reason) {
          StateInfo.Reason reason = (StateInfo.Reason) transferObjects[0];
          if (reason == StateInfo.Reason.DestroyRequest) {
            quit();
          }
        }
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_CONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener connectionListener = (ActionCallbackListener) objects[1];
            connectionListener.onError(
                new ConnectException("errorState:" + getCurrentState().getName()));
            break;
          }
          case EVT_CHANGE_PERIPHERAL: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener actionCallbackListener = (ActionCallbackListener) objects[1];
            actionCallbackListener.onError(new ChangePeripheralException());
            break;
          }
          case EVT_DISCONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener disconnectionListener = (ActionCallbackListener) objects[0];
            disconnectionListener.onError(
                new DisconnectException("errorState:" + getCurrentState().getName()));
            break;
          }
          case EVT_DESTROY: {
            quit();
            break;
          }
          case EVT_COMMUNICATION_REQ: {
            onCommunicationRequestError((Object[]) msg.obj, ErrorCode.DeadObject);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }
    }

    private class BluetoothOffState extends State {
      @Override public void enter(Object[] transferObjects) {
        if (mBluetoothDeviceWrapper.hasGatt()) {
          mBluetoothDeviceWrapper.closeGatt();
        }
        if (null != transferObjects) {
          ErrorCode errorCode = (ErrorCode) transferObjects[0];
          if (null != mConnectionListener) {
            mConnectionListener.onError(new ConnectException("errorCode:" + errorCode.name()));
            mConnectionListener = null;
          }
          if (null != mDisconnectionListener) {
            mDisconnectionListener.onError(new DisconnectException("errorCode:" + errorCode));
            mDisconnectionListener = null;
          }
        }
        if (null != mActionReceiver) {
          mActionReceiver.didDisconnection(mBluetoothDeviceWrapper.getAddress());
        }
        mActionReceiver = null;
        mStateInfo.setDetailedState(StateInfo.DetailedState.BluetoothOff, null, true);
        mStateInfo.setBondState(StateInfo.BondState.Unknown, false);
        mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Unknown, false);
        mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Unknown, false);
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case BleReceiver.EVT_BLUETOOTH_STATE_CHANGED: {
            final int bluetoothState = msg.arg1;
            if (BluetoothAdapter.STATE_ON == bluetoothState) {
              transitionTo(mDisconnectedState);
            }
            break;
          }
          case EVT_CONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener connectionListener = (ActionCallbackListener) objects[1];
            connectionListener.onError(
                new ConnectException("errorCode:" + ErrorCode.BluetoothOff.name()));
            break;
          }
          case EVT_CHANGE_PERIPHERAL: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener actionCallbackListener = (ActionCallbackListener) objects[1];
            actionCallbackListener.onError(new ChangePeripheralException());
            break;
          }
          case EVT_DISCONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener disconnectionListener = (ActionCallbackListener) objects[0];
            disconnectionListener.onError(
                new DisconnectException("errorCode:" + ErrorCode.BluetoothOff.name()));
            break;
          }
          case EVT_COMMUNICATION_REQ: {
            onCommunicationRequestError((Object[]) msg.obj, ErrorCode.BluetoothOff);
            break;
          }
          case EVT_DESTROY: {
            transitionTo(mDeadObjectState, new Object[] { StateInfo.Reason.DestroyRequest });
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }
    }

    private class BluetoothOnState extends State {
      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_DESTROY: {
            Object[] transferObjects = { StateInfo.Reason.DestroyRequest };
            transitionTo(mDeadObjectState, transferObjects);
            break;
          }
          case BleReceiver.EVT_BLUETOOTH_STATE_CHANGED: {
            final int bluetoothState = msg.arg1;
            if (BluetoothAdapter.STATE_TURNING_OFF == bluetoothState
                || BluetoothAdapter.STATE_OFF == bluetoothState) {
              Object[] transferObjects = { ErrorCode.BluetoothOff };
              transitionTo(mBluetoothOffState, transferObjects);
            }
            break;
          }
          case EVT_CONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener connectionListener = (ActionCallbackListener) objects[1];
            connectionListener.onError(
                new ConnectException("errorState:" + getCurrentState().getName()));
            break;
          }
          case EVT_CHANGE_PERIPHERAL: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener actionCallbackListener = (ActionCallbackListener) objects[1];
            actionCallbackListener.onError(new ChangePeripheralException());
            break;
          }
          case EVT_DISCONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            ActionCallbackListener disconnectionListener = (ActionCallbackListener) objects[0];
            disconnectionListener.onError(
                new DisconnectException("errorCode" + ErrorCode.BadState.name()));
            break;
          }
          case EVT_COMMUNICATION_REQ: {
            onCommunicationRequestError((Object[]) msg.obj, ErrorCode.BadState);
            break;
          }
          case BleReceiver.EVT_BOND_NONE: {
            mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
            break;
          }
          case BleReceiver.EVT_BONDING: {
            mStateInfo.setBondState(StateInfo.BondState.Bonding, true);
            break;
          }
          case BleReceiver.EVT_BONDED: {
            mStateInfo.setBondState(StateInfo.BondState.Bonded, true);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }
    }

    private class ConnectionFailedState extends State {
      @Override public void enter(Object[] transferObjects) {
        ErrorCode errorCode = (ErrorCode) transferObjects[0];
        mConnectionListener.onError(new ConnectException("errorCode:" + errorCode));
        mConnectionListener = null;
      }
    }

    private class DisconnectedState extends State {
      @Override public void enter(Object[] transferObjects) {

        if (mBluetoothDeviceWrapper.hasGatt()) {
          if (mBlePeripheralSettings.UseRefreshGatt) {
            mBluetoothDeviceWrapper.refreshGatt();
          }
          mBluetoothDeviceWrapper.closeGatt();
        }

        if (null != mDisconnectionListener) {
          // Disconnection by disconnect request.
          mDisconnectionListener.onComplete();
          mDisconnectionListener = null;
        } else if (null != mActionReceiver) {
          // Disconnection by peripheral or OS.
          mActionReceiver.didDisconnection(mBluetoothDeviceWrapper.getAddress());
        }
        mActionReceiver = null;
        mStateInfo.setDetailedState(StateInfo.DetailedState.Disconnected, null, true);
        if (transferObjects != null && transferObjects[0] instanceof StateInfo.Reason) {
          StateInfo.Reason reason = (StateInfo.Reason) transferObjects[0];
          if (reason == StateInfo.Reason.DestroyRequest) {
            transitionTo(mDeadObjectState, transferObjects);
          }
        }
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_CONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            mActionReceiver = (ActionReceiver) objects[0];
            mConnectionListener = (ActionCallbackListener) objects[1];
            final StateInfo.StateMonitor stateMonitor = (StateInfo.StateMonitor) objects[2];
            mStateInfo.setStateMonitor(stateMonitor);
            transitionTo(mConnectStartingState);
            break;
          }
          case EVT_CHANGE_PERIPHERAL: {
            final Object[] objects = (Object[]) msg.obj;
            BluetoothDevice bluetoothDevice = (BluetoothDevice) objects[0];
            ActionCallbackListener actionCallbackListener = (ActionCallbackListener) objects[1];
            setDevice(bluetoothDevice, actionCallbackListener);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }
    }

    private class ConnectingState extends State {

      private void transitionToCleanupState() {
        if (mConnectCleanupState == getCurrentState()) {
          Timber.d("[transitionToCleanupState]Already transition to ConnectCleanupState.");
          return;
        }
        if (mIsShowPairingDialog) {
          // No retry when connection failed in showing pairing dialog.
          // ex) Select [Cancel] / Invalid PIN input
          Timber.w("[transitionToCleanupState]Pairing failed.");
          Object[] transferObjects = { ErrorCode.PairingFailed, ConnectCleanupState.NOT_RETRY };
          transitionTo(mConnectCleanupState, transferObjects);
        } else {
          // Retry when unexpected connection failed.
          Timber.e("[transitionToCleanupState]Connection failed.");
          Object[] transferObjects = { ErrorCode.GattConnectionFailure, ConnectCleanupState.RETRY };
          transitionTo(mConnectCleanupState, transferObjects);
        }
      }

      @Override public void enter(Object[] transferObjects) {
        mConnectRetryCount = 0;
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_DISCONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            mDisconnectionListener = (ActionCallbackListener) objects[0];
            Object[] transferObjects = { StateInfo.Reason.DisconnectRequest };
            transitionTo(mDisconnectingState, transferObjects);
            break;
          }
          case EVT_DESTROY: {
            Object[] transferObjects = { StateInfo.Reason.DestroyRequest };
            transitionTo(mDisconnectingState, transferObjects);
            break;
          }
          case EVT_GATT_DISCONNECTED: {
            mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Disconnected, true);
            transitionToCleanupState();
            break;
          }
          case BleReceiver.EVT_BOND_NONE: {
            mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
            transitionToCleanupState();
            break;
          }
          case BleReceiver.EVT_ACL_DISCONNECTED: {
            mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Disconnected, true);
            transitionToCleanupState();
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }
    }

    private class DisconnectingState extends State {

      private static final int EVT_DISCONNECTING_TIMEOUT = LOCAL_EVT_BASE + 0x0001;
      private static final long DISCONNECTING_WAIT_TIME = 1000 * 10;
      private StateInfo.Reason disconnectingReason;

      private boolean isTeardownCompleted() {
        if (StateInfo.GattConnectionState.Disconnected != mStateInfo.getGattConnectionState()) {
          Timber.i("[isTeardownCompleted]Gatt disconnecting.");
          return false;
        }
        if (mBlePeripheralSettings.UseRemoveBond
            && StateInfo.BondState.NotBonded != mStateInfo.getBondState()) {
          Timber.i("[isTeardownCompleted]Bond removing.");
          return false;
        }
        if (StateInfo.BondState.Bonding == mStateInfo.getBondState()) {
          Timber.i("[isTeardownCompleted]Bond processing.");
          return false;
        }
        if (StateInfo.AclConnectionState.Disconnected != mStateInfo.getAclConnectionState()) {
          Timber.i("[isTeardownCompleted]Acl disconnecting.");
          return false;
        }
        Timber.i("[isTeardownCompleted]Teardown completed.");
        return true;
      }

      private void teardownOrTransitionToDisconnectedState() {
        if (isTeardownCompleted()) {
          transitionTo(mDisconnectedState,
              disconnectingReason == null ? null : new Object[] { disconnectingReason });
        } else {
          if (StateInfo.GattConnectionState.Disconnected != mStateInfo.getGattConnectionState()) {
            mBluetoothDeviceWrapper.disconnectGatt();
          } else if (mBlePeripheralSettings.UseRemoveBond && mStateInfo.isBonded()) {
            mBluetoothDeviceWrapper.removeBond();
          } else if (StateInfo.BondState.Bonding == mStateInfo.getBondState()) {
            mBluetoothDeviceWrapper.cancelBondProcess();
          }
        }
      }

      @Override public void enter(Object[] transferObjects) {
        disconnectingReason = (StateInfo.Reason) transferObjects[0];
        mStateInfo.setDetailedState(StateInfo.DetailedState.Disconnecting, disconnectingReason,
            true);
        teardownOrTransitionToDisconnectedState();
        sendMessageDelayed(EVT_DISCONNECTING_TIMEOUT, DISCONNECTING_WAIT_TIME);
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_DISCONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            final ActionCallbackListener disconnectionListener =
                (ActionCallbackListener) objects[0];
            if (null != mDisconnectionListener) {
              disconnectionListener.onError(
                  new DisconnectException("errorCode:" + ErrorCode.Busy.name()));
              break;
            }
            mDisconnectionListener = disconnectionListener;
            break;
          }
          case EVT_DESTROY: {
            disconnectingReason = StateInfo.Reason.DestroyRequest;
            break;
          }
          case EVT_GATT_DISCONNECTED: {
            mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Disconnected, true);
            teardownOrTransitionToDisconnectedState();
            break;
          }
          case BleReceiver.EVT_BOND_NONE: {
            mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
            teardownOrTransitionToDisconnectedState();
            break;
          }
          case BleReceiver.EVT_ACL_DISCONNECTED: {
            mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Disconnected, true);
            teardownOrTransitionToDisconnectedState();
            break;
          }
          case EVT_DISCONNECTING_TIMEOUT: {
            // There are cases when timeout has occurred without notification of
            // ACL Disconnected or Bond None and move to next state in theses cases.
            transitionTo(mDisconnectedState);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }

      @Override public void exit() {
        removeMessages(EVT_DISCONNECTING_TIMEOUT);
        disconnectingReason = null;
      }
    }

    private class ConnectStartingState extends State {
      private static final int EVT_REMOVE_BOND_TIMEOUT = LOCAL_EVT_BASE + 0x0001;
      private static final int TIMEOUT = 2000;
      @Override public void enter(Object[] transferObjects) {
        mIsShowPairingDialog = false;
        if (mBlePeripheralSettings.UseCreateBond) {
          if(mBluetoothDeviceWrapper.isBonded()){
            if(!mBluetoothDeviceWrapper.removeBond()) {
              transitionTo(mPairingState);
            }else {
              sendMessageDelayed(EVT_REMOVE_BOND_TIMEOUT,TIMEOUT);
            }
          }else {
            transitionTo(mPairingState);
          }
        } else {
          transitionTo(mGattConnectingState);
        }
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what){
          case BleReceiver.EVT_BOND_NONE:{
            mStateInfo.setBondState(StateInfo.BondState.NotBonded, false);
            transitionTo(mPairingState);
            break;
          }
          case EVT_REMOVE_BOND_TIMEOUT:{
            transitionTo(mConnectCleanupState, new Object[]{ ErrorCode.RemovePairTimeout, ConnectCleanupState.RETRY });
            break;
          }
          default:return NOT_HANDLED;
        }
        return HANDLED;
      }

      @Override public void exit() {
        super.exit();
        removeMessages(EVT_REMOVE_BOND_TIMEOUT);
      }
    }

    private class PairingState extends State {

      private static final int EVT_PAIRING_TIMEOUT = LOCAL_EVT_BASE + 0x0001;

      private static final long PAIRING_TIME = 1000 * 10;

      private void transitionToNextStateIfPaired() {
        if (!mStateInfo.isBonded()) {
          Timber.i("[transitionToNextStateIfPaired]Wait bonded.");
          return;
        }
        if (StateInfo.AclConnectionState.Connected != mStateInfo.getAclConnectionState()) {
          Timber.i("Acl connecting. skip wait acl_connected");
        }
        Timber.i("Pairing completed.");
        transitionTo(mGattConnectingState);
      }

      @Override public void enter(Object[] transferObjects) {
        mStateInfo.setDetailedState(StateInfo.DetailedState.Pairing, null, true);
        assistPairingDialogIfNeeded();
        if (!mBluetoothDeviceWrapper.createBond()) {
          Object[] objects = { ErrorCode.OSNativeError, ConnectCleanupState.RETRY };
          transitionTo(mConnectCleanupState, objects);
          return;
        }
        sendMessageDelayed(EVT_PAIRING_TIMEOUT, PAIRING_TIME);
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case BleReceiver.EVT_PAIRING_REQUEST: {
            removeMessages(EVT_PAIRING_TIMEOUT);
            mIsShowPairingDialog = true;
            autoPairingIfNeeded(msg.arg1);
            break;
          }
          case BleReceiver.EVT_ACL_CONNECTED: {
            mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Connected, true);
            transitionToNextStateIfPaired();
            break;
          }
          case BleReceiver.EVT_BONDED: {
            mIsShowPairingDialog = false;
            mStateInfo.setBondState(StateInfo.BondState.Bonded, true);
            transitionToNextStateIfPaired();
            break;
          }
          case EVT_PAIRING_TIMEOUT: {
            Object[] transferObjects = { ErrorCode.PairingTimeout, ConnectCleanupState.NOT_RETRY };
            transitionTo(mConnectCleanupState, transferObjects);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }

      @Override public void exit() {
        removeMessages(EVT_PAIRING_TIMEOUT);
      }
    }

    private class GattConnectingState extends State {

      private static final int EVT_GATT_CONNECTION_TIMEOUT = LOCAL_EVT_BASE + 0x0001;

      private static final int EVT_STABLE_CONNECTION = LOCAL_EVT_BASE + 0x0002;

      private static final long GATT_CONNECTION_TIME = 1000 * 30;

      private boolean mNotBeenPairing;

      private void transitionToNextStateIfGattConnectionStabled() {
        if (StateInfo.GattConnectionState.Connected != mStateInfo.getGattConnectionState()) {
          Timber.i("Gatt connecting.");
          return;
        }
        if (hasMessage(EVT_STABLE_CONNECTION)) {
          Timber.i("Wait connection stabled.");
          return;
        }
        if (!mNotBeenPairing && !mStateInfo.isBonded()) {
          Timber.i("Wait bonded.");
          return;
        }
        if (StateInfo.AclConnectionState.Connected != mStateInfo.getAclConnectionState()) {
          Timber.i("Acl connecting. skip wait acl_connected");
        }
        Timber.i("Gatt connection completed.");
        if (mBlePeripheralSettings.mtuSize > MTU_SIZE_DEFAULT) {
          transitionTo(mRequestMtuState);
        } else {
          transitionTo(mServiceDiscoveringState);
        }
      }

      @Override public void enter(Object[] transferObjects) {
        mStateInfo.setDetailedState(StateInfo.DetailedState.GattConnecting, null, true);
        mNotBeenPairing = false;
        if (!mStateInfo.isBonded()) {
          assistPairingDialogIfNeeded();
        }
        if (!mBluetoothDeviceWrapper.connectGatt(mContext, mGattCallbackWrapper)) {
          Object[] objects = { ErrorCode.OSNativeError, ConnectCleanupState.RETRY };
          transitionTo(mConnectCleanupState, objects);
          return;
        }
        sendMessageDelayed(EVT_GATT_CONNECTION_TIMEOUT, GATT_CONNECTION_TIME);
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case BleReceiver.EVT_PAIRING_REQUEST: {
            removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
            mIsShowPairingDialog = true;
            autoPairingIfNeeded(msg.arg1);
            break;
          }
          case BleReceiver.EVT_ACL_CONNECTED: {
            mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Connected, true);
            transitionToNextStateIfGattConnectionStabled();
            break;
          }
          case EVT_GATT_CONNECTED: {
            removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
            mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Connected, true);
            long stableConnectionWaitTime = 0;
            if (mBlePeripheralSettings.StableConnection) {
              stableConnectionWaitTime = mBlePeripheralSettings.StableConnectionWaitTime;
            }
            sendMessageDelayed(EVT_STABLE_CONNECTION, stableConnectionWaitTime);
            break;
          }
          case BleReceiver.EVT_BONDING: {
            if (mStateInfo.isBonded()) {
              // Set foreground because of showing pairing dialog when state is from Bonded to Bonding.
              assistPairingDialogIfNeeded();
            }
            mStateInfo.setBondState(StateInfo.BondState.Bonding, true);
            break;
          }
          case BleReceiver.EVT_BONDED: {
            mIsShowPairingDialog = false;
            mStateInfo.setBondState(StateInfo.BondState.Bonded, true);
            transitionToNextStateIfGattConnectionStabled();
            break;
          }
          case EVT_STABLE_CONNECTION: {
            Timber.i("EVT_STABLE_CONNECTION");
            if (StateInfo.BondState.NotBonded == mStateInfo.getBondState()) {
              // Target device does not pair by connectGatt() if pairing function
              // is not run after GATT connect within defined time.
              mNotBeenPairing = true;
              Timber.i("Not been pairing in the connection process.");
            }
            transitionToNextStateIfGattConnectionStabled();
            break;
          }
          case EVT_GATT_CONNECTION_TIMEOUT: {
            Timber.i("EVT_GATT_CONNECTION_TIMEOUT");
            Object[] transferObjects =
                { ErrorCode.GattConnectionTimeout, ConnectCleanupState.NOT_RETRY };
            transitionTo(mConnectCleanupState, transferObjects);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }

      @Override public void exit() {
        removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
        removeMessages(EVT_STABLE_CONNECTION);
      }
    }

    private class RequestMTUState extends State {
      private static final int EVT_START_REQUEST_MTU = LOCAL_EVT_BASE + 0x0001;
      private static final int START_REQUEST_MTU_DELAY = 500;
      private static final int EVT_REQUEST_MTU_TIMEOUT = LOCAL_EVT_BASE + 0x0002;
      private static final long REQUEST_WAIT_TIME = 1000 * 4;

      @Override public void enter(@Nullable Object[] transferObjects) {
        mStateInfo.setDetailedState(StateInfo.DetailedState.MtuRequesting, null, true);
        mCommunicatingState.mMtuSize = MTU_SIZE_DEFAULT;
        Object[] objects = new Object[] { mBlePeripheralSettings.mtuSize };
        sendMessageDelayed(EVT_START_REQUEST_MTU, objects, START_REQUEST_MTU_DELAY);
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_START_REQUEST_MTU: {
            Object[] objects = (Object[]) msg.obj;
            int mtu = (int) objects[0];
            int returnCode = mBluetoothDeviceWrapper.requestMtu(mtu);
            switch (returnCode) {
              case NativeCode.NOT_SUPPORT: {
                transitionTo(mServiceDiscoveringState);
                break;
              }
              case NativeCode.FAIL: {
                Object[] transferObjects =
                    { ErrorCode.RequestMtuFailure, ConnectCleanupState.NOT_RETRY };
                transitionTo(mConnectCleanupState, transferObjects);
                break;
              }
              case NativeCode.SUCCESS: {
                sendMessageDelayed(EVT_REQUEST_MTU_TIMEOUT, REQUEST_WAIT_TIME);
                break;
              }
              default:
                return NOT_HANDLED;
            }
            break;
          }
          case EVT_REQUEST_MTU_FAILURE: {
            Object[] transferObjects =
                { ErrorCode.RequestMtuFailure, ConnectCleanupState.NOT_RETRY };
            transitionTo(mConnectCleanupState, transferObjects);
            return HANDLED;
          }
          case EVT_REQUEST_MTU_TIMEOUT: {
            Timber.i("EVT_REQUEST_MTU_TIMEOUT");
            Object[] transferObjects = { ErrorCode.RequestMtuTimeout, ConnectCleanupState.RETRY };
            transitionTo(mConnectCleanupState, transferObjects);
            return HANDLED;
          }
          case EVT_ON_MTU_CHANGED: {
            Object[] objects = (Object[]) msg.obj;
            mCommunicatingState.mMtuSize = (int) objects[0];
            transitionTo(mServiceDiscoveringState);
            return HANDLED;
          }
          default:
            return NOT_HANDLED;
        }
        return HANDLED;
      }

      @Override public void exit() {
        removeMessages(EVT_REQUEST_MTU_TIMEOUT);
        removeMessages(EVT_START_REQUEST_MTU);
      }
    }

    private class ServiceDiscoveringState extends State {

      private static final int EVT_START_DISCOVER_SERVICE = LOCAL_EVT_BASE + 0x0001;
      private static final int EVT_DISCOVER_SERVICE_TIMEOUT = LOCAL_EVT_BASE + 0x0002;

      private static final long SERVICE_DISCOVERED_WAIT_TIME = 1000 * 30;
      private boolean needRefresh;
      @Override public void enter(Object[] transferObjects) {
        needRefresh = true;
        mStateInfo.setDetailedState(StateInfo.DetailedState.ServiceDiscovering, null, true);
        List<BluetoothGattService> services = mBluetoothDeviceWrapper.getServices();
        if (null == services || 0 == services.size()) {
          sendMessageDelayed(EVT_START_DISCOVER_SERVICE,
              mBlePeripheralSettings.DiscoverServiceDelayTime);
        } else {
          sendMessage(EVT_DISCOVER_SERVICE_SUCCESS);
        }
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_START_DISCOVER_SERVICE: {
            boolean result = mBluetoothDeviceWrapper.discoverServices();
            if (!result) {
              sendMessage(EVT_DISCOVER_SERVICE_FAILURE);
              break;
            }
            sendMessageDelayed(EVT_DISCOVER_SERVICE_TIMEOUT, SERVICE_DISCOVERED_WAIT_TIME);
            break;
          }
          case EVT_DISCOVER_SERVICE_SUCCESS: {
            for (BluetoothGattService service : mBluetoothDeviceWrapper.getServices()) {
              for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Timber.d("service:%s - characteristic:%s", service.getUuid().toString(),
                    characteristic.getUuid().toString());
              }
            }
            if(needRefresh) {
              mBluetoothDeviceWrapper.refreshGatt();
              sendMessage(EVT_START_DISCOVER_SERVICE);
              needRefresh = false;
            }else {
              transitionTo(mCommunicationReadyState);
            }
            break;
          }
          case EVT_DISCOVER_SERVICE_FAILURE: {
            Timber.e("Discover service failure.");
            mBluetoothDeviceWrapper.disconnectGatt();
            Object[] transferObjects =
                { ErrorCode.DiscoverServiceFailure, ConnectCleanupState.NOT_RETRY };
            transitionTo(mConnectCleanupState, transferObjects);
            break;
          }
          case EVT_DISCOVER_SERVICE_TIMEOUT: {
            Timber.e("Discover service timeout.");
            mBluetoothDeviceWrapper.disconnectGatt();
            Object[] transferObjects =
                { ErrorCode.DiscoverServiceTimeout, ConnectCleanupState.RETRY };
            transitionTo(mConnectCleanupState, transferObjects);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }

      @Override public void exit() {
        removeMessages(EVT_START_DISCOVER_SERVICE);
        removeMessages(EVT_DISCOVER_SERVICE_TIMEOUT);
      }
    }

    private class ConnectCleanupState extends State {

      public static final int NOT_RETRY = 0;
      public static final int RETRY = 1;

      // There are cases when GATT Disconnect is notified before Bluetooth Turning Off notification
      // within few ms under Bluetooth off in connect running.
      private static final int EVT_START_CLEANUP = LOCAL_EVT_BASE + 0x0001;

      private static final int EVT_CLEANUP_TIMEOUT = LOCAL_EVT_BASE + 0x0002;

      private static final long CLEANUP_DELAY_TIME = 100;

      private static final long CLEANUP_TIME = 1000 * 10;

      private ErrorCode mCleanupReason;
      private boolean mIsRetryRequested;

      private boolean isCleanupCompleted() {
        if (hasMessage(EVT_START_CLEANUP)) {
          Timber.i("[isCleanupCompleted]Wait cleanup start.");
          return false;
        }
        if (StateInfo.GattConnectionState.Disconnected != mStateInfo.getGattConnectionState()) {
          Timber.i("[isCleanupCompleted]Gatt disconnecting.");
          return false;
        }
        if (StateInfo.BondState.Bonding == mStateInfo.getBondState()) {
          Timber.i("[isCleanupCompleted]Bond process canceling.");
          return false;
        }
        if (StateInfo.AclConnectionState.Disconnected != mStateInfo.getAclConnectionState()) {
          Timber.i("[isCleanupCompleted]Acl disconnecting.");
          return false;
        }
        Timber.i("[isCleanupCompleted]Cleanup completed.");
        return true;
      }

      private void cleanupOrTransitionToNextState() {
        if (isCleanupCompleted()) {
          if (!mBlePeripheralSettings.EnableConnectRetry || !mIsRetryRequested) {
            Timber.w("[cleanupOrTransitionToNextState]Connection end because not request a retry.");
            Object[] transferObjects = { mCleanupReason };
            transitionTo(mConnectionFailedState, transferObjects);
            return;
          }
          if (mBlePeripheralSettings.ConnectRetryCount <= mConnectRetryCount) {
            Timber.e("[cleanupOrTransitionToNextState]Connection failed because retry count reaches the maximum value.");
            Object[] transferObjects = { mCleanupReason };
            transitionTo(mConnectionFailedState, transferObjects);
            return;
          }
          mConnectRetryCount++;
          Timber.w("[cleanupOrTransitionToNextState]Connection retry. count:" + mConnectRetryCount);
          transitionTo(mConnectStartingState);
        } else {
          if (StateInfo.GattConnectionState.Disconnected != mStateInfo.getGattConnectionState()) {
            mBluetoothDeviceWrapper.disconnectGatt();
          } else if (StateInfo.BondState.Bonding == mStateInfo.getBondState()) {
            mBluetoothDeviceWrapper.cancelBondProcess();
          }
        }
      }

      @Override public void enter(Object[] transferObjects) {
        mStateInfo.setDetailedState(StateInfo.DetailedState.Cleanup, null, true);
        mCleanupReason = (ErrorCode) transferObjects[0];
        int retry = (int) transferObjects[1];
        mIsRetryRequested = (RETRY == retry);
        sendMessageDelayed(EVT_START_CLEANUP, CLEANUP_DELAY_TIME);
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_START_CLEANUP: {
            sendMessageDelayed(EVT_CLEANUP_TIMEOUT, CLEANUP_TIME);
            cleanupOrTransitionToNextState();
            break;
          }
          case EVT_GATT_DISCONNECTED: {
            mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Disconnected, true);
            cleanupOrTransitionToNextState();
            break;
          }
          case BleReceiver.EVT_BOND_NONE: {
            mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
            cleanupOrTransitionToNextState();
            break;
          }
          case BleReceiver.EVT_ACL_DISCONNECTED: {
            mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Disconnected, true);
            cleanupOrTransitionToNextState();
            break;
          }
          case EVT_CLEANUP_TIMEOUT: {
            Object[] transferObjects = { mCleanupReason };
            transitionTo(mConnectionFailedState, transferObjects);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }

      @Override public void exit() {
        removeMessages(EVT_CLEANUP_TIMEOUT);
      }
    }

    private class ConnectedState extends State {

      @Override public void enter(Object[] transferObjects) {
        mStateInfo.setConnectionState(StateInfo.ConnectionState.Connected, true);
        mConnectionListener.onComplete();
        mConnectionListener = null;
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_ON_CHARACTERISTIC_CHANGED: {
            final Object[] objects = (Object[]) msg.obj;
            final BluetoothGattCharacteristic characteristic =
                (BluetoothGattCharacteristic) objects[0];
            final byte[] data = (byte[]) objects[1];
            int length = (int) objects[2];
            mActionReceiver.onCharacteristicChanged(characteristic.getUuid(), data, length);
            break;
          }
          case EVT_DISCONNECT: {
            final Object[] objects = (Object[]) msg.obj;
            mDisconnectionListener = (ActionCallbackListener) objects[0];
            Object[] transferObjects = { StateInfo.Reason.DisconnectRequest };
            transitionTo(mDisconnectingState, transferObjects);
            break;
          }
          case EVT_DESTROY: {
            Object[] transferObjects = { StateInfo.Reason.DestroyRequest };
            transitionTo(mDisconnectingState, transferObjects);
            break;
          }
          case EVT_GATT_DISCONNECTED: {
            mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Disconnected, true);
            Object[] transferObjects = { StateInfo.Reason.DidDisconnection };
            transitionTo(mDisconnectingState, transferObjects);
            break;
          }
          case BleReceiver.EVT_ACL_DISCONNECTED: {
            mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Disconnected, true);
            break;
          }
          case BleReceiver.EVT_BOND_NONE: {
            mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
            Object[] transferObjects = { StateInfo.Reason.EncryptionFailed };
            transitionTo(mDisconnectingState, transferObjects);
            break;
          }
          case EVT_REQUEST_CONNECTION_PRIORITY: {
            final Object[] objects = (Object[]) msg.obj;
            int priority = (int) objects[0];
            ActionCallbackListener actionCallbackListener = (ActionCallbackListener) objects[1];
            boolean success = mBluetoothDeviceWrapper.requestConnectionPriority(priority);
            if (success) {
              actionCallbackListener.onComplete();
            } else {
              actionCallbackListener.onError(
                  new CommunicateException("request connection priority:Native Request fail"));
            }
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }
    }

    private class CommunicationReadyState extends State {

      @Override public void enter(Object[] transferObjects) {
        mStateInfo.setDetailedState(StateInfo.DetailedState.CommunicationReady, null, true);
      }

      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_COMMUNICATION_REQ: {
            final Object[] objects = (Object[]) msg.obj;
            transitionTo(mCommunicatingState, objects);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }
    }

    private class CommunicatingState extends State {

      private static final int EVT_START_COMMUNICATION = LOCAL_EVT_BASE + 0x0001;
      private static final int EVT_COMMUNICATION_TIMEOUT = LOCAL_EVT_BASE + 0x0002;
      private static final int EVT_START_COMMUNICATION2 = LOCAL_EVT_BASE + 0x0003;
      private static final int COMMUNICATION_TIMEOUT = 5000;
      private static final int RETRY_DELAY_TIME = 100;
      private static final int RETRY_COUNT_MAX = 0;

      private int mRetryCount;
      private BaseCommunicationItem mCommunicationItem;
      private CommunicateListener mCommunicateListener;
      private Iterator<? extends BaseCommunicationItem> mIterator;

      private int mMtuSize = MTU_SIZE_DEFAULT;

      private void nextCommunication() {
        if (mIterator.hasNext()) {
          mCommunicationItem = mIterator.next();
          if (!preset()) {
            onCommunicationError(ErrorCode.OSNativeError);
            transitionTo(mCommunicationReadyState);
          } else {
            sendMessage(EVT_START_COMMUNICATION2);
          }
        } else {
          onCommunicationComplete(GattStatusCode.GATT_SUCCESS);
          transitionTo(mCommunicationReadyState);
        }
      }

      private void onCommunicationComplete(int gattStatus) {
        mCommunicateListener.onComplete(gattStatus);
        mCommunicateListener = null;
        mIterator = null;
        mCommunicationItem = null;
      }

      private void onCommunicationError(ErrorCode errorCode) {
        mCommunicateListener.onError(new CommunicateException("errorCode:" + errorCode.name()));
        mCommunicateListener = null;
        mIterator = null;
        mCommunicationItem = null;
      }

      private boolean preset() {
        switch (mCommunicationItem.getType()) {
          case BaseCommunicationItem.TYPE_SET_NOTIFICATION: {
            SetNotificationEnabledCommunication item =
                (SetNotificationEnabledCommunication) mCommunicationItem;
            BluetoothGattDescriptor descriptor = item.getCharacteristic()
                .getDescriptor(
                    GattUUID.Descriptor.ClientCharacteristicConfigurationDescriptor.getUuid());
            if (null == descriptor) {
              Timber.e("[preset]null == descriptor");
              return false;
            }

            byte[] value;
            int properties = item.getCharacteristic().getProperties();
            if (CharacteristicProperty.contains(properties, CharacteristicProperty.Indicate)) {
              if (item.isEnable()) {
                Timber.d("[preset]Enable indication.");
                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
              } else {
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
              }
            } else if (CharacteristicProperty.contains(properties, CharacteristicProperty.Notify)) {
              if (item.isEnable()) {
                Timber.d("[preset]Enable notification.");
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
              } else {
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
              }
            } else {
              Timber.e("[preset]Notification unsupported.");
              return false;
            }

            boolean result = descriptor.setValue(value);
            if (!result) {
              Timber.e("[preset]Descriptor set value failed.");
              return false;
            }
            return true;
          }
          case BaseCommunicationItem.TYPE_WRITE_CHARACTERISTIC: {
            WriteCharacteristicCommunicationItem item =
                (WriteCharacteristicCommunicationItem) mCommunicationItem;
            if (item.isNoResponse()) {
              item.getCharacteristic()
                  .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            } else {
              item.getCharacteristic().setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
            return item.getCharacteristic().setValue(item.getData());
          }
          case BaseCommunicationItem.TYPE_READ_CHARACTERISTIC: {
            return true;
          }
        }
        return false;
      }

      private ErrorCode startCommunication2() {
        switch (mCommunicationItem.getType()) {
          case BaseCommunicationItem.TYPE_SET_NOTIFICATION: {
            SetNotificationEnabledCommunication item =
                (SetNotificationEnabledCommunication) mCommunicationItem;
            final BluetoothGattCharacteristic characteristic = item.getCharacteristic();
            final boolean enable = item.isEnable();
            boolean result;
            result = mBluetoothDeviceWrapper.setCharacteristicNotification(characteristic, enable);
            if (!result) {
              return ErrorCode.OSNativeError;
            }
            result = mBluetoothDeviceWrapper.writeDescriptor(characteristic.getDescriptor(
                GattUUID.Descriptor.ClientCharacteristicConfigurationDescriptor.getUuid()));
            if (!result) {
              return ErrorCode.OSNativeError;
            }
            return null;
          }
          case BaseCommunicationItem.TYPE_WRITE_CHARACTERISTIC: {
            boolean result = mBluetoothDeviceWrapper.writeCharacteristic(mCommunicationItem.getCharacteristic());
            if (!result) {
              return ErrorCode.OSNativeError;
            }
            return null;
          }
          case BaseCommunicationItem.TYPE_READ_CHARACTERISTIC: {
            final BluetoothGattCharacteristic characteristic =
                mCommunicationItem.getCharacteristic();
            boolean result = mBluetoothDeviceWrapper.readCharacteristic(characteristic);
            if (!result) {
              return ErrorCode.OSNativeError;
            }
            return null;
          }
        }
        return null;
      }

      @Override public void enter(Object[] transferObjects) {
        mStateInfo.setDetailedState(StateInfo.DetailedState.Communicating, null, true);
        if (transferObjects[0] instanceof WriteCharacteristicCommunicationItem) {
          mIterator =
              ((WriteCharacteristicCommunicationItem) transferObjects[0]).split(mMtuSize - 3);
        } else {
          mIterator = IteratorUtils.just((BaseCommunicationItem) transferObjects[0]);
        }
        mCommunicateListener = (CommunicateListener) transferObjects[1];

        mRetryCount = 0;
        nextCommunication();
      }

      private void communicationFinished(Object[] responseObjects) {
        final CommunicationResType resType = (CommunicationResType) responseObjects[0];
        switch (resType) {
          case OnCharacteristicWrite: {
            final BluetoothGattCharacteristic reqCharacteristic =
                mCommunicationItem.getCharacteristic();
            final BluetoothGattCharacteristic resCharacteristic =
                (BluetoothGattCharacteristic) responseObjects[1];
            final int gattStatus = (int) responseObjects[2];
            if (!reqCharacteristic.getUuid().equals(resCharacteristic.getUuid())) {
              retryOrErrorFinish2(ErrorCode.InvalidResponseData, 0);
              break;
            }
            if (BaseCommunicationItem.TYPE_WRITE_CHARACTERISTIC != mCommunicationItem.getType()) {
              retryOrErrorFinish2(ErrorCode.Unknown, 0);
              break;
            }
            if (GattStatusCode.GATT_SUCCESS == gattStatus) {
              nextCommunication();
            } else {
              onCommunicationComplete(gattStatus);
              transitionTo(mCommunicationReadyState);
            }
            break;
          }
          case OnCharacteristicRead: {
            final BluetoothGattCharacteristic reqCharacteristic =
                mCommunicationItem.getCharacteristic();
            final BluetoothGattCharacteristic resCharacteristic =
                (BluetoothGattCharacteristic) responseObjects[1];
            final int gattStatus = (int) responseObjects[2];
            if (!reqCharacteristic.getUuid().equals(resCharacteristic.getUuid())) {
              retryOrErrorFinish2(ErrorCode.InvalidResponseData, 0);
              break;
            }
            if (BaseCommunicationItem.TYPE_READ_CHARACTERISTIC != mCommunicationItem.getType()) {
              retryOrErrorFinish2(ErrorCode.Unknown, 0);
              break;
            }
            if (gattStatus == GattStatusCode.GATT_SUCCESS) {
              nextCommunication();
            } else {
              onCommunicationComplete(gattStatus);
              transitionTo(mCommunicationReadyState);
            }
            break;
          }
          case OnDescriptorWrite: {
            SetNotificationEnabledCommunication item =
                (SetNotificationEnabledCommunication) mCommunicationItem;
            final BluetoothGattCharacteristic reqCharacteristic = item.getCharacteristic();
            final BluetoothGattDescriptor resDescriptor =
                (BluetoothGattDescriptor) responseObjects[1];
            final int gattStatus = (int) responseObjects[2];
            if (!reqCharacteristic.getUuid().equals(resDescriptor.getCharacteristic().getUuid())) {
              retryOrErrorFinish2(ErrorCode.InvalidResponseData, 0);
              break;
            }
            if (gattStatus == GattStatusCode.GATT_SUCCESS) {
              nextCommunication();
            } else {
              onCommunicationComplete(gattStatus);
              transitionTo(mCommunicationReadyState);
            }
            break;
          }
          default:
            Timber.e("[communicationFinished]Fatal error.");
            break;
        }
      }

      private void retryOrErrorFinish2(ErrorCode errorCode, int retryInterval) {
        if (RETRY_COUNT_MAX > mRetryCount++) {
          Timber.w( "[retryOrErrorFinish2]%d retry.", mRetryCount);
          sendMessageDelayed(EVT_START_COMMUNICATION2, retryInterval);
        } else {
          Timber.e("[retryOrErrorFinish2]retry ... NG.");
          onCommunicationError(errorCode);
          transitionTo(mCommunicationReadyState);
        }
      }



      @Override public boolean processMessage(@NonNull Message msg) {
        switch (msg.what) {
          case EVT_START_COMMUNICATION2: {
            ErrorCode errorCode = startCommunication2();
            if (errorCode == null) {
              sendMessageDelayed(EVT_COMMUNICATION_TIMEOUT, COMMUNICATION_TIMEOUT);
            } else {
              retryOrErrorFinish2(errorCode, RETRY_DELAY_TIME);
            }
            break;
          }
          case EVT_COMMUNICATION_RES: {
            communicationFinished((Object[]) msg.obj);
            break;
          }
          case EVT_COMMUNICATION_REQ: {
            CommunicateListener communicateListener = (CommunicateListener) ((Object[]) msg.obj)[1];
            communicateListener.onError(new CommunicateException("errorCode:" + ErrorCode.Busy.name()));
            break;
          }
          case EVT_COMMUNICATION_TIMEOUT: {
            retryOrErrorFinish2(ErrorCode.CommunicationTimeout, 0);
            break;
          }
          default:
            return StateMachine.NOT_HANDLED;
        }
        return StateMachine.HANDLED;
      }

      @Override public void exit() {
        removeMessages(EVT_START_COMMUNICATION);
        removeMessages(EVT_COMMUNICATION_TIMEOUT);
        if (mCommunicateListener != null) {
          mCommunicateListener.onError(
              new CommunicateException("errorCode:" + ErrorCode.InvalidResponseData.name()));
        }
        mCommunicateListener = null;
      }
    }
  }
}
