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
 * Domain model contract for a calendar event within an Interest Group.
 *
 * <p>An {@code Event} extends {@link Appointment} (which carries the common scheduling attributes
 * such as start/end dates, title and location) and augments it with event-specific classification:
 * an {@link EventType} describing the kind of event and an {@link EventPriority} indicating its
 * importance. Implementations act as data transfer objects exchanged through the REST layer.
 */
public interface Event extends Appointment {
  /**
   * Returns the classification of this event.
   *
   * @return the event type, or {@code null} if none has been set
   */
  EventType getEventType();

  /**
   * Sets the classification of this event.
   *
   * @param value the event type to assign
   */
  void setEventType(EventType value);

  /**
   * Returns the priority level of this event.
   *
   * @return the event priority, or {@code null} if none has been set
   */
  EventPriority getPriority();

  /**
   * Sets the priority level of this event.
   *
   * @param value the event priority to assign
   */
  void setPriority(EventPriority value);
}
