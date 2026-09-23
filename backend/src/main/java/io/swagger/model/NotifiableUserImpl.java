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
package io.swagger.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Default implementation of {@link NotifiableUser}, representing a user who can be notified
 * (for example, by email) about repository events.
 *
 * <p>An instance is built from the Alfresco person node and its property map. The commonly used
 * user attributes (username, first name, last name, email) are extracted eagerly from the property
 * map during construction, while the full property map and preferred notification locale are
 * retained for later use.
 *
 * @author Yanick Pignot
 */
public class NotifiableUserImpl implements NotifiableUser {

  /** The user's login/account name ({@link ContentModel#PROP_USERNAME}). */
  private String userName;

  /** The user's last name ({@link ContentModel#PROP_LASTNAME}). */
  private String lastName;

  /** The user's first name ({@link ContentModel#PROP_FIRSTNAME}). */
  private String firstName;

  /** The user's email address ({@link ContentModel#PROP_EMAIL}), used as the notification target. */
  private String email;

  /** Reference to the Alfresco person node this user is derived from. */
  private NodeRef person;

  /** The user's preferred locale, used to localize notification content. */
  private Locale locale;

  /** The full set of properties of the person node, keyed by property {@link QName}. */
  private Map<QName, Serializable> properties;

  /**
   * Creates a notifiable user from a person node and its properties. The username, first name, last
   * name and email fields are populated from the supplied property map.
   *
   * @param person the Alfresco person node representing the user
   * @param locale the user's preferred locale for notifications
   * @param properties the person node's properties, keyed by property {@link QName}
   */
  public NotifiableUserImpl(
    NodeRef person,
    Locale locale,
    Map<QName, Serializable> properties
  ) {
    super();
    this.person = person;
    this.properties = properties;
    this.locale = locale;
    userName = (String) properties.get(ContentModel.PROP_USERNAME);
    lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
    firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
    email = (String) properties.get(ContentModel.PROP_EMAIL);
  }

  /**
   * Returns the user's email address, used as the notification target.
   *
   * @return the email address
   */
  public final String getEmailAddress() {
    return email;
  }

  /**
   * Returns the user's first name.
   *
   * @return the first name
   */
  public final String getFirstName() {
    return firstName;
  }

  /**
   * Returns the user's last name.
   *
   * @return the last name
   */
  public final String getLastName() {
    return lastName;
  }

  /**
   * Returns the Alfresco person node this user is derived from.
   *
   * @return the person node reference
   */
  public final NodeRef getPerson() {
    return person;
  }

  /**
   * Returns the user's login/account name.
   *
   * @return the username
   */
  public final String getUserName() {
    return userName;
  }

  /**
   * Returns the full set of properties of the person node.
   *
   * @return the property map, keyed by property {@link QName}
   */
  public Map<QName, Serializable> getUserProperties() {
    return properties;
  }

  /**
   * Returns the user's preferred locale for notifications.
   *
   * @return the notification locale
   */
  public Locale getNotificationLanguage() {
    return locale;
  }

  /**
   * Returns a string representation of this user in the form {@code userName(email)}.
   *
   * @return a human-readable representation combining username and email
   */
  @Override
  public String toString() {
    return userName + '(' + email + ')';
  }
}
