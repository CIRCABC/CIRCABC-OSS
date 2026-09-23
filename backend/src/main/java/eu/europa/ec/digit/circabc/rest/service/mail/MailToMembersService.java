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
package eu.europa.ec.digit.circabc.rest.service.mail;

import jakarta.mail.MessagingException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for broadcasting an e-mail to all members associated with a given CIRCABC node
 * (typically an Interest Group or one of its services).
 *
 * <p>Implementations are responsible for resolving the set of member recipients for the supplied
 * node and dispatching the message to each of them.
 */
public interface MailToMembersService {
  /**
   * Sends an e-mail to all members associated with the given node.
   *
   * @param nodeRef the reference of the node whose members should receive the message
   * @param from the sender address of the e-mail
   * @param to the primary recipient address (e.g. the address shown in the {@code To} field)
   * @param subject the subject line of the e-mail
   * @param body the body content of the e-mail
   * @param html {@code true} if the body should be sent as HTML, {@code false} for plain text
   * @return {@code true} if the message was successfully sent to the members, {@code false}
   *     otherwise
   * @throws MessagingException if an error occurs while composing or sending the message
   */
  boolean sendToAllMembers(
    final NodeRef nodeRef,
    final String from,
    final String to,
    final String subject,
    final String body,
    final boolean html
  ) throws MessagingException;
}
