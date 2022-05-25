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
package eu.cec.digit.circabc.web.wai.dialog.ig;


public class UserRecord {

    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String ecasDomain;
    private String moniker;
    private String profile;
    private String orgdepnumber;


    public UserRecord(String userName, String firstName, String lastName,
                      String email, String ecasDomain, String moniker, String profile, String orgdepnumber) {

        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.ecasDomain = ecasDomain;
        this.moniker = moniker;
        this.profile = profile;
        this.orgdepnumber = orgdepnumber;
    }

    public String getUserName() {
        if (this.userName != null) {
            return this.userName;
        } else {
            return "N/A";
        }
    }

    public String getLastName() {

        if (this.lastName != null) {
            return this.lastName;
        } else {
            return "N/A";
        }
    }

    public String getFirstName() {

        if (this.firstName != null) {
            return this.firstName;
        } else {
            return "N/A";
        }
    }

    public String getEmail() {

        if (this.email != null) {
            return this.email;
        } else {
            return "N/A";
        }
    }

    public String getECASDomain() {

        if (this.ecasDomain != null) {
            return this.ecasDomain;
        } else {
            return "N/A";
        }
    }

    public String getECASMoniker() {

        if (this.moniker != null) {
            return this.moniker;
        } else {
            return "N/A";
        }
    }

    public String getProfile() {

        if (this.profile != null) {
            return this.profile;
        } else {
            return "N/A";
        }
    }

    public String getOrgdepnumber() {
        return orgdepnumber;
    }

    public void setOrgdepnumber(String orgdepnumber) {
        this.orgdepnumber = orgdepnumber;
    }

}
