/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.admin;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author beaurpi
 */
public class UrlBean {

    private String name;
    private String url;
    private NodeRef nodeRef;

    public UrlBean(String name, String url, NodeRef nodeRef) {
        this.setName(name);
        this.setUrl(url);
        this.setNodeRef(nodeRef);
    }

    public UrlBean() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the nodeRef
     */
    public NodeRef getNodeRef() {
        return nodeRef;
    }

    /**
     * @param nodeRef the nodeRef to set
     */
    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }


}
