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
package eu.cec.digit.circabc.web.comparator;

import eu.cec.digit.circabc.web.wai.dialog.profile.AccessProfileWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class CircabcProfileDisplayTitleSort implements Comparator<AccessProfileWrapper> {

    private static final Log logger = LogFactory.getLog(CircabcProfileDisplayTitleSort.class);
    Collator laguageCollator;

    public CircabcProfileDisplayTitleSort(String language) {
        try {
            Locale locale = new Locale(language);
            laguageCollator = Collator.getInstance(locale);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Can not set Collator for language :" + language, e);
                logger.error("Availible locales are :" + Arrays.toString(Collator.getAvailableLocales()));
            }
        }
    }

    @Override
    public int compare(AccessProfileWrapper profile1, AccessProfileWrapper profile2) {
        if (laguageCollator == null) {
            return profile1.getDisplayTitle().compareToIgnoreCase(profile2.getDisplayTitle());
        } else {
            return laguageCollator.compare(profile1.getDisplayTitle(), profile2.getDisplayTitle());
        }


    }

}
