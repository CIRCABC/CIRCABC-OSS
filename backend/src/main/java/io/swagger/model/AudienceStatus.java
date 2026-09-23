package io.swagger.model;

/*
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

/**
 * Represents the enrolment status of an audience (typically an Interest Group's membership).
 *
 * <p>The status indicates whether new members are currently allowed to join.
 */
@SuppressWarnings("java:S115")
public enum AudienceStatus {
  /** The audience is open to new members; enrolment is currently permitted. */
  Open,
  /** The audience is closed; enrolment is not currently permitted. */
  Closed,
}
