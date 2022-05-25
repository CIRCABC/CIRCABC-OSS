/*******************************************************************************
 * Copyright 2006 European Community
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 ******************************************************************************/
/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.module.circabc;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.repo.newsgroup.AbuseReportImpl;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.customisation.NodePreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.translation.TranslationService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.util.CommonUtils;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * A basic component that will be started for this module. This class is started only once by
 * default. A parameter "executeOnceOnly" can be use to force starting of this class.
 * <p>
 * This class is loaded after Spring configuration
 *
 * @author Clinckart Stephane
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 PersonDaoImpl was commented.
 */
public class CircabcComponent extends AbstractModuleComponent {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(CircabcComponent.class);
    private static final String COMPONENT_LOCK = "circabc-component-execute-lock";
    private static final String HEADER_ROOT_LOCK  = "header-root-lock";
    private static final String FAQS = "faqs";
    private static final String FAQS_LINKS = "faqsLinks";
    private static boolean initialized = false;
    private NodeService nodeService;
    private ManagementService managementService;
    private ProfileManagerServiceFactory profileManagerServiceFactory;
    private PersonService personService;
    private UserService userService;
    private CategoryService categoryService;
    private TransactionService transactionService;
    private MailPreferencesService mailPreferencesService;
    private NodePreferencesService nodePreferencesService;
    private TranslationService translationService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private LockService lockService;
    private String execute;
    private String forceTemplateUpdate;
    private boolean isExecute;
    private boolean isForceTemplateUpdate;

    private ImporterBootstrap importer;
    private Properties bootstrapView;
    private List<Properties> bootstrapViews;

    /**
     * Gets the value of the initialized
     *
     * @return the initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    protected void executeInternal() throws Throwable {
        final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

        addConverters();

        if (!isExecute) {
            if (isForceTemplateUpdate) {
                boolean isLocked = lockService.tryLock(COMPONENT_LOCK);
                if (!isLocked) {
                    initialized = true;
                    return;
                }

                try {
                    runUpdateRootPreferance(txnHelper);
                    runUpdateSupportMailTemplate(txnHelper);
                } finally {
                    lockService.unlock(COMPONENT_LOCK);
                }
                // Run this, when the component execution is finished
                initialized = true;
                return;
            } else {
                initialized = true;
                return;
            }
        }

        boolean isLocked = lockService.tryLock(COMPONENT_LOCK);
        if (!isLocked) {
            initialized = true;
            return;
        }

        try {
            bootstrap();

            runSetGuestReadPermissionOnVersionStoreCallback(txnHelper);

            runCreateCircabcCallback(txnHelper);

            runCreateCircabcAdminUser(txnHelper);

            runUpdateRootPreferance(txnHelper);

            runCreateTemplateUser(txnHelper);

            runCreateMachineTranslationUser(txnHelper);

            runCreateMachineTranslationSpace(txnHelper);

            runMakeStatisticFolderSecure(txnHelper);

            runCreateMachineTranslationSpaceEUSurvey(txnHelper);

            runUpdateSupportMailTemplate(txnHelper);

            runCreateFaqsFolder(txnHelper);

            runCreateFaqLinksFolder(txnHelper);

        } finally {
            lockService.unlock(COMPONENT_LOCK);
        }
        // Run this, when the component execution is finished
        initialized = true;

    }

    private void runMakeStatisticFolderSecure(final RetryingTransactionHelper txnHelper) {

        final RetryingTransactionCallback<Object> secureStatFolderCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        makeSecureStatFolder();
                        return null;
                    }
                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(secureStatFolderCallback, false, true);

                return null;
            }

        }, AuthenticationUtil.getAdminUserName());

    }

    private void makeSecureStatFolder() {

        String statisticsDictionaryFolderName = "statistics";
        NodeRef statisticsFolder =
                nodeService.getChildByName(getManagementService().getCircabcDictionaryNodeRef(),
                        ContentModel.ASSOC_CONTAINS, statisticsDictionaryFolderName);

        if (statisticsFolder != null) {
            permissionService.setInheritParentPermissions(statisticsFolder, false);

            final String prefixedUserGroupName = PermissionService.GROUP_PREFIX
                    + getProfileManagerServiceFactory().getCircabcRootProfileManagerService()
                    .getInvitedUsersGroupName(getManagementService().getCircabcNodeRef());
            permissionService.setPermission(statisticsFolder, prefixedUserGroupName,
                    PermissionService.CONTRIBUTOR, true);
        }

    }

    /**
     * Method added for external connectors like Autonomy's Alfresco connector that need to convert
     * some custom values.
     */
    private void addConverters() {

        DefaultTypeConverter.INSTANCE.addConverter(ArrayList.class, String.class,
                new TypeConverter.Converter<ArrayList, String>() {

                    public String convert(ArrayList source) {

                        StringBuilder result = new StringBuilder();

                        try {
                            for (Object element : source) {
                                result.append(DefaultTypeConverter.INSTANCE.convert(String.class, element)).append(" ");
                            }

                            return result.toString().trim();
                        } catch (Exception e) {
                            throw new TypeConversionException("Failed to convert ArrayList to String: " + source,
                                    e);
                        }
                    }
                });

        DefaultTypeConverter.INSTANCE.addConverter(HashMap.class, String.class,
                new TypeConverter.Converter<HashMap, String>() {

                    public String convert(HashMap source) {

                        StringBuilder result = new StringBuilder();

                        try {
                            for (Entry element : (Set<Entry>) source.entrySet()) {
                                result.append(DefaultTypeConverter.INSTANCE.convert(String.class, element.getValue())).append(" ");
                            }

                            return result.toString().trim();
                        } catch (Exception e) {
                            throw new TypeConversionException("Failed to convert HashMap to String: " + source,
                                    e);
                        }
                    }
                });

        // Converters used by the CircabcExporter

        DefaultTypeConverter.INSTANCE.addConverter(AbuseReportImpl.class, String.class,
                new TypeConverter.Converter<AbuseReportImpl, String>() {

                    public String convert(AbuseReportImpl source) {

                        try {
                            return CommonUtils.toBase64String(source);
                        } catch (Exception e) {
                            throw new TypeConversionException(
                                    "Failed to convert " + source.getClass().getName() + " to String: " + source, e);
                        }
                    }
                });
    }

    private void runCreateTemplateUser(final RetryingTransactionHelper txnHelper) {
        final RetryingTransactionCallback<Object> createTemplateUserCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        createTemplateUser();
                        return null;
                    }

                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(createTemplateUserCallback, false, true);

                return null;
            }

        }, AuthenticationUtil.getAdminUserName());
    }

    private void runCreateMachineTranslationUser(final RetryingTransactionHelper txnHelper) {
        final RetryingTransactionCallback<Object> createMTUserCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        createMTUser();
                        return null;
                    }

                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(createMTUserCallback, false, true);

                return null;
            }

        }, AuthenticationUtil.getAdminUserName());
    }

    private void runUpdateRootPreferance(final RetryingTransactionHelper txnHelper) {
        final RetryingTransactionCallback<Object> updateRootPreferanceCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        updateRootPreferance();
                        return null;
                    }

                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(updateRootPreferanceCallback, false, true);

                return null;
            }

        }, AuthenticationUtil.getAdminUserName());
    }

    private void runCreateCircabcAdminUser(final RetryingTransactionHelper txnHelper) {
        final RetryingTransactionCallback<Object> createCircabcAdminUserCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        createCircabcAdminUser();
                        return null;
                    }

                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(createCircabcAdminUserCallback, false, true);

                return null;
            }

        }, AuthenticationUtil.getAdminUserName());
    }

    private void runCreateCircabcCallback(final RetryingTransactionHelper txnHelper) {
        final RetryingTransactionCallback<Object> createCircabcCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {

                        createCircabc();
                        return null;
                    }

                };
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(createCircabcCallback, false, true);

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private void runCreateMachineTranslationSpace(final RetryingTransactionHelper txnHelper) {
        final RetryingTransactionCallback<Object> createMTCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {

                        createMT();
                        return null;
                    }

                };
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(createMTCallback, false, true);

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    private void runCreateMachineTranslationSpaceEUSurvey(final RetryingTransactionHelper txnHelper) {
        final RetryingTransactionCallback<Object> createMTCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {

                        createMTEUS();
                        return null;
                    }

                };
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(createMTCallback, false, true);

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    private void runSetGuestReadPermissionOnVersionStoreCallback(
            final RetryingTransactionHelper txnHelper) {
        final RetryingTransactionCallback<Object> setGuestReadPermissionOnVersionStoreCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {

                        setGuestReadPermissionOnVersionStore();
                        return null;
                    }

                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(setGuestReadPermissionOnVersionStoreCallback, false, true);

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    private void runCreateFaqsFolder(final RetryingTransactionHelper txnHelper) {

        final RetryingTransactionCallback<Object> setCreateFaqsFolderCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        setCreateFaqsFolder();
                        return null;
                    }
                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(setCreateFaqsFolderCallback, false, true);

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());

    }

    private void setCreateFaqsFolder() {

        NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef faqsRef = nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, FAQS);

        if (faqsRef == null) {
            ChildAssociationRef faqsAssocRef = nodeService.createNode(ddRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.ALFRESCO_URI, FAQS), ContentModel.TYPE_FOLDER);
            nodeService.setProperty(faqsAssocRef.getChildRef(), ContentModel.PROP_NAME, FAQS);
            permissionService.setPermission(faqsAssocRef.getChildRef(), "guest", "Consumer", true);

        }
    }

    private void runCreateFaqLinksFolder(final RetryingTransactionHelper txnHelper) {

        final RetryingTransactionCallback<Object> setCreateFaqsFolderCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        setCreateFaqLinksFolder();
                        return null;
                    }
                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(setCreateFaqsFolderCallback, false, true);

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());

    }

    private void setCreateFaqLinksFolder() {

        NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef faqLinksRef = nodeService
                .getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, FAQS_LINKS);

        if (faqLinksRef == null) {
            ChildAssociationRef faqsAssocRef = nodeService.createNode(ddRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.ALFRESCO_URI, FAQS_LINKS), ContentModel.TYPE_FOLDER);
            nodeService.setProperty(faqsAssocRef.getChildRef(), ContentModel.PROP_NAME, FAQS_LINKS);
            permissionService.setPermission(faqsAssocRef.getChildRef(), "guest", "Consumer", true);

        }
    }

    private void runUpdateSupportMailTemplate(final RetryingTransactionHelper txnHelper) {

        final RetryingTransactionCallback<Object> setGuestReadPermissionOnMailTemplateCallback =
                new RetryingTransactionCallback<Object>() {
                    public Object execute() throws Throwable {
                        setGuestReadPermissionOnMailTemplate();
                        return null;
                    }
                };

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() {

                txnHelper.doInTransaction(setGuestReadPermissionOnMailTemplateCallback, false, true);

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());

    }

    private void setGuestReadPermissionOnMailTemplate() {

        NodeRef ddRef = managementService.getCircabcDictionaryNodeRef();
        NodeRef templatesRef =
                nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, "templates");

        if (templatesRef != null) {
            NodeRef mailsRef =
                    nodeService.getChildByName(templatesRef, ContentModel.ASSOC_CONTAINS, "mails");

            if (mailsRef != null) {
                NodeRef supportRef =
                        nodeService.getChildByName(mailsRef, ContentModel.ASSOC_CONTAINS, "supportRequest");

                if (supportRef != null) {
                    NodeRef templateMailRef =
                            nodeService.getChildByName(supportRef, ContentModel.ASSOC_CONTAINS, "default.ftl");

                    permissionService.setPermission(templateMailRef, "guest", "Consumer", true);
                }
            }
        }
    }

    private void updateRootPreferance() {
        nodePreferencesService.updateRootReference();
    }

    private void createTemplateUser() {
        final CircabcUserDataBean templateUser = mailPreferencesService.getTemplateUserDetails();
        if (!personService.personExists(templateUser.getUserName())) {
            userService.createUser(templateUser, true);

            if (logger.isInfoEnabled()) {
                logger.info("Circabc template dirty user successfully created");
            }
        }
    }

    private void createMTUser() {
        final CircabcUserDataBean mtUser = translationService.getMTUserDetails();
        if (!personService.personExists(mtUser.getUserName())) {
            userService.createUser(mtUser, true);
            userService.setPassword(mtUser.getUserName(), mtUser.getPassword().toCharArray());

            if (logger.isInfoEnabled()) {
                logger.info("Circabc template dirty user successfully created");
            }
        }
    }

    private void createCircabc() {
        if (managementService.getCircabcNodeRef() == null) {
            // create it

            final NodeRef companyHome = managementService.getCompanyHomeNodeRef();
            final String description = CircabcConfiguration
                    .getProperty(CircabcConfiguration.CIRCABC_ROOT_NODE_DESCRIPTION_PROPERTIES);
            final String title =
                    CircabcConfiguration.getProperty(CircabcConfiguration.CIRCABC_ROOT_NODE_TITLE_PROPERTIES);

            final NodeRef circabcNodeRef = managementService.createCircabc(companyHome);

            // apply the uifacets aspect - icon, title and description props
            final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
            uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-default");
            uiFacetsProps.put(ContentModel.PROP_TITLE, title);
            uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, description);

            nodeService.addAspect(circabcNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        }
    }

    private void createMT() {
        if (managementService.getMTNodeRef() == null) {
            // create it

            final NodeRef companyHome = managementService.getCompanyHomeNodeRef();
            final String description =
                    CircabcConfiguration.getProperty(CircabcConfiguration.MT_ROOT_NODE_NAME_PROPERTIES);
            final String title =
                    CircabcConfiguration.getProperty(CircabcConfiguration.MT_ROOT_NODE_NAME_PROPERTIES);

            final NodeRef mtNodeRef = managementService.createMTSpace(companyHome);

            // apply the uifacets aspect - icon, title and description props
            final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
            uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-default");
            uiFacetsProps.put(ContentModel.PROP_TITLE, title);
            uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, description);

            nodeService.addAspect(mtNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
            permissionService.setInheritParentPermissions(mtNodeRef, false);
            final CircabcUserDataBean mtUser = translationService.getMTUserDetails();
            permissionService.setPermission(mtNodeRef, mtUser.getUserName(),
                    PermissionService.CONTRIBUTOR, true);
        }
    }

    private void createMTEUS() {
        if (CircabcConfig.ENT && !CircabcConfig.ECHA) {
            final NodeRef circabcHome = managementService.getCircabcNodeRef();
            if (circabcHome != null) {

                String mtRootSpaceName =
                        CircabcConfiguration.getProperty(CircabcConfiguration.MT_ROOT_NODE_NAME_PROPERTIES);

                NodeRef mtNodeRef =
                        nodeService.getChildByName(circabcHome, ContentModel.ASSOC_CONTAINS, mtRootSpaceName);
                if (mtNodeRef == null) {
                    // create it

                    final String description =
                            CircabcConfiguration.getProperty(CircabcConfiguration.MT_ROOT_NODE_NAME_PROPERTIES);
                    final String title =
                            CircabcConfiguration.getProperty(CircabcConfiguration.MT_ROOT_NODE_NAME_PROPERTIES);

                    mtNodeRef = managementService.createMTSpace(circabcHome);

                    // apply the uifacets aspect - icon, title and description
                    // props
                    final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
                    uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-default");
                    uiFacetsProps.put(ContentModel.PROP_TITLE, title);
                    uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, description);

                    nodeService.addAspect(mtNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
                    permissionService.setInheritParentPermissions(mtNodeRef, false);
                    final CircabcUserDataBean mtUser = translationService.getMTUserDetails();
                    permissionService.setPermission(mtNodeRef, mtUser.getUserName(),
                            PermissionService.CONTRIBUTOR, true);

                }
            }

        }

    }

    private void createCircabcAdminUser() {
        final NodeRef circabcNodeRef = managementService.getCircabcNodeRef();
        String adminName =
                CircabcConfiguration.getProperty(CircabcConfiguration.ROOT_ADMIN_NAME_PROPERTIES);
        if (personService.getUserNamesAreCaseSensitive()) {
            adminName = adminName.toLowerCase();
        }

        String adminPassword =
        CircabcConfiguration.getProperty(CircabcConfiguration.ROOT_ADMIN_PWD_PROPERTIES);
        if (personService.getUserNamesAreCaseSensitive()) {
          adminPassword = adminPassword.toLowerCase();
        }
    

        String adminMail =
                CircabcConfiguration.getProperty(CircabcConfiguration.ROOT_ADMIN_MAIL_PROPERTIES);
        if (personService.getUserNamesAreCaseSensitive()) {
            adminMail = adminMail.toLowerCase();
        }

        if ((circabcNodeRef != null) && !personService.personExists(adminName)) {

            final String profile = CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN;
            CircabcUserDataBean circaBCAdmin = new CircabcUserDataBean();
            if (CircabcConfig.OSS) {
                circaBCAdmin.setUserName(adminName);
                circaBCAdmin.setFirstName(adminName);
                circaBCAdmin.setLastName(adminName);
                circaBCAdmin.setEmail(adminMail);
                circaBCAdmin.setPassword(adminPassword);
            } else {
                final CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataByUid(adminName);
                circaBCAdmin.copyLdapProperties(ldapUserDetail);
            }

            circaBCAdmin.setHomeSpaceNodeRef(circabcNodeRef);
            userService.createUser(circaBCAdmin, true);

            final CircabcRootProfileManagerService circabcRootProfileManagerService =
                    getProfileManagerServiceFactory().getCircabcRootProfileManagerService();
            circabcRootProfileManagerService.addPersonToProfile(circabcNodeRef,
                    circaBCAdmin.getUserName(), profile);
        }
    }

    /**
     * Workaround: Execute this method at the startup of Circabc to allow the Guest authority to
     * access to the version store.
     */
    private void setGuestReadPermissionOnVersionStore() {

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "The Read permission is being setted to the Guest Authority on the version store...");
        }

        final NodeRef versionRootNodeRef = nodeService
                .getRootNode(this.serviceRegistry.getVersionService().getVersionStoreReference());

        this.serviceRegistry.getPermissionService().setPermission(versionRootNodeRef,
                CircabcConstant.GUEST_AUTHORITY, PermissionService.READ, true);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "The Read permission successfully setted to the Guest Authority on the version store");
        }
    }

    protected boolean needToCreateCircabc() {
        return managementService.getCircabcNodeRef() == null;
    }

    /**
     * @return the profileManagerServiceFactory
     */
    protected final ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return profileManagerServiceFactory;
    }

    /**
     * @param profileManagerServiceFactory the profileManagerServiceFactory to set
     */
    public final void setProfileManagerServiceFactory(
            final ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    /**
     * @return the transactionService
     */
    public final TransactionService getTransactionService() {
        return transactionService;
    }

    /**
     * @param transactionService the transactionService to set
     */
    public final void setTransactionService(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * @return the categoryService
     */
    public final CategoryService getCategoryService() {
        return categoryService;
    }

    /**
     * @param categoryService the categoryService to set
     */
    public void setCategoryService(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * @return the managementService
     */
    public final ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @return the nodeService
     */
    public final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the personService
     */
    public final PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return the userService
     */
    public final UserService getUserService() {
        return userService;
    }

    /**
     * @param userService the UserService to set
     */
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    /**
     * @param mailPreferencesService the mailPreferencesService to set
     */
    public final void setMailPreferencesService(final MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    public final void setNodePreferencesService(NodePreferencesService nodePreferencesService) {
        this.nodePreferencesService = nodePreferencesService;
    }

    public String getExecute() {
        return execute;
    }

    public void setExecute(String execute) {

        this.execute = execute;
        isExecute = Boolean.valueOf(execute);
    }

    public String getForceTemplateUpdate() {
        return forceTemplateUpdate;
    }

    public void setForceTemplateUpdate(String forceTemplateUpdate) {

        this.forceTemplateUpdate = forceTemplateUpdate;
        isForceTemplateUpdate = Boolean.valueOf(forceTemplateUpdate);
    }

    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public LockService getLockService() {
        return lockService;
    }

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    public void setImporter(ImporterBootstrap importer)
    {
        this.importer = importer;
    }


    public void setBootstrapView(Properties bootstrapView)
    {
        this.bootstrapView = bootstrapView;
    }

    public void setBootstrapViews(List<Properties> bootstrapViews)
    {
        this.bootstrapViews = bootstrapViews;
    }

    private void bootstrap() {

        boolean isLocked = lockService.tryLockForever(HEADER_ROOT_LOCK);
        if (!isLocked) {
            return;
        }

        try {
            List<Properties> views = new ArrayList<>(1);

            if (bootstrapViews != null) {
                views.addAll(bootstrapViews);
            }

            if (bootstrapView != null) {
                views.add(bootstrapView);
            }
            importer.setBootstrapViews(views);
            importer.setUseExistingStore(true);

            importer.bootstrap();

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Ignore error during bootstrap", e);
            }
        }

    }
}
