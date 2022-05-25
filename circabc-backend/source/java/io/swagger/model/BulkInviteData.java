package io.swagger.model;

import java.util.List;

public class BulkInviteData {

    private List<BulkImportUserDataModel> bulkImportUserData;
    private List<NameValue> igProfiles;

    public BulkInviteData(
            List<BulkImportUserDataModel> bulkImportUserData, List<NameValue> igProfiles) {
        super();
        this.bulkImportUserData = bulkImportUserData;
        this.igProfiles = igProfiles;
    }

    /**
     * @return the bulkImportUserData
     */
    public List<BulkImportUserDataModel> getBulkImportUserData() {
        return bulkImportUserData;
    }

    /**
     * @param bulkImportUserData the bulkImportUserData to set
     */
    public void setBulkImportUserData(List<BulkImportUserDataModel> bulkImportUserData) {
        this.bulkImportUserData = bulkImportUserData;
    }

    /**
     * @return the igProfiles
     */
    public List<NameValue> getIgProfiles() {
        return igProfiles;
    }

    /**
     * @param igProfiles the igProfiles to set
     */
    public void setIgProfiles(List<NameValue> igProfiles) {
        this.igProfiles = igProfiles;
    }
}
