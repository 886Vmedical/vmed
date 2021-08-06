package com.mediatek.mt6381.ble.events;

import com.mediatek.mt6381.ble.command.BaseCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter public abstract class BaseCommandEvent {
  private final BaseCommand baseCommand;
}
