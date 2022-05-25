package eu.cec.digit.circabc.repo.app.model;

public class UserIGMembership {

    String catNodeRef;
    String igNodeRef;
    String profileName;
    String profileNodeRefId;
    String alfrescoGroup;
    Long profileId;

    public String getCatNodeRef() {
        return catNodeRef;
    }

    public void setCatNodeRef(String catNodeRef) {
        this.catNodeRef = catNodeRef;
    }

    public String getIgNodeRef() {
        return igNodeRef;
    }

    public void setIgNodeRef(String igNodeRef) {
        this.igNodeRef = igNodeRef;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getAlfrescoGroup() {
        return alfrescoGroup;
    }

    public void setAlfrescoGroup(String alfrescoGroup) {
        this.alfrescoGroup = alfrescoGroup;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getProfileNodeRefId() {
        return profileNodeRefId;
    }

    public void setProfileNodeRefId(String profileNodeRefId) {
        this.profileNodeRefId = profileNodeRefId;
    }
}
