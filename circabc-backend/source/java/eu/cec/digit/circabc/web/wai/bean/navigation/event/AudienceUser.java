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

import org.apache.commons.lang.StringUtils;


/**
 * Wrapper that is used to display an single user of the audience with its status
 *
 * @author Yanick Pignot
 */
public class AudienceUser implements Comparable<AudienceUser> {

    private String userFirstName;
    private String userLastName;
    private String email;
    private String status;

    /**
     * @param userFirstName
     * @param userLastName
     * @param status
     */
    public AudienceUser(String userFirstName, String userLastName, String email, String status) {
        super();
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.setEmail(email);
        this.status = status;
    }

    public final String getShortDisplayName() {
        String userName = StringUtils.isBlank(userFirstName) ? "" : userFirstName + " ";
        userName += StringUtils.isBlank(userLastName) ? "" : userLastName;
        String email = StringUtils.isBlank(this.email) ? "" : " (" + this.email + ")";
        return StringUtils.isBlank(userName) ? this.email : userName + email;
    }

    /**
     * @return the status
     */
    public final String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public final void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the userFirstName
     */
    public final String getUserFirstName() {
        return userFirstName;
    }

    /**
     * @param userFirstName the userFirstName to set
     */
    public final void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    /**
     * @return the userLastName
     */
    public final String getUserLastName() {
        return userLastName;
    }

    /**
     * @param userLastName the userLastName to set
     */
    public final void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int compareTo(AudienceUser o) {

        return this.email.compareTo(o.email);
    }

}
