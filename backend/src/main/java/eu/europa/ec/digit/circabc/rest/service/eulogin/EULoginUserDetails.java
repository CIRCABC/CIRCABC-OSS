package eu.europa.ec.digit.circabc.rest.service.eulogin;

/**
 * DTO holding user details extracted from the ECAS CAS assertion.
 *
 * <p>These attributes are returned by the ECAS {@code laxValidate} endpoint when
 * {@code userDetails=true} is requested. They can be used as a fallback for user
 * creation when LDAP is unavailable.
 */
public class EULoginUserDetails {

  /** Immutable EU Login (ECAS) account name identifying the user. */
  private final String username;

  /** Primary e-mail address of the user. */
  private String email;

  /** Given (first) name of the user. */
  private String firstName;

  /** Family (last) name of the user. */
  private String lastName;

  /** EU Login domain the user authenticated against (e.g. {@code eu.europa.ec}, {@code external}). */
  private String domain;

  /** Department or organisational unit number the user belongs to. */
  private String departmentNumber;

  /** Contact telephone number of the user. */
  private String telephoneNumber;

  /** Short display alias (moniker) associated with the user. */
  private String moniker;

  /** Preferred locale of the user (e.g. {@code en}, {@code fr}). */
  private String locale;

  /** Identifier of the organisation the user is affiliated with. */
  private String orgId;

  /**
   * Creates a new holder for the given EU Login username.
   *
   * @param username the EU Login (ECAS) account name; used as the immutable identity of this DTO
   */
  public EULoginUserDetails(String username) {
    this.username = username;
  }

  /**
   * Returns the EU Login account name identifying the user.
   *
   * @return the username, never modified after construction
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the primary e-mail address of the user.
   *
   * @return the e-mail address, or {@code null} if not provided
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the primary e-mail address of the user.
   *
   * @param email the e-mail address to store
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the given (first) name of the user.
   *
   * @return the first name, or {@code null} if not provided
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the given (first) name of the user.
   *
   * @param firstName the first name to store
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Returns the family (last) name of the user.
   *
   * @return the last name, or {@code null} if not provided
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the family (last) name of the user.
   *
   * @param lastName the last name to store
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Returns the EU Login domain the user authenticated against.
   *
   * @return the domain, or {@code null} if not provided
   */
  public String getDomain() {
    return domain;
  }

  /**
   * Sets the EU Login domain the user authenticated against.
   *
   * @param domain the domain to store
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * Returns the department or organisational unit number of the user.
   *
   * @return the department number, or {@code null} if not provided
   */
  public String getDepartmentNumber() {
    return departmentNumber;
  }

  /**
   * Sets the department or organisational unit number of the user.
   *
   * @param departmentNumber the department number to store
   */
  public void setDepartmentNumber(String departmentNumber) {
    this.departmentNumber = departmentNumber;
  }

  /**
   * Returns the contact telephone number of the user.
   *
   * @return the telephone number, or {@code null} if not provided
   */
  public String getTelephoneNumber() {
    return telephoneNumber;
  }

  /**
   * Sets the contact telephone number of the user.
   *
   * @param telephoneNumber the telephone number to store
   */
  public void setTelephoneNumber(String telephoneNumber) {
    this.telephoneNumber = telephoneNumber;
  }

  /**
   * Returns the short display alias (moniker) of the user.
   *
   * @return the moniker, or {@code null} if not provided
   */
  public String getMoniker() {
    return moniker;
  }

  /**
   * Sets the short display alias (moniker) of the user.
   *
   * @param moniker the moniker to store
   */
  public void setMoniker(String moniker) {
    this.moniker = moniker;
  }

  /**
   * Returns the preferred locale of the user.
   *
   * @return the locale, or {@code null} if not provided
   */
  public String getLocale() {
    return locale;
  }

  /**
   * Sets the preferred locale of the user.
   *
   * @param locale the locale to store
   */
  public void setLocale(String locale) {
    this.locale = locale;
  }

  /**
   * Returns the identifier of the organisation the user is affiliated with.
   *
   * @return the organisation id, or {@code null} if not provided
   */
  public String getOrgId() {
    return orgId;
  }

  /**
   * Sets the identifier of the organisation the user is affiliated with.
   *
   * @param orgId the organisation id to store
   */
  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }
}
