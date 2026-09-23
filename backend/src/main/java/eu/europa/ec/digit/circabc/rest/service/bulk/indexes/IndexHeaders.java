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

import java.util.List;

/**
 * Represents the collection of column headers used during bulk import processing.
 *
 * <p>An {@code IndexHeaders} instance acts as a registry of {@link IndexHeader} definitions,
 * allowing headers to be added and later looked up by name. It is used by the bulk import machinery
 * to describe the expected columns of an index (import) file and to resolve the validators that
 * apply to each column.
 */
public interface IndexHeaders {
  /**
   * Adds a header definition to this collection.
   *
   * @param header the header to register; must not be {@code null}
   */
  void addHeader(final IndexHeader header);

  /**
   * Returns all header definitions currently registered in this collection.
   *
   * @return the list of registered headers; may be empty but never {@code null}
   */
  List<IndexHeader> getHeaders();

  /**
   * Looks up a registered header by its name.
   *
   * @param headerName the name of the header to retrieve
   * @return the matching {@link IndexHeader}, or {@code null} if no header with the given name is
   *     registered
   */
  IndexHeader getHeader(final String headerName);
}
