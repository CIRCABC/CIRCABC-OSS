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
package eu.cec.digit.circabc.web.wai.dialog.forums;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.newsgroup.AbuseReport;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.*;

/**
 * Base bean that back common moderation operation like approve, reject and signal an abuse
 *
 * @author Yanick Pignot
 */
public class ModerationDialog extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = 7926013769552414836L;

    private static final Log logger = LogFactory.getLog(ModerationDialog.class);
    private static final String MSG_APPROVED_SUCCESS = "post_moderation_approve_success";
    private static final String MSG_APPROVED_FAILURE = "post_moderation_approve_failure";
    private static final String MSG_REMOVED_SUCCESS = "post_moderation_remove_abuse_success";
    private static final String MSG_REMOVED_FAILURE = "post_moderation_remove_abuse_failure";
    /**
     * The contentService reference
     */
    private transient ModerationService moderationService;
    private transient PersonService personService;

    // ------------------------------------------------------------------------------
    // Wizard implementation

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id parameter is mandatory");
        }
    }

    public void acceptDirect(ActionEvent event) {
        final UIActionLink link = (UIActionLink) event.getComponent();
        final Map<String, String> parameters = link.getParameterMap();

        this.init(parameters);

        try {
            approve();

            // log the records
            this.doPostCommitProcessing(FacesContext.getCurrentInstance(), "");

            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_APPROVED_SUCCESS));
        } catch (final Exception e) {
            // log the records
            this.getErrorOutcome(e);

            Utils.addErrorMessage(translate(MSG_APPROVED_FAILURE, e.getMessage()), e);
        }

        //refresh and click
        getBrowseBean().refreshBrowsing();
    }

    public void notAbuseDirect(ActionEvent event) {
        final UIActionLink link = (UIActionLink) event.getComponent();
        final Map<String, String> parameters = link.getParameterMap();

        this.init(parameters);

        try {
            signalNotAbuse();

            // log the records
            this.doPostCommitProcessing(FacesContext.getCurrentInstance(), "");

            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_REMOVED_SUCCESS));
        } catch (final Exception e) {
            // log the records
            this.getErrorOutcome(e);

            Utils.addErrorMessage(translate(MSG_REMOVED_FAILURE, e.getMessage()), e);
        }

        //refresh and click
        getBrowseBean().refreshBrowsing();
    }

    protected void approve() throws Exception {
        final NodeRef nodeRef = getActionNode().getNodeRef();
        getModerationService().accept(nodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug("Node successfully approved: " + nodeRef);
        }
    }

    protected void reject(final String message) throws Exception {
        final NodeRef nodeRef = getActionNode().getNodeRef();

        // get the content for mailing before it is versionned.
        final String oldContent = getContent();

        getModerationService().reject(nodeRef, message);

        if (logger.isDebugEnabled()) {
            logger.debug("Node successfully reject: " + nodeRef + ". With message: " + message);
        }

        final String creator = (String) getActionNode().getProperties()
                .get(ContentModel.PROP_CREATOR.toString());

        if (personService.personExists(creator)) {
            //	the properties are not refreshed in ActionNodeYet
            final Map<QName, Serializable> props = getNodeService().getProperties(nodeRef);
            final String moderator = (String) props.get(ModerationModel.PROP_REJECT_BY);
            final Date moderated = (Date) props.get(ModerationModel.PROP_REJECT_ON);
            final String reason = (String) props.get(ModerationModel.PROP_REJECT_MESSAGE);

            final NodeRef creatorRef = getPersonService().getPerson(creator);
            final String creatorEmail = getUserService().getUserEmail(creator);
            String noReply = getMailService().getNoReplyEmailAddress();

            final Locale locale = getMailLanguage(creatorRef);
            final Map<String, Object> model = getMailPreferencesService()
                    .buildDefaultModel(nodeRef, creatorRef, null);
            model.put(MailTemplate.KEY_REJECT_DATE, moderated);
            model.put(MailTemplate.KEY_REJECT_REASON, (reason == null) ? "" : reason);
            model.put(MailTemplate.KEY_REJECTED_CONTENT, (oldContent == null) ? "" : oldContent);
            final MailWrapper mail = getMailPreferencesService()
                    .getDefaultMailTemplate(nodeRef, MailTemplate.REJECT_POST);

            getMailService().send(noReply, creatorEmail, null, mail.getSubject(model, locale),
                    mail.getBody(model, locale), true, false);

            if (logger.isDebugEnabled()) {
                logger.debug("Mail successfully sent to " + creatorEmail);
            }
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Impossible to found the nodRef of the creator of the post  " + nodeRef
                        + ". Impossible to contact him !!!");
            }
        }
    }

    protected void signalAbuse(final String message) throws Exception {
        final NodeRef nodeRef = getActionNode().getNodeRef();
        final AbuseReport report = getModerationService().signalAbuse(nodeRef, message);
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Node successfully signaled as abuse: " + nodeRef + ". With message: " + message);
        }

        final Set<NodeRef> moderators = getModerators();

        if (logger.isDebugEnabled()) {
            logger.debug(moderators.size() + " moderator(s) fond for : " + nodeRef);
        }

        //get the current user and properties, he is the reporter
        final String reporterEmail = getUserService().getUserEmail(report.getReporter());

        for (final NodeRef moderator : moderators) {
            final String moderatorEmail = (String) getNodeService()
                    .getProperty(moderator, ContentModel.PROP_EMAIL);
            String noReply = getMailService().getNoReplyEmailAddress();
            final Locale locale = getMailLanguage(moderator);
            final Map<String, Object> model = getMailPreferencesService()
                    .buildDefaultModel(nodeRef, moderator, null);
            model.put(MailTemplate.KEY_ABUSE_DATE, report.getReportDate());
            model.put(MailTemplate.KEY_ABUSE_REASON,
                    (report.getMessage() == null) ? "" : report.getMessage());
            final MailWrapper mail = getMailPreferencesService()
                    .getDefaultMailTemplate(nodeRef, MailTemplate.SIGNAL_ABUSE);

            getMailService().send(noReply, moderatorEmail, reporterEmail, mail.getSubject(model, locale),
                    mail.getBody(model, locale), true, false);

        }

        if (logger.isDebugEnabled()) {
            logger.debug("Mails successfull sent");
        }
    }

    protected void signalNotAbuse() throws Exception {
        final NodeRef nodeRef = getActionNode().getNodeRef();
        getModerationService().signalNotAbuse(nodeRef);
        if (logger.isDebugEnabled()) {
            logger.debug("Node successfully signaled as Not An Abuse: " + nodeRef);
        }
    }

    protected Set<NodeRef> getModerators() {
        final NodeRef postRef = getActionNode().getNodeRef();
        final NodeRef topicRef = getNodeService().getPrimaryParent(postRef).getParentRef();
        final NodeRef igRef = getManagementService().getCurrentInterestGroup(topicRef);
        final List<String> moderatorPermissions = new ArrayList<>(2);
        final ProfileManagerService profileService = getProfileManagerServiceFactory()
                .getIGRootProfileManagerService();

        if (getNodeService().hasAspect(topicRef, CircabcModel.ASPECT_LIBRARY)) {
            moderatorPermissions.add(LibraryPermissions.LIBADMIN.toString());
        } else if (getNodeService().hasAspect(topicRef, CircabcModel.ASPECT_NEWSGROUP)) {
            moderatorPermissions.add(NewsGroupPermissions.NWSMODERATE.toString());
            moderatorPermissions.add(NewsGroupPermissions.NWSADMIN.toString());
        }

        final List<Map> moderators = PermissionUtils
                .getInterestGroupAuthorities(topicRef, moderatorPermissions);
        final Set<NodeRef> moderatorsRef = new HashSet<>(moderators.size());

        for (Map moderator : moderators) {
            final String authority = (String) moderator.get(PermissionUtils.KEY_AUTHORITY);

            if (getPersonService().personExists(authority) == true) {
                moderatorsRef.add(getPersonService().getPerson(authority));
            } else {
                final Profile profile = profileService.getProfileFromGroup(igRef, authority);
                if (profile != null) {
                    for (final String userId : profileService
                            .getPersonInProfile(igRef, profile.getProfileName())) {
                        moderatorsRef.add(getPersonService().getPerson(userId));
                    }
                }
            }
        }

        return moderatorsRef;

    }

    private Locale getMailLanguage(NodeRef person) {
        Serializable langObject = getUserService()
                .getPreference(person, UserService.PREF_INTERFACE_LANGUAGE);
        Locale locale;

        if (langObject == null) {
            locale = null;
        } else if (langObject instanceof Locale) {
            locale = (Locale) langObject;
        } else {
            locale = new Locale(langObject.toString());
        }

        return locale;
    }

    public String getCreator() {
        return (String) getActionNode().getProperties().get(ContentModel.PROP_CREATOR.toString());
    }

    public String getCreated() {
        final Date date = (Date) getActionNode().getProperties()
                .get(ContentModel.PROP_CREATED.toString());
        final String formatedDate = WebClientHelper
                .formatLocalizedDate(date, FacesContext.getCurrentInstance(), true, true);
        return formatedDate;
    }

    public String getContent() {
        return (String) getBrowseBean().resolverContent.get(getActionNode());
    }

    protected final ModerationService getModerationService() {
        if (moderationService == null) {
            moderationService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getModerationService();
        }
        return moderationService;
    }

    public final void setModerationService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        throw new IllegalStateException("Should never launch as a dialog");
    }

    public String getBrowserTitle() {
        throw new IllegalStateException("Should never launch as a dialog");
    }

    public String getPageIconAltText() {
        throw new IllegalStateException("Should never launch as a dialog");
    }

    protected final PersonService getPersonService() {
        if (personService == null) {
            personService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getPersonService();
        }
        return personService;
    }

    public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
