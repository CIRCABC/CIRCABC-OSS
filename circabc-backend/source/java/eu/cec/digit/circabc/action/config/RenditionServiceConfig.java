package eu.cec.digit.circabc.action.config;

/**
 * Class for the RenditionService config MBean Control the rendition service parameters through JMX
 *
 * @author schwerr
 */
public class RenditionServiceConfig {

    private boolean renderJobEnabled = true;
    private int renderSleepSeconds = 60;
    private boolean previewJobEnabled = false;

    /**
     * Gets the value of the renderJobEnabled
     *
     * @return the renderJobEnabled
     */
    public boolean isRenderJobEnabled() {
        return renderJobEnabled;
    }

    /**
     * Sets the value of the renderJobEnabled
     *
     * @param renderJobEnabled the renderJobEnabled to set.
     */
    public void setRenderJobEnabled(boolean renderJobEnabled) {
        this.renderJobEnabled = renderJobEnabled;
    }

    /**
     * Gets the value of the previewJobEnabled
     *
     * @return the previewJobEnabled
     */
    public boolean isPreviewJobEnabled() {
        return previewJobEnabled;
    }

    /**
     * Sets the value of the previewJobEnabled
     *
     * @param previewJobEnabled the previewJobEnabled to set.
     */
    public void setPreviewJobEnabled(boolean previewJobEnabled) {
        this.previewJobEnabled = previewJobEnabled;
    }

    /**
     * Gets the value of the renderSleepSeconds
     *
     * @return the renderSleepSeconds
     */
    public int getRenderSleepSeconds() {
        return renderSleepSeconds;
    }

    /**
     * Sets the value of the renderSleepSeconds
     *
     * @param renderSleepSeconds the renderSleepSeconds to set.
     */
    public void setRenderSleepSeconds(int renderSleepSeconds) {
        this.renderSleepSeconds = renderSleepSeconds;
    }
}
