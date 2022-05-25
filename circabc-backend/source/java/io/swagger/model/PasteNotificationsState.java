package io.swagger.model;

public class PasteNotificationsState {

    boolean pasteEnabled;
    boolean pasteAllEnabled;

    public PasteNotificationsState(boolean pasteEnabled, boolean pasteAllEnabled) {
        super();
        this.pasteEnabled = pasteEnabled;
        this.pasteAllEnabled = pasteAllEnabled;
    }

    /**
     * @return the pasteEnabled
     */
    public boolean isPasteEnabled() {
        return pasteEnabled;
    }

    /**
     * @return the pasteAllEnabled
     */
    public boolean isPasteAllEnabled() {
        return pasteAllEnabled;
    }
}
