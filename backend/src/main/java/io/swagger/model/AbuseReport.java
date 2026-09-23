package io.swagger.model;

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

import java.util.Date;

/**
 * Represents a single abuse report raised during a content moderation process.
 *
 * <p>An abuse report captures who reported the abuse, when it was reported and the reason provided
 * by the reporter. Implementations expose this information as read-only accessors.
 *
 * @author Yanick Pignot
 */
public interface AbuseReport {
  /**
   * Returns the date on which the abuse was reported.
   *
   * @return the date the abuse was reported
   */
  Date getReportDate();

  /**
   * Returns the identifier of the user who reported the abuse.
   *
   * @return the user name of the reporter
   */
  String getReporter();

  /**
   * Returns the message supplied by the reporter describing the reason for the abuse report.
   *
   * @return the reason for the abuse report as provided by the reporter
   */
  String getMessage();
}
