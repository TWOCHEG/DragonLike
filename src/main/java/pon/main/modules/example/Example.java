// пример модуля для создания
package pon.main.modules.example;

import pon.main.Main;
import pon.main.modules.Parent;
import pon.main.modules.settings.*;

import java.util.*;

public class Example extends Parent {
    public Header header = new Header("header");
    public Setting<Float> intSetting = new Setting<>("int", 3.0f, 1.0f, 6.0f);
    public Setting<Boolean> boolSetting = new Setting<>("bool", true);
    public Setting<String> strSetting = new Setting<>("str", "your text");
    public Setting<String> list = new Setting<>(
        "list",
        Arrays.asList("1", "2", "3")
    );
    public Header header2 = new Header("настойки типо");
    public Setting<String> list2 = new Setting<>(
        "list",
        Arrays.asList("1", "2", "3") // первый элемент используется как стандартный
    );
    public Group group = new Group(
        "group/category",
        header2,
        list2
    );

    public Setting<Boolean> visible = new Setting<>("visible", false);
    public Header header3 = (Header) new Header("ты видишь меня").visibleIf(m -> visible.getValue());

    public Example() {
        super("example", Main.Categories.example);
    }
}
