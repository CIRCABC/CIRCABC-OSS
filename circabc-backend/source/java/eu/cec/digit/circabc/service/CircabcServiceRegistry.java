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
package eu.cec.digit.circabc.service;

import eu.cec.digit.circabc.repo.app.SecurityService;
import eu.cec.digit.circabc.repo.ares.AresBridgeServiceImpl;
import eu.cec.digit.circabc.service.admin.debug.ServerConfigurationService;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.bulk.BulkService;
import eu.cec.digit.circabc.service.bulk.indexes.IndexService;
import eu.cec.digit.circabc.service.compress.ZipService;
import eu.cec.digit.circabc.service.customisation.ApplicationCustomisationService;
import eu.cec.digit.circabc.service.customisation.NodePreferencesService;
import eu.cec.digit.circabc.service.customisation.logo.LogoPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.nav.NavigationConfigService;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.event.EventService;
import eu.cec.digit.circabc.service.keyword.KeywordsService;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.mail.MailToMembersService;
import eu.cec.digit.circabc.service.namespace.CircabcNameSpaceService;
import eu.cec.digit.circabc.service.newsgroup.AttachementService;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.service.notification.NotificationManagerService;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.service.profile.*;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionService;
import eu.cec.digit.circabc.service.sharespace.ShareSpaceService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.support.SupportService;
import eu.cec.digit.circabc.service.translation.TranslationService;
import eu.cec.digit.circabc.service.user.UserService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This interface represents the registry of public Repository Services. The registry provides
 * meta-data about each service and provides access to the service interface.
 *
 * @author Clinckart Stephane
 */
public interface CircabcServiceRegistry {

    String CIRCABC_SERVICE_REGISTRY = "circabcServiceRegistry";

    QName ALFRESCO_REGISTRY_SERVICE =
            QName.createQName(NamespaceService.ALFRESCO_URI, "ServiceRegistry");

    QName ROOT_PROFILE_MANAGER_SERVICE =
            QName.createQName(
                    CircabcNameSpaceService.CEC_DIGIT_URI,
                    CircabcRootProfileManagerService.PROXIED_SERVICE_NAME);
    QName CATEGORY_PROFILE_MANAGER_SERVICE =
            QName.createQName(
                    CircabcNameSpaceService.CEC_DIGIT_URI,
                    CategoryProfileManagerService.PROXIED_SERVICE_NAME);
    QName IGROOT_PROFILE_MANAGER_SERVICE =
            QName.createQName(
                    CircabcNameSpaceService.CEC_DIGIT_URI, IGRootProfileManagerService.PROXIED_SERVICE_NAME);
    QName LIBRARY_PROFILE_MANAGER_SERVICE =
            QName.createQName(
                    CircabcNameSpaceService.CEC_DIGIT_URI, LibraryProfileManagerService.PROXIED_SERVICE_NAME);
    QName NEWSGROUP_PROFILE_MANAGER_SERVICE =
            QName.createQName(
                    CircabcNameSpaceService.CEC_DIGIT_URI,
                    NewsGroupProfileManagerService.PROXIED_SERVICE_NAME);
    QName SURVEY_PROFILE_MANAGER_SERVICE =
            QName.createQName(
                    CircabcNameSpaceService.CEC_DIGIT_URI, SurveyProfileManagerService.PROXIED_SERVICE_NAME);
    QName INFORMATION_PROFILE_MANAGER_SERVICE =
            QName.createQName(
                    CircabcNameSpaceService.CEC_DIGIT_URI,
                    InformationProfileManagerService.PROXIED_SERVICE_NAME);
    QName EVENT_PROFILE_MANAGER_SERVICE =
            QName.createQName(
                    CircabcNameSpaceService.CEC_DIGIT_URI, EventProfileManagerService.PROXIED_SERVICE_NAME);

    QName PROFILE_MANAGER_SERVICE_FACTORY =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ProfileManagerServiceFactory");
    QName MANAGEMENT_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ManagementService");
    QName KEYWORDS_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "KeywordsService");
    QName CIRCABC_USER_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "UserService");
    QName MAIL_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "CircabcMailService");
    QName MAIL_TO_MEMBERS_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "MailToMembersService");
    QName BULK_SERVICE = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "BulkService");
    QName NOTIFICATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "NotificationService");
    QName NOTIFICATION_SUBSCRIPTION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "NotificationSubscriptionService");
    QName NOTIFICATION_MANAGER_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "NotificationManagerService");
    QName DYNAMIC_PROPERTY_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "DynamicPropertiesService");
    QName SERVER_CONFIGURATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ServerConfigurationService");
    QName NODE_CUSTOMISATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "NodePreferencesService");
    QName MAIL_CUSTOMISATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "MailPreferencesService");
    QName NAVIGATION_CUSTOMISATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "NavigationPreferencesService");
    QName NAVIGATION_CONFIGURATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "navigationConfigService");
    QName LOGO_CUSTOMISATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "LogoPreferencesService");
    QName EVENT_SERVICE = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "EventService");
    QName SHARE_SPACE_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ShareSpaceService");
    QName NON_SECURED_EVENT_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "eventService");
    QName LOCK_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "CircabcLockService");
    QName MODERATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ModerationService");
    QName SUPPORT_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "SupportService");
    QName ATTACHEMENT_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "AttachementService");
    QName STATISTICS_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "StatisticsService");
    QName ASYNC_THREAD_POOL_EXECUTOR =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "AsyncThreadPoolExecutor");
    QName TRANSLATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "TranslationService");
    QName APPLICATION_CUSTOMISATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "ApplicationCustomisationService");
    QName NON_SECURED_APPLICATION_CUSTOMISATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "applicationCustomisationService");

    QName NON_SECURED_PROFILE_MANAGER_SERVICE_FACTORY =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "profileManagerServiceFactory");
    QName NON_SECURED_LOG_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "logService");
    QName NON_SECURED_ZIP_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "zipService");
    QName NON_SECURED_BULK_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "bulkService");
    QName NON_SECURED_INDEX_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "indexService");
    QName NON_SECURED_DICTIONARY_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "dictionaryService");
    QName NON_SECURED_TRANSACTION_SERVICE =
            QName.createQName(NamespaceService.ALFRESCO_URI, "transactionService");
    QName NON_SECURED_NODE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "nodeService");
    QName NON_SECURED_SEARCH_SERVICE =
            QName.createQName(NamespaceService.ALFRESCO_URI, "searchService");
    QName POLICY_BEHAVIOUR_FILTER =
            QName.createQName(NamespaceService.ALFRESCO_URI, "policyBehaviourFilter");
    QName NON_SECURED_PERSON_SERVICE =
            QName.createQName(NamespaceService.ALFRESCO_URI, "personService");
    QName NON_SECURED_TRANSLATION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "translationService");
    QName CIRCABC_RENDITION_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "circabcRenditionService");
    QName RENDITION_DAO_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "renditionDaoService");

    QName CIRCABC_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "CircabcService");
    QName NON_SECURED_CIRCABC_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "circabcService");

    QName SECURITY_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "SecurityService");

    QName CIRCABC_RENDITION_HELPER =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "circabcRenditionHelper");

    QName CIRCABC_ARES_BRIDGE_SERVICE =
            QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "aresBridgeService");

    /**
     * Get the list of services provided by Circabc
     *
     * @return list of provided Services
     */
    @NotAuditable
    Collection<QName> getServices();

    /**
     * Is the specified service provided by Circabc?
     *
     * @param service name of service to test provision of
     * @return true => provided, false => not provided
     */
    @NotAuditable
    boolean isServiceProvided(QName service);

    /**
     * Get the specified service.
     *
     * @param service name of service to retrieve
     * @return the service interface (must cast to interface as described in service meta-data)
     */
    @NotAuditable
    Object getService(QName service);

    /**
     * @return the ProfileManagerServiceFactory service
     */
    @NotAuditable
    ProfileManagerServiceFactory getProfileManagerServiceFactory();

    /**
     * @return the CircabcRootProfileManagerServie service
     */
    @NotAuditable
    CircabcRootProfileManagerService getCircabcRootProfileManagerService();

    /**
     * @return the CategoryProfileManagerService service
     */
    @NotAuditable
    CategoryProfileManagerService getCategoryProfileManagerService();

    /**
     * @return the IGRootProfileManagerService service
     */
    @NotAuditable
    IGRootProfileManagerService getIGRootProfileManagerService();

    /**
     * @return the LibraryProfileManagerService service
     */
    @NotAuditable
    LibraryProfileManagerService getLibraryProfileManagerService();

    /**
     * @return the NewsGroupProfileManagerService service
     */
    @NotAuditable
    NewsGroupProfileManagerService getNewsGroupProfileManagerService();

    /**
     * @return the SurveyProfileManagerService service
     */
    @NotAuditable
    SurveyProfileManagerService getSurveyProfileManagerService();

    /**
     * @return the EventProfileManagerService service
     */
    @NotAuditable
    EventProfileManagerService getEventProfileManagerService();

    /**
     * @return the InformationProfileManagerService service
     */
    @NotAuditable
    InformationProfileManagerService getInformationProfileManagerService();

    /**
     * @return the global circabc management service
     */
    @NotAuditable
    ManagementService getManagementService();

    /**
     * @return the keywords service
     */
    @NotAuditable
    KeywordsService getKeywordsService();

    /**
     * @return the user service for circabc
     */
    @NotAuditable
    UserService getUserService();

    @NotAuditable
    MailService getMailService();

    @NotAuditable
    MailToMembersService getMailToMembersService();

    /**
     * @return the bulk service
     */
    @NotAuditable
    BulkService getBulkService();

    /**
     * @return the bulk service
     */
    @NotAuditable
    BulkService getNonSecuredBulkService();

    /**
     * @return the Notification Subscription Service
     */
    @NotAuditable
    NotificationSubscriptionService getNotificationSubscriptionService();

    /**
     * @return the Notification Service
     */
    @NotAuditable
    NotificationService getNotificationService();

    /**
     * @return the Dynamic Properties Service
     */
    @NotAuditable
    DynamicPropertyService getDynamicPropertieService();

    /**
     * @return the Server Configuration Service
     */
    @NotAuditable
    ServerConfigurationService getServerConfigurationService();

    /**
     * @return the Node Preferences Service for customization purposes
     */
    @NotAuditable
    NodePreferencesService getNodePreferencesService();

    /**
     * @return the Navigation Preferences Service for customization purposes
     */
    @NotAuditable
    NavigationPreferencesService getNavigationPreferencesService();

    /**
     * @return the Navigation Preferences config Service
     */
    @NotAuditable
    NavigationConfigService getNavigationConfigService();

    /**
     * @return the Mail Preferences Service for customization purposes
     */
    @NotAuditable
    MailPreferencesService getMailPreferencesService();

    /**
     * @return the Logo Preferences Service for customization purposes
     */
    @NotAuditable
    LogoPreferencesService getLogoPreferencesService();

    /**
     * @return the Event Service
     */
    @NotAuditable
    EventService getEventService();

    @NotAuditable
    EventService getNonSecureEventService();

    /**
     * @return the SharedSpace Service
     */
    @NotAuditable
    ShareSpaceService getShareSpaceService();

    /**
     * @return the non secure ProfileManagerServiceFactory service
     */
    @NotAuditable
    ProfileManagerServiceFactory getNonSecureProfileManagerServiceFactory();

    /**
     * @return the non secure service
     */
    @NotAuditable
    LogService getLogService();

    /**
     * @return the non secure service
     */
    @NotAuditable
    LockService getLockService();

    /**
     * @return the translation service
     */
    @NotAuditable
    TranslationService getTranslationService();

    /**
     * @return the translation service
     */
    @NotAuditable
    TranslationService getNonSecuredTranslationService();

    /**
     * @return the moderation service
     */
    @NotAuditable
    ModerationService getModerationService();

    /**
     * @return the attachment service
     */
    @NotAuditable
    AttachementService getAttachementService();

    /**
     * @return the support service
     */
    @NotAuditable
    SupportService getSupportService();

    /**
     * @return the alfresco service registry
     */
    @NotAuditable
    ServiceRegistry getAlfrescoServiceRegistry();

    /**
     * @return the non secure zipservice
     */
    @NotAuditable
    ZipService getNonSecuredZipService();

    /**
     * @return the non secure indexservice
     */
    @NotAuditable
    IndexService getNonSecuredIndexService();

    /**
     * @return the non secure dictionaryService
     */
    @NotAuditable
    DictionaryService getNonSecuredDictionaryService();

    /**
     * @return the non secure Transaction Service
     */
    @NotAuditable
    TransactionService getNonSecuredTransactionService();

    /**
     * @return the non secure Node Service
     */
    @NotAuditable
    NodeService getNonSecuredNodeService();

    /**
     * @return the non secure Search Service
     */
    @NotAuditable
    SearchService getNonSecuredSearchService();

    /**
     * @return the policy Behaviour Filter
     */
    @NotAuditable
    BehaviourFilter getPolicyBehaviourFilter();

    /**
     * @return the non secure Person Service
     */
    @NotAuditable
    PersonService getNonSecuredPersonService();

    /**
     * @return the non secure Notification Manager Service
     */
    @NotAuditable
    NotificationManagerService getNotificationManagerService();

    /**
     * @return thread pool executor in order to execute long running wai dialog action async
     */
    @NotAuditable
    ThreadPoolExecutor getAsyncThreadPoolExecutor();

    /**
     * @return the application customisation service used to get the custom logo banner for examle.
     */
    @NotAuditable
    ApplicationCustomisationService getApplicationCustomisationService();

    @NotAuditable
    ApplicationCustomisationService getNonSecuredApplicationCustomisationService();

    @NotAuditable
    CircabcService getCircabcService();

    @NotAuditable
    CircabcService getNonSecuredCircabcService();

    @NotAuditable
    SecurityService getSecurityService();

    @NotAuditable
    CircabcRenditionService getCircabcRenditionService();

    @NotAuditable
    BehaviourFilter getBehaviourFilter();

    @NotAuditable
    AresBridgeServiceImpl getAresBridgeService();
}
