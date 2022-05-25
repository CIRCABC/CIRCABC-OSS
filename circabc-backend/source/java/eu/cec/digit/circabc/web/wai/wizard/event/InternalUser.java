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
package eu.cec.digit.circabc.web.wai.wizard.event;

import org.apache.commons.lang.ObjectUtils;

/**
 * Client side Internal user representation
 *
 * @author Yanick Pignot
 */
public class InternalUser implements Comparable<InternalUser> {

    private String authority;
    private String firstName;
    private String lastName;
    private String email;

    public InternalUser(final String authority, final String firstName, final String lastName,
                        final String email) {
        this.authority = authority;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public InternalUser(final String authority, final String firstName) {
        this(authority, firstName, null, null);
    }

    public final String getAuthority() {
        return authority;
    }

    public final String getLongDisplayName() {
        return getShortDisplayName() + ((email == null) ? "" : " (" + email + ")");
    }

    public final String getShortDisplayName() {
        return ((firstName == null) ? "" : firstName + " ") + ((lastName == null) ? "" : lastName);
    }


    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    @Override
    public int compareTo(InternalUser o) {
        return ObjectUtils.compare(this.email, o.email);
    }
}
