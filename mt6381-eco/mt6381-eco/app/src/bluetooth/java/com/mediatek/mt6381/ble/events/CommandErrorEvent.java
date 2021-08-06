package com.mediatek.mt6381.ble.events;

import com.mediatek.mt6381.ble.command.BaseCommand;
import lombok.Getter;

@Getter public class CommandErrorEvent extends BaseCommandEvent {
  private final Throwable throwable;

  public CommandErrorEvent(BaseCommand baseCommand, Throwable throwable) {
    super(baseCommand);
    this.throwable = throwable;
  }
}
