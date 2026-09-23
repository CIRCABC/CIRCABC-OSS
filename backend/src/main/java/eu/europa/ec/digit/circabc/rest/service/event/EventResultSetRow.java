/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.event;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;

/**
 * Implements a ResultSetRow to store the events accessed directly through the store, without the
 * search subsystem. Implements only the strictly necessary methods.
 *
 * @author schwerr
 */
public class EventResultSetRow implements ResultSetRow {

  /** The node reference of the event that this row wraps; the only value actually backed by data. */
  private NodeRef nodeRef = null;

  /**
   * Creates a row wrapping the given event node.
   *
   * @param nodeRef the {@link NodeRef} of the event to expose through this result set row
   */
  public EventResultSetRow(NodeRef nodeRef) {
    super();
    this.nodeRef = nodeRef;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation does not carry any column values.
   *
   * @return an empty, immutable map
   */
  @Override
  public Map<String, Serializable> getValues() {
    return Collections.emptyMap();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation does not carry any column values.
   *
   * @param columnName the name of the column whose value is requested
   * @return always {@code null}
   */
  @Override
  public Serializable getValue(String columnName) {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation does not carry any column values.
   *
   * @param qname the qualified name of the property whose value is requested
   * @return always {@code null}
   */
  @Override
  public Serializable getValue(QName qname) {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @return the {@link NodeRef} of the event supplied at construction time
   */
  @Override
  public NodeRef getNodeRef() {
    return nodeRef;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation exposes no named node references.
   *
   * @return an empty, immutable map
   */
  @Override
  public Map<String, NodeRef> getNodeRefs() {
    return Collections.emptyMap();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation exposes no named node references.
   *
   * @param selectorName the name of the selector whose node reference is requested
   * @return always {@code null}
   */
  @Override
  public NodeRef getNodeRef(String selectorName) {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Rows obtained outside the search subsystem carry no relevance score.
   *
   * @return always {@code 0}
   */
  @Override
  public float getScore() {
    return 0;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Rows obtained outside the search subsystem carry no relevance scores.
   *
   * @return an empty, immutable map
   */
  @Override
  public Map<String, Float> getScores() {
    return Collections.emptyMap();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Rows obtained outside the search subsystem carry no relevance score.
   *
   * @param selectorName the name of the selector whose score is requested
   * @return always {@code 0}
   */
  @Override
  public float getScore(String selectorName) {
    return 0;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This row is not associated with an owning result set.
   *
   * @return always {@code null}
   */
  @Override
  public ResultSet getResultSet() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @return always {@code null}
   */
  @Override
  public QName getQName() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @return always {@code 0}
   */
  @Override
  public int getIndex() {
    return 0;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation exposes no child association reference.
   *
   * @return always {@code null}
   */
  @Override
  public ChildAssociationRef getChildAssocRef() {
    return null;
  }
}
