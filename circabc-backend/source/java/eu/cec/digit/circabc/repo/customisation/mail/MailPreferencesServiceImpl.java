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
package eu.cec.digit.circabc.repo.customisation.mail;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.customisation.NodePreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Implementation of the MailPreferencesService.
 *
 * @author Yanick Pignot
 */
public class MailPreferencesServiceImpl implements MailPreferencesService {

    private static final String LOGO_FOLDER = "logo";
    private static final String DISCLAMER_FOLDER = "disclamer";
    private static final String FTL_EXTENSION = ".ftl";
    /**
     * The folder name in which all templates are inclue (welcome page, mail, icons, list elements)
     */
    private static final String TEMPLATES_ROOT_FOLDER = "templates";

    private static final String MAILS_TEMPLATE_FOLDER = "mails";

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(MailPreferencesServiceImpl.class);

    private NodePreferencesService nodePreferencesService;
    private NodeService nodeService;
    private TemplateService templateService;
    private PersonService personService;
    private DictionaryService dictionaryService;
    private ManagementService managementService;
    private MultilingualContentService multilingualContentService;

    public void addMailTemplate(
            final NodeRef ref,
            final MailTemplate forTemplate,
            final String name,
            final String body,
            final String subject,
            final Locale language)
            throws CustomizationException {
        ParameterCheck.mandatory("The node reference", ref);
        ParameterCheck.mandatory("The mail template", forTemplate);
        ParameterCheck.mandatoryString("The mail name", name);
        ParameterCheck.mandatoryString("The mail body", body);
        ParameterCheck.mandatoryString("The mail subject", subject);
        ParameterCheck.mandatory("The mail language", language);

        final NodeRef mailRef;
        try {
            if (nodePreferencesService.isNodeConfigurable(ref) == false) {
                nodePreferencesService.makeConfigurable(ref);
            }

            final String cleanName;
            if (name.endsWith(FTL_EXTENSION) == false) {
                cleanName = name + FTL_EXTENSION;
            } else {
                cleanName = name;
            }

            if (nodePreferencesService.customizationFileExists(
                    ref,
                    TEMPLATES_ROOT_FOLDER,
                    MAILS_TEMPLATE_FOLDER,
                    forTemplate.getTemplateDirectoryName(),
                    cleanName)) {
                final NodeRef existingRef =
                        nodePreferencesService.getCustomization(
                                ref,
                                TEMPLATES_ROOT_FOLDER,
                                MAILS_TEMPLATE_FOLDER,
                                forTemplate.getTemplateDirectoryName(),
                                name);

                Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(existingRef);
                if (translations.containsKey(language)) {
                    throw new CustomizationException(
                            "A template with name " + name + " and language " + language + " already exists.");
                }

                final NodeRef pivot = multilingualContentService.getPivotTranslation(existingRef);

                int tries = -1;
                String translationName = cleanName;
                while (nodePreferencesService.customizationFileExists(
                        ref,
                        TEMPLATES_ROOT_FOLDER,
                        MAILS_TEMPLATE_FOLDER,
                        forTemplate.getTemplateDirectoryName(),
                        name)) {
                    translationName = computeName(translationName, ++tries, language);
                }

                mailRef =
                        nodePreferencesService.addCustomizationFile(
                                ref,
                                TEMPLATES_ROOT_FOLDER,
                                MAILS_TEMPLATE_FOLDER,
                                forTemplate.getTemplateDirectoryName(),
                                translationName,
                                body);

                multilingualContentService.addTranslation(mailRef, pivot, language);
            } else {
                mailRef =
                        nodePreferencesService.addCustomizationFile(
                                ref,
                                TEMPLATES_ROOT_FOLDER,
                                MAILS_TEMPLATE_FOLDER,
                                forTemplate.getTemplateDirectoryName(),
                                cleanName,
                                body);

                multilingualContentService.makeTranslation(mailRef, language);
            }

            nodeService.setProperty(mailRef, ContentModel.PROP_TITLE, subject);
        } catch (CustomizationException e) {
            throw new IllegalStateException(
                    "Problem during adding mail with name "
                            + forTemplate.getTemplateDirectoryName()
                            + "/"
                            + name
                            + ". Is the datadictionary customization folder up to date?");
        }
    }

    public void replaceDefaultMailTemplate(
            final NodeRef ref,
            final MailTemplate forTemplate,
            final String body,
            final String subject,
            final Locale language)
            throws CustomizationException {
        addMailTemplate(
                ref, forTemplate, forTemplate.getDefaultTemplateName(), body, subject, language);
    }

    public List<MailWrapper> getMailTemplates(final NodeRef ref, final MailTemplate mailTemplate) {
        ParameterCheck.mandatory("The node reference", ref);
        ParameterCheck.mandatory("The mail template", mailTemplate);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Trying to retreive the " + mailTemplate + " mail templates for the node " + ref + ".");
        }

        try {
            final List<NodeRef> templatesNodes =
                    nodePreferencesService.getConfigurationFiles(
                            ref,
                            TEMPLATES_ROOT_FOLDER,
                            MAILS_TEMPLATE_FOLDER,
                            mailTemplate.getTemplateDirectoryName());

            final Map<NodeRef, MailWrapper> mailMap = new HashMap<>(templatesNodes.size());
            MailWrapper wrapper;
            for (final NodeRef nodeRef : templatesNodes) {
                wrapper = computeWrapper(mailTemplate, nodeRef);

                if (wrapper != null) {
                    mailMap.put(nodeRef, wrapper);
                }
            }

            final List<MailWrapper> mails = new ArrayList<>(mailMap.size());
            mails.addAll(mailMap.values());

            return mails;
        } catch (CustomizationException ex) {
            throw new IllegalStateException(
                    "Problem during getting mails in folder: "
                            + mailTemplate.getTemplateDirectoryName()
                            + ". Is the datadictionary customization folder up to date?",
                    ex);
        }
    }

    /**
     * @param mailTemplate
     * @param nodeRef
     * @return
     * @throws InvalidNodeRefException
     */
    private MailWrapper computeWrapper(final MailTemplate mailTemplate, final NodeRef nodeRef)
            throws InvalidNodeRefException {
        MailWrapper wrapper;
        if (multilingualContentService.isTranslation(nodeRef) == false) {
            wrapper = new MailWrapperImpl(nodeRef, mailTemplate, templateService, nodeService);
        } else if (multilingualContentService.getPivotTranslation(nodeRef).equals(nodeRef) == true) {
            final Locale pivot = (Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
            wrapper =
                    new MailWrapperImpl(
                            multilingualContentService.getTranslations(nodeRef),
                            mailTemplate,
                            templateService,
                            nodeService,
                            pivot);
        } else {
            // added throught the pivot.
            wrapper = null;
        }
        return wrapper;
    }

    public MailWrapper getDefaultMailTemplate(final NodeRef ref, final MailTemplate mailTemplate) {
        ParameterCheck.mandatory("The node reference", ref);
        ParameterCheck.mandatory("The mail template", mailTemplate);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Trying to retreive the default "
                            + mailTemplate
                            + " mail templates for the node "
                            + ref
                            + ".");
        }

        NodeRef templateNodeRef;
        try {
            templateNodeRef =
                    nodePreferencesService.getDefaultConfigurationFile(
                            ref,
                            TEMPLATES_ROOT_FOLDER,
                            MAILS_TEMPLATE_FOLDER,
                            mailTemplate.getTemplateDirectoryName());

            // should never occurs...
            if (templateNodeRef == null) {
                throw new CustomizationException(
                        "No default mail mail template in folder: "
                                + mailTemplate.getTemplateDirectoryName()
                                + ". Is the datadictionary customization folder up to date?");
            }

            if (multilingualContentService.isTranslation(templateNodeRef)) {
                templateNodeRef = multilingualContentService.getPivotTranslation(templateNodeRef);
            }

            final MailWrapper wrapper = computeWrapper(mailTemplate, templateNodeRef);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        wrapper
                                + " is the default mail template for template: "
                                + mailTemplate.getTemplateDirectoryName());
            }

            return wrapper;
        } catch (CustomizationException ex) {
            throw new IllegalStateException(
                    "Problem during getting mails in folder: "
                            + mailTemplate.getTemplateDirectoryName()
                            + ". Is the datadictionary customization folder up to date?",
                    ex);
        }
    }

    public Map<String, Object> buildDefaultModel(
            final NodeRef currentRef,
            final NodeRef otherPerson,
            final TemplateImageResolver imageResolver) {

        final Map<String, Object> model = new HashMap<>(12, 1.0f);

        CircabcConfiguration.addApplicationNameToModel(model);

        // Place the image resolver into the model
        if (imageResolver != null) {
            model.put(MailTemplate.KEY_IMAGE_RESOLVER, imageResolver);
        }

        // current date/time is useful to have and isn't supplied by FreeMarker by
        // default
        model.put(MailTemplate.KEY_DATE, new Date());

        // get the information about the current user
        NodeRef me = null;
        final String currentUsername = AuthenticationUtil.getFullyAuthenticatedUser();
        if (isValidPerson(currentUsername)) {
            me = personService.getPerson(currentUsername);
            model.put(MailTemplate.KEY_ME, me);
        }

        model.put(MailTemplate.KEY_PERSON, otherPerson != null ? otherPerson : me);

        // fill the information about the given node
        if (currentRef != null) {
            final QName type = nodeService.getType(currentRef);

            model.put(MailTemplate.KEY_LOCATION, currentRef);

            if (CircabcModel.TYPE_INFORMATION_NEWS.equals(type)) {
                model.put(MailTemplate.KEY_DOCUMENT, currentRef);
            } else if (ContentModel.TYPE_FOLDER.equals(type)
                    || dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER)) {
                model.put(MailTemplate.KEY_SPACE, currentRef);
            } else if (ContentModel.TYPE_CONTENT.equals(type)
                    || dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
                model.put(MailTemplate.KEY_DOCUMENT, currentRef);
                model.put(MailTemplate.KEY_SPACE, nodeService.getPrimaryParent(currentRef).getParentRef());
            }

            model.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());

            // fill the information about the Circabc structure
            model.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());

            final NodeRef igRef = managementService.getCurrentInterestGroup(currentRef);
            if (igRef != null) {
                final NodeRef catRef = managementService.getCurrentCategory(igRef);

                model.put(MailTemplate.KEY_INTEREST_GROUP, igRef);
                model.put(MailTemplate.KEY_CATEGORY, catRef);
            } else {
                final NodeRef catRef = managementService.getCurrentCategory(currentRef);
                if (catRef != null) {
                    model.put(MailTemplate.KEY_CATEGORY, catRef);
                }
            }
        }

        model.put(TemplateService.KEY_IMAGE_RESOLVER, null);

        return model;
    }

    /*
     * TODO replace the creation of a template User by a custom representation of
     * the TemplateNode object.
     */
    public CircabcUserDataBean getTemplateUserDetails() {
        final CircabcUserDataBean templateUser = new CircabcUserDataBean();
        templateUser.setUserName("<USERNAME>");
        templateUser.setFirstName("<USER_FIRST_NAME>");
        templateUser.setLastName("<USER_LAST_NAME>");
        templateUser.setEmail("<USER_EMAIL>");
        templateUser.setCompanyId("<USER_COMPANY_ID>");
        // templateUser.setHomeSpaceNodeRef(managementService.getGuestHomeNodeRef());
        templateUser.setTitle("<USER_TITLE>");
        templateUser.setPhone("<USER_PHONE>");
        templateUser.setFax("<USER_PHONE>");
        templateUser.setURL("<USER_URL>");
        templateUser.setPostalAddress("<USER_POSTAL_ADDRESS>");
        templateUser.setDescription("<USER_DESCRIPTION>");
        templateUser.setDomain("<USER_DOMAIN>");
        templateUser.setOrgdepnumber("<USER_ORG_DEP_NUMBER>");
        templateUser.setVisibility(true);
        templateUser.setGlobalNotification(true);
        templateUser.setEcasUserName("<USER_ECAS_USERNAME>");

        return templateUser;
    }

    public NodeRef getDisclamerLogo(final NodeRef ref) {
        ParameterCheck.mandatory("The node reference", ref);

        if (logger.isDebugEnabled()) {
            logger.debug("Trying to retreive the default mail disclamer logo for the node " + ref + ".");
        }

        NodeRef templateNodeRef;
        try {
            templateNodeRef =
                    nodePreferencesService.getDefaultConfigurationFile(
                            ref, TEMPLATES_ROOT_FOLDER, DISCLAMER_FOLDER, LOGO_FOLDER);

            // should never occurs...
            if (templateNodeRef == null) {
                throw new CustomizationException(
                        "No default mail disclamer logo in folder: "
                                + DISCLAMER_FOLDER
                                + "/"
                                + LOGO_FOLDER
                                + ". Is the datadictionary customization folder up to date?");
            }

            return templateNodeRef;
        } catch (CustomizationException ex) {
            throw new IllegalStateException(
                    "Problem during getting mail disclamer logo in folder: "
                            + DISCLAMER_FOLDER
                            + "/"
                            + LOGO_FOLDER
                            + ". Is the datadictionary customization folder up to date?",
                    ex);
        }
    }

    private boolean isValidPerson(final String userName) {
        return userName != null
                && !userName.equals(CircabcConstant.GUEST_AUTHORITY)
                && !userName.equals(AuthenticationUtil.getSystemUserName());
    }

    private String computeName(final String originalName, final int tries, final Locale locale) {
        final int pointIdx = originalName.lastIndexOf('.');
        final int langIdx = originalName.lastIndexOf('_');

        final String extension = originalName.substring(pointIdx);
        final String fileName;

        if (langIdx > -1) {
            fileName = originalName.substring(0, langIdx);
        } else {
            fileName = originalName.substring(0, pointIdx);
        }

        return fileName
                + "_"
                + locale.getLanguage()
                + ((tries == 0) ? "" : "(" + tries + ")")
                + extension;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param templateService the templateService to set
     */
    public final void setTemplateService(final TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public final void setDictionaryService(final DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param nodePreferencesService the nodePreferencesService to set
     */
    public final void setNodePreferencesService(NodePreferencesService nodePreferencesService) {
        this.nodePreferencesService = nodePreferencesService;
    }

    /**
     * @param multilingualContentService the multilingualContentService to set
     */
    public final void setMultilingualContentService(
            MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    public NodeRef getHeaderLogo(final NodeRef ref) {
        ParameterCheck.mandatory("The node reference", ref);

        NodeRef dicoRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef templatesRef =
                nodeService.getChildByName(dicoRef, ContentModel.ASSOC_CONTAINS, "templates");
        NodeRef headerRef =
                nodeService.getChildByName(templatesRef, ContentModel.ASSOC_CONTAINS, "header");
        NodeRef logoRef = nodeService.getChildByName(headerRef, ContentModel.ASSOC_CONTAINS, "logo");
        NodeRef headerLogoRef =
                nodeService.getChildByName(logoRef, ContentModel.ASSOC_CONTAINS, "header-mail.jpg");

        return headerLogoRef;
    }

    @Override
    public NodeRef getHeaderBackground(NodeRef circabcRootRef) {
        ParameterCheck.mandatory("The node reference", circabcRootRef);

        NodeRef dicoRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef templatesRef =
                nodeService.getChildByName(dicoRef, ContentModel.ASSOC_CONTAINS, "templates");
        NodeRef headerRef =
                nodeService.getChildByName(templatesRef, ContentModel.ASSOC_CONTAINS, "header");
        NodeRef logoRef = nodeService.getChildByName(headerRef, ContentModel.ASSOC_CONTAINS, "logo");
        NodeRef headerBackgroundRef =
                nodeService.getChildByName(logoRef, ContentModel.ASSOC_CONTAINS, "header-background.jpg");

        return headerBackgroundRef;
    }

    @Override
    public NodeRef getHeaderEULogo(NodeRef circabcRootRef) {
        ParameterCheck.mandatory("The node reference", circabcRootRef);

        NodeRef dicoRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef templatesRef =
                nodeService.getChildByName(dicoRef, ContentModel.ASSOC_CONTAINS, "templates");
        NodeRef headerRef =
                nodeService.getChildByName(templatesRef, ContentModel.ASSOC_CONTAINS, "header");
        NodeRef logoRef = nodeService.getChildByName(headerRef, ContentModel.ASSOC_CONTAINS, "logo");
        NodeRef headerEULogogroundRef =
                nodeService.getChildByName(
                        logoRef, ContentModel.ASSOC_CONTAINS, "header-eu-circabc-logo.png");

        return headerEULogogroundRef;
    }
}
