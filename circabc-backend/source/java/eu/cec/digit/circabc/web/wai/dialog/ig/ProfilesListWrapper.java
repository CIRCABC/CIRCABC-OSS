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

import java.io.Serializable;
import java.util.List;

public class ProfilesListWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1188993444330837226L;

    private String profileName;
    private List<String> igs;

    private ProfilesListWrapper() {

    }

    public ProfilesListWrapper(final String profileName, final List<String> igs) {
        this.profileName = profileName;
        this.igs = igs;
    }

    public String getProfileName() {
        return this.profileName;
    }

    public List<String> getIgs() {
        return this.igs;
    }
}
