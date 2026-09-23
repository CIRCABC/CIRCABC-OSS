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

import java.util.Date;

/**
 * Data Access Object representing a single audit/activity log record.
 *
 * <p>An instance captures who ({@link #user}) performed which action ({@link #activityID}) on which
 * document ({@link #documentID}) within a given Interest Group ({@link #igID} / {@link #igName}), at
 * what time ({@link #date}), along with the affected repository path, free-form info and an
 * outcome/status flag ({@link #isOK}).
 *
 * <p>The string setters automatically truncate their values so they never exceed the byte-size
 * limits of the underlying database columns.
 *
 * @author Slobodan Filipovic
 */
public class LogRecordDAO {

  /** Maximum size, in bytes, allowed for the {@link #path} column. */
  private static final int MAX_PATH_SIZE_IN_BYTES = 4000;
  /** Maximum size, in bytes, allowed for the {@link #info} column. */
  private static final int MAX_INFO_SIZE_IN_BYTES = 4000;
  /** Maximum size, in bytes, allowed for the {@link #user} column. */
  private static final int MAX_USER_NAME_SIZE_IN_BYTES = 50;
  /** Maximum size, in bytes, allowed for the {@link #igName} column. */
  private static final int MAX_IG_NAME_SIZE_IN_BYTES = 256;

  /** Internal DB-generated sequence id. */
  private long id; // internal DB-generated sequence id
  /** Identifier of the Interest Group the logged action belongs to. */
  private long igID;
  /** Human-readable name of the Interest Group. */
  private String igName;
  /** Identifier of the document affected by the logged action. */
  private long documentID;
  /** User name of the actor that performed the logged action. */
  private String user;
  /** Timestamp at which the logged action occurred. */
  private Date date;
  /** Numeric code identifying the type of activity/action that was logged. */
  private int activityID;
  /** Free-form additional information associated with the log record. */
  private String info;
  /** Repository path of the affected node. */
  private String path;
  /** Raw status flag ({@code 1} = success, otherwise failure). */
  private int isOK;
  /** Boolean view of {@link #isOK}, kept in sync by {@link #setIsOK(int)}. */
  private boolean isOKBoolean;

  /**
   * Returns the outcome status as a boolean.
   *
   * @return {@code true} if the logged action succeeded, {@code false} otherwise
   */
  public boolean isOKBoolean() {
    return isOKBoolean;
  }

  /**
   * Sets the boolean outcome status.
   *
   * @param isOKBoolean {@code true} if the logged action succeeded, {@code false} otherwise
   */
  public void setOKBoolean(boolean isOKBoolean) {
    this.isOKBoolean = isOKBoolean;
  }

  /**
   * Returns the unique identifier (UUID) of the affected node.
   *
   * @return the node UUID, may be {@code null}
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Sets the unique identifier (UUID) of the affected node.
   *
   * @param uuid the node UUID to set
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /** Unique identifier (UUID) of the affected node. */
  private String uuid;

  /**
   * Truncates the given string so its byte representation does not exceed {@code maxSizeInBytes}.
   *
   * @param value the string to truncate; may be {@code null}
   * @param maxSizeInBytes the maximum allowed size in bytes
   * @return the original string if within the limit, a truncated copy if it exceeds the limit, or
   *     {@code null} if {@code value} is {@code null}
   */
  private static String getTruncatedString(String value, int maxSizeInBytes) {
    if (value == null) {
      return null;
    }
    final String result;
    final byte[] bytes = value.getBytes();
    if (bytes.length > maxSizeInBytes) {
      final byte[] destbytes = new byte[maxSizeInBytes];
      System.arraycopy(bytes, 0, destbytes, 0, maxSizeInBytes);
      result = new String(destbytes);
    } else {
      result = value;
    }
    return result;
  }

  /**
   * @return the igID
   */
  public long getIgID() {
    return igID;
  }

  /**
   * @param igID the igID to set
   */
  public void setIgID(long igID) {
    this.igID = igID;
  }

  /**
   * @return the documentID
   */
  public long getDocumentID() {
    return documentID;
  }

  /**
   * @param documentID the documentID to set
   */
  public void setDocumentID(long documentID) {
    this.documentID = documentID;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user == null ? "" : user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(String user) {
    this.user = getTruncatedString(user, MAX_USER_NAME_SIZE_IN_BYTES);
  }

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * @return the action
   */
  public int getActivityID() {
    return activityID;
  }

  /**
   * @param activityID the action to set
   */
  public void setActivityID(int activityID) {
    this.activityID = activityID;
  }

  /**
   * @return the info
   */
  public String getInfo() {
    return info == null ? "" : info;
  }

  /**
   * @param info the info to set
   */
  public void setInfo(String info) {
    this.info = getTruncatedString(info, MAX_INFO_SIZE_IN_BYTES);
  }

  /**
   * @return the isOK
   */
  public int getIsOK() {
    return isOK;
  }

  /**
   * @param isOK the isOK to set
   */
  public void setIsOK(int isOK) {
    this.isOK = isOK;
    this.isOKBoolean = this.isOK == 1;
  }

  /**
   * @return the id
   */
  public long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path == null ? "" : path;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = getTruncatedString(path, MAX_PATH_SIZE_IN_BYTES);
  }

  /**
   * @return the igName
   */
  public String getIgName() {
    return igName == null ? "" : igName;
  }

  /**
   * @param igName the igName to set
   */
  public void setIgName(String igName) {
    this.igName = getTruncatedString(igName, MAX_IG_NAME_SIZE_IN_BYTES);
  }
}
