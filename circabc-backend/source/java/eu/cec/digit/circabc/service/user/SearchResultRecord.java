/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.user;

import java.io.Serializable;

public class SearchResultRecord implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7895552530213168492L;
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String profile;
    private String moniker;

    public SearchResultRecord(
            final String uid,
            final String moniker,
            final String firstName,
            final String lastName,
            final String email) {
        this.id = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.moniker = moniker;
        this.profile = "N/A";
    }

    public SearchResultRecord(
            final String uid, final String firstName, final String lastName, final String email) {
        this(uid, null, firstName, lastName, email);
    }

    @Override
    public int hashCode() {
    	//DIGITCIRCABC-5063 - moniker is null if the SearchResult is coming from LuceneUserServiceImpl
        String sb = this.email + this.firstName + this.id + this.lastName + (this.moniker!=null? this.moniker: "") + this.profile;
        return sb.hashCode();
    }

    public int compareTo(final SearchResultRecord compareTo) {
        int result;

        result = this.email.compareTo(compareTo.email);
        if (result != 0) {
            return result;
        }

        result = this.firstName.compareTo(compareTo.firstName);
        if (result != 0) {
            return result;
        }

        result = this.id.compareTo(compareTo.id);
        if (result != 0) {
            return result;
        }

        result = this.lastName.compareTo(compareTo.lastName);
        if (result != 0) {
            return result;
        }

        //DIGITCIRCABC-5063 - moniker is null if the SearchResult is coming from LuceneUserServiceImpl
        if(this.moniker!=null && compareTo.moniker!=null) {
        	result = this.moniker.compareTo(compareTo.moniker);
        	if (result != 0) {
        		return result;
        	}
        }

        result = this.profile.compareTo(compareTo.profile);
        if (result != 0) {
            return result;
        }

        return 0;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj != null && obj instanceof SearchResultRecord) {
            final SearchResultRecord compareTo = (SearchResultRecord) obj;

            if (!isEqual(this.email, compareTo.email)) {
                return false;
            }

            if (!isEqual(this.firstName, compareTo.firstName)) {
                return false;
            }

            if (!isEqual(this.id, compareTo.id)) {
                return false;
            }

            if (!isEqual(this.lastName, compareTo.lastName)) {
                return false;
            }

            //DIGITCIRCABC-5063 - moniker is null if the SearchResult is coming from LuceneUserServiceImpl
            if(this.moniker!=null && compareTo.moniker!=null) {
            	if (!isEqual(this.moniker, compareTo.moniker)) {
                  return false;
            	}
            }

            if (!isEqual(this.profile, compareTo.profile)) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isEqual(final String a, final String b) {
        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        }
        if ((a != null) && (!a.equals(b))) {
            return false;
        }
        return true;
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
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the profile
     */
    public String getProfile() {
        return profile;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(final String profile) {
        this.profile = profile;
    }

    /**
     * @return the username
     */
    public String getUserName() {
        return id;
    }

    /**
     * @return the email
     */
    public final String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public final void setEmail(final String email) {
        this.email = email;
    }

    /**
     * @see java.lang.String#toString()
     */
    public String toString() {
        final StringBuilder builder = new StringBuilder("");

        builder.append(firstName).append(' ').append(lastName);

        if (moniker != null) {
            builder.append(" - ").append(moniker);
        }

        builder.append(" (").append(email).append(')');

        return builder.toString();
    }

    /**
     * @return the moniker
     */
    public final String getMoniker() {
        return moniker;
    }

    /**
     * @param moniker the moniker to set
     */
    public final void setMoniker(final String moniker) {
        this.moniker = moniker;
    }
}
