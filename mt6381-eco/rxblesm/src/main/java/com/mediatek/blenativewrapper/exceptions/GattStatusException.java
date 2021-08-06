package com.mediatek.blenativewrapper.exceptions;

public class GattStatusException extends CommunicateException {
  public final int gattStatus;
  public GattStatusException(String message, int gattStatus) {
    super(message);
    this.gattStatus = gattStatus;
  }
}
