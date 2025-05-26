// пример модуля для создания
package com.skylong.modules.example;

import com.skylong.modules.*;
import com.skylong.modules.settings.*;

import java.util.*;

public class Example extends Parent {
    public TextSetting header = new TextSetting("эээ короче текст да");
    public Setting<Float> attackRange = new Setting<>("move speed", "mouse_move_speed", 3.5f, 1.0f, 6.0f);
    public Setting<Boolean> autoAttack = new Setting<>("move", "mouse_move", true);
    public ListSetting<String> targetMode = new ListSetting<>(
        "image",
        "image",
        "none",
        Arrays.asList("none", "furry", "cat")
    );

    public Example() {
        super("example", "example", "category");
    }
}
