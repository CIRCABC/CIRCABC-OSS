package eu.cec.digit.circabc.repo.app.model;

public class ProfileUser {

    private long userID;
    private String alfrescoGroup;

    public ProfileUser() {
    }

    public ProfileUser(long userID, String alfrescoGroup) {
        this.userID = userID;
        this.alfrescoGroup = alfrescoGroup;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getAlfrescoGroup() {
        return alfrescoGroup;
    }

    public void setAlfrescoGroup(String alfrescoGroup) {
        this.alfrescoGroup = alfrescoGroup;
    }

    @Override
    public String toString() {
        return "ProfileUser [userID=" + userID + ", alfrescoGroup=" + alfrescoGroup + "]";
    }
}
