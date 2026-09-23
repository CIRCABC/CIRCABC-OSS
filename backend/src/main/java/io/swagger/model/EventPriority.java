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

/**
 * Enumerates the priority levels that can be assigned to a calendar event.
 *
 * <p>The values are ordered from least to most pressing ({@link #Low} to {@link #Urgent}) and are
 * used to classify the importance of events within an Interest Group's calendar. The constant names
 * intentionally use PascalCase to match the JSON representation exchanged with the frontend, which
 * is why the Sonar rule {@code java:S115} (enum constant naming convention) is suppressed for this
 * type.
 */
@SuppressWarnings("java:S115")
public enum EventPriority {
  /** Lowest priority; the event is informational and requires no immediate attention. */
  Low,
  /** Standard priority for routine events. */
  Medium,
  /** Elevated priority; the event is important and should be attended to promptly. */
  High,
  /** Highest priority; the event demands immediate attention. */
  Urgent,
}
