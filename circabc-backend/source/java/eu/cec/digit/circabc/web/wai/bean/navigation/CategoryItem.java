package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;

public class CategoryItem implements Serializable, Comparable<CategoryItem> {


    /**
     *
     */
    private static final long serialVersionUID = 7379466817750758108L;
    private String id;
    private String title;
    private Boolean isLink;
    private String name;
    private NodeRef nodeRef;
    public CategoryItem(String id, String title, Boolean isLink, String name,
                        NodeRef nodeRef) {
        super();
        this.id = id;
        this.title = title;
        this.isLink = isLink;
        this.name = name;
        this.nodeRef = nodeRef;
    }
    public CategoryItem(NavigableNode node) {
        if (node != null) {
            isLink = node.hasAspect(DocumentModel.ASPECT_URLABLE);
            name = (String) node.get("name");
            nodeRef = (NodeRef) node.get("nodeRef");
            id = (String) node.get("id");
            title = (String) node.get("bestTitle");
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getIsLink() {
        return isLink;
    }

    public void setIsLink(Boolean isLink) {
        this.isLink = isLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(CategoryItem o) {
        return title.compareToIgnoreCase(o.getTitle());
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }
}
