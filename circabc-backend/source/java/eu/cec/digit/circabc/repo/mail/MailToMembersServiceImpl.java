/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.cec.digit.circabc.repo.mail;

import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.mail.MailToMembersService;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.MessagingException;
import java.util.Set;

/** @author clincst */
public class MailToMembersServiceImpl implements MailToMembersService {

    private static final Log logger = LogFactory.getLog(MailToMembersServiceImpl.class);

    private MailService mailService;

    private ProfileManagerServiceFactory profileManagerServiceFactory;

    public boolean sendToAllMembers(
            final NodeRef nodeRef,
            final String from,
            final String to,
            final String subject,
            final String body,
            final boolean html)
            throws MessagingException {

        String noReply = mailService.getNoReplyEmailAddress();

        final ProfileManagerService profileManagerService =
                profileManagerServiceFactory.getProfileManagerService(nodeRef);
        if (profileManagerService != null) {
            final Set<String> users = profileManagerService.getMasterUsers(nodeRef);
            for (final String user : users) {
                logger.debug("User:" + user);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Send email to: " + to + "\n...with subject:\n" + subject + "\n...with body:\n" + body);
            }

            return mailService.send(noReply, to, from, subject, body, html, false);
        } else {
            return false;
        }
    }

    public MailService getMailService() {
        return mailService;
    }

    public void setMailService(final MailService mailService) {
        this.mailService = mailService;
    }

    public ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return profileManagerServiceFactory;
    }

    public void setProfileManagerServiceFactory(
            final ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }
}
