package pon.main.modules.settings;

public class Header extends Setting<Void> {
    public Runnable onClick = null;
    public Header(String text) {
        super(text, null);
    }
    public Header(String text, Runnable onClick) {
        super(text, null);
        this.onClick = onClick;
    }
}

