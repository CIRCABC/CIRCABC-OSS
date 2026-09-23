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
package io.swagger.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Serializable data-holder capturing a single audit entry for a CIRCABC REST (webscript) request.
 *
 * <p>An instance of this class is populated while a mutating webscript endpoint executes and is
 * later persisted/rendered as a log record. It records the webscript that handled the call (the
 * {@code template}), the HTTP method, the resulting HTTP status code, the invoking user, the
 * requested URL, the timestamp of the call, up to three named path parameters extracted from the
 * request, free-form contextual information accumulated during processing, and identifying details
 * of the Alfresco node that the request acted upon.
 */
public class LogRestRecord implements Serializable {

  /** Serialization version identifier for this record. */
  private static final long serialVersionUID = -2932718978323728183L;

  /** Name of the webscript template/endpoint that handled the request. */
  private String template;

  /** HTTP method of the request (e.g. {@code GET}, {@code POST}, {@code PUT}, {@code DELETE}). */
  private String method;

  /** HTTP status code returned for the request. */
  private Integer statusCode;

  /** Name of the first path parameter of the request URL. */
  private String pathOneName;

  /** Value of the first path parameter of the request URL. */
  private String pathOneValue;

  /** Name of the second path parameter of the request URL. */
  private String pathTwoName;

  /** Value of the second path parameter of the request URL. */
  private String pathTwoValue;

  /** Name of the third path parameter of the request URL. */
  private String pathThreeName;

  /** Value of the third path parameter of the request URL. */
  private String pathThreeValue;

  /** Identifier of the user who issued the request. */
  private String user;

  /** Full URL of the request. */
  private String url;

  /** Timestamp at which the request was processed. */
  private Date date;

  /** Accumulator for free-form contextual information gathered while processing the request. */
  private StringBuilder info = new StringBuilder();

  /** Repository path of the Alfresco node affected by the request. */
  private String nodePath;

  /** Database identifier of the Alfresco node affected by the request. */
  private Long nodeID;

  /** Identifier of the parent node of the affected Alfresco node. */
  private String nodeParent;

  /**
   * Returns the webscript template/endpoint that handled the request.
   *
   * @return the template name
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets the webscript template/endpoint that handled the request.
   *
   * @param template the template name to set
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * Returns the HTTP method of the request.
   *
   * @return the HTTP method
   */
  public String getMethod() {
    return method;
  }

  /**
   * Sets the HTTP method of the request.
   *
   * @param method the HTTP method to set
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Returns the HTTP status code returned for the request.
   *
   * @return the HTTP status code
   */
  public Integer getStatusCode() {
    return statusCode;
  }

  /**
   * Sets the HTTP status code returned for the request.
   *
   * @param statusCode the HTTP status code to set
   */
  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * Returns the identifier of the user who issued the request.
   *
   * @return the user identifier
   */
  public String getUser() {
    return user;
  }

  /**
   * Sets the identifier of the user who issued the request.
   *
   * @param user the user identifier to set
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Returns the full URL of the request.
   *
   * @return the request URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the full URL of the request.
   *
   * @param url the request URL to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Returns the timestamp at which the request was processed.
   *
   * @return the request date
   */
  public Date getDate() {
    return date;
  }

  /**
   * Sets the timestamp at which the request was processed.
   *
   * @param date the request date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Returns the accumulated free-form contextual information for this record.
   *
   * @return the info as a string
   */
  public String getInfo() {
    return info.toString();
  }

  /**
   * Replaces the entire contextual information with the given value.
   *
   * @param info the information to set, discarding any previously accumulated content
   */
  public void setInfo(String info) {
    this.info.delete(0, this.info.length());
    this.info.append(info);
  }

  /**
   * Appends an additional fragment (prefixed with a space) to the accumulated contextual
   * information.
   *
   * @param info the information fragment to append
   */
  public void addInfo(String info) {
    this.info.append(" ");
    this.info.append(info);
  }

  /**
   * Returns the name of the first path parameter of the request URL.
   *
   * @return the first path parameter name
   */
  public String getPathOneName() {
    return pathOneName;
  }

  /**
   * Sets the name of the first path parameter of the request URL.
   *
   * @param pathOneName the first path parameter name to set
   */
  public void setPathOneName(String pathOneName) {
    this.pathOneName = pathOneName;
  }

  /**
   * Returns the value of the first path parameter of the request URL.
   *
   * @return the first path parameter value
   */
  public String getPathOneValue() {
    return pathOneValue;
  }

  /**
   * Sets the value of the first path parameter of the request URL.
   *
   * @param pathOneValue the first path parameter value to set
   */
  public void setPathOneValue(String pathOneValue) {
    this.pathOneValue = pathOneValue;
  }

  /**
   * Returns the name of the second path parameter of the request URL.
   *
   * @return the second path parameter name
   */
  public String getPathTwoName() {
    return pathTwoName;
  }

  /**
   * Sets the name of the second path parameter of the request URL.
   *
   * @param pathTwoName the second path parameter name to set
   */
  public void setPathTwoName(String pathTwoName) {
    this.pathTwoName = pathTwoName;
  }

  /**
   * Returns the value of the second path parameter of the request URL.
   *
   * @return the second path parameter value
   */
  public String getPathTwoValue() {
    return pathTwoValue;
  }

  /**
   * Sets the value of the second path parameter of the request URL.
   *
   * @param pathTwoValue the second path parameter value to set
   */
  public void setPathTwoValue(String pathTwoValue) {
    this.pathTwoValue = pathTwoValue;
  }

  /**
   * Returns the name of the third path parameter of the request URL.
   *
   * @return the third path parameter name
   */
  public String getPathThreeName() {
    return pathThreeName;
  }

  /**
   * Sets the name of the third path parameter of the request URL.
   *
   * @param pathThreeName the third path parameter name to set
   */
  public void setPathThreeName(String pathThreeName) {
    this.pathThreeName = pathThreeName;
  }

  /**
   * Returns the value of the third path parameter of the request URL.
   *
   * @return the third path parameter value
   */
  public String getPathThreeValue() {
    return pathThreeValue;
  }

  /**
   * Sets the value of the third path parameter of the request URL.
   *
   * @param pathThreeValue the third path parameter value to set
   */
  public void setPathThreeValue(String pathThreeValue) {
    this.pathThreeValue = pathThreeValue;
  }

  /**
   * Returns the identifier of the parent node of the affected Alfresco node.
   *
   * @return the parent node identifier
   */
  public String getNodeParent() {
    return nodeParent;
  }

  /**
   * Sets the identifier of the parent node of the affected Alfresco node.
   *
   * @param parentNode the parent node identifier to set
   */
  public void setNodeParent(String parentNode) {
    this.nodeParent = parentNode;
  }

  /**
   * Returns the repository path of the Alfresco node affected by the request.
   *
   * @return the node path
   */
  public String getNodePath() {
    return nodePath;
  }

  /**
   * Sets the repository path of the Alfresco node affected by the request.
   *
   * @param nodePath the node path to set
   */
  public void setNodePath(String nodePath) {
    this.nodePath = nodePath;
  }

  /**
   * Returns the database identifier of the Alfresco node affected by the request.
   *
   * @return the node identifier
   */
  public Long getNodeID() {
    return nodeID;
  }

  /**
   * Sets the database identifier of the Alfresco node affected by the request.
   *
   * @param nodeID the node identifier to set
   */
  public void setNodeID(Long nodeID) {
    this.nodeID = nodeID;
  }
}
