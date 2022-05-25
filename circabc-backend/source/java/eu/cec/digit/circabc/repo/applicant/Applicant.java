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
package eu.cec.digit.circabc.repo.applicant;

import eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv;
import eu.cec.digit.circabc.web.Services;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * POJO that represents a applicant
 *
 * @author Yanick Pignot
 */
public class Applicant implements Serializable {

    private static final long serialVersionUID = 1L;

    private String lastName;
    private String firstName;
    private String userName;
    private Date date;
    private String message;

    /**
     * Ininitialize an applicant with all parameters
     *
     * @param userName  the mandatory username of the applicant which resquest to be invited to the
     *                  interest group
     * @param date      the mandatory date of the application
     * @param message   the message of the applicant sent to the interest groups dir admins
     * @param firstName the first name of the applicant
     * @param lastName  the last name of the applicant
     */
    public Applicant(
            final String userName,
            final Date date,
            final String message,
            final String firstName,
            final String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.date = date;
        this.message = message;
    }

    /**
     * Ininitialize an applicant without the optional first name and last name
     *
     * @param userName the mandatory username of the applicant which resquest to be invited to the
     *                 interest group
     * @param date     the mandatory date of the application
     * @param message  the message of the applicant
     */
    public Applicant(final String userName, final Date date, final String message) {
        this(userName, date, message, null, null);
    }

    /**
     * Ininitialize an applicant without the optional names of the applicant and the optional message
     *
     * @param userName the mandatory username of the applicant which resquest to be invited to the
     *                 interest group
     * @param date     the mandatory date of the application
     */
    public Applicant(final String userName, final Date date) {
        this(userName, date, null);
    }

    /**
     * @return the date of the application
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date of the application the date to set
     */
    public void setDate(final Date date) {
        this.date = date;
    }

    /**
     * @return the message of the applicant sent to the ig dir admins
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message of the applicant sent to the ig dir admins to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * @return the name of the applicant
     */
    public String getDisplayName() {
        String displayName = "";
        displayName += (this.firstName == null) ? "" : this.firstName + " ";
        displayName += (this.lastName == null) ? "" : this.lastName;

        displayName = displayName.trim();

        // if it is impossible to set a display name, get the user name
        if (displayName.length() < 1) {
            displayName = userName;
        }

        return this.firstName + " " + this.lastName;
    }

    /**
     * @return the userName of the applicant
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName of the applicant to set
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        final UserDetailsBusinessSrv userDetServ =
                Services.getBusinessRegistry(FacesContext.getCurrentInstance()).getUserDetailsBusinessSrv();
        return userDetServ.getUserDetails(this.userName).getEmail();
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(getDate());

        String sb =
                "Application details: "
                        + "Date="
                        + cal.get(Calendar.YEAR)
                        + "/"
                        + cal.get(Calendar.MONTH)
                        + "/"
                        + cal.get(Calendar.DAY_OF_MONTH)
                        + "|User="
                        + getUserName()
                        + "|Message="
                        + getMessage();

        return sb;
    }
}
