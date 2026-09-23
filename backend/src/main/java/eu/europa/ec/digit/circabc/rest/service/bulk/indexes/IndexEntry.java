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
 * Represents a single index column entry used during bulk import/export operations.
 *
 * <p>An {@code IndexEntry} pairs a fixed column header name with a mutable value. Implementations
 * are typically used to map tabular index data (e.g. from a bulk metadata sheet) where each entry
 * corresponds to one named field and its associated value.
 */
public interface IndexEntry {
  /**
   * Returns the name of the header (column) this entry is associated with.
   *
   * @return the header name identifying this entry
   */
  String getHeaderName();

  /**
   * Returns the current value held by this entry.
   *
   * @return the entry value, or {@code null} if no value has been set
   */
  String getValue();

  /**
   * Sets the value held by this entry.
   *
   * @param value the value to assign to this entry
   */
  void setValue(final String value);
}
