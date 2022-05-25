/**
 *
 */
package eu.cec.digit.circabc.repo.event;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * Implements a ResultSetRow to store the events accessed directly through the store, without the
 * search subsystem. Implements only the strictly necessary methods.
 *
 * @author schwerr
 */
public class EventResultSetRow implements ResultSetRow {

    private NodeRef nodeRef = null;

    public EventResultSetRow(NodeRef nodeRef) {
        super();
        this.nodeRef = nodeRef;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getValues()
     */
    @Override
    public Map<String, Serializable> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getValue(java.lang.String)
     */
    @Override
    public Serializable getValue(String columnName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getValue(org.alfresco.service.namespace.QName)
     */
    @Override
    public Serializable getValue(QName qname) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRef()
     */
    @Override
    public NodeRef getNodeRef() {
        return nodeRef;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRefs()
     */
    @Override
    public Map<String, NodeRef> getNodeRefs() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRef(java.lang.String)
     */
    @Override
    public NodeRef getNodeRef(String selectorName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScore()
     */
    @Override
    public float getScore() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScores()
     */
    @Override
    public Map<String, Float> getScores() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScore(java.lang.String)
     */
    @Override
    public float getScore(String selectorName) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getResultSet()
     */
    @Override
    public ResultSet getResultSet() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getQName()
     */
    @Override
    public QName getQName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getIndex()
     */
    @Override
    public int getIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getChildAssocRef()
     */
    @Override
    public ChildAssociationRef getChildAssocRef() {
        // TODO Auto-generated method stub
        return null;
    }
}
