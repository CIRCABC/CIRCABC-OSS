package io.swagger.model;

public class BulkImportUserDataModel {

    private String username;
    private String igName;
    private String igRef;
    private String fromFile;
    private String email;
    private String status;
    private String profileId;

    public BulkImportUserDataModel(
            String username,
            String igName,
            String igRef,
            String fromFile,
            String email,
            String status,
            String profileId) {
        super();
        this.username = username;
        this.igName = igName;
        this.igRef = igRef;
        this.fromFile = fromFile;
        this.email = email;
        this.status = status;
        this.profileId = profileId;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the igName
     */
    public String getIgName() {
        return igName;
    }

    /**
     * @param igName the igName to set
     */
    public void setIgName(String igName) {
        this.igName = igName;
    }

    /**
     * @return the igRef
     */
    public String getIgRef() {
        return igRef;
    }

    /**
     * @param igRef the igRef to set
     */
    public void setIgRef(String igRef) {
        this.igRef = igRef;
    }

    /**
     * @return the fromFile
     */
    public String getFromFile() {
        return fromFile;
    }

    /**
     * @param fromFile the fromFile to set
     */
    public void setFromFile(String fromFile) {
        this.fromFile = fromFile;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the profileId
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * @param profileId the profileId to set
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
}
