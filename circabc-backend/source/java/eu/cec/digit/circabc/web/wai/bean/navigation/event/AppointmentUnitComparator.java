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

import eu.cec.digit.circabc.web.ui.tag.AppointmentUnit;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Order events (ideally of the same day) according the start hour
 *
 * @author yanick pignot
 */
public class AppointmentUnitComparator implements Comparator<AppointmentUnit>, Serializable {

    private static final long serialVersionUID = 6227528170880231785L;

    public int compare(AppointmentUnit a1, AppointmentUnit a2) {
        final int diffDate = a1.getStartDay().compareTo(a2.getStartDay());

        if (diffDate == 0) {
            // if same day, compare the time
            return a1.getStart().compareTo(a2.getStart());
        } else {
            return diffDate;
        }
    }
}
