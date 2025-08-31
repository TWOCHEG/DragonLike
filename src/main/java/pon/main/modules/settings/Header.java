package pon.main.modules.settings;

public class Header extends Setting<Integer> {
    public Runnable onClick = null;
    public Header(String text) {
        super(text, 0);
    }
    public Header(String text, Runnable onClick) {
        super(text, 0);
        this.onClick = onClick;
    }
}

