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
package eu.cec.digit.circabc.repo.event;

import eu.cec.digit.circabc.service.event.AppointmentUpdateInfo;
import eu.cec.digit.circabc.service.event.Event;
import eu.cec.digit.circabc.service.event.EventPriority;
import eu.cec.digit.circabc.service.event.EventType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

import java.io.Serializable;
import java.util.Map;

import static eu.cec.digit.circabc.model.EventModel.PROP_EVENT_PRIORITY;
import static eu.cec.digit.circabc.model.EventModel.PROP_EVENT_TYPE;

public class EventImpl extends AppointmentImpl implements Event {

    private EventType eventType;
    private EventPriority priority;

    public EventType getEventType() {

        return eventType;
    }

    public void setEventType(EventType value) {
        eventType = value;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public void setPriority(EventPriority value) {
        priority = value;
    }

    @Override
    public PropertyMap getProperties() {
        PropertyMap properties = super.getProperties();

        if (this.getPriority() != null) {
            properties.put(PROP_EVENT_PRIORITY, this.getPriority());
        }
        if (this.getEventType() != null) {
            properties.put(PROP_EVENT_TYPE, this.getEventType());
        }

        return properties;
    }

    @Override
    public PropertyMap getProperties(AppointmentUpdateInfo updateInfo) {
        PropertyMap properties = super.getProperties(updateInfo);

        switch (updateInfo) {
            case GeneralInformation:
                if (this.getPriority() != null) {
                    properties.put(PROP_EVENT_PRIORITY, this.getPriority());
                }
                if (this.getEventType() != null) {
                    properties.put(PROP_EVENT_TYPE, this.getEventType());
                }

                break;

            default:
                break;
        }
        return properties;
    }

    @Override
    public void init(Map<QName, Serializable> properties) {
        super.init(properties);

        Serializable eventPriority = properties.get(PROP_EVENT_PRIORITY);
        if (eventPriority != null) {
            this.setPriority(EventPriority.valueOf(eventPriority.toString()));
        }
        Serializable propertyEventType = properties.get(PROP_EVENT_TYPE);
        if (propertyEventType != null) {
            this.setEventType(EventType.valueOf(propertyEventType.toString()));
        }
    }
}
