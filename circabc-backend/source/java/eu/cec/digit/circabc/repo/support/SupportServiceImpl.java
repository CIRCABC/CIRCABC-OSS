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
package eu.cec.digit.circabc.repo.support;

import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.business.impl.user.UserDetailsBusinessImpl;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.support.SupportService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Implementation of support service.
 *
 * @author Stephane Clinckart
 */
public class SupportServiceImpl implements SupportService {

    private static final Log logger = LogFactory.getLog(SupportServiceImpl.class);
    private static String contactFile = "contact-helpdesk.properties";
    private static String circabcDictionaryFolderName = "CircaBC";
    private static String helpdeskFolderName = "helpdesk";
    private static String contactFolderName = "contact";
    private static String contactKey = "contact";
    private static String titleKey = "title";
    private static String phoneKey = "phone";
    private static String emailKey = "email";
    private static String requestPrefix = "Ticket Request: ";
    private String supportGroupName;
    private AuthorityService authorityService;
    private NodeService nodeService;
    private ContentService contentService;
    private ManagementService managementService;
    private MailService mailService;
    private MailPreferencesService mailPreferencesService;
    private UserDetailsBusinessImpl userService;

    public boolean isUserFromSupport(final String user) {
        final Set<String> containingAuthoritiesForUser =
                authorityService.getContainingAuthorities(AuthorityType.GROUP, user, true);
        if (containingAuthoritiesForUser != null
                && containingAuthoritiesForUser.contains("GROUP_" + supportGroupName)) {
            return true;
        } else {
            return false;
        }
    }

    public void setSupportGroupName(final String supportGroupName) {
        this.supportGroupName = supportGroupName;
    }

    @Override
    public List<SupportContact> getAllSupportContacts() {

        List<SupportContact> listOfContacts = new ArrayList<>();

        NodeRef fileRef = getContactFileRef();

        if (fileRef != null) {
            ContentReader reader = contentService.getReader(fileRef, ContentModel.PROP_CONTENT);
            InputStream contentStream = reader.getContentInputStream();
            Properties prop = new Properties();
            try {

                prop.load(contentStream);

                List<String> listOfKeys = new ArrayList<>();

                for (Object key : prop.keySet()) {
                    String keyString = key.toString();
                    String tmpKey =
                            keyString.substring(keyString.indexOf('.') + 1, keyString.lastIndexOf('.'));
                    if (!listOfKeys.contains(tmpKey)) {
                        listOfKeys.add(tmpKey);
                    }
                }

                Collections.sort(listOfKeys);

                for (String key : listOfKeys) {
                    SupportContact contact = new SupportContact();
                    contact.setId(key);
                    contact.setEmail(prop.getProperty(contactKey + "." + key + "." + emailKey));
                    contact.setPhone(prop.getProperty(contactKey + "." + key + "." + phoneKey));
                    contact.setTitle(prop.getProperty(contactKey + "." + key + "." + titleKey));

                    listOfContacts.add(contact);
                }

            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Problem during reading contact file from datadictionnary", e);
                }
            }
        }

        return listOfContacts;
    }

    private NodeRef getContactFileRef() {

        NodeRef dicoNodeRef = managementService.getAlfrescoDictionaryNodeRef();
        NodeRef circabcFolderNodeRef =
                nodeService.getChildByName(
                        dicoNodeRef, ContentModel.ASSOC_CONTAINS, circabcDictionaryFolderName);
        NodeRef helpdeskFolderNodeRef =
                nodeService.getChildByName(
                        circabcFolderNodeRef, ContentModel.ASSOC_CONTAINS, helpdeskFolderName);
        NodeRef contactFolderNodeRef =
                nodeService.getChildByName(
                        helpdeskFolderNodeRef, ContentModel.ASSOC_CONTAINS, contactFolderName);
        NodeRef fileRef =
                nodeService.getChildByName(contactFolderNodeRef, ContentModel.ASSOC_CONTAINS, contactFile);

        return fileRef;
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the contentService
     */
    public ContentService getContentService() {
        return contentService;
    }

    /**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @return the managementService
     */
    public ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    @Override
    public Boolean sendSupportRequest(
            String subject,
            String description,
            SupportTypes byId,
            String currentUserName,
            SupportContact contact,
            NodeRef actionNodeRef) {

        UserDetails userDetails = userService.getUserDetails(currentUserName);
        String title = requestPrefix + subject;
        Map<String, Object> model = new HashMap<>();
        model.put("helpdesk", contact.getTitle());

        CircabcConfiguration.addApplicationNameToModel(model);
        model.put("userFullName", userDetails.getFullName());
        model.put("email", userDetails.getEmail());
        model.put("organisation", userDetails.getOrganisation());
        model.put("subject", subject);
        model.put("contentMessage", description);

        MailWrapper mail =
                getMailPreferencesService()
                        .getDefaultMailTemplate(actionNodeRef, MailTemplate.SUPPORT_REQUEST);

        Boolean result = false;

        try {

            mailService.send(
                    mailService.getNoReplyEmailAddress(),
                    contact.getEmail(),
                    userDetails.getEmail(),
                    title,
                    mail.getBody(model),
                    true,
                    false);
            result = true;

        } catch (InvalidNodeRefException e) {
            if (logger.isErrorEnabled()) {
                logger.error("User does not exist impossible to send support request to contact mail", e);
            }

        } catch (MessagingException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Impossible to send support request to contact by email", e);
            }
        }

        return result;
    }

    /**
     * @return the mailService
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * @param mailService the mailService to set
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public SupportContact getContactById(String id) {
        SupportContact result = null;

        NodeRef fileRef = getContactFileRef();

        if (fileRef != null) {
            ContentReader reader = contentService.getReader(fileRef, ContentModel.PROP_CONTENT);
            InputStream contentStream = reader.getContentInputStream();
            Properties prop = new Properties();
            try {

                prop.load(contentStream);

                result = new SupportContact();
                result.setId(id);
                result.setEmail(prop.getProperty(contactKey + "." + id + "." + emailKey));
                result.setPhone(prop.getProperty(contactKey + "." + id + "." + phoneKey));
                result.setTitle(prop.getProperty(contactKey + "." + id + "." + titleKey));

            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Problem during reading contact file from datadictionnary", e);
                }
            }
        }

        return result;
    }

    /**
     * @return the mailPreferencesService
     */
    public MailPreferencesService getMailPreferencesService() {
        return mailPreferencesService;
    }

    /**
     * @param mailPreferencesService the mailPreferencesService to set
     */
    public void setMailPreferencesService(MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    /**
     * @return the authorityService
     */
    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(final AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @return the userService
     */
    public UserDetailsBusinessImpl getUserService() {
        return userService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(UserDetailsBusinessImpl userService) {
        this.userService = userService;
    }

    @Override
    public Boolean sendSupportRequestAsGuest(
            String subject,
            String description,
            SupportTypes byId,
            String mailAddress,
            SupportContact contact,
            NodeRef actionNodeRef) {

        String title = requestPrefix + subject;
        Map<String, Object> model = new HashMap<>();
        model.put("helpdesk", contact.getTitle());

        CircabcConfiguration.addApplicationNameToModel(model);
        model.put("userFullName", "");
        model.put("email", mailAddress);
        model.put("organisation", "");
        model.put("subject", subject);
        model.put("contentMessage", description);

        MailWrapper mail =
                getMailPreferencesService()
                        .getDefaultMailTemplate(actionNodeRef, MailTemplate.SUPPORT_REQUEST);

        Boolean result = false;

        try {

            mailService.send(
                    mailService.getNoReplyEmailAddress(),
                    contact.getEmail(),
                    mailAddress,
                    title,
                    mail.getBody(model),
                    true,
                    false);
            result = true;

        } catch (InvalidNodeRefException e) {
            if (logger.isErrorEnabled()) {
                logger.error("User does not exist impossible to send support request to contact mail", e);
            }

        } catch (MessagingException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Impossible to send support request to contact by email", e);
            }
        }

        return result;
    }
}
