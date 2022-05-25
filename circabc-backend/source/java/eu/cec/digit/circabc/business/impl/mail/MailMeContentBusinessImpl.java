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
package eu.cec.digit.circabc.business.impl.mail;

import eu.cec.digit.circabc.business.api.BusinessValidationError;
import eu.cec.digit.circabc.business.api.mail.MailMeContentBusinessSrv;
import eu.cec.digit.circabc.business.helper.ContentManager;
import eu.cec.digit.circabc.business.helper.NodeTypeManager;
import eu.cec.digit.circabc.business.helper.UserManager;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import eu.cec.digit.circabc.business.impl.ValidationUtils;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.user.UserService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * Business service implementation to sent a content using email.
 *
 * @author patrice.coppens@trasys.lu
 */
public class MailMeContentBusinessImpl implements MailMeContentBusinessSrv {

    /**
     * The defaul locale for the mail
     */
    public static final Locale DEFAULT_MAIL_LOCALE = new Locale("en");
    private static final String MSG_NO_EMAIL = "business_validation_no_email";
    private final Log logger = LogFactory.getLog(MailMeContentBusinessImpl.class);

    private MailService mailService;
    private MailPreferencesService mailPreferencesService;

    private UserManager userManager;
    private NodeTypeManager nodeTypeManager;
    private ValidationManager validationManager;
    private ContentManager contentManager;

    //--------------
    //-- public methods


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.mail.MailMeContentBusinessSrv#send(javax.faces.context.FacesContext, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean send(final NodeRef nodeRef, boolean attachContent) {
        ValidationUtils.assertNodeRef(nodeRef, validationManager, logger);

        final String userName = AuthenticationUtil.getFullyAuthenticatedUser();

        return send(nodeRef, userName, attachContent);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.mail.MailMeContentBusinessSrv#send(javax.faces.context.FacesContext, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public boolean send(final NodeRef nodeRef, String userId, boolean attachContent) {
        ValidationUtils.assertNodeRef(nodeRef, validationManager, logger);

        final NodeRef person = userManager.getPerson(userId);

        if (person == null) {
            throw new IllegalArgumentException("Invalid userId == " + userId);
        }

        // set recipient
        final String to = getTo(person);

        if (to == null) {
            throw new BusinessValidationError(MSG_NO_EMAIL);
        }

        final Locale locale = getMailLanguage(person);
        final Map<String, Object> model = mailPreferencesService.buildDefaultModel(nodeRef, null, null);

        try {
            final NodeRef targetIfLink = contentManager.getTargetRef(nodeRef);

            if (nodeTypeManager.isContent(targetIfLink)) {
                if (attachContent) {
                    final MailWrapper mail = mailPreferencesService
                            .getDefaultMailTemplate(targetIfLink, MailTemplate.MAIL_ME_CONTENT);
                    return mailService
                            .sendNode(targetIfLink, getFromAddress(), to, null, mail.getSubject(model, locale),
                                    mail.getBody(model, locale), true);
                } else {
                    final MailWrapper mail = mailPreferencesService
                            .getDefaultMailTemplate(targetIfLink, MailTemplate.MAIL_ME_CONTENT_URL);
                    return mailService.send(getFromAddress(), to, null, mail.getSubject(model, locale),
                            mail.getBody(model, locale), true, false);
                }
            } else {
                final MailWrapper mail = mailPreferencesService
                        .getDefaultMailTemplate(targetIfLink, MailTemplate.MAIL_ME_DOSSIER);
                return mailService.send(getFromAddress(), to, null, mail.getSubject(model, locale),
                        mail.getBody(model, locale), true, false);
            }
        } catch (final Throwable silentError) {
            if (logger.isErrorEnabled()) {
                logger.error("impossible to send node by email: " + silentError.getMessage(), silentError);
            }

            return false;
        }
    }

    //--------------
    //-- private helpers

    private String getTo(NodeRef person) {
        final String address = (String) userManager.getUserProperty(person, ContentModel.PROP_EMAIL);

        if (address != null && address.length() != 0) {
            return address;
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("cannot found email address for: " + person);
            }
        }

        return null;
    }

    private Locale getMailLanguage(NodeRef person) {
        Serializable langObject = userManager
                .getUserPreference(person, UserService.PREF_INTERFACE_LANGUAGE);
        Locale locale;

        if (langObject == null) {
            locale = DEFAULT_MAIL_LOCALE;
        } else if (langObject instanceof Locale) {
            locale = (Locale) langObject;
        } else {
            locale = new Locale(langObject.toString());
        }

        return locale;
    }


    private String getFromAddress() {
        return mailService.getNoReplyEmailAddress();
    }

    //--------------
    //-- IOC

    /**
     * @param mailPreferencesService the mailPreferencesService to set
     */
    public final void setMailPreferencesService(MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }


    /**
     * @param mailService the mailService to set
     */
    public final void setMailService(MailService mailService) {
        this.mailService = mailService;
    }


    /**
     * @param nodeTypeManager the nodeTypeManager to set
     */
    public final void setNodeTypeManager(NodeTypeManager nodeTypeManager) {
        this.nodeTypeManager = nodeTypeManager;
    }


    /**
     * @param userManager the userManager to set
     */
    public final void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }


    /**
     * @param validationManager the validationManager to set
     */
    public final void setValidationManager(ValidationManager validationManager) {
        this.validationManager = validationManager;
    }


    /**
     * @param contentManager the contentManager to set
     */
    public final void setContentManager(ContentManager contentManager) {
        this.contentManager = contentManager;
    }


}
