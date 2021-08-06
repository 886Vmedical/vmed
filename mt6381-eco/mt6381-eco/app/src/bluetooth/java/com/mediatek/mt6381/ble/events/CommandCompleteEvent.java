package com.mediatek.mt6381.ble.events;

import com.mediatek.mt6381.ble.command.BaseCommand;

public class CommandCompleteEvent extends BaseCommandEvent {
  public CommandCompleteEvent(BaseCommand baseCommand) {
    super(baseCommand);
  }
}
