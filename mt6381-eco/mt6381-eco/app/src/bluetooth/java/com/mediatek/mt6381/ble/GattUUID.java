package com.mediatek.mt6381.ble;

import com.mediatek.blenativewrapper.UUIDNameMapping;
import java.util.UUID;
import lombok.Getter;

public class GattUUID {
  public enum Service {
    Data(UUID.fromString("88381c26-e6bc-47c0-9c39-a34a29d7e48a")), System(
        UUID.fromString("db176eed-14ae-4ce8-8ee2-4cab6c34573c"));
    @Getter private final UUID uuid;

    Service(UUID uuid) {
      this.uuid = uuid;
    }
  }

  //TODO BY HERMAN
  public enum Characteristic {

    Command(UUID.fromString("ede42f5b-a890-4d64-9134-726d59fc5ac8")),
    RawData(UUID.fromString("9f3b9d23-e753-4a46-a091-39ee6bee55d0")),
    Response(UUID.fromString("1b38cce9-3171-464b-96f1-f1a99ab89671")),
    SysInfo(UUID.fromString("b0498e33-5445-4601-8891-9e41d44942b3"));

    static {
      for (Characteristic type : values()) {
        UUIDNameMapping.getDefault().register(type.getUuid(), type.name());
      }
    }

    @Getter private final UUID uuid;

    Characteristic(UUID uuid) {
      this.uuid = uuid;
    }

  }
}
