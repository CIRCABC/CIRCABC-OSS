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
package eu.cec.digit.circabc.web.wai.dialog.applicant;

import eu.cec.digit.circabc.repo.applicant.Applicant;
import eu.cec.digit.circabc.web.PermissionUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Web side wrapper for Applicant object encapsulation
 *
 * @author Yanick Pignot
 */
public class WebApplicant implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -9120647048820773893L;

    private final Applicant applicant;
    private final String login;

    /*package*/ WebApplicant(final Applicant applicant) {
        this.applicant = applicant;
        login = PermissionUtils.computeUserLogin(applicant.getUserName());
    }

    /**
     * @see eu.cec.digit.circabc.repo.applicant.Applicant#getDate()
     */
    public Date getDate() {
        return applicant.getDate();
    }

    /**
     * @see eu.cec.digit.circabc.repo.applicant.Applicant#getDisplayName()
     */
    public String getDisplayName() {
        return applicant.getDisplayName();
    }

    /**
     * @see eu.cec.digit.circabc.repo.applicant.Applicant#getFirstName()
     */
    public String getFirstName() {
        return applicant.getFirstName();
    }

    /**
     * @see eu.cec.digit.circabc.repo.applicant.Applicant#getLastName()
     */
    public String getLastName() {
        return applicant.getLastName();
    }

    /**
     * @see eu.cec.digit.circabc.repo.applicant.Applicant#getMessage()
     */
    public String getMessage() {
        return applicant.getMessage();
    }

    /**
     * @see eu.cec.digit.circabc.repo.applicant.Applicant#getUserName()
     */
    public String getUserName() {
        return applicant.getUserName();
    }

    /**
     * @return the login
     */
    public final String getLogin() {
        return login;
    }

    /**
     * @return the applicant
     */
    public final Applicant getApplicant() {
        return applicant;
    }
}
