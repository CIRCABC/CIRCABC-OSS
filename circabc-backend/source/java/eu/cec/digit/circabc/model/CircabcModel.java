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
package eu.cec.digit.circabc.model;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

/**
 * Constants for models used in Circabc
 *
 * @author atadian
 */
public interface CircabcModel extends BaseCircabcModel {

    /**
     * Circabc Model Prefix
     */
    String CIRCABC_MODEL_PREFIX = "ci";


    /**
     * Circabc model namespace
     */
    String CIRCABC_CONTENT_MODEL_1_0_URI = CIRCABC_NAMESPACE + "/model/content/1.0";

    /**
     * Circabc childs node Aspect name
     */
    QName ASPECT_CIRCABC_MANAGEMENT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circabcManagement");
    /**
     * Circabc Root node Aspect name
     */
    QName ASPECT_CIRCABC_ROOT = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaBC");
    /**
     * Library Aspect name
     */
    QName ASPECT_CATEGORY = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategory");
    /**
     * Interest Group root Aspect name
     */
    QName ASPECT_IGROOT = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRoot");
    /**
     * Lirary Root Aspect Name
     */
    QName ASPECT_LIBRARY_ROOT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaLibraryRoot");
    /**
     * Lirary childs Aspect Name
     */
    QName ASPECT_LIBRARY =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaLibrary");
    /**
     * NewsGroup root Aspect name
     */
    QName ASPECT_NEWSGROUP_ROOT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaNewsGroupRoot");
    /**
     * NewsGroup childs Aspect name
     */
    QName ASPECT_NEWSGROUP =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaNewsGroup");
    /**
     * Survey Root Aspect name
     */
    QName ASPECT_SURVEY_ROOT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaSurveyRoot");
    /**
     * Survey Childs Aspect name
     */
    QName ASPECT_SURVEY =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaSurvey");
    /**
     * Information Root Aspect name
     */
    QName ASPECT_INFORMATION_ROOT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circabcInformationRoot");
    /**
     * Information Childs Aspect name
     */
    QName ASPECT_INFORMATION =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circabcInformation");

    /**
     * Information NEWS Childs Aspect name
     */
    QName ASPECT_INFORMATION_NEWS =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circabcInformationNews");

    /**
     * Event Root Aspect name
     */
    QName ASPECT_EVENT_ROOT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circabcEventRoot");
    /**
     * Event Childs Aspect name
     */
    QName ASPECT_EVENT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circabcEvent");
    /**
     * Shared space aspect
     */
    QName ASPECT_SHARED_SPACE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circabcSharedSpace");
    /**
     * Shared space aspect
     */
    QName ASPECT_REVISIONABLE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "revisionable");

    /**
     * Profile addon importable aspect
     */
    QName ASPECT_PROFILE_IMPORTABLE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "importable");

    QName ASPECT_NOTIFY_PASTE_ALL =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "notifyPasteAll");
    QName ASPECT_NOTIFY_PASTE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "notifyPaste");

    /**
     * Category Headers Type name
     */
    QName TYPE_CATEGORY_HEADER = ContentModel.TYPE_CATEGORY;
    /**
     * Directory Root type name
     */
    QName TYPE_DIRECTORY_SERVICE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaDirectoryRoot");

    /**
     * Circabc root profile type
     */
    QName TYPE_CIRCABC_ROOT_PROFILE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaBCProfile");
    /**
     * Category profile type
     */
    QName TYPE_CATEGORY_PROFILE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryProfile");
    /**
     * Information NEWS Childs Aspect name
     */
    QName TYPE_INFORMATION_NEWS =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "news");
    /**
     * Interest Group profile type
     */
    QName TYPE_INTEREST_GROUP_PROFILE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootProfile");

    QName TYPE_CUSTOMIZATION_CONTENT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customizationContent");

    QName TYPE_CUSTOMIZATION_CONTAINER =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customizationContainer");

    QName TYPE_CUSTOMIZATION_FOLDER =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customizationFolder");

    QName TYPE_IGLOOKANDFEEL_CONTAINER =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "igLookAndFeel");

    QName ASSOC_CUSTOMIZE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customize");

    /**
     * The association between the ig root and the directory
     */
    QName ASSOC_IG_DIRECTORY_CONTAINER =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "igDirectoryContainer");

    /**
     * Contact information property for interest group leader
     */
    QName PROP_CONTACT_INFORMATION = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "contact");
    /**
     * Light description property for interest group
     */
    QName PROP_LIGHT_DESCRIPTION =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "lightDescription");

    /**
     * Property for interest group that determine if registered user can apply for membership
     */
    QName PROP_CAN_REGISTERED_APPLY =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "canRegisteredApply");

    /**
     * Ig Root node id to put on archived node when node is deleted
     */
    QName PROP_IG_ROOT_NODE_ID_ARCHIVED =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "igRootNodeIdArchived");
    /**
     * The boolean that define if the information service should be adpat to the screen or not
     */
    QName PROP_INF_ADAPT = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "infAdapt");
    /**
     * The boolean that define if the old information page should be displayed or not in the new UI
     **/
    QName PROP_INF_DISPLAY_OLD_INFORMATION =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "displayOldInformation");
    /**
     * The name of the index page of the information service
     */
    QName PROP_INF_INDEX_PAGE = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "infIndexPage");
    /**
     * The revision number as a Integer
     */
    QName PROP_REVISION_NUMBER = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "revisionNumber");

    /**
     * The navigationListRenderType as a String
     */
    QName PROP_NAVIGATION_LIST_RENDER_TYPE =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "navigationListRenderType");

    QName PROP_CONTENT = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "content");

    /**
     * IAM synchronization property
     */
    QName PROP_ECORDA_THEME_ID = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "ecordaThemeID");

    /**
     * External Repository type & properties
     */
    QName TYPE_EXTERNAL_REPOSITORY_CONFIGURATION_FOLDER =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "externalRepositoryConfigurationFolder");
    QName ASSOC_CONTAINSCON_FIGURATIONS =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "containsConfigurations");
    QName TYPE_EXTERNAL_REPOSITORY_CONFIGURATION =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "externalRepositoryConfiguration");

    QName ASPECT_EXTERNALLY_PUBLISHED =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "externallyPublished");
    QName PROP_REPOSITORIES_INFO =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "repositoriesInfo");

    /**
     * Root reference for the saved searches. It holds the reference of the interest group where this
     * search was taken from to not collide with searches saved in other IGs or services
     */
    QName ASPECT_SAVED_ROOT_SEARCHABLE =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "savedRootSearchable");
    QName PROP_LOCATION = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "location");

    QName ASPECT_BELONG_TO_INTEREST_GROUP =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "belongToInterestGroup");
    QName PROP_INTEREST_GROUP_NODE_REF =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "interestGroupNodeRef");

    /**
     * Should be defined in ContentModel because it's and Alfresco property, but it's not there, so we
     * define it here
     */
    QName PROP_DESTINATION =
            QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), "destination");


    QName PROP_HELP_LINK = QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), "helpLink");
    QName PROP_CONTACT_LINK =
            QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), "contactLink");

    /**
     * Number of day for the lucene query in what's news page
     */
    QName PROP_NB_DAY_WHATS_NEW = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "nbDaysWhatsNew");


    QName PROP_SEARCH_LINK_DISPLAYED =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "displayBannerSearchLink");
    QName PROP_LEGAL_LINK_DISPLAYED =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "displayBannerLegalNoticeLink");

    QName PROP_ELEARNING_LINK =
            QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), "eLearningLink");
    QName PROP_ELEARNING_LINK_DISPLAYED =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "displayELearningLink");

    QName PROP_APPLICANTS = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
            "circaIGRootApplicantUsersProperty");

    QName ASSOC_CIRCA_CATEGORY_PROFILE =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryProfileAssoc");

    QName ASPECT_LOCKED_FOR_ACCESS =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "lockedForAccess");
    QName ASPECT_LOCKED_FOR_ACCESS_ENABLED =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "lockedForAccessEnabled");

    QName PROP_ERROR_MESSAGE_CONTENT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "errorMessageContent");

    QName PROP_IG_ROOT_PROFILE_GROUP_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootProfileGroupName");
    QName PROP_IG_ROOT_PROFILE_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootProfileName");
    QName PROP_IG_ROOT_PERMISSION_SET =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootPermissionSet");
    QName PROP_IG_ROOT_SERVICE_ASSOC =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootServiceAssoc");

    /* new properties of the information News */

    QName PROP_NEWS_CONTENT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "newsContent");
    QName PROP_NEWS_DATE = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "newsDate");
    QName PROP_NEWS_PATTERN =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "newsPattern");
    QName PROP_NEWS_SIZE = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "newsSize");
    QName PROP_NEWS_LAYOUT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "newsLayout");
    QName PROP_NEWS_URL = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "newsUrl");

    QName PROP_CATEGORY_INVITED_USER_GROUP = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryInvitedUsersGroup");
    QName PROP_IG_ROOT_INVITED_USER_GROUP =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootInvitedUsersGroup");

    QName PROP_CATEGORY_MASTER_GROUP =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryMasterGroup");
    QName PROP_CATEGORY_SUBS_GROUP =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategorySubsGroup");
    QName PROP_CATEGORY_INVITED_USERS_GROUP = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryInvitedUsersGroup");

    QName PROP_IG_ROOT_MASTER_GROUP =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootMasterGroup");
    QName PROP_IG_ROOT_SUBS_GROUP =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootSubsGroup");


    QName ASSOC_CATEGORY_LOGOS =
            QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "logoContainerAssoc");


    QName PROP_LOGO_REF = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "logoRef");


    QName PROP_IG_ROOT_CONFIGURATION =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "interestGroupConfiguration");


    QName PROP_SINGLE_CONTACT =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "useSingleContact");
    QName PROP_CONTACT_EMAILS =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "contactEmails");
    QName PROP_CONTACT_VERIFIED =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "contactVerified");


    QName ASPECT_HELP_CATEGORY =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "helpCategory");
    QName ASPECT_HELP_ARTICLE = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "helpArticle");
    QName ASPECT_HELP_ARTICLE_HIGHLIGHTED = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "helpArticleHighlighted");
    QName ASPECT_HELP_LINK = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "helpLink");
    QName PROP_HELP_LINK_HREF = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "href");


    QName PROP_DISPLAY_OLD_APP_MESSAGE = QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "displayOldAppMessage");
    ;
}
