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
package eu.cec.digit.circabc.web.bean.surveys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper to convert data from IPM webservice to local object.
 *
 * @author Matthieu Sprunck
 */
public class SurveyHelper {

    /**
     * Transforms a Survey coming from the IPM webservice to the Survey object used in Circabc
     *
     * @param ipmSurveys An array of Survey coming from IPM
     * @return a list of circabc Survey
     */
    @SuppressWarnings("unchecked")
    public static List<Survey> transform(
            ipm.webservice.generic.Survey[] ipmSurveys) {

        if (ipmSurveys == null) {
            return Collections.EMPTY_LIST;
        }

        List<Survey> result = new ArrayList<>();

        Survey survey = null;
        for (ipm.webservice.generic.Survey is : ipmSurveys) {
            survey = new Survey(is.getShortName(), is.getSubject(), is
                    .getStatus(), is.getStartDate(), is.getCloseDate(), Arrays
                    .asList(is.getTranslations()), is.getPivotLang());
            result.add(survey);
        }
        return result;
    }
}
