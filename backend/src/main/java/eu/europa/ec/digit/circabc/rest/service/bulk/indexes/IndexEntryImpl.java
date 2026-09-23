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

/**
 * Default implementation of {@link IndexEntry} used during bulk import/export operations.
 *
 * <p>Holds a fixed header (column) name together with a mutable value. Once constructed, the header
 * name is immutable while the value may be updated via {@link #setValue(String)}.
 */
public class IndexEntryImpl implements IndexEntry {

  /** The immutable header (column) name this entry is associated with. */
  private String headerName;

  /** The mutable value held by this entry; may be {@code null} until set. */
  private String value;

  /**
   * Creates a new index entry pairing the given header name with an initial value.
   *
   * @param headerName the header (column) name identifying this entry
   * @param value the initial value to associate with the entry; may be {@code null}
   */
  public IndexEntryImpl(final String headerName, final String value) {
    this.headerName = headerName;
    this.value = value;
  }

  /**
   * Returns the name of the header (column) this entry is associated with.
   *
   * @return the header name identifying this entry
   */
  public String getHeaderName() {
    return headerName;
  }

  /**
   * Returns the current value held by this entry.
   *
   * @return the entry value, or {@code null} if no value has been set
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value held by this entry.
   *
   * @param value the value to assign to this entry; may be {@code null}
   */
  public void setValue(final String value) {
    this.value = value;
  }
}
