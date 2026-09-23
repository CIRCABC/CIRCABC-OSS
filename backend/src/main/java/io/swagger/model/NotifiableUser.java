package io.swagger.model;

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

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Contract exposing the details of a user who is a candidate to receive a notification.
 *
 * <p>Implementations act as a lightweight, read-only view over an Alfresco person, providing the
 * identity, contact information, preferred locale and repository reference required by the
 * notification machinery (for example when composing and sending e-mail alerts). It decouples the
 * notification logic from the underlying person representation.
 *
 * @author Yanick Pignot
 */
public interface NotifiableUser {
  /**
   * Returns the account name (login) of the person to be notified.
   *
   * @return the username of the notifiable user
   */
  String getUserName();

  /**
   * Returns the family (last) name of the person to be notified.
   *
   * @return the last name of the notifiable user
   */
  String getLastName();

  /**
   * Returns the given (first) name of the person to be notified.
   *
   * @return the first name of the notifiable user
   */
  String getFirstName();

  /**
   * Returns the e-mail address used to deliver notifications to the person.
   *
   * @return the e-mail address of the notifiable user
   */
  String getEmailAddress();

  /**
   * Returns the raw Alfresco person properties, keyed by their {@link QName}.
   *
   * @return a map of the user's properties indexed by qualified name
   */
  Map<QName, Serializable> getUserProperties();

  /**
   * Returns the locale in which the user wishes to receive notifications, used to localise the
   * notification content.
   *
   * @return the preferred notification language of the user
   */
  Locale getNotificationLanguage();

  /**
   * Returns the reference to the Alfresco person node backing this notifiable user.
   *
   * @return the {@link NodeRef} of the person to be notified
   */
  NodeRef getPerson();
}
