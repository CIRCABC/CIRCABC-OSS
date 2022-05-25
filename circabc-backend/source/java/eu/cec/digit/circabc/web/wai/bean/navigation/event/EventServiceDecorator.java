/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.bean.navigation.event;

import com.google.ical.values.DateValue;
import eu.cec.digit.circabc.repo.event.AppointmentUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.ui.tag.AppointmentUnit;
import eu.cec.digit.circabc.web.ui.tag.JSFCalendarDecorator;
import eu.cec.digit.circabc.web.wai.bean.navigation.EventBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author James Smith
 * @see org.calendartag.tags.CalendarTag org.calendartag.decorators.CalendarDecorator The default
 * implementation of <code>CalendarDecorator</code> used when a decorator is not defined.
 * @since Aug 23, 2004
 */
public class EventServiceDecorator extends JSFCalendarDecorator {

    private EventBean eventBean;
    private Map<DateValue, List<AppointmentUnit>> appointmentsOfRange;


    public List<AppointmentUnit> getAppointments() {
        if (appointmentsOfRange == null) {
            appointmentsOfRange = getEventBean().getAppointmentUnitByDay(start, end);
        }

        List<AppointmentUnit> appointments = appointmentsOfRange
                .get(AppointmentUtils.convertDateToDateValue(calendar.getTime()));

        // return the appointments of the day.
        return appointments == null ? Collections.<AppointmentUnit>emptyList() : appointments;
    }

    private EventBean getEventBean() {
        if (eventBean == null) {
            eventBean = (EventBean) Beans.getBean(EventBean.BEAN_NAME);
        }

        return eventBean;
    }

    public void initializeCalendar() {
        // release cache the events of the current period
        appointmentsOfRange = null;
    }

}

