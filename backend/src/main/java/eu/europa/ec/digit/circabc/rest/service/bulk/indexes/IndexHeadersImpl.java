/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.bulk.indexes;

import java.util.LinkedList;
import java.util.List;

/**
 * Default {@link IndexHeaders} implementation backing an ordered collection of {@link IndexHeader}
 * entries used during bulk import processing.
 *
 * <p>Headers are stored in a {@link LinkedList}, preserving insertion order, which typically mirrors
 * the column order of the imported source (e.g. a spreadsheet). The class supports both building the
 * collection programmatically via {@link #addHeader(IndexHeader)} and constructing it directly from
 * an array of raw header names.
 */
public class IndexHeadersImpl implements IndexHeaders {

  /** Ordered list of headers, kept in insertion order to reflect the source column order. */
  private final List<IndexHeader> headerList = new LinkedList<>();

  /** Creates an empty header collection to be populated later via {@link #addHeader(IndexHeader)}. */
  public IndexHeadersImpl() {}

  /**
   * Creates a header collection from an array of raw header names.
   *
   * <p>Each name is wrapped in an {@link IndexHeaderImpl} (with no validators) and appended to the
   * collection.
   *
   * @param headers the raw header (column) names to register; must not be {@code null}
   */
  public IndexHeadersImpl(final String[] headers) {
    boolean found = false;
    for (final String header : headers) {
      for (final IndexHeader indexHeader : headerList) {
        if (indexHeader.getHeaderName().equals(header)) {
          found = true;
          break;
        }
      }
      if (!found) {
        addHeader(new IndexHeaderImpl(header, null));
      }
    }
  }

  /**
   * Appends a header to the end of the collection, preserving insertion order.
   *
   * @param header the header to add
   */
  public void addHeader(final IndexHeader header) {
    ((LinkedList<IndexHeader>) this.headerList).addLast(header);
  }

  /**
   * Returns the headers in insertion order.
   *
   * @return the live list of registered headers; may be empty but never {@code null}
   */
  public List<IndexHeader> getHeaders() {
    return this.headerList;
  }

  /**
   * Looks up a header by its name.
   *
   * @param headerName the header name to search for
   * @return the matching {@link IndexHeader}, or {@code null} if no header with the given name is
   *     registered
   */
  public IndexHeader getHeader(final String headerName) {
    for (final IndexHeader indexHeader : headerList) {
      if (indexHeader.getHeaderName().equals(headerName)) {
        return indexHeader;
      }
    }
    return null;
  }
}
