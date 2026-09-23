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
 * Default immutable implementation of {@link IndexHeader}.
 *
 * <p>Holds the expected name of a column header used during bulk import processing together with
 * the {@link HeaderValidator}s that must be applied to the values found under that header. Both
 * properties are supplied at construction time and simply exposed through their accessors.
 */
public class IndexHeaderImpl implements IndexHeader {

  /** Expected name of the header (column) represented by this index. */
  private String headerName;

  /** Validators to be applied to the values found under this header (column). */
  private List<HeaderValidator> headerValidators;

  /**
   * Creates a new header index.
   *
   * @param headerName the expected name of the header (column)
   * @param headerValidators the validators to apply to the values of this header (column)
   */
  public IndexHeaderImpl(
    final String headerName,
    final List<HeaderValidator> headerValidators
  ) {
    this.headerName = headerName;
    this.headerValidators = headerValidators;
  }

  /**
   * {@inheritDoc}
   *
   * @return the header name provided at construction time
   */
  public String getHeaderName() {
    return headerName;
  }

  /**
   * {@inheritDoc}
   *
   * @return the list of validators provided at construction time
   */
  public List<HeaderValidator> getHeaderValidators() {
    return headerValidators;
  }
}
