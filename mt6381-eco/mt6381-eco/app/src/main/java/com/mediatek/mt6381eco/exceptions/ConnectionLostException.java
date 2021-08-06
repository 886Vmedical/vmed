package com.mediatek.mt6381eco.exceptions;

public class ConnectionLostException extends Exception {
  public ConnectionLostException() {
    super("Connection is lost");
  }
}
