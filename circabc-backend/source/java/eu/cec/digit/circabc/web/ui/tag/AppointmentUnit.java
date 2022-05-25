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
package eu.cec.digit.circabc.web.ui.tag;

import com.google.ical.values.DateValue;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalTime;


/**
 * Wrapper that is used to display an event unit in the Calendar Component
 *
 * @author Yanick Pignot
 */
public class AppointmentUnit {

    private DateValue startDay;

    private LocalTime start;
    private LocalTime end;
    private String id;
    private String title;

    private String type;

    /**
     * @return the end
     */
    public LocalTime getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(LocalTime end) {
        this.end = end;
    }

    /**
     * @return the end as String
     */
    public String getEndAsString() {
        return getAsString(end);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the start
     */
    public LocalTime getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(LocalTime start) {
        this.start = start;
    }

    /**
     * @return the start
     */
    public String getStartAsString() {
        return getAsString(start);
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the type
     */
    public final String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public final void setType(String type) {
        this.type = type;
    }

    public boolean isInQuarter(LocalTime from) {
        return from.equals(start) || (from.isAfter(start) && from.isBefore(end));

    }

    /**
     * @return the startDay
     */
    public final DateValue getStartDay() {
        return startDay;
    }

    /**
     * @param startDay the startDay to set
     */
    public final void setStartDay(DateValue startDay) {
        this.startDay = startDay;
    }

    private String getAsString(LocalTime LocalTime) {
        return paddInt(LocalTime.get(DateTimeFieldType.hourOfDay())) + ':' + paddInt(
                LocalTime.get(DateTimeFieldType.minuteOfHour()));
    }

    private String paddInt(int i) {
        return ((i < 10) ? "0" : "") + i;

    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
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
        final AppointmentUnit other = (AppointmentUnit) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
