package pon.purr.modules.settings;

public class Group {
    private boolean open;
    private final String name;

    public Group(String name, boolean open) {
        this.open = open;
        this.name = name;
    }
    public Group(String name) {
        this.open = false;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}