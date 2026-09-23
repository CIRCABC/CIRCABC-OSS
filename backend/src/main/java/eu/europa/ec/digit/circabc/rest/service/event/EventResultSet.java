/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.event;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.util.Pair;

/**
 * Implements a ResultSet to store the events accessed directly through the store, without the
 * search subsystem. Implements only the strictly necessary methods.
 *
 * @author schwerr
 */
public class EventResultSet implements ResultSet {

  /** The backing list of event rows that this result set wraps and exposes. */
  private List<EventResultSetRow> results = null;

  /**
   * Creates a result set backed by the given list of event rows.
   *
   * @param results the event rows to expose through this result set; retained by reference (not
   *     copied)
   */
  public EventResultSet(List<EventResultSetRow> results) {
    super();
    this.results = results;
  }

  /**
   * Returns the number of rows held by this result set.
   *
   * @return the size of the backing results list
   * @see org.alfresco.service.cmr.search.ResultSetSPI#length()
   */
  @Override
  public int length() {
    return results.size();
  }

  /**
   * Returns the total number of results found.
   *
   * <p>Since results are loaded directly from the store without paging, this is equal to {@link
   * #length()}.
   *
   * @return the size of the backing results list
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound()
   */
  @Override
  public long getNumberFound() {
    return results.size();
  }

  /**
   * Not supported by this store-backed result set.
   *
   * @param n the zero-based row index
   * @return always {@code null}
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRef(int)
   */
  @Override
  public NodeRef getNodeRef(int n) {
    return null;
  }

  /**
   * Not supported by this store-backed result set; no relevance score is computed.
   *
   * @param n the zero-based row index
   * @return always {@code 0}
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getScore(int)
   */
  @Override
  public float getScore(int n) {
    return 0;
  }

  /**
   * Releases any resources associated with this result set.
   *
   * <p>Intentionally a no-op: the results are held in memory and require no cleanup.
   *
   * @see org.alfresco.service.cmr.search.ResultSetSPI#close()
   */
  @Override
  @SuppressWarnings("java:S1186") // Empty method is intentional
  public void close() {
    // No resources to release - results are held in memory
  }

  /**
   * Returns the row at the given index.
   *
   * @param i the zero-based row index
   * @return the {@link ResultSetRow} at position {@code i}
   * @throws IndexOutOfBoundsException if the index is out of range
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getRow(int)
   */
  @Override
  public ResultSetRow getRow(int i) {
    return results.get(i);
  }

  /**
   * Not supported by this store-backed result set.
   *
   * @return always an empty list
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRefs()
   */
  @Override
  public List<NodeRef> getNodeRefs() {
    return Collections.emptyList();
  }

  /**
   * Not supported by this store-backed result set.
   *
   * @return always an empty list
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getChildAssocRefs()
   */
  @Override
  public List<ChildAssociationRef> getChildAssocRefs() {
    return Collections.emptyList();
  }

  /**
   * Not supported by this store-backed result set.
   *
   * @param n the zero-based row index
   * @return always {@code null}
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getChildAssocRef(int)
   */
  @Override
  public ChildAssociationRef getChildAssocRef(int n) {
    return null;
  }

  /**
   * Not supported by this store-backed result set; no search metadata is available.
   *
   * @return always {@code null}
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getResultSetMetaData()
   */
  @Override
  public ResultSetMetaData getResultSetMetaData() {
    return null;
  }

  /**
   * Returns the start offset of this result set.
   *
   * @return always {@code 0} as results are not paged
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getStart()
   */
  @Override
  public int getStart() {
    return 0;
  }

  /**
   * Indicates whether more results are available beyond this set.
   *
   * @return always {@code false} as all results are returned at once
   * @see org.alfresco.service.cmr.search.ResultSetSPI#hasMore()
   */
  @Override
  public boolean hasMore() {
    return false;
  }

  /**
   * Bulk fetching is not supported by this store-backed result set.
   *
   * @param bulkFetch the requested bulk-fetch flag (ignored)
   * @return always {@code false}
   * @see org.alfresco.service.cmr.search.ResultSetSPI#setBulkFetch(boolean)
   */
  @Override
  public boolean setBulkFetch(boolean bulkFetch) {
    return false;
  }

  /**
   * Bulk fetching is not supported by this store-backed result set.
   *
   * @return always {@code false}
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getBulkFetch()
   */
  @Override
  public boolean getBulkFetch() {
    return false;
  }

  /**
   * Bulk fetching is not supported by this store-backed result set.
   *
   * @param bulkFetchSize the requested bulk-fetch size (ignored)
   * @return always {@code 0}
   * @see org.alfresco.service.cmr.search.ResultSetSPI#setBulkFetchSize(int)
   */
  @Override
  public int setBulkFetchSize(int bulkFetchSize) {
    return 0;
  }

  /**
   * Bulk fetching is not supported by this store-backed result set.
   *
   * @return always {@code 0}
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getBulkFetchSize()
   */
  @Override
  public int getBulkFetchSize() {
    return 0;
  }

  /**
   * Field faceting is not supported by this store-backed result set.
   *
   * @param field the field name to facet on (ignored)
   * @return always an empty list
   * @see org.alfresco.service.cmr.search.ResultSetSPI#getFieldFacet(java.lang.String)
   */
  @Override
  public List<Pair<String, Integer>> getFieldFacet(String field) {
    return Collections.emptyList();
  }

  /**
   * Returns an iterator over the rows of this result set.
   *
   * @return an iterator over the backing {@link EventResultSetRow} entries, typed as {@link
   *     ResultSetRow}
   * @see java.lang.Iterable#iterator()
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Iterator<ResultSetRow> iterator() {
    return (Iterator) results.iterator();
  }

  /**
   * Facet queries are not supported by this store-backed result set.
   *
   * @return always an empty map
   */
  @Override
  public Map<String, Integer> getFacetQueries() {
    return Collections.emptyMap();
  }

  /**
   * Result highlighting is not supported by this store-backed result set.
   *
   * @return always an empty map
   */
  @Override
  public Map<NodeRef, List<Pair<String, List<String>>>> getHighlighting() {
    return Collections.emptyMap();
  }

  /**
   * Spell checking is not supported by this store-backed result set.
   *
   * @return always {@code null}
   */
  @Override
  public SpellCheckResult getSpellCheckResult() {
    return null;
  }
}
