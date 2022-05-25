package eu.cec.digit.circabc.repo.app.model;

public class ExportedProfileItem {

    private String prefixedAlfrescoGroup;
    private String profileName;
    private String name;
    private String profileRef;
    private String nodeRef;

    public String getPrefixedAlfrescoGroup() {
        return prefixedAlfrescoGroup;
    }

    public void setPrefixedAlfrescoGroup(String prefixedAlfrescoGroup) {
        this.prefixedAlfrescoGroup = prefixedAlfrescoGroup;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getProfileRef() {
        return profileRef;
    }

    public void setProfileRef(String profileRef) {
        this.profileRef = profileRef;
    }
}
