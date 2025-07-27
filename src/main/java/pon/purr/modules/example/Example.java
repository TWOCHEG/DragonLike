// пример модуля для создания
package pon.purr.modules.example;

import pon.purr.modules.Parent;
import pon.purr.modules.settings.*;

import java.util.*;

public class Example extends Parent {
    public Header header = new Header("header");
    public Setting<Float> intSetting = new Setting<>("int", 3.0f, 1.0f, 6.0f);
    public Setting<Boolean> boolSetting = new Setting<>("bool", true);
    public Setting<String> strSetting = new Setting<>("str", "your text");
    public ListSetting<String> list = new ListSetting<>(
        "list",
        Arrays.asList("1", "2", "3") // первый элемент используется как стандартный
    );
    public Group group = new Group("group/category");
    public Header header2 = (Header) new Header("настойки типо").addToGroup(group);
    public ListSetting<String> list2 = (ListSetting<String>) new ListSetting<>(
            "list",
            Arrays.asList("1", "2", "3") // первый элемент используется как стандартный
    ).addToGroup(group);

    public Setting<Boolean> visible = new Setting<>("visible", false);
    public Header header3 = (Header) new Header("ты видишь меня").visibleIf(m -> visible.getValue());

    public Example() {
        super("example", "example");
    }
}
