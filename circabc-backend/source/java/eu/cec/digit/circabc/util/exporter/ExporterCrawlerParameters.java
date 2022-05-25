package eu.cec.digit.circabc.util.exporter;

/**
 * Additional exporter parameters that affect the behaviour of the export for CIRCABC Categories and
 * IGs
 *
 * @author schwerr
 */
public class ExporterCrawlerParameters extends
        org.alfresco.service.cmr.view.ExporterCrawlerParameters {

    private String[] crawlOnlyGivenUsers = null;
    private String[] crawlOnlyGivenGroups = null;
    private boolean exportOnlyRoot = false;

    /**
     * Gets the value of the crawlOnlyGivenUsers
     *
     * @return the crawlOnlyGivenUsers
     */
    public String[] getCrawlOnlyGivenUsers() {
        return crawlOnlyGivenUsers;
    }

    /**
     * Sets the value of the crawlOnlyGivenUsers
     *
     * @param crawlOnlyGivenUsers the crawlOnlyGivenUsers to set.
     */
    public void setCrawlOnlyGivenUsers(String[] crawlOnlyGivenUsers) {
        this.crawlOnlyGivenUsers = crawlOnlyGivenUsers;
    }

    /**
     * Gets the value of the crawlOnlyGivenGroups
     *
     * @return the crawlOnlyGivenGroups
     */
    public String[] getCrawlOnlyGivenGroups() {
        return crawlOnlyGivenGroups;
    }

    /**
     * Sets the value of the crawlOnlyGivenGroups
     *
     * @param crawlOnlyGivenGroups the crawlOnlyGivenGroups to set.
     */
    public void setCrawlOnlyGivenGroups(String[] crawlOnlyGivenGroups) {
        this.crawlOnlyGivenGroups = crawlOnlyGivenGroups;
    }

    /**
     * Gets the value of the exportOnlyRoot
     *
     * @return the exportOnlyRoot
     */
    public boolean isExportOnlyRoot() {
        return exportOnlyRoot;
    }

    /**
     * Sets the value of the exportOnlyRoot
     *
     * @param exportOnlyRoot the exportOnlyRoot to set.
     */
    public void setExportOnlyRoot(boolean exportOnlyRoot) {
        this.exportOnlyRoot = exportOnlyRoot;
    }
}
