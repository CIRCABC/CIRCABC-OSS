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
package eu.cec.digit.circabc.business.api.mail;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business service to sent a content using email.
 *
 * @author patrice.coppens@trasys.lu
 */
public interface MailMeContentBusinessSrv {

    /**
     * Send a nodeRef in email.
     *
     * @param contentRef NodeRef is any node.
     * @return true if the node is successfully sent
     */
    boolean send(final NodeRef anyRef, boolean attachContent);

    /**
     * Send a nodeRef by email to the given user.
     *
     * @param contentRef NodeRef is any node.
     * @param userId     id of the user.
     * @return true if the node is successfully sent
     */
    boolean send(final NodeRef anyRef, String userId, boolean attachContent);

}
