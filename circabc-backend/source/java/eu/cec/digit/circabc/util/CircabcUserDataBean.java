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
package eu.cec.digit.circabc.util;

import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.ecas.client.jaas.DetailedUser;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * It is just a bean that contains all the data to be populated in Alfresco for each CIRCABC user
 *
 * @author atadian
 * @author guillaume
 */
public class CircabcUserDataBean {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(CircabcUserDataBean.class);

    // Standard data for Alfresco Users
    private String userName;

    private String firstName;

    private String lastName;

    private String email;

    private String companyId;

    private NodeRef homeSpaceNodeRef;

    // Extra data for CIRCABC users
    private String title;

    private String phone;

    private String fax;

    private String url;

    private String postalAddress;

    private String description;

    private String domain;

    private String orgdepnumber;

    private String password;

    /**
     * If the profile is fully visible for everybody
     */
    private boolean visibility;

    /**
     * If user want to get global nofitication
     */
    private boolean globalNotification = true;

    /**
     * Last time user log on
     */
    private Date lastLoginTime;

    /**
     * The last modification date of the details
     */
    private Date lastModificationDetailsTime;

    /**
     * The creation date of the user
     */
    private Date creationDate;

    /**
     * The ECAS user name
     */
    private String ecasUserName;

    private String sourceOrganisation;
    private String dg;

    /**
     * Return attributes as a Map of QName (just person data)
     *
     * @return attributes as a Map of QName
     */
    public Map<QName, Serializable> getAttributesAsMap() {
        Map<QName, Serializable> props = new HashMap<>(15,
                1.0f);
        //Mandatory parameters !!!
        props.put(ContentModel.PROP_USERNAME, this.getUserName());
        props.put(ContentModel.PROP_FIRSTNAME, this.getFirstName());
        props.put(ContentModel.PROP_LASTNAME, this.getLastName());
        if (this.getHomeSpaceNodeRef() != null) {
            props.put(ContentModel.PROP_HOMEFOLDER, this.getHomeSpaceNodeRef());
        }
        props.put(ContentModel.PROP_EMAIL, (this.getEmail() == null) ? "" : this.getEmail());
        logMissingMandatoryParameters();

        //Optional parameters
        props.put(ContentModel.PROP_ORGID, (this.getCompanyId() == null) ? "" : this.getCompanyId());

        return props;
    }

    /**
     * A bug exist in the application. In some unknown situation an null value is inserted in the
     * repository. This method should tell us from where the is generated
     */
    private void logMissingMandatoryParameters() {
        if (logger.isErrorEnabled()) {
            final StringBuilder sb = new StringBuilder();
            if (this.getUserName() == null || this.getUserName().length() == 0) {
                sb.append("This value should be defined:").append(ContentModel.PROP_USERNAME).append("\n");
            }
            if (this.getFirstName() == null || this.getFirstName().length() == 0) {
                sb.append("This value should be defined:").append(ContentModel.PROP_FIRSTNAME).append("\n");
            }
            if (this.getLastName() == null || this.getLastName().length() == 0) {
                sb.append("This value should be defined:").append(ContentModel.PROP_LASTNAME).append("\n");
            }
            if (this.getHomeSpaceNodeRef() == null) {
                //sb.append("This value should be defined:").append(ContentModel.PROP_HOMEFOLDER).append("\n");
            }
            if (this.getEmail() == null || this.getEmail().length() == 0) {
                sb.append("This value should be defined:").append(ContentModel.PROP_EMAIL).append("\n");
            }
            if (sb.length() > 0) {
                //Error found
                final RuntimeException trickToGetStackTrace = new RuntimeException("");
                logger.error("WARNING: Null value in repository found at: : \n" + sb, trickToGetStackTrace);
            }
        }
    }

    /**
     * Returns the attributes as a Map of QName (just a CIRCABC data)
     *
     * @return attributes as a Map of QName
     */
    public Map<QName, Serializable> getAspectAttributesInMap() {
        Map<QName, Serializable> props = new HashMap<>(7,
                1.0f);
        //Optional parameters
        props.put(UserModel.PROP_TITLE, (this.getTitle() == null) ? "" : this.getTitle());
        props.put(UserModel.PROP_PHONE, (this.getPhone() == null) ? "" : this.getPhone());
        props.put(UserModel.PROP_DESCRIPTION,
                (this.getDescription() == null) ? "" : this.getDescription());
        props.put(UserModel.PROP_DOMAIN, (this.getDomain() == null) ? "" : this.getDomain());
        props.put(UserModel.PROP_POSTAL_ADDRESS,
                (this.getPostalAddress() == null) ? "" : this.getPostalAddress());
        props.put(UserModel.PROP_FAX, (this.getFax() == null) ? "" : this.getFax());
        props.put(UserModel.PROP_URL, (this.getURL() == null) ? "" : this.getURL());
        props.put(UserModel.PROP_ORGDEPNUMBER,
                (this.getOrgdepnumber() == null) ? "" : this.getOrgdepnumber());
        props.put(UserModel.PROP_VISISBILITY, this.getVisibility());
        props.put(UserModel.PROP_GLOBAL_NOTIFICATION, this.getGlobalNotification());
        props.put(UserModel.PROP_LAST_LOGIN_TIME, this.getLastLoginTime());
        props.put(UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME, this.getLastModificationDetailsTime());
        props.put(UserModel.PROP_CREATION_DATE, this.getCreationDate());
        props.put(UserModel.PROP_ECAS_USER_NAME, this.getEcasUserName());

        return props;

    }

    /**
     * Returns all the attributes of the bean in the Map
     *
     * @return all attributes of the bean
     */
    public Map<QName, Serializable> getAllAttributesInMap() {
        Map<QName, Serializable> all = this.getAttributesAsMap();
        all.putAll(getAspectAttributesInMap());
        return all;
    }

    /**
     * Populates the bean from the Map
     *
     * @param pProps Map with all Circabc User Data
     */
    public void populateIt(final Map<QName, Serializable> pProps) {
        //Mandatory parameters !!!
        this.setUserName(pProps.get(ContentModel.PROP_USERNAME).toString());
        this.setFirstName(pProps.get(ContentModel.PROP_FIRSTNAME).toString());
        this.setLastName(pProps.get(ContentModel.PROP_LASTNAME).toString());
        this.setHomeSpaceNodeRef((NodeRef) pProps.get(ContentModel.PROP_HOMEFOLDER));
        this.setEmail(pProps.get(ContentModel.PROP_EMAIL).toString());
        this.setCompanyId(pProps.get(ContentModel.PROP_ORGID).toString());
        logMissingMandatoryParameters();

        //Optional parameters !!!
        this.setDescription(pProps.get(UserModel.PROP_DESCRIPTION).toString());
        this.setDomain(pProps.get(UserModel.PROP_DOMAIN).toString());
        this.setFax(pProps.get(UserModel.PROP_FAX).toString());
        this.setOrgdepnumber(pProps.get(UserModel.PROP_ORGDEPNUMBER).toString());
        this.setPhone(pProps.get(UserModel.PROP_PHONE).toString());
        this.setPostalAddress(pProps.get(UserModel.PROP_POSTAL_ADDRESS).toString());
        this.setTitle(pProps.get(UserModel.PROP_TITLE).toString());
        this.setURL(pProps.get(UserModel.PROP_URL).toString());
        this.setVisibility((Boolean) pProps.get(UserModel.PROP_VISISBILITY));
        this.setGlobalNotification((Boolean) pProps.get(UserModel.PROP_GLOBAL_NOTIFICATION));
        this.setLastLoginTime((Date) pProps.get(UserModel.PROP_LAST_LOGIN_TIME));
        this.setLastModificationDetailsTime(
                (Date) pProps.get(UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME));
        this.setCreationDate((Date) pProps.get(UserModel.PROP_CREATION_DATE));

    }

    public void copyLdapProperties(final CircabcUserDataBean circabcUserDataBean) {
        if (circabcUserDataBean != null) {
            //Mandatory parameters !!!
            this.userName = circabcUserDataBean.getUserName();
            this.ecasUserName = circabcUserDataBean.getEcasUserName();
            this.firstName = circabcUserDataBean.getFirstName();
            this.lastName = circabcUserDataBean.getLastName();
            this.email = circabcUserDataBean.getEmail();
            logMissingMandatoryParameters();

            //Optional parameters

            this.title = circabcUserDataBean.getTitle();
            this.orgdepnumber = circabcUserDataBean.getOrgdepnumber();
            this.phone = circabcUserDataBean.getPhone();
            this.description = circabcUserDataBean.getDescription();
            this.fax = circabcUserDataBean.getFax();
            this.postalAddress = circabcUserDataBean.getPostalAddress();
            this.domain = circabcUserDataBean.getDomain();

        }

    }

    /**
     * The string version of this class
     *
     * @return string version of this class
     */
    public String toString() {
        String sb = "[" +
                getUserName() +
                "|" +
                getLastName() +
                "|" +
                getEmail() +
                "|" +
                getPhone() +
                "|" +
                getDomain() +
                "]";

        return sb;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(final String companyId) {
        this.companyId = companyId;
    }

    /**
     * Getter for creation Date
     *
     * @return Date The creation Date
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Setter for creation Date
     *
     * @param creationDate The creation Date to set
     */
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(final String fax) {
        this.fax = fax;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * Getter for global notification state
     *
     * @return boolean The global notification state
     */
    public boolean getGlobalNotification() {
        return this.globalNotification;
    }

    /**
     * Setter for global notification state
     *
     * @param globalNotification The global notification state to set
     */
    public void setGlobalNotification(final boolean globalNotification) {
        this.globalNotification = globalNotification;
    }

    public NodeRef getHomeSpaceNodeRef() {
        return homeSpaceNodeRef;
    }

    public void setHomeSpaceNodeRef(final NodeRef homeSpaceNodeRef) {
        this.homeSpaceNodeRef = homeSpaceNodeRef;
    }

    /**
     * Getter for last login time
     *
     * @return Date The last login time
     */
    public Date getLastLoginTime() {
        return this.lastLoginTime;
    }

    /**
     * Setter for last login time
     *
     * @param lastLoginTime The last login time to set
     */
    public void setLastLoginTime(final Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * Getter for last modification details time
     *
     * @return Date The last modification details time
     */
    public Date getLastModificationDetailsTime() {
        return this.lastModificationDetailsTime;
    }

    /**
     * Setter for last modification details time
     *
     * @param lastModificationDetailsTime The last modification details time to set
     */
    public void setLastModificationDetailsTime(final Date lastModificationDetailsTime) {
        this.lastModificationDetailsTime = lastModificationDetailsTime;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(final String postalAddress) {
        this.postalAddress = postalAddress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Getter for visibility state
     *
     * @return boolean The visibility state
     */
    public boolean getVisibility() {
        return this.visibility;
    }

    /**
     * Setter for visibility state
     *
     * @param visibility The visibility state to set
     */
    public void setVisibility(final boolean visibility) {
        this.visibility = visibility;
    }

    public String getURL() {
        return url;
    }

    public void setURL(final String value) {
        url = value;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getOrgdepnumber() {
        return orgdepnumber;
    }

    public void setOrgdepnumber(final String orgdepnumber) {
        this.orgdepnumber = orgdepnumber;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return the ecasUserName - also know as moniker
     */
    public String getEcasUserName() {
        return ecasUserName;
    }

    /**
     * @param ecasUserName the ecasUserName to set
     */
    public void setEcasUserName(final String ecasUserName) {
        this.ecasUserName = ecasUserName;
    }

    /**
     * @return the sourceOrganisation
     */
    public String getSourceOrganisation() {
        return sourceOrganisation;
    }

    /**
     * @param sourceOrganisation the sourceOrganisation to set
     */
    public void setSourceOrganisation(String sourceOrganisation) {
        this.sourceOrganisation = sourceOrganisation;
    }

    public void copyDetailedUserProperties(DetailedUser detailedUser) {
        //Mandatory parameters !!!

        if (detailedUser != null) {
            if (detailedUser.getDomainUsername() != null) {
                this.ecasUserName = detailedUser.getDomainUsername();
            }
            if (detailedUser.getFirstName() != null) {
                this.firstName = detailedUser.getFirstName();
            }
            if (detailedUser.getLastName() != null) {
                this.lastName = detailedUser.getLastName();
            }
            if (detailedUser.getEmail() != null) {
                this.email = detailedUser.getEmail();
            }
            logMissingMandatoryParameters();

            //Optional parameters

            if (detailedUser.getDepartmentNumber() != null) {
                this.orgdepnumber = detailedUser.getDepartmentNumber();
            }
            if (detailedUser.getTelephoneNumber() != null) {
                this.phone = detailedUser.getTelephoneNumber();
            }
            if (detailedUser.getDomain() != null) {
                this.domain = detailedUser.getDomain();
            }
        }

    }

    /**
     * @return the dg
     */
    public String getDg() {
        return dg;
    }

    /**
     * @param dg the dg to set
     */
    public void setDg(String dg) {
        this.dg = dg;
    }

}
