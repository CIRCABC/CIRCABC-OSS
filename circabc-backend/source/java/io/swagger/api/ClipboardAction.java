package io.swagger.api;

public enum ClipboardAction {
    COPY(0),
    MOVE(1),
    LINK(2);
    private int value;

    ClipboardAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
