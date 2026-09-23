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
package io.swagger.model.db;

/**
 * Data access object representing a single logged activity entry as stored in the database.
 *
 * <p>An instance couples a service (the CIRCABC service or area where the action took place) with a
 * textual description of the activity that was performed. The optional {@link #id} corresponds to
 * the primary key of the underlying persistence record.
 *
 * @author Slobodan Filipovic
 */
public class LogActivityDAO {

  /** Primary key identifier of the log activity record; {@code null} for transient instances. */
  private Integer id;

  /** Human-readable description of the activity that was performed. */
  private String activityDescription;

  /** Description of the service or area in which the activity occurred. */
  private String serviceDescription;

  /** Creates an empty log activity DAO with no fields initialised. */
  public LogActivityDAO() {}

  /**
   * Creates a fully populated log activity DAO, including its persistence identifier.
   *
   * @param id the primary key identifier of the record
   * @param service the service or area description
   * @param activity the activity description
   */
  public LogActivityDAO(Integer id, String service, String activity) {
    this.id = id;
    this.serviceDescription = service;
    this.activityDescription = activity;
  }

  /**
   * Creates a transient log activity DAO without a persistence identifier.
   *
   * @param service the service or area description
   * @param activity the activity description
   */
  public LogActivityDAO(String service, String activity) {
    this.serviceDescription = service;
    this.activityDescription = activity;
  }

  /**
   * @return the id
   */
  public Integer getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * @return the activityDescription
   */
  public String getActivityDescription() {
    return activityDescription;
  }

  /**
   * @param activityDescription the activityDescription to set
   */
  public void setActivityDescription(String activityDescription) {
    this.activityDescription = activityDescription;
  }

  /**
   * @return the serviceDescription
   */
  public String getServiceDescription() {
    return serviceDescription;
  }

  /**
   * @param serviceDescription the serviceDescription to set
   */
  public void setServiceDescription(String serviceDescription) {
    this.serviceDescription = serviceDescription;
  }
}
