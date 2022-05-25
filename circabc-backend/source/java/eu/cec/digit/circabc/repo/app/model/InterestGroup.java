package eu.cec.digit.circabc.repo.app.model;

public class InterestGroup {

    private long id;
    private String name;
    private String title;
    private String nodeRef;
    private boolean isPublic;
    private boolean isRegistered;
    private boolean isApplyForMembership;
    private long categoryID;
    private String logoRef;

    public InterestGroup() {
        super();
    }

    public InterestGroup(
            long categoryID,
            long id,
            String name,
            String title,
            String nodeRef,
            boolean isPublic,
            boolean isRegistered,
            boolean isApplyForMembership,
            String logoRef) {
        super();
        this.categoryID = categoryID;
        this.id = id;
        this.name = name;
        this.title = title;
        this.nodeRef = nodeRef;
        this.isPublic = isPublic;
        this.isRegistered = isRegistered;
        this.isApplyForMembership = isApplyForMembership;
        this.logoRef = logoRef;
    }

    public long getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(long categoryID) {
        this.categoryID = categoryID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public boolean isApplyForMembership() {
        return isApplyForMembership;
    }

    public void setApplyForMembership(boolean isApplyForMemdership) {
        this.isApplyForMembership = isApplyForMemdership;
    }

    /**
     * @return the logoRef
     */
    public String getLogoRef() {
        return logoRef;
    }

    /**
     * @param logoRef the logoRef to set
     */
    public void setLogoRef(String logoRef) {
        this.logoRef = logoRef;
    }

    @Override
    public String toString() {
        return "InterestGroup [categoryID="
                + categoryID
                + ", id="
                + id
                + ", name="
                + name
                + ", title="
                + title
                + ", nodeRef="
                + nodeRef
                + ", isPublic="
                + isPublic
                + ", isRegistered="
                + isRegistered
                + ", isApplyForMembership="
                + isApplyForMembership
                + ", logoRef="
                + logoRef
                + "]";
    }
}
