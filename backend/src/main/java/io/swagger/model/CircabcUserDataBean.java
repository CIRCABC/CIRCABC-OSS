package io.swagger.model;

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

import io.swagger.model.alfresco.UserModel;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Data-transfer bean that aggregates all the attributes describing a single CIRCABC user.
 *
 * <p>It carries both the standard Alfresco person properties (user name, first/last name, email,
 * home space, company id) and the CIRCABC-specific extension properties (title, phone, fax, url,
 * postal address, description, domain, visibility, notification preferences, ECAS user name,
 * timestamps, etc.).
 *
 * <p>The bean acts as a bridge between the strongly typed Java model and the Alfresco repository
 * representation: it can convert its state to/from a {@link java.util.Map} of {@link QName}
 * properties (see {@link #getAttributesAsMap()}, {@link #getAspectAttributesInMap()},
 * {@link #getAllAttributesInMap()} and {@link #populateIt(Map)}) as expected by the Alfresco
 * node service and CIRCABC user aspect.
 *
 * @author atadian
 * @author guillaume
 */
public class CircabcUserDataBean {

  /**
   * Message prefix used when logging a mandatory property that was found to be missing or empty.
   */
  private static final String THIS_VALUE_SHOULD_BE_DEFINED =
    "This value should be defined:";

  /**
   * Logger
   */
  private static final Log logger = LogFactory.getLog(
    CircabcUserDataBean.class
  );

  // Standard data for Alfresco Users

  /** The unique login name of the user (Alfresco {@code cm:userName}). */
  private String userName;

  /** The user's first name (Alfresco {@code cm:firstName}). */
  private String firstName;

  /** The user's last name (Alfresco {@code cm:lastName}). */
  private String lastName;

  /** The user's email address (Alfresco {@code cm:email}). */
  private String email;

  /** The identifier of the user's organisation/company (Alfresco {@code cm:organizationId}). */
  private String companyId;

  /** Reference to the user's personal home space folder (Alfresco {@code cm:homeFolder}). */
  private NodeRef homeSpaceNodeRef;

  // Extra data for CIRCABC users

  /** The user's title (e.g. Mr, Mrs, Dr). */
  private String title;

  /** The user's phone number. */
  private String phone;

  /** The user's fax number. */
  private String fax;

  /** The user's personal or professional web address. */
  private String url;

  /** The user's postal address. */
  private String postalAddress;

  /** A free-text description of the user. */
  private String description;

  /** The organisational domain the user belongs to. */
  private String domain;

  /** The organisation/department number of the user. */
  private String orgdepnumber;

  /** The user's password (only populated when creating/updating credentials). */
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
   * The ECAS (EU Login) user name, also known as the moniker.
   */
  private String ecasUserName;

  /** The source organisation the user's data originates from. */
  private String sourceOrganisation;

  /** The Directorate-General (DG) the user is affiliated with. */
  private String dg;

  /**
   * Return attributes as a Map of QName (just person data)
   *
   * @return attributes as a Map of QName
   */
  public Map<QName, Serializable> getAttributesAsMap() {
    Map<QName, Serializable> props = new HashMap<>(15, 1.0f);
    //Mandatory parameters !!!
    props.put(ContentModel.PROP_USERNAME, this.getUserName());
    props.put(ContentModel.PROP_FIRSTNAME, this.getFirstName());
    props.put(ContentModel.PROP_LASTNAME, this.getLastName());
    if (this.getHomeSpaceNodeRef() != null) {
      props.put(ContentModel.PROP_HOMEFOLDER, this.getHomeSpaceNodeRef());
    }
    props.put(
      ContentModel.PROP_EMAIL,
      (this.getEmail() == null) ? "" : this.getEmail()
    );
    logMissingMandatoryParameters();

    //Optional parameters
    props.put(
      ContentModel.PROP_ORGID,
      (this.getCompanyId() == null) ? "" : this.getCompanyId()
    );

    return props;
  }

  /**
   * A bug exist in the application. In some unknown situation an null value is inserted in the
   * repository. This method should tell us from where the is generated
   */
  private void logMissingMandatoryParameters() {
    if (logger.isErrorEnabled()) {
      final StringBuilder sb = new StringBuilder();
      if (this.getUserName() == null || this.getUserName().isEmpty()) {
        sb
          .append(THIS_VALUE_SHOULD_BE_DEFINED)
          .append(ContentModel.PROP_USERNAME)
          .append("\n");
      }
      if (this.getFirstName() == null || this.getFirstName().isEmpty()) {
        sb
          .append(THIS_VALUE_SHOULD_BE_DEFINED)
          .append(ContentModel.PROP_FIRSTNAME)
          .append("\n");
      }
      if (this.getLastName() == null || this.getLastName().isEmpty()) {
        sb
          .append(THIS_VALUE_SHOULD_BE_DEFINED)
          .append(ContentModel.PROP_LASTNAME)
          .append("\n");
      }

      if (this.getEmail() == null || this.getEmail().isEmpty()) {
        sb
          .append(THIS_VALUE_SHOULD_BE_DEFINED)
          .append(ContentModel.PROP_EMAIL)
          .append("\n");
      }
      if (!sb.isEmpty()) {
        //Error found
        final RuntimeException trickToGetStackTrace = new RuntimeException("");
        logger.error(
          "WARNING: Null value in repository found at: : \n" + sb,
          trickToGetStackTrace
        );
      }
    }
  }

  /**
   * Returns the attributes as a Map of QName (just a CIRCABC data)
   *
   * @return attributes as a Map of QName
   */
  public Map<QName, Serializable> getAspectAttributesInMap() {
    Map<QName, Serializable> props = new HashMap<>(7, 1.0f);
    //Optional parameters
    props.put(
      UserModel.PROP_TITLE,
      (this.getTitle() == null) ? "" : this.getTitle()
    );
    props.put(
      UserModel.PROP_PHONE,
      (this.getPhone() == null) ? "" : this.getPhone()
    );
    props.put(
      UserModel.PROP_DESCRIPTION,
      (this.getDescription() == null) ? "" : this.getDescription()
    );
    props.put(
      UserModel.PROP_DOMAIN,
      (this.getDomain() == null) ? "" : this.getDomain()
    );
    props.put(
      UserModel.PROP_POSTAL_ADDRESS,
      (this.getPostalAddress() == null) ? "" : this.getPostalAddress()
    );
    props.put(UserModel.PROP_FAX, (this.getFax() == null) ? "" : this.getFax());
    props.put(UserModel.PROP_URL, (this.getURL() == null) ? "" : this.getURL());
    props.put(
      UserModel.PROP_ORGDEPNUMBER,
      (this.getOrgdepnumber() == null) ? "" : this.getOrgdepnumber()
    );
    props.put(UserModel.PROP_VISISBILITY, this.getVisibility());
    props.put(UserModel.PROP_GLOBAL_NOTIFICATION, this.getGlobalNotification());
    props.put(UserModel.PROP_LAST_LOGIN_TIME, this.getLastLoginTime());
    props.put(
      UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME,
      this.getLastModificationDetailsTime()
    );
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
    this.setHomeSpaceNodeRef(
      (NodeRef) pProps.get(ContentModel.PROP_HOMEFOLDER)
    );
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
    this.setGlobalNotification(
      (Boolean) pProps.get(UserModel.PROP_GLOBAL_NOTIFICATION)
    );
    this.setLastLoginTime((Date) pProps.get(UserModel.PROP_LAST_LOGIN_TIME));
    this.setLastModificationDetailsTime(
      (Date) pProps.get(UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME)
    );
    this.setCreationDate((Date) pProps.get(UserModel.PROP_CREATION_DATE));
  }

  /**
   * Copies the LDAP-sourced properties from the given bean into this instance.
   *
   * <p>Both the mandatory person attributes (user name, ECAS user name, first/last name, email)
   * and the optional CIRCABC attributes (title, department number, phone, description, fax,
   * postal address, domain) are copied. If the supplied bean is {@code null} the current state is
   * left unchanged.
   *
   * @param circabcUserDataBean the source bean to copy the LDAP properties from; may be
   *     {@code null}
   */
  public void copyLdapProperties(
    final CircabcUserDataBean circabcUserDataBean
  ) {
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
    return (
      "[" +
      getUserName() +
      "|" +
      getLastName() +
      "|" +
      getEmail() +
      "|" +
      getPhone() +
      "|" +
      getDomain() +
      "]"
    );
  }

  /**
   * Returns the identifier of the user's organisation/company.
   *
   * @return the company id
   */
  public String getCompanyId() {
    return companyId;
  }

  /**
   * Sets the identifier of the user's organisation/company.
   *
   * @param companyId the company id to set
   */
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

  /**
   * Returns the free-text description of the user.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the free-text description of the user.
   *
   * @param description the description to set
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Returns the organisational domain the user belongs to.
   *
   * @return the domain
   */
  public String getDomain() {
    return domain;
  }

  /**
   * Sets the organisational domain the user belongs to.
   *
   * @param domain the domain to set
   */
  public void setDomain(final String domain) {
    this.domain = domain;
  }

  /**
   * Returns the user's email address.
   *
   * @return the email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the user's email address.
   *
   * @param email the email address to set
   */
  public void setEmail(final String email) {
    this.email = email;
  }

  /**
   * Returns the user's fax number.
   *
   * @return the fax number
   */
  public String getFax() {
    return fax;
  }

  /**
   * Sets the user's fax number.
   *
   * @param fax the fax number to set
   */
  public void setFax(final String fax) {
    this.fax = fax;
  }

  /**
   * Returns the user's first name.
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the user's first name.
   *
   * @param firstName the first name to set
   */
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

  /**
   * Returns the reference to the user's personal home space folder.
   *
   * @return the home space {@link NodeRef}
   */
  public NodeRef getHomeSpaceNodeRef() {
    return homeSpaceNodeRef;
  }

  /**
   * Sets the reference to the user's personal home space folder.
   *
   * @param homeSpaceNodeRef the home space {@link NodeRef} to set
   */
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
  public void setLastModificationDetailsTime(
    final Date lastModificationDetailsTime
  ) {
    this.lastModificationDetailsTime = lastModificationDetailsTime;
  }

  /**
   * Returns the user's last name.
   *
   * @return the last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the user's last name.
   *
   * @param lastName the last name to set
   */
  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  /**
   * Returns the user's phone number.
   *
   * @return the phone number
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Sets the user's phone number.
   *
   * @param phone the phone number to set
   */
  public void setPhone(final String phone) {
    this.phone = phone;
  }

  /**
   * Returns the user's postal address.
   *
   * @return the postal address
   */
  public String getPostalAddress() {
    return postalAddress;
  }

  /**
   * Sets the user's postal address.
   *
   * @param postalAddress the postal address to set
   */
  public void setPostalAddress(final String postalAddress) {
    this.postalAddress = postalAddress;
  }

  /**
   * Returns the user's title (e.g. Mr, Mrs, Dr).
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the user's title (e.g. Mr, Mrs, Dr).
   *
   * @param title the title to set
   */
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

  /**
   * Returns the user's personal or professional web address.
   *
   * @return the URL
   */
  public String getURL() {
    return url;
  }

  /**
   * Sets the user's personal or professional web address.
   *
   * @param value the URL to set
   */
  public void setURL(final String value) {
    url = value;
  }

  /**
   * Returns the unique login name of the user.
   *
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the unique login name of the user.
   *
   * @param userName the user name to set
   */
  public void setUserName(final String userName) {
    this.userName = userName;
  }

  /**
   * Returns the organisation/department number of the user.
   *
   * @return the organisation/department number
   */
  public String getOrgdepnumber() {
    return orgdepnumber;
  }

  /**
   * Sets the organisation/department number of the user.
   *
   * @param orgdepnumber the organisation/department number to set
   */
  public void setOrgdepnumber(final String orgdepnumber) {
    this.orgdepnumber = orgdepnumber;
  }

  /**
   * Returns the user's password.
   *
   * @return the password
   */
  public String getPassword() {
    return this.password;
  }

  /**
   * Sets the user's password.
   *
   * @param password the password to set
   */
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
