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
 * Enumerates the kinds of calendar events that can be scheduled within an Interest Group's Events
 * service.
 *
 * <p>The value classifies an event so that clients can render and handle it appropriately.
 */
@SuppressWarnings("java:S115")
public enum EventType {
  /** A to-do item or actionable task. */
  Task,
  /** A meeting or scheduled appointment. */
  Appointment,
  /** Any event that does not fall into one of the more specific categories. */
  Other,
}
