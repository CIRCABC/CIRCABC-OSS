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
package eu.cec.digit.circabc.repo.translation;

import com.google.common.base.Joiner;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.translation.TranslationService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.util.PathUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ExecuteAllRulesActionExecuter;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import java.io.*;
import java.util.*;

public class TranslationServiceImpl implements TranslationService {

    private static final String TRANSLATIONS = "translations";
    private static final String REQUEST_ID = "?requestId=";
    private static final String NOTIFY_SUCCESS_PATH = "/notifySuccess";
    private static final String NOTIFY_ERROR_PATH = "/notifyError";
    private static final Log logger = LogFactory.getLog(TranslationServiceImpl.class);
    private MachineTranslationService machineTranslationService;
    private String webRootUrl = CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);
    private String callbackUrl =
            CircabcConfiguration.getProperty(CircabcConfiguration.MT_CALLBACK_URL);
    private String mtUserName = CircabcConfiguration.getProperty(CircabcConfiguration.MT_USER);
    private String mtPassword = CircabcConfiguration.getProperty(CircabcConfiguration.MT_PASSWORD);

    private String applicationName =
            CircabcConfiguration.getProperty(CircabcConfiguration.MT_APPLICATION_NAME);
    private UserService userService;
    private Set<String> languages;
    private Set<String> fileExtensions;
    private NodeService nodeService;
    private ManagementService managementService;
    private String ftpUrl;
    private String mtRootSpace;
    private AuthenticationService authenticationService;
    private ContentService contentService;
    private MultilingualContentService multilingualContentService;
    private TranslationDaoService translationDaoService;
    private OwnableService ownableService;
    private DictionaryService dictionaryService;
    private MailService mailService;
    private MailPreferencesService mailPreferencesService;
    private PersonService personService;
    private ActionService actionService;
    private long maxFileSizeInBytes;

    private static String replaceLast(String string, String substring, String replacement) {
        int index = string.lastIndexOf(substring);
        if (index == -1) {
            return string;
        }
        return string.substring(0, index) + replacement + string.substring(index + substring.length());
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public Set<String> getLanguages() {
        return languages;
    }

    public void setLanguages(Set<String> languages) {
        this.languages = languages;
    }

    @Override
    public Set<String> getAvailableLanguages() {
        return languages;
    }

    public TranslationDaoService getTranslationDaoService() {
        return translationDaoService;
    }

    public void setTranslationDaoService(TranslationDaoService translationDaoService) {
        this.translationDaoService = translationDaoService;
    }

    @Override
    public void translateProperty(
            NodeRef nodeRef,
            org.alfresco.service.namespace.QName property,
            String sourceLanguage,
            Set<String> languages,
            boolean notifyUserByEmail) {

        String externalReference = GUID.generate();
        String username = getAuthenticationService().getCurrentUserName();
        CircabcUserDataBean circabcUserDataBean = getUserService().getCircabcUserDataBean(username);

        Serializable propertyValue = getNodeService().getProperty(nodeRef, property);
        String textToTranslate;
        if (propertyValue instanceof MLText) {
            MLText value = (MLText) propertyValue;
            textToTranslate = value.getValue(new Locale(sourceLanguage));
        } else if (propertyValue instanceof String) {
            textToTranslate = (String) propertyValue;
        } else {
            throw new IllegalArgumentException(
                    "property "
                            + property.toPrefixString()
                            + " of node "
                            + nodeRef.toString()
                            + " is not String or MLText");
        }
        String targetTranslationPath = "none";
        String email = "";
        if (notifyUserByEmail) {
            email = circabcUserDataBean.getEmail();
        }

        String departmentNumber = circabcUserDataBean.getOrgdepnumber();
        String documentToTranslate = "";
        String domains = "all";
        final String translationCallbackUrl = getCallbackURL();
        String errorCallback =
                translationCallbackUrl + NOTIFY_ERROR_PATH + REQUEST_ID + externalReference;
        String institution = circabcUserDataBean.getDomain();
        String originalFileName = "";
        String outputFormat = "";
        int priority = 6;
        String requesterCallback =
                translationCallbackUrl + NOTIFY_SUCCESS_PATH + REQUEST_ID + externalReference;
        String requestType = "txt";
        String targetLanguage = Joiner.on(",").join(languages);

        // save in database ftpURL so we know from what serves request was send
        savePropertyRequest(
                nodeRef,
                property,
                sourceLanguage,
                externalReference,
                username,
                textToTranslate,
                ftpUrl,
                targetLanguage,
                notifyUserByEmail,
                email);

        machineTranslationService.sendMessage(
                applicationName,
                departmentNumber,
                documentToTranslate,
                domains,
                errorCallback,
                externalReference,
                institution,
                originalFileName,
                outputFormat,
                priority,
                requesterCallback,
                requestType,
                sourceLanguage,
                targetLanguage,
                targetTranslationPath,
                textToTranslate,
                username);
    }

    private void savePropertyRequest(
            NodeRef nodeRef,
            org.alfresco.service.namespace.QName property,
            String sourceLanguage,
            String externalReference,
            String username,
            String textToTranslate,
            String targetTranslationPath,
            String targetLanguage,
            boolean notify,
            String email) {
        Request request = new Request();
        request.setUsername(username);
        request.setDocURL("");
        request.setReqDate(new Date());
        request.setRequestID(externalReference);
        request.setSourceLang(sourceLanguage);
        request.setTargetLangs(targetLanguage);
        request.setTargetPath(targetTranslationPath);
        request.setText(textToTranslate);

        request.setDocumentID(nodeRef.toString());
        request.setPropertyQName(property.toString());

        request.setNotify(notify);
        request.setEmail(email);

        try {
            translationDaoService.saveRequest(request);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when saving request", e);
            }
        }
    }

    @Override
    public void translateDocument(
            final NodeRef origianalDocument,
            final NodeRef copyOfDocument,
            String sourceLanguage,
            Set<String> languages,
            boolean notifyUserByEmail) {

        final String username = getAuthenticationService().getCurrentUserName();
        CircabcUserDataBean circabcUserDataBean = getUserService().getCircabcUserDataBean(username);

        final NodeRef source = copyOfDocument;
        final NodeRef parentFolder =
                (NodeRef)
                        AuthenticationUtil.runAs(
                                new AuthenticationUtil.RunAsWork<Object>() {
                                    public Object doWork() {
                                        return getNodeService().getPrimaryParent(source).getParentRef();
                                    }
                                },
                                AuthenticationUtil.getSystemUserName());

        final String documentToTranslate =
                (String)
                        AuthenticationUtil.runAs(
                                new AuthenticationUtil.RunAsWork<Object>() {
                                    public Object doWork() {
                                        String sourceName =
                                                (String) getNodeService().getProperty(source, ContentModel.PROP_NAME);
                                        return getDocumentToTranslate(parentFolder, sourceName);
                                    }
                                },
                                AuthenticationUtil.getSystemUserName());

        final String targetTranslationPath =
                (String)
                        AuthenticationUtil.runAs(
                                new AuthenticationUtil.RunAsWork<Object>() {
                                    public Object doWork() {
                                        return getTargetTranslationPath(parentFolder);
                                    }
                                },
                                AuthenticationUtil.getSystemUserName());

        String departmentNumber = circabcUserDataBean.getOrgdepnumber();

        String domains = "all";
        String externalReference = GUID.generate();
        final String translationCallbackUrl = getCallbackURL();
        String errorCallback =
                translationCallbackUrl + NOTIFY_ERROR_PATH + REQUEST_ID + externalReference;
        String institution = circabcUserDataBean.getDomain();
        final String originalFileName =
                (String)
                        AuthenticationUtil.runAs(
                                new AuthenticationUtil.RunAsWork<Object>() {
                                    public Object doWork() {
                                        return getNodeService().getProperty(copyOfDocument, ContentModel.PROP_NAME);
                                    }
                                },
                                AuthenticationUtil.getSystemUserName());

        String outputFormat = "default";
        int priority = 1;
        String requesterCallback =
                translationCallbackUrl + NOTIFY_SUCCESS_PATH + REQUEST_ID + externalReference;
        String requestType = "doc";
        String textToTranslate = "";
        String targetLanguage = Joiner.on(",").join(languages);

        String email = "";
        if (notifyUserByEmail) {
            email = circabcUserDataBean.getEmail();
        }

        saveDocumentRequest(
                origianalDocument,
                documentToTranslate,
                sourceLanguage,
                externalReference,
                username,
                targetTranslationPath,
                textToTranslate,
                targetLanguage,
                notifyUserByEmail,
                email);

        machineTranslationService.sendMessage(
                applicationName,
                departmentNumber,
                documentToTranslate,
                domains,
                errorCallback,
                externalReference,
                institution,
                originalFileName,
                outputFormat,
                priority,
                requesterCallback,
                requestType,
                sourceLanguage,
                targetLanguage,
                targetTranslationPath,
                textToTranslate,
                username);
    }

    private String getCallbackURL() {
        if (webRootUrl.startsWith("https") && (callbackUrl != null)) {
            return callbackUrl;
        } else {
            return webRootUrl;
        }
    }

    private void saveDocumentRequest(
            final NodeRef nodeRef,
            String documentToTranslate,
            String sourceLanguage,
            String externalReference,
            final String username,
            final String targetTranslationPath,
            String textToTranslate,
            String targetLanguage,
            boolean notify,
            String email) {
        Request request = new Request();
        request.setUsername(username);
        request.setDocURL(documentToTranslate);
        request.setReqDate(new Date());
        request.setRequestID(externalReference);
        request.setSourceLang(sourceLanguage);
        request.setTargetLangs(targetLanguage);
        request.setTargetPath(targetTranslationPath);
        request.setText(textToTranslate);
        request.setNotify(notify);
        request.setEmail(email);

        request.setDocumentID(nodeRef.toString());

        try {
            translationDaoService.saveRequest(request);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when saving request", e);
            }
        }
    }

    private NodeRef createFolderStructure(NodeRef nodeRef, String username) {

        NodeRef mtRootNode = managementService.getMTNodeRef();
        if (mtRootNode == null) {
            throw new IllegalStateException("machine translation root folder does not exists");
        } else {
            Calendar now = Calendar.getInstance();
            String year = String.valueOf(now.get(Calendar.YEAR));
            String month = String.valueOf(now.get(Calendar.MONTH) + 1);
            String day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));

            NodeRef yearFolder = getFolder(year, mtRootNode);
            NodeRef monthFolder = getFolder(month, yearFolder);
            NodeRef dayFolder = getFolder(day, monthFolder);
            NodeRef userFolder = getFolder(username, dayFolder);
            return getFolder(nodeRef.getId(), userFolder);
        }
    }

    private NodeRef getFolder(String spaceName, NodeRef parentNodeRef) {
        NodeRef result =
                getNodeService().getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, spaceName);
        if (result == null) {
            final ChildAssociationRef childRef =
                    getNodeService()
                            .createNode(
                                    parentNodeRef,
                                    ContentModel.ASSOC_CONTAINS,
                                    org.alfresco.service.namespace.QName.createQName(
                                            NamespaceService.CONTENT_MODEL_1_0_URI, spaceName),
                                    ContentModel.TYPE_FOLDER);
            result = childRef.getChildRef();
            getNodeService().setProperty(result, ContentModel.PROP_NAME, spaceName);
        }
        return result;
    }

    private String getTargetTranslationPath(NodeRef source) {
        return ftpUrl
                + PathUtils.getCircabcPath(getNodeService().getPath(source), true)
                + "/"
                + TRANSLATIONS;
    }

    private String getDocumentToTranslate(NodeRef source, String name) {

        return ftpUrl + PathUtils.getCircabcPath(getNodeService().getPath(source), true) + "/" + name;
    }

    private NodeRef copyDocument(NodeRef parentFolder, NodeRef originalDocument) {
        String name = (String) getNodeService().getProperty(originalDocument, ContentModel.PROP_NAME);
        Map<org.alfresco.service.namespace.QName, Serializable> props = new HashMap<>(1);
        props.put(ContentModel.PROP_NAME, (Serializable) name);
        final org.alfresco.service.namespace.QName qname =
                org.alfresco.service.namespace.QName.createQName(
                        NamespaceService.CONTENT_MODEL_1_0_URI, name);
        final List<ChildAssociationRef> childAssocs =
                getNodeService().getChildAssocs(parentFolder, ContentModel.ASSOC_CONTAINS, qname, 1, false);
        if (childAssocs.size() == 1) {
            getNodeService().deleteNode(childAssocs.get(0).getChildRef());
        }
        final ChildAssociationRef childRef =
                getNodeService()
                        .createNode(
                                parentFolder, ContentModel.ASSOC_CONTAINS, qname, ContentModel.TYPE_CONTENT, props);
        NodeRef targetDocument = childRef.getChildRef();

        byte[] binaryData = getContent(originalDocument);

        ContentWriter writer =
                contentService.getWriter(targetDocument, ContentModel.PROP_CONTENT, true);
        writer.putContent(new ByteArrayInputStream(binaryData));

        return targetDocument;
    }

    private byte[] getContent(NodeRef nodeRef) {
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        InputStream originalInputStream = reader.getContentInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final int BUF_SIZE = 4096;
        byte[] buffer = new byte[BUF_SIZE];
        int bytesRead = -1;
        try {
            while ((bytesRead = originalInputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            originalInputStream.close();
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when copy document to be translated", e);
            }
        }
        byte[] binaryData = outputStream.toByteArray();
        return binaryData;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public CircabcUserDataBean getMTUserDetails() {
        final CircabcUserDataBean mtUser = new CircabcUserDataBean();
        mtUser.setUserName(mtUserName);
        mtUser.setPassword(mtPassword);
        mtUser.setFirstName("Machine translation");
        mtUser.setLastName("Machine translation");
        mtUser.setEmail("DGT-MT@ec.europa.eu");
        mtUser.setCompanyId("");
        mtUser.setTitle("");
        mtUser.setPhone("");
        mtUser.setFax(">");
        mtUser.setURL("");
        mtUser.setPostalAddress("");
        mtUser.setDescription("");
        mtUser.setDomain("");
        mtUser.setOrgdepnumber("");
        mtUser.setVisibility(false);
        mtUser.setGlobalNotification(false);
        mtUser.setEcasUserName("");

        return mtUser;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    @Override
    public NodeRef copyDocumentToBeTranslated(final NodeRef nodeRef) {
        org.alfresco.service.namespace.QName type = getNodeService().getType(nodeRef);

        final String username = getAuthenticationService().getCurrentUserName();

        if (!type.equals(org.alfresco.model.ContentModel.TYPE_CONTENT)) {
            throw new IllegalArgumentException(
                    "invalid type  " + type + " of node " + nodeRef.toString() + " is not String or MLText");
        }

        final NodeRef parentFolder =
                (NodeRef)
                        AuthenticationUtil.runAs(
                                new AuthenticationUtil.RunAsWork<Object>() {
                                    public Object doWork() {
                                        return createFolderStructure(nodeRef, username);
                                    }
                                },
                                AuthenticationUtil.getSystemUserName());

        final NodeRef result =
                (NodeRef)
                        AuthenticationUtil.runAs(
                                new AuthenticationUtil.RunAsWork<Object>() {
                                    public Object doWork() {
                                        return copyDocument(parentFolder, nodeRef);
                                        // source
                                    }
                                },
                                AuthenticationUtil.getSystemUserName());
        return result;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    public void processTranslatedFiles() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();
        processTranslations(yesterday);
        processUserNotification(yesterday);
    }

    private void processUserNotification(Date fromDate) {
        try {
            List<SearchResultNotify> translationsToProcess =
                    translationDaoService.getUserToNotify(fromDate);
            for (final SearchResultNotify searchResult : translationsToProcess) {
                if (searchResult
                        .getTargetPath()
                        .startsWith(ftpUrl)) // process only rows from server that init machine translation
                {
                    if (isTranslationProcessFinished(searchResult)) {
                        AuthenticationUtil.runAs(
                                new AuthenticationUtil.RunAsWork<Object>() {
                                    public Object doWork() {
                                        return notifyUser(searchResult);
                                    }
                                },
                                AuthenticationUtil.getSystemUserName());
                    }
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when getting  translated documents: ", e);
            }
        }
    }

    private Boolean notifyUser(SearchResultNotify searchResult) {
        if (searchResult.getPropertyQName() == null) {
            notifyUserForDocumentTranslation(searchResult);
        } else {
            notifyUserForPropertyTranslation(searchResult);
        }
        try {
            translationDaoService.markAsNotified(searchResult.getRequestID());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Can not mark row as proccesed:", e);
            }
        }
        return true;
    }

    private void notifyUserForPropertyTranslation(SearchResultNotify searchResult) {
        final MailTemplate mailtemplate;
        if (hasErrorTranslation(searchResult.getRequestID())) {
            mailtemplate = MailTemplate.UNSUCCESSFUL_PROPERTY_TRANSLATION;
        } else {
            mailtemplate = MailTemplate.SUCCESSFUL_PROPERTY_TRANSLATION;
        }
        sendNotificationMessage(mailtemplate, searchResult);
    }

    private void notifyUserForDocumentTranslation(SearchResultNotify searchResult) {
        if (hasErrorTranslation(searchResult.getRequestID())) {
            sendNotificationMessage(MailTemplate.UNSUCCESSFUL_DOCUMENT_TRANSLATION, searchResult);
        } else {
            sendNotificationMessage(MailTemplate.SUCCESSFUL_DOCUMENT_TRANSLATION, searchResult);
        }
    }

    private void sendNotificationMessage(
            MailTemplate mailTemplate, SearchResultNotify searchNotifyItem) {
        NodeRef currentRef = new NodeRef(searchNotifyItem.getDocumentID());
        NodeRef otherPerson = getPersonService().getPerson(searchNotifyItem.getUsername());
        Map<String, Object> model =
                getMailPreferencesService().buildDefaultModel(currentRef, otherPerson, null);
        model.put("targetLangs", searchNotifyItem.getTargetLangs());
        if (searchNotifyItem.getPropertyQName() != null
                && !searchNotifyItem.getPropertyQName().equals("")) {
            model.put(
                    "targetProperty",
                    org.alfresco.service.namespace.QName.createQName(searchNotifyItem.getPropertyQName())
                            .getLocalName());
        }

        MailWrapper mail = getMailPreferencesService().getDefaultMailTemplate(currentRef, mailTemplate);

        String from = mailService.getNoReplyEmailAddress();
        String to = (String) getNodeService().getProperty(otherPerson, ContentModel.PROP_EMAIL);

        boolean html = true;
        try {
            mailService.send(from, to, null, mail.getSubject(model), mail.getBody(model), html, false);
        } catch (MessagingException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        }
    }

    private boolean isTranslationProcessFinished(SearchResultNotify searchResult) {
        return (StringUtils.countOccurrencesOf(searchResult.getTargetLangs(), ",") + 1
                == searchResult.getTranslationCount());
    }

    private void processTranslations(Date fromDate) {
        try {
            List<SearchResult> translationsToProcess =
                    translationDaoService.getTranslationsToProcess(fromDate);
            for (SearchResult searchResult : translationsToProcess) {
                if (searchResult
                        .getTargetPath()
                        .startsWith(ftpUrl)) // process only rows from server that init machine translation
                {
                    processRow(searchResult);
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when getting  translated documents: ", e);
            }
        }
    }

    private void processRow(SearchResult searchResult) {
        if (searchResult.getPropertyQName() == null) {
            updateMLDocument(searchResult);
        } else {
            updateProperty(searchResult);
        }
        try {
            translationDaoService.markAsProccesed(
                    searchResult.getRequestID(), searchResult.getTargetLang());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Can not mark row as processed:", e);
            }
        }
    }

    private void updateProperty(SearchResult searchResult) {
        NodeRef docNodeRef = new NodeRef(searchResult.getDocumentID());
        if (!nodeService.exists(docNodeRef)) {
            return;
        }
        org.alfresco.service.namespace.QName propQname =
                org.alfresco.service.namespace.QName.createQName(searchResult.getPropertyQName());
        PropertyDefinition propDef = this.getDictionaryService().getProperty(propQname);

        if ((propDef != null && propDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))) {
            MLText mlText = getMLPropertyValue(docNodeRef, propQname);

            mlText.addValue(new Locale(searchResult.getTargetLang()), searchResult.getTranslatedText());
            getNodeService().setProperty(docNodeRef, propQname, mlText);
        }
    }

    private MLText getMLPropertyValue(
            final NodeRef nodeRef, final org.alfresco.service.namespace.QName propertyQname) {
        MLText properties = null;

        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(true);
            properties = (MLText) getNodeService().getProperty(nodeRef, propertyQname);
        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }

        return properties;
    }

    private void updateMLDocument(SearchResult searchResult) {
        NodeRef docNodeRef = new NodeRef(searchResult.getDocumentID());
        if (!nodeService.exists(docNodeRef)) {
            return;
        }
        if (multilingualContentService.isTranslation(docNodeRef)) {
            String documentName =
                    (String) getNodeService().getProperty(docNodeRef, ContentModel.PROP_NAME);
            String name = replaceLast(documentName, ".", "_" + searchResult.getTargetLang() + ".");

            Long id = (Long) getNodeService().getProperty(docNodeRef, ContentModel.PROP_NODE_DBID);

            String ftpName = String.valueOf(id) + "_" + searchResult.getTargetLang();
            int lastIndexOf = documentName.lastIndexOf(".");

            if (lastIndexOf > -1) {
                String extension = documentName.substring(lastIndexOf + 1);                
                ftpName = ftpName + "." + extension;     
            }

            NodeRef emptyTranslation =
                    multilingualContentService.addEmptyTranslation(
                            docNodeRef, name, new Locale(searchResult.getTargetLang()));

            NodeRef translationNodeRef =
                    getTranslationNodeRef(
                            searchResult.getTargetPath(), searchResult.getTargetLang(), ftpName);

            if (translationNodeRef != null) {
                byte[] binaryData = getContent(translationNodeRef);
                ContentWriter writer =
                        contentService.getWriter(emptyTranslation, ContentModel.PROP_CONTENT, true);
                writer.putContent(new ByteArrayInputStream(binaryData));
                getNodeService().addAspect(translationNodeRef, ContentModel.ASPECT_TEMPORARY, null);
                getNodeService().deleteNode(translationNodeRef);
            }
            ownableService.setOwner(emptyTranslation, searchResult.getUsername());
            ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(docNodeRef);
            NodeRef parent = childAssociationRef.getParentRef();
            // reapply rules on parent
            Action action = this.getActionService().createAction(ExecuteAllRulesActionExecuter.NAME);
            action.setParameterValue(ExecuteAllRulesActionExecuter.PARAM_EXECUTE_INHERITED_RULES, true);
            // Execute the action
            this.getActionService().executeAction(action, parent);
        }
    }

    private NodeRef getTranslationNodeRef(String ftpPath, String targetLang, String name) {
        NodeRef result = null;

        ftpPath = ftpPath.replace(ftpUrl, "");
        ftpPath = ftpPath.replace("/" + mtRootSpace + "/", "");
        String[] spaces = ftpPath.split("/");
        NodeRef currentNode = getManagementService().getMTNodeRef();
        for (String space : spaces) {
            if (!space.equals("")) {
                currentNode =
                        getNodeService().getChildByName(currentNode, ContentModel.ASSOC_CONTAINS, space);
                if (currentNode == null) {
                    break;
                }
            }
        }
        if (currentNode != null) {

            List<ChildAssociationRef> children = getNodeService().getChildAssocs(currentNode);
            for (ChildAssociationRef childAssoc : children) {
                NodeRef childNodeRef = childAssoc.getChildRef();
                String childName =
                        (String) getNodeService().getProperty(childNodeRef, ContentModel.PROP_NAME);
                if (childName.equalsIgnoreCase(name)) {
                    result = childNodeRef;
                    break;
                }
            }
        }

        return result;
    }

    private boolean hasErrorTranslation(String requestID) {
        try {
            return (translationDaoService.getCountOfErrorTranslation(requestID) > 0);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error checking for error translation: ", e);
            }
        }
        return false;
    }

    public MultilingualContentService getMultilingualContentService() {
        return multilingualContentService;
    }

    public void setMultilingualContentService(MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    public String getMtRootSpace() {
        return mtRootSpace;
    }

    public void setMtRootSpace(String mtRootSpace) {
        this.mtRootSpace = mtRootSpace;
    }

    public Set<String> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(Set<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    @Override
    public boolean canBeTranslated(String fileName) {
        boolean result = false;
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            String extension = fileName.substring(i + 1);
            //DIGITCIRCABC-5048 we have to call toLowerCase on the extension to manage UpperCase names.
            result = extension!=null && fileExtensions.contains(extension.toLowerCase());
        }
        return result;
    }

    @Override
    public void cleanTempSpace(int year, int month) {

        NodeRef mtRoNodeRef = getManagementService().getMTNodeRef();
        final NodeRef yearFolder =
                getNodeService()
                        .getChildByName(mtRoNodeRef, ContentModel.ASSOC_CONTAINS, String.valueOf(year));
        if (yearFolder != null) {
            final NodeRef monthFolder =
                    getNodeService()
                            .getChildByName(yearFolder, ContentModel.ASSOC_CONTAINS, String.valueOf(month));
            if (monthFolder != null) {
                nodeService.addAspect(monthFolder, ContentModel.ASPECT_TEMPORARY, null);
                nodeService.deleteNode(monthFolder);
            }
        }
    }

    public OwnableService getOwnableService() {
        return ownableService;
    }

    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
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

    /**
     * @return the nodePreferencesService
     */
    protected final MailPreferencesService getMailPreferencesService() {
        return mailPreferencesService;
    }

    /**
     * @param mailPreferencesService the mailPreferencesService to set
     */
    public final void setMailPreferencesService(MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public long getMaxFileSizeInBytes() {
        return maxFileSizeInBytes;
    }

    public void setMaxFileSizeInBytes(long maxFileSizeInBytes) {
        this.maxFileSizeInBytes = maxFileSizeInBytes;
    }

    @Override
    public long fileMaxSize() {
        return maxFileSizeInBytes;
    }

    @Override
    public Set<String> getAvailableFileExtensions() {
        return fileExtensions;
    }

    public ActionService getActionService() {
        return actionService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public MachineTranslationService getMachineTranslationService() {
        return machineTranslationService;
    }

    public void setMachineTranslationService(MachineTranslationService machineTranslationService) {
        this.machineTranslationService = machineTranslationService;
    }
}
