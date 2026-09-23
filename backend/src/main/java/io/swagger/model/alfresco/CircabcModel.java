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
package io.swagger.model.alfresco;

import static io.swagger.model.alfresco.BaseCircabcModel.CIRCABC_NAMESPACE;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

/**
 * Central registry of Alfresco content-model constants used throughout CIRCABC.
 *
 * <p>This is a non-instantiable utility class that exposes, as {@code public static final}
 * constants, the {@link QName} identifiers of the CIRCABC content model: types (e.g. profiles,
 * services, customization content), aspects (e.g. category, interest-group root, library,
 * newsgroup, survey, information, event), associations and properties. These constants are built
 * from the model namespace URIs ({@link #CIRCABC_CONTENT_MODEL_1_0_URI} and {@link
 * #CIRCABC_MODEL_URL}) and are used by the REST layer and services to read and write nodes in the
 * Alfresco repository without having to hard-code namespace/local-name strings.
 *
 * @author atadian
 */
public final class CircabcModel {

  /**
   * Private constructor to prevent instantiation of this constants holder class.
   */
  private CircabcModel() {}

  /**
   * Base URL of the CIRCABC content model (version 1.0), used as the namespace URI for the
   * {@code CircaBC*} group properties defined at the end of this class.
   */
  public static final String CIRCABC_MODEL_URL =
    "http://www.cc.cec/circabc/model/content/1.0";

  /**
   * Circabc Model Prefix
   */
  public static final String CIRCABC_MODEL_PREFIX = "ci";

  /**
   * Circabc model namespace
   */
  public static final String CIRCABC_CONTENT_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/content/1.0";
  /**
   * Property holding the CIRCABC profile name.
   */
  public static final QName PROP_CIRCABC_PROFILE_NAME = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaBCProfileName"
  );

  /**
   * Property holding the CIRCABC profile group name.
   */
  public static final QName PROP_CIRCABC_PROFILE_GROUP_NAME = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaBCProfileGroupName"
  );

  /**
   * Migrated Node Aspect name. Applied to nodes created by the migration/import
   * process to keep a reference to the original (source) node.
   */
  public static final QName ASPECT_MIGRATED = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "migrated"
  );

  /**
   * Original node reference property of the {@link #ASPECT_MIGRATED} aspect.
   * Holds the NodeRef of the node in the source system the node was migrated from.
   */
  public static final QName PROP_ORIGINAL_NODE_REF = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "originalNodeRef"
  );

  /**
   * Circabc childs node Aspect name
   */
  public static final QName ASPECT_CIRCABC_MANAGEMENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcManagement"
  );
  /**
   * Circabc Root node Aspect name
   */
  public static final QName ASPECT_CIRCABC_ROOT = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaBC"
  );
  /**
   * Library Aspect name
   */
  public static final QName ASPECT_CATEGORY = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategory"
  );
  /**
   * Interest Group root Aspect name
   */
  public static final QName ASPECT_IGROOT = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRoot"
  );
  /**
   * Lirary Root Aspect Name
   */
  public static final QName ASPECT_LIBRARY_ROOT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaLibraryRoot"
  );
  /**
   * Lirary childs Aspect Name
   */
  public static final QName ASPECT_LIBRARY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaLibrary"
  );
  /**
   * NewsGroup root Aspect name
   */
  public static final QName ASPECT_NEWSGROUP_ROOT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaNewsGroupRoot"
  );
  /**
   * NewsGroup childs Aspect name
   */
  public static final QName ASPECT_NEWSGROUP = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaNewsGroup"
  );
  /**
   * Survey Root Aspect name
   */
  public static final QName ASPECT_SURVEY_ROOT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaSurveyRoot"
  );
  /**
   * Survey Childs Aspect name
   */
  public static final QName ASPECT_SURVEY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaSurvey"
  );
  /**
   * Information Root Aspect name
   */
  public static final QName ASPECT_INFORMATION_ROOT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcInformationRoot"
  );
  /**
   * Information Childs Aspect name
   */
  public static final QName ASPECT_INFORMATION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcInformation"
  );

  /**
   * Information NEWS Childs Aspect name
   */
  public static final QName ASPECT_INFORMATION_NEWS = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcInformationNews"
  );

  /**
   * Event Root Aspect name
   */
  public static final QName ASPECT_EVENT_ROOT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcEventRoot"
  );
  /**
   * Event Childs Aspect name
   */
  public static final QName ASPECT_EVENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcEvent"
  );
  /**
   * Shared space aspect
   */
  public static final QName ASPECT_SHARED_SPACE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcSharedSpace"
  );
  /**
   * Shared space aspect
   */
  public static final QName ASPECT_REVISIONABLE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "revisionable"
  );

  /**
   * Profile addon importable aspect
   */
  public static final QName ASPECT_PROFILE_IMPORTABLE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "importable"
  );

  /**
   * Aspect marking a node that should notify all recipients when content is pasted into it.
   */
  public static final QName ASPECT_NOTIFY_PASTE_ALL = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "notifyPasteAll"
  );

  /**
   * Aspect marking a node that should trigger a notification when content is pasted into it.
   */
  public static final QName ASPECT_NOTIFY_PASTE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "notifyPaste"
  );

  /**
   * Category Headers Type name
   */
  public static final QName TYPE_CATEGORY_HEADER = ContentModel.TYPE_CATEGORY;
  /**
   * Directory Root type name
   */
  public static final QName TYPE_DIRECTORY_SERVICE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaDirectoryRoot"
  );

  /**
   * Circabc root profile type
   */
  public static final QName TYPE_CIRCABC_ROOT_PROFILE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaBCProfile"
  );
  /**
   * Category profile type
   */
  public static final QName TYPE_CATEGORY_PROFILE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategoryProfile"
  );
  /**
   * Information NEWS Childs Aspect name
   */
  public static final QName TYPE_INFORMATION_NEWS = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "news"
  );
  /**
   * Interest Group profile type
   */
  public static final QName TYPE_INTEREST_GROUP_PROFILE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootProfile"
  );

  /**
   * Type for customization content nodes (e.g. custom look-and-feel resources).
   */
  public static final QName TYPE_CUSTOMIZATION_CONTENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "customizationContent"
  );

  /**
   * Type for the container node grouping customization content.
   */
  public static final QName TYPE_CUSTOMIZATION_CONTAINER = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "customizationContainer"
  );

  /**
   * Type for a folder holding customization content.
   */
  public static final QName TYPE_CUSTOMIZATION_FOLDER = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "customizationFolder"
  );

  /**
   * Type for the interest-group look-and-feel container.
   */
  public static final QName TYPE_IGLOOKANDFEEL_CONTAINER = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "igLookAndFeel"
  );

  /**
   * Association linking a node to its customization content.
   */
  public static final QName ASSOC_CUSTOMIZE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "customize"
  );

  /**
   * The association between the ig root and the directory
   */
  public static final QName ASSOC_IG_DIRECTORY_CONTAINER = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "igDirectoryContainer"
  );

  /**
   * Contact information property for interest group leader
   */
  public static final QName PROP_CONTACT_INFORMATION = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "contact"
  );
  /**
   * Light description property for interest group
   */
  public static final QName PROP_LIGHT_DESCRIPTION = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "lightDescription"
  );

  /**
   * Property for interest group that determine if registered user can apply for membership
   */
  public static final QName PROP_CAN_REGISTERED_APPLY = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "canRegisteredApply"
  );

  /**
   * Ig Root node id to put on archived node when node is deleted
   */
  public static final QName PROP_IG_ROOT_NODE_ID_ARCHIVED = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "igRootNodeIdArchived"
  );
  /**
   * The boolean that define if the information service should be adpat to the screen or not
   */
  public static final QName PROP_INF_ADAPT = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "infAdapt"
  );
  /**
   * The boolean that define if the old information page should be displayed or not in the new UI
   **/
  public static final QName PROP_INF_DISPLAY_OLD_INFORMATION =
    QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "displayOldInformation");
  /**
   * The name of the index page of the information service
   */
  public static final QName PROP_INF_INDEX_PAGE = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "infIndexPage"
  );
  /**
   * The revision number as a Integer
   */
  public static final QName PROP_REVISION_NUMBER = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "revisionNumber"
  );

  /**
   * The navigationListRenderType as a String
   */
  public static final QName PROP_NAVIGATION_LIST_RENDER_TYPE =
    QName.createQName(
      CIRCABC_CONTENT_MODEL_1_0_URI,
      "navigationListRenderType"
    );

  /**
   * Property holding the node content (generic content property in the CIRCABC model).
   */
  public static final QName PROP_CONTENT = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "content"
  );

  /**
   * IAM synchronization property
   */
  public static final QName PROP_ECORDA_THEME_ID = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "ecordaThemeID"
  );

  /**
   * External Repository type &amp; properties
   */
  public static final QName TYPE_EXTERNAL_REPOSITORY_CONFIGURATION_FOLDER =
    QName.createQName(
      CIRCABC_CONTENT_MODEL_1_0_URI,
      "externalRepositoryConfigurationFolder"
    );
  /**
   * Association linking the configuration folder to its external repository configurations.
   */
  public static final QName ASSOC_CONTAINSCON_FIGURATIONS = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "containsConfigurations"
  );

  /**
   * Type for a single external repository configuration node.
   */
  public static final QName TYPE_EXTERNAL_REPOSITORY_CONFIGURATION =
    QName.createQName(
      CIRCABC_CONTENT_MODEL_1_0_URI,
      "externalRepositoryConfiguration"
    );

  /**
   * Aspect marking a node that has been published to an external repository.
   */
  public static final QName ASPECT_EXTERNALLY_PUBLISHED = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "externallyPublished"
  );

  /**
   * Property holding information about the external repositories a node is published to.
   */
  public static final QName PROP_REPOSITORIES_INFO = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "repositoriesInfo"
  );

  /**
   * Root reference for the saved searches. It holds the reference of the interest group where this
   * search was taken from to not collide with searches saved in other IGs or services
   */
  public static final QName ASPECT_SAVED_ROOT_SEARCHABLE = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "savedRootSearchable"
  );
  /**
   * Property holding the location of a saved search.
   */
  public static final QName PROP_LOCATION = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "location"
  );

  /**
   * Aspect marking a node as belonging to a specific interest group.
   */
  public static final QName ASPECT_BELONG_TO_INTEREST_GROUP = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "belongToInterestGroup"
  );

  /**
   * Property holding the node reference of the interest group a node belongs to.
   */
  public static final QName PROP_INTEREST_GROUP_NODE_REF = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "interestGroupNodeRef"
  );

  /**
   * Should be defined in ContentModel because it's and Alfresco property, but it's not there, so we
   * define it here
   */
  public static final QName PROP_DESTINATION = QName.createQName(
    ContentModel.PROP_NAME.getNamespaceURI(),
    "destination"
  );

  /**
   * Property holding the help link URL displayed in the banner.
   */
  public static final QName PROP_HELP_LINK = QName.createQName(
    ContentModel.PROP_NAME.getNamespaceURI(),
    "helpLink"
  );

  /**
   * Property holding the contact link URL displayed in the banner.
   */
  public static final QName PROP_CONTACT_LINK = QName.createQName(
    ContentModel.PROP_NAME.getNamespaceURI(),
    "contactLink"
  );

  /**
   * Number of day for the lucene query in what's news page
   */
  public static final QName PROP_NB_DAY_WHATS_NEW = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "nbDaysWhatsNew"
  );

  /**
   * Property indicating whether the search link is displayed in the banner.
   */
  public static final QName PROP_SEARCH_LINK_DISPLAYED = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "displayBannerSearchLink"
  );

  /**
   * Property indicating whether the legal notice link is displayed in the banner.
   */
  public static final QName PROP_LEGAL_LINK_DISPLAYED = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "displayBannerLegalNoticeLink"
  );

  /**
   * Property holding the e-learning link URL.
   */
  public static final QName PROP_ELEARNING_LINK = QName.createQName(
    ContentModel.PROP_NAME.getNamespaceURI(),
    "eLearningLink"
  );

  /**
   * Property indicating whether the e-learning link is displayed.
   */
  public static final QName PROP_ELEARNING_LINK_DISPLAYED = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "displayELearningLink"
  );

  /**
   * Property holding the list of users who applied for membership of an interest-group root.
   */
  public static final QName PROP_APPLICANTS = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootApplicantUsersProperty"
  );

  /**
   * Association linking a category to its category profile.
   */
  public static final QName ASSOC_CIRCA_CATEGORY_PROFILE = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategoryProfileAssoc"
  );

  /**
   * Aspect marking a node as locked for access.
   */
  public static final QName ASPECT_LOCKED_FOR_ACCESS = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "lockedForAccess"
  );

  /**
   * Aspect indicating that the "locked for access" behaviour is enabled on a node.
   */
  public static final QName ASPECT_LOCKED_FOR_ACCESS_ENABLED =
    QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "lockedForAccessEnabled");

  /**
   * Property holding the content of the error message shown when access is locked.
   */
  public static final QName PROP_ERROR_MESSAGE_CONTENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "errorMessageContent"
  );

  /**
   * Property holding the profile group name of an interest-group root.
   */
  public static final QName PROP_IG_ROOT_PROFILE_GROUP_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootProfileGroupName"
  );

  /**
   * Property holding the profile name of an interest-group root.
   */
  public static final QName PROP_IG_ROOT_PROFILE_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootProfileName"
  );

  /**
   * Property holding the permission set of an interest-group root.
   */
  public static final QName PROP_IG_ROOT_PERMISSION_SET = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootPermissionSet"
  );

  /**
   * Property holding the service association of an interest-group root.
   */
  public static final QName PROP_IG_ROOT_SERVICE_ASSOC = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootServiceAssoc"
  );

  /* new properties of the information News */

  /**
   * Property holding the content (body) of an information news item.
   */
  public static final QName PROP_NEWS_CONTENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "newsContent"
  );

  /**
   * Property holding the date of an information news item.
   */
  public static final QName PROP_NEWS_DATE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "newsDate"
  );

  /**
   * Property holding the display pattern of an information news item.
   */
  public static final QName PROP_NEWS_PATTERN = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "newsPattern"
  );

  /**
   * Property holding the size of an information news item.
   */
  public static final QName PROP_NEWS_SIZE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "newsSize"
  );

  /**
   * Property holding the layout of an information news item.
   */
  public static final QName PROP_NEWS_LAYOUT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "newsLayout"
  );

  /**
   * Property holding the URL of an information news item.
   */
  public static final QName PROP_NEWS_URL = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "newsUrl"
  );

  /**
   * Property holding the invited-users group associated with a category.
   */
  public static final QName PROP_CATEGORY_INVITED_USER_GROUP =
    QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaCategoryInvitedUsersGroup"
    );

  /**
   * Property holding the invited-users group associated with an interest-group root.
   */
  public static final QName PROP_IG_ROOT_INVITED_USER_GROUP = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootInvitedUsersGroup"
  );

  /**
   * Property holding the master group associated with a category.
   */
  public static final QName PROP_CATEGORY_MASTER_GROUP = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategoryMasterGroup"
  );

  /**
   * Property holding the subscribers group associated with a category.
   */
  public static final QName PROP_CATEGORY_SUBS_GROUP = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategorySubsGroup"
  );

  /**
   * Property holding the invited-users group associated with a category.
   */
  public static final QName PROP_CATEGORY_INVITED_USERS_GROUP =
    QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaCategoryInvitedUsersGroup"
    );

  /**
   * Property holding the master group associated with an interest-group root.
   */
  public static final QName PROP_IG_ROOT_MASTER_GROUP = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootMasterGroup"
  );

  /**
   * Property holding the subscribers group associated with an interest-group root.
   */
  public static final QName PROP_IG_ROOT_SUBS_GROUP = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootSubsGroup"
  );

  /**
   * Association linking a category to its logo container.
   */
  public static final QName ASSOC_CATEGORY_LOGOS = QName.createQName(
    CIRCABC_CONTENT_MODEL_1_0_URI,
    "logoContainerAssoc"
  );

  /**
   * Property holding the node reference of a logo.
   */
  public static final QName PROP_LOGO_REF = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "logoRef"
  );

  /**
   * Property holding the configuration of an interest group.
   */
  public static final QName PROP_IG_ROOT_CONFIGURATION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "interestGroupConfiguration"
  );

  /**
   * Property indicating whether a single contact should be used for the interest group.
   */
  public static final QName PROP_SINGLE_CONTACT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "useSingleContact"
  );

  /**
   * Property holding the list of contact e-mail addresses.
   */
  public static final QName PROP_CONTACT_EMAILS = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "contactEmails"
  );

  /**
   * Property indicating whether the contact e-mail addresses have been verified.
   */
  public static final QName PROP_CONTACT_VERIFIED = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "contactVerified"
  );

  /**
   * Aspect marking a node as a help category.
   */
  public static final QName ASPECT_HELP_CATEGORY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "helpCategory"
  );

  /**
   * Aspect marking a node as a help article.
   */
  public static final QName ASPECT_HELP_ARTICLE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "helpArticle"
  );

  /**
   * Aspect marking a help article as highlighted.
   */
  public static final QName ASPECT_HELP_ARTICLE_HIGHLIGHTED = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "helpArticleHighlighted"
  );

  /**
   * Aspect marking a node as a help link.
   */
  public static final QName ASPECT_HELP_LINK = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "helpLink"
  );

  /**
   * Property holding the target href of a help link.
   */
  public static final QName PROP_HELP_LINK_HREF = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "href"
  );

  /**
   * Property indicating whether the "old application" message should be displayed.
   */
  public static final QName PROP_DISPLAY_OLD_APP_MESSAGE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "displayOldAppMessage"
  );

  /**
   * Property holding the CircaBC invited-users group at the repository root level.
   */
  public static final QName CIRCA_BC_INVITED_USERS_GROUP_PROPERTY =
    QName.createQName(CIRCABC_MODEL_URL, "circaBCInvitedUsersGroup");

  /**
   * Property holding the CircaBC subscribers group at the repository root level.
   */
  public static final QName CIRCA_BC_SUBS_GROUP_PROPERTY = QName.createQName(
    CIRCABC_MODEL_URL,
    "circaBCSubsGroup"
  );

  /**
   * Property holding the CircaBC master group at the repository root level.
   */
  public static final QName CIRCA_BC_MASTER_GROUP_PROPERTY = QName.createQName(
    CIRCABC_MODEL_URL,
    "circaBCMasterGroup"
  );

  /**
   * Property holding the CircaBC admin group at the repository root level.
   */
  public static final QName CIRCA_BC_ADMIN_GROUP_PROPERTY = QName.createQName(
    CIRCABC_MODEL_URL,
    "circaBCAdminGroup"
  );

  /**
   * Name of the CircaBC invited-users group.
   */
  public static final String CIRCA_BC_INVITED_USERS_GROUP =
    "CircaBC--InvitedUsersGroup";

  /**
   * Name of the CircaBC subscribers group.
   */
  public static final String CIRCA_BC_SUBS_GROUP = "CircaBC--SubsGroup";

  /**
   * Name prefix of the CircaBC master group.
   */
  public static final String CIRCA_BC_MASTER_GROUP = "CircaBC--MasterGroup--";

  /**
   * Name prefix of the CircaBC admin group.
   */
  public static final String CIRCA_BC_ADMIN_GROUP = "CircaBCAdmin--";
}
