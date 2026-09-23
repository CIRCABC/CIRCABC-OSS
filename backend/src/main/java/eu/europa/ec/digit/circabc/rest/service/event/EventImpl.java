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
package eu.europa.ec.digit.circabc.rest.service.event;

import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.Event;
import io.swagger.model.EventPriority;
import io.swagger.model.EventType;
import io.swagger.model.alfresco.EventModel;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 * Default implementation of the {@link Event} domain model used by the CIRCABC event/calendar
 * service.
 *
 * <p>An event is a specialization of an {@link io.swagger.model.Appointment} (see
 * {@link AppointmentImpl}) that additionally carries an {@link EventType} (the category/kind of the
 * event) and an {@link EventPriority}. Beyond the appointment data inherited from its parent, this
 * class maps these two event-specific attributes between the domain model and the underlying
 * Alfresco content model:
 *
 * <ul>
 *   <li>{@link #init(Map)} reads {@code ce:priority} and {@code ce:eventType} from a map of node
 *       properties and populates this instance.
 *   <li>{@link #getProperties()} and {@link #getProperties(AppointmentUpdateInfo)} add the priority
 *       and event type back into the Alfresco {@link PropertyMap} for persistence.
 * </ul>
 *
 * <p>This is a mutable data holder and is not thread-safe.
 */
public class EventImpl extends AppointmentImpl implements Event {

  /** Category/kind of the event, backed by {@link EventModel#PROP_EVENT_TYPE}. */
  private EventType eventType;
  /** Priority of the event, backed by {@link EventModel#PROP_EVENT_PRIORITY}. */
  private EventPriority priority;

  /**
   * Returns the type (category/kind) of this event.
   *
   * @return the event type, or {@code null} if none has been set
   */
  public EventType getEventType() {
    return eventType;
  }

  /**
   * Sets the type (category/kind) of this event.
   *
   * @param value the event type to set
   */
  public void setEventType(EventType value) {
    eventType = value;
  }

  /**
   * Returns the priority of this event.
   *
   * @return the event priority, or {@code null} if none has been set
   */
  public EventPriority getPriority() {
    return priority;
  }

  /**
   * Sets the priority of this event.
   *
   * @param value the event priority to set
   */
  public void setPriority(EventPriority value) {
    priority = value;
  }

  /**
   * Builds the full Alfresco {@link PropertyMap} for this event.
   *
   * <p>Starts from the properties produced by the parent appointment
   * ({@link AppointmentImpl#getProperties()}) and adds the event-specific priority and type
   * properties when they are set (non-null values only).
   *
   * @return a {@link PropertyMap} containing the appointment properties plus the event priority and
   *     type when present
   */
  @Override
  public PropertyMap getProperties() {
    PropertyMap properties = super.getProperties();

    if (this.getPriority() != null) {
      properties.put(EventModel.PROP_EVENT_PRIORITY, this.getPriority());
    }
    if (this.getEventType() != null) {
      properties.put(EventModel.PROP_EVENT_TYPE, this.getEventType());
    }

    return properties;
  }

  /**
   * Builds a partial Alfresco {@link PropertyMap} restricted to the section of the event targeted by
   * the given update scope.
   *
   * <p>Delegates to the parent appointment implementation for the appointment-level properties, then
   * adds the event-specific priority and type only when the scope is
   * {@link AppointmentUpdateInfo#GeneralInformation} and the corresponding values are set.
   *
   * @param updateInfo the section of the event being updated, which determines which properties are
   *     included
   * @return a {@link PropertyMap} containing the properties for the requested update scope
   */
  @Override
  public PropertyMap getProperties(AppointmentUpdateInfo updateInfo) {
    PropertyMap properties = super.getProperties(updateInfo);

    if (updateInfo == AppointmentUpdateInfo.GeneralInformation) {
      if (this.getPriority() != null) {
        properties.put(EventModel.PROP_EVENT_PRIORITY, this.getPriority());
      }
      if (this.getEventType() != null) {
        properties.put(EventModel.PROP_EVENT_TYPE, this.getEventType());
      }
    }
    return properties;
  }

  /**
   * Populates this event from a map of Alfresco node properties.
   *
   * <p>First initializes the inherited appointment fields via
   * {@link AppointmentImpl#init(Map)}, then reads the event-specific
   * {@link EventModel#PROP_EVENT_PRIORITY} and {@link EventModel#PROP_EVENT_TYPE} properties. Each is
   * applied only when present, converting the stored string value to the corresponding enum via
   * {@link EventPriority#valueOf(String)} and {@link EventType#valueOf(String)}.
   *
   * @param properties the node properties read from the Alfresco repository, keyed by their
   *     {@link QName}
   */
  @Override
  public void init(Map<QName, Serializable> properties) {
    super.init(properties);

    Serializable eventPriority = properties.get(EventModel.PROP_EVENT_PRIORITY);
    if (eventPriority != null) {
      this.setPriority(EventPriority.valueOf(eventPriority.toString()));
    }
    Serializable propertyEventType = properties.get(EventModel.PROP_EVENT_TYPE);
    if (propertyEventType != null) {
      this.setEventType(EventType.valueOf(propertyEventType.toString()));
    }
  }
}
