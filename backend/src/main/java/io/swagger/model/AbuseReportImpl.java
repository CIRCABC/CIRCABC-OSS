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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.ParameterCheck;

/**
 * Concrete, immutable implementation of {@link AbuseReport} used within the moderation process.
 *
 * <p>An abuse report captures the fact that a user flagged a content item as inappropriate. It
 * holds the moment the report was raised, the identifier of the reporting user and an optional
 * free-text message explaining the reason. Instances are value objects: all state is set at
 * construction time and never mutated afterwards.
 *
 * @author Yanick Pignot
 */
public class AbuseReportImpl implements AbuseReport, Serializable {

  /** Serialization version identifier for this value object. */
  private static final long serialVersionUID = -3614767880321596490L;

  /** Date and time at which the abuse was reported. */
  private final Date reportDate;

  /** Identifier (username) of the user who raised the abuse report. */
  private final String reporter;

  /** Free-text message describing the reason for the report; never {@code null} (empty if absent). */
  private final String message;

  /**
   * Creates an abuse report with an explicit date, reporter and message.
   *
   * @param reportDate the date and time at which the abuse was reported; must not be {@code null}
   * @param reporter the identifier of the reporting user; must not be {@code null} or empty
   * @param message the reason for the report; may be {@code null}, in which case it is stored as an
   *     empty string
   * @throws IllegalArgumentException if {@code reportDate} is {@code null} or {@code reporter} is
   *     {@code null} or empty
   */
  public AbuseReportImpl(Date reportDate, String reporter, String message) {
    super();
    ParameterCheck.mandatory("The date", reportDate);
    ParameterCheck.mandatoryString("The reporter", reporter);

    this.reportDate = reportDate;
    this.reporter = reporter;
    this.message = message == null ? "" : message;
  }

  /**
   * Creates an abuse report for the currently authenticated user, timestamped with the current
   * date and time.
   *
   * @param message the reason for the report; may be {@code null}, in which case it is stored as an
   *     empty string
   */
  public AbuseReportImpl(final String message) {
    this(new Date(), AuthenticationUtil.getFullyAuthenticatedUser(), message);
  }

  /**
   * Returns the reason supplied for the abuse report.
   *
   * @return the report message; never {@code null} (empty string when no message was provided)
   */
  public final String getMessage() {
    return message;
  }

  /**
   * Returns the date and time at which the abuse was reported.
   *
   * @return the report date
   */
  public final Date getReportDate() {
    return reportDate;
  }

  /**
   * Returns the identifier of the user who raised the abuse report.
   *
   * @return the reporter's identifier
   */
  public final String getReporter() {
    return reporter;
  }

  /**
   * Returns a human-readable representation of this abuse report, including its date, reporter and
   * message.
   *
   * @return a string representation of this report
   */
  @Override
  public String toString() {
    return (
      "Abuse Report: [Date:" +
      reportDate +
      ", Reporter:" +
      reporter +
      ", message:" +
      message +
      "]"
    );
  }

  /**
   * Computes a hash code consistent with {@link #equals(Object)}, derived from the message, report
   * date and reporter.
   *
   * @return the hash code for this report
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((message == null) ? 0 : message.hashCode());
    result =
      PRIME * result + ((reportDate == null) ? 0 : reportDate.hashCode());
    result = PRIME * result + ((reporter == null) ? 0 : reporter.hashCode());
    return result;
  }

  /**
   * Compares this abuse report with another object for equality. Two reports are equal when they
   * are of the same type and share the same message, report date and reporter.
   *
   * @param obj the object to compare against
   * @return {@code true} if the given object is an equal abuse report, {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AbuseReportImpl other = (AbuseReportImpl) obj;
    if (message == null) {
      if (other.message != null) {
        return false;
      }
    } else if (!message.equals(other.message)) {
      return false;
    }
    if (reportDate == null) {
      if (other.reportDate != null) {
        return false;
      }
    } else if (!reportDate.equals(other.reportDate)) {
      return false;
    }
    if (reporter == null) {
      if (other.reporter != null) {
        return false;
      }
    } else if (!reporter.equals(other.reporter)) {
      return false;
    }
    return true;
  }
}
