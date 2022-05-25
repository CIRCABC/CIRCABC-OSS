package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.Date;

public class WhatsNewResult implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7383426000698489313L;

    private Boolean isContainer;
    private String bestTitle;
    private String url;
    private String fileType16;
    private String lang;
    private NodeRef nodeRef;
    private String smallIcon;
    private String id;
    private String displayLigthPath;
    private Long size;
    private Date modified;
    private String parentId;

    public WhatsNewResult(NavigableNode node) {
        if (node != null) {
            isContainer = (Boolean) node.get("isContainer");
            bestTitle = (String) node.get("bestTitle");
            url = (String) node.get("url");
            fileType16 = (String) node.get("fileType16");
            lang = (String) node.get("lang");
            nodeRef = (NodeRef) node.get("nodeRef");
            smallIcon = (String) node.get("smallIcon");
            id = (String) node.get("id");
            displayLigthPath = (String) node.get("displayLigthPath");
            size = (Long) node.get("size");
            modified = (Date) node.get("modified");
            parentId = (String) node.get("parentId");
        }

    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Boolean getIsContainer() {
        return isContainer;
    }

    public void setIsContainer(Boolean isContainer) {
        this.isContainer = isContainer;
    }

    public String getBestTitle() {
        return bestTitle;
    }

    public void setBestTitle(String bestTitle) {
        this.bestTitle = bestTitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileType16() {
        return fileType16;
    }

    public void setFileType16(String fileType16) {
        this.fileType16 = fileType16;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayLigthPath() {
        return displayLigthPath;
    }

    public void setDisplayLigthPath(String displayLigthPath) {
        this.displayLigthPath = displayLigthPath;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

}
