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
package eu.cec.digit.circabc.service.notification;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * The interface used to support reporting back a notifiable user.
 *
 * @author Yanick Pignot
 */
public interface NotifiableUser {

    /**
     * Get the username of the person that will be notified
     */
    String getUserName();

    /**
     * Get the user last name of the person that will be notified
     */
    String getLastName();

    /**
     * Get the user last name of the person that will be notified
     */
    String getFirstName();

    /**
     * Get the user email address of the person that will be notified
     */
    String getEmailAddress();

    /**
     * Get the user email properties of the person that will be notified
     */
    Map<QName, Serializable> getUserProperties();

    /**
     * Get the language in which the user want to be notified
     */
    Locale getNotificationLanguage();

    /**
     * Get the noderef representation of the user that will be notified
     */
    NodeRef getPerson();
}
