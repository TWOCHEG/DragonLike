package com.purr.events.impl;

import com.purr.events.Event;
import com.purr.modules.settings.*;

public class EventSetting extends Event {
    final Setting<?> setting;

    public EventSetting(Setting<?> setting){
        this.setting = setting;
    }

    public Setting<?> getSetting() {
        return setting;
    }
}
