package com.mediatek.blenativewrapper;

import java.util.UUID;

public interface BLEDataReceiver {
  void reset();

  void receive(UUID uuid, byte[] data, int len);

  void destroy();
}