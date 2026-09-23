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
 * Describes a single column header used during bulk import processing.
 *
 * <p>Implementations expose the expected name of a header (column) and the set of {@link
 * HeaderValidator}s that must be applied to the values found under that header. This allows the bulk
 * import machinery to identify columns and validate their content in a uniform way.
 */
public interface IndexHeader {
  /**
   * Returns the expected name of the header (column) represented by this index.
   *
   * @return the header name; never {@code null}
   */
  String getHeaderName();

  /**
   * Returns the validators that must be applied to the values of this header (column).
   *
   * @return the list of validators associated with this header; may be empty but never {@code null}
   */
  List<HeaderValidator> getHeaderValidators();
}
