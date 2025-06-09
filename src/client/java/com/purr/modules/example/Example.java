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
    public Group group = new Group("group/category");
    public Header header2 = (Header) new Header("настойки типо").addToGroup(group);

    public Setting<Boolean> visible = new Setting<>("visible", false);
    public Header header3 = (Header) new Header("ты видишь меня").visibleIf(visible, true);

    public Example() {
        super("example", "example", "example");
    }
}
