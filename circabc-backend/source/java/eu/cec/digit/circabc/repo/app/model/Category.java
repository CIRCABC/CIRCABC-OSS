package eu.cec.digit.circabc.repo.app.model;

public class Category {

    private long id;
    private String name;
    private String title;
    private String nodeRef;
    private String logoRef;
    private long headerID;

    public Category(long id, String name, String title, String nodeRef, long headerID) {
        super();
        this.id = id;
        this.name = name;
        this.title = title;
        this.nodeRef = nodeRef;
        this.headerID = headerID;
        this.logoRef = "";
    }

    public Category(
            long id, String name, String title, String nodeRef, long headerID, String logoRef) {
        super();
        this.id = id;
        this.name = name;
        this.title = title;
        this.nodeRef = nodeRef;
        this.headerID = headerID;
        this.logoRef = logoRef;
    }

    public Category() {
        // TODO Auto-generated constructor stub
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

    public long getHeaderID() {
        return headerID;
    }

    public void setHeaderID(long headerID) {
        this.headerID = headerID;
    }

    public String getLogoRef() {
        return logoRef;
    }

    public void setLogoRef(String logoRef) {
        this.logoRef = logoRef;
    }

    @Override
    public String toString() {
        return "Category [id="
                + id
                + ", name="
                + name
                + ", title="
                + title
                + ", nodeRef="
                + nodeRef
                + ", headerID="
                + headerID
                + "]";
    }
}
