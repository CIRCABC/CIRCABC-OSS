package eu.cec.digit.circabc.repo.app.model;

public class Header {

    private long id;
    private String name;
    private String description;
    private String nodeRef;

    public Header() {
    }

    public Header(long id, String name, String description, String nodeRef) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.nodeRef = nodeRef;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public String toString() {
        return "Header [id="
                + id
                + ", name="
                + name
                + ", description="
                + description
                + ", nodeRef="
                + nodeRef
                + "]";
    }
}
