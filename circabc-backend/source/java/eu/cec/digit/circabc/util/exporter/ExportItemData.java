package eu.cec.digit.circabc.util.exporter;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;

/**
 * Encapsulates each property of one item to export.
 *
 * @author schwerr
 */
public class ExportItemData implements Serializable {

    private static final long serialVersionUID = -8452037015022663735L;

    private String name = null;
    private NodeRef nodeRef = null;

    public ExportItemData(String name, NodeRef nodeRef) {
        this.name = name;
        this.nodeRef = nodeRef;
    }

    /**
     * Gets the value of the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the nodeRef
     *
     * @return the nodeRef
     */
    public NodeRef getNodeRef() {
        return nodeRef;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass() + "(Name: " + name + ", NodeRef: " + nodeRef + ")";
    }
}
