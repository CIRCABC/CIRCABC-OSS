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
package io.swagger.model.alfresco;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Serializable POJO representing an applicant, i.e. a user who has requested to be invited to (or to
 * join) an interest group.
 *
 * <p>It carries the applicant's identity ({@code userName}, optional {@code firstName} and {@code
 * lastName}), the {@link Date} on which the application was made, and an optional {@code message}
 * addressed to the interest group's directory administrators. Instances are typically used to convey
 * membership request information within the CIRCABC domain and REST layers.
 *
 * @author Yanick Pignot
 */
public class Applicant implements Serializable {

  /** Serialization version identifier for this POJO. */
  private static final long serialVersionUID = 1L;

  /** The last name of the applicant (optional). */
  private String lastName;

  /** The first name of the applicant (optional). */
  private String firstName;

  /** The mandatory username identifying the applicant. */
  private String userName;

  /** The date on which the application was submitted. */
  private Date date;

  /** The optional message sent by the applicant to the interest group directory administrators. */
  private String message;

  /**
   * Ininitialize an applicant with all parameters
   *
   * @param userName  the mandatory username of the applicant which resquest to be invited to the
   *                  interest group
   * @param date      the mandatory date of the application
   * @param message   the message of the applicant sent to the interest groups dir admins
   * @param firstName the first name of the applicant
   * @param lastName  the last name of the applicant
   */
  public Applicant(
    final String userName,
    final Date date,
    final String message,
    final String firstName,
    final String lastName
  ) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.userName = userName;
    this.date = date;
    this.message = message;
  }

  /**
   * Ininitialize an applicant without the optional first name and last name
   *
   * @param userName the mandatory username of the applicant which resquest to be invited to the
   *                 interest group
   * @param date     the mandatory date of the application
   * @param message  the message of the applicant
   */
  public Applicant(
    final String userName,
    final Date date,
    final String message
  ) {
    this(userName, date, message, null, null);
  }

  /**
   * Ininitialize an applicant without the optional names of the applicant and the optional message
   *
   * @param userName the mandatory username of the applicant which resquest to be invited to the
   *                 interest group
   * @param date     the mandatory date of the application
   */
  public Applicant(final String userName, final Date date) {
    this(userName, date, null);
  }

  /**
   * @return the date of the application
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date of the application the date to set
   */
  public void setDate(final Date date) {
    this.date = date;
  }

  /**
   * @return the message of the applicant sent to the ig dir admins
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the message of the applicant sent to the ig dir admins to set
   */
  public void setMessage(final String message) {
    this.message = message;
  }

  /**
   * Builds a human-readable display name for the applicant by concatenating the first and last
   * names. If neither name is available, the username is used as a fallback.
   *
   * @return the display name of the applicant, never {@code null}
   */
  public String getDisplayName() {
    String displayName = "";
    displayName += (this.firstName == null) ? "" : this.firstName + " ";
    displayName += (this.lastName == null) ? "" : this.lastName;

    displayName = displayName.trim();

    // if it is impossible to set a display name, get the user name
    if (displayName.isEmpty()) {
      displayName = userName;
    }

    return displayName;
  }

  /**
   * @return the userName of the applicant
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName the userName of the applicant to set
   */
  public void setUserName(final String userName) {
    this.userName = userName;
  }

  /**
   * @return the firstName
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @param firstName the firstName to set
   */
  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  /**
   * @return the lastName
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @param lastName the lastName to set
   */
  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  /**
   * Returns a textual representation of this applicant, including the application date, the
   * username and the message.
   *
   * @return a string describing the application details
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(getDate());

    return (
      "Application details: " +
      "Date=" +
      cal.get(Calendar.YEAR) +
      "/" +
      cal.get(Calendar.MONTH) +
      "/" +
      cal.get(Calendar.DAY_OF_MONTH) +
      "|User=" +
      getUserName() +
      "|Message=" +
      getMessage()
    );
  }
}
