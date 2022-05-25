package eu.cec.digit.circabc.repo.app.model;

public class Profile {

    private long interestGroupID;
    private long id;
    private String alfrescoGroup;
    private String name;
    private String title;
    private String directoryPermission;
    private String informationPermission;
    private String libraryPermission;
    private String newsgroupPermission;
    private String eventPermission;
    private boolean isExported;
    private boolean isImported;
    private boolean isVisible;
    private String nodeRef;
    private String igFromNodeRef;

    public Profile() {
        super();
    }

    public Profile(
            long interestGroupID,
            long id,
            String alfrescoGroup,
            String name,
            String title,
            String directoryPermission,
            String informationPermission,
            String libraryPermission,
            String newsgroupPermission,
            String eventPermission,
            boolean isExported,
            boolean isImported,
            boolean isVisible,
            String nodeRef,
            String igFromNodeRef) {
        super();
        this.interestGroupID = interestGroupID;
        this.id = id;
        this.alfrescoGroup = alfrescoGroup;
        this.name = name;
        this.title = title;
        this.directoryPermission = directoryPermission;
        this.informationPermission = informationPermission;
        this.libraryPermission = libraryPermission;
        this.newsgroupPermission = newsgroupPermission;
        this.eventPermission = eventPermission;
        this.isExported = isExported;
        this.isImported = isImported;
        this.isVisible = isVisible;
        this.nodeRef = nodeRef;
        this.igFromNodeRef = igFromNodeRef;
    }

    public static String replaceLast(String string, String from, String to) {
        if (string == null) {
            return null;
        }
        int lastIndex = string.lastIndexOf(from);
        if (lastIndex < 0) {
            return string;
        }
        String tail = string.substring(lastIndex).replaceFirst(from, to);
        return string.substring(0, lastIndex) + tail;
    }

    @Override
    public String toString() {
        return "Profile [interestGroupID="
                + interestGroupID
                + ", id="
                + id
                + ", alfrescoGroup="
                + alfrescoGroup
                + ", name="
                + name
                + ", title="
                + title
                + ", directoryPermission="
                + directoryPermission
                + ", informationPermission="
                + informationPermission
                + ", libraryPermission="
                + libraryPermission
                + ", newsgroupPermission="
                + newsgroupPermission
                + ", eventPermission="
                + eventPermission
                + ", isExported="
                + isExported
                + ", isImported="
                + isImported
                + ", isVisible="
                + isVisible
                + ", nodeRef="
                + nodeRef
                + ", igFromNodeRef="
                + igFromNodeRef
                + "]";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAlfrescoGroup() {
        return alfrescoGroup;
    }

    public void setAlfrescoGroup(String alfrescoGroup) {
        this.alfrescoGroup = alfrescoGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirectoryPermission() {
        return directoryPermission;
    }

    public void setDirectoryPermission(String directoryPermission) {
        this.directoryPermission = directoryPermission;
    }

    public String getInformationPermission() {
        return informationPermission;
    }

    public void setInformationPermission(String informationPermission) {
        this.informationPermission = informationPermission;
    }

    public String getLibraryPermission() {
        return libraryPermission;
    }

    public void setLibraryPermission(String libraryPermission) {
        this.libraryPermission = libraryPermission;
    }

    public String getNewsgroupPermission() {
        return newsgroupPermission;
    }

    public void setNewsgroupPermission(String newsgroupPermission) {
        this.newsgroupPermission = newsgroupPermission;
    }

    public String getEventPermission() {
        return eventPermission;
    }

    public void setEventPermission(String eventsPermission) {
        this.eventPermission = eventsPermission;
    }

    public boolean isExported() {
        return isExported;
    }

    public void setExported(boolean isExported) {
        this.isExported = isExported;
    }

    public boolean isImported() {
        return isImported;
    }

    public void setImported(boolean isImported) {
        this.isImported = isImported;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public long getInterestGroupID() {
        return interestGroupID;
    }

    public void setInterestGroupID(long interestGroupID) {
        this.interestGroupID = interestGroupID;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public String getIgFromNodeRef() {
        return igFromNodeRef;
    }

    public void setIgFromNodeRef(String igFromNodeRef) {
        this.igFromNodeRef = igFromNodeRef;
    }
}
