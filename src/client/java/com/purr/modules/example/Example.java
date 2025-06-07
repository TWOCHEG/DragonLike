// пример модуля для создания
package com.purr.modules.example;

import com.purr.modules.*;
import com.purr.modules.settings.*;

import java.util.*;

public class Example extends Parent {
    public Header header = new Header("header");
    public Setting<Float> intSetting = new Setting<>("int", 3.0f, 1.0f, 6.0f);
    public Setting<Boolean> boolSetting = new Setting<>("bool", true);
    public ListSetting<String> list = new ListSetting<>(
        "list",
        Arrays.asList("1", "2", "3") // первый элемент используется как стандартный
    );

    public Example() {
        super("example", "example", "category");
    }
}
