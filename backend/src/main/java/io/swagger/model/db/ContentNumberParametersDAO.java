/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package io.swagger.model.db;

/**
 * Data access parameter holder bundling the identifiers required to query content-number related
 * information from the database.
 *
 * <p>This is a plain data carrier (POJO): it groups the numeric identifier of a content {@code
 * QName} together with the identifier of the version store, so both values can be passed together
 * to persistence-layer queries.
 *
 * @author beaurpi
 */
public class ContentNumberParametersDAO {

  /** Numeric database identifier of the content {@code QName}. */
  private Integer contentQnameID;

  /** Numeric database identifier of the version store. */
  private Integer versionStoreID;

  /**
   * Creates a new parameter holder with the given content {@code QName} and version store
   * identifiers.
   *
   * @param contentQnameID the numeric database identifier of the content {@code QName}
   * @param versionStoreID the numeric database identifier of the version store
   */
  public ContentNumberParametersDAO(
    Integer contentQnameID,
    Integer versionStoreID
  ) {
    this.contentQnameID = contentQnameID;
    this.versionStoreID = versionStoreID;
  }

  /**
   * Returns the numeric database identifier of the content {@code QName}.
   *
   * @return the content {@code QName} identifier, or {@code null} if not set
   */
  public Integer getContentQnameID() {
    return contentQnameID;
  }

  /**
   * Sets the numeric database identifier of the content {@code QName}.
   *
   * @param contentQnameID the content {@code QName} identifier to set
   */
  public void setContentQnameID(Integer contentQnameID) {
    this.contentQnameID = contentQnameID;
  }

  /**
   * Returns the numeric database identifier of the version store.
   *
   * @return the version store identifier, or {@code null} if not set
   */
  public Integer getVersionStoreID() {
    return versionStoreID;
  }

  /**
   * Sets the numeric database identifier of the version store.
   *
   * @param versionStoreID the version store identifier to set
   */
  public void setVersionStoreID(Integer versionStoreID) {
    this.versionStoreID = versionStoreID;
  }
}
