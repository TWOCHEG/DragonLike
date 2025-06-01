// пример модуля для создания
package com.skylong.modules.example;

import com.skylong.modules.*;
import com.skylong.modules.settings.*;

import java.util.*;

public class Example extends Parent {
    public TextSetting header = new TextSetting("header");
    public Setting<Float> attackRange = new Setting<>("int", "int", 3.0f, 1.0f, 6.0f);
    public Setting<Boolean> autoAttack = new Setting<>("bool", "bool", true);
    public ListSetting<String> targetMode = new ListSetting<>(
        "list",
        "list",
        "1",
        Arrays.asList("1", "2", "3")
    );

    public Example() {
        super("example", "example", "category");
    }
}
