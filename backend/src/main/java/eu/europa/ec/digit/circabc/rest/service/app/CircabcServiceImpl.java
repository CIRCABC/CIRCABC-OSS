package eu.europa.ec.digit.circabc.rest.service.app;

import eu.europa.ec.digit.circabc.rest.exception.ProfilePersistenceException;
import eu.europa.ec.digit.circabc.rest.exception.UserDeletionException;
import eu.europa.ec.digit.circabc.rest.service.customization.logo.DefaultLogoConfiguration;
import eu.europa.ec.digit.circabc.rest.service.customization.logo.LogoPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.profile.ProfileService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.exception.CustomizationException;
import io.swagger.model.Profile;
import io.swagger.model.UserCategoryMembershipRecord;
import io.swagger.model.UserIGMembershipRecord;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ProfileModel;
import io.swagger.model.alfresco.UserModel;
import io.swagger.model.db.Category;
import io.swagger.model.db.CategoryAdmin;
import io.swagger.model.db.CircabcAdmin;
import io.swagger.model.db.Header;
import io.swagger.model.db.IGData;
import io.swagger.model.db.InterestGroup;
import io.swagger.model.db.InterestGroupItem;
import io.swagger.model.db.InterestGroupResult;
import io.swagger.model.db.ProfileUser;
import io.swagger.model.db.TranslationEntry;
import io.swagger.model.db.User;
import io.swagger.model.db.UserIGMembership;
import io.swagger.model.db.UserWithProfile;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link CircabcService}.
 *
 * <p>This service bridges the Alfresco content repository and the CIRCABC relational database.
 * It keeps a denormalized, query-optimized copy of the CIRCABC domain hierarchy (Headers,
 * Categories, Interest Groups, Profiles and Users) in the database in sync with the underlying
 * Alfresco nodes. Most operations resolve the Alfresco node's database id
 * ({@link ContentModel#PROP_NODE_DBID}) from a {@link NodeRef} and then delegate to
 * {@link CircabcDaoServiceImpl} for persistence, as well as answering administration and
 * membership questions (CIRCABC admins, category admins, interest group members, profiles, etc.).
 *
 * <p>Database access is deliberately defensive: persistence failures are caught and logged rather
 * than propagated, so that repository operations that trigger these synchronization hooks are not
 * rolled back by database errors, unless otherwise documented on a specific method.
 */
public class CircabcServiceImpl implements CircabcService {

  /** Alfresco locale key prefix for the {@code en_US} locale, used when resolving translations. */
  public static final String EN_US_UNDERSCORE = "en_US_";
  /** Alfresco locale key prefix for the {@code en_GB} locale, used when resolving translations. */
  public static final String EN_GB_UNDERSCORE = "en_GB_";

  /** Common prefix used when logging failures to load the Alfresco locale map. */
  private static final String ERROR_GET_ALL_ALFRESCO_LOCALE =
    "Error getAllAlfrescoLocale : ";

  /** DAO providing all CIRCABC-specific SQL persistence operations. */
  @Autowired
  private CircabcDaoServiceImpl circabcDaoService;

  /** Alfresco node service used to read node properties (ids, names, titles, aspects). */
  @Autowired
  private NodeService nodeService;

  /** Alfresco person service used to resolve user names to person nodes. */
  @Autowired
  private PersonService personService;

  /** Alfresco service used to locate a node's configuration folder (user preferences). */
  @Autowired
  private ConfigurableService configurableService;

  /** Alfresco search service used to locate preference nodes via XPath. */
  @Autowired
  private SearchService searchService;

  /** Alfresco namespace service required to resolve prefixed XPath queries. */
  @Autowired
  private NamespaceService namespaceService;

  /** Service resolving the configured logo of an interest group during resynchronization. */
  @Autowired
  private LogoPreferencesService logoPreferencesService;

  /** Service exposing profile definitions and profile membership for interest groups. */
  @Autowired
  private ProfileService profileService;

  /** Shared commons-logging logger for this service. */
  static final Log logger = LogFactory.getLog(CircabcServiceImpl.class);

  /**
   * Indicates whether the given user is a global CIRCABC administrator.
   *
   * @param userName the user name to check
   * @return {@code true} if the user is a CIRCABC administrator, {@code false} otherwise
   */
  @Override
  public boolean isCircabcAdmin(String userName) {
    return circabcDaoService.getIsCircabcAdmin(userName) > 0;
  }

  /**
   * Indicates whether the given user is an administrator of the specified category.
   *
   * @param categoryNodeRef the Alfresco node reference of the category
   * @param userName the user name to check
   * @return {@code true} if the user administers the category, {@code false} otherwise
   */
  @Override
  public boolean isCategoryAdmin(NodeRef categoryNodeRef, String userName) {
    long categoryID = (Long) nodeService.getProperty(
      categoryNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.getIsCategoryAdmin(categoryID, userName) > 0;
  }

  /**
   * Indicates whether the user holds any administrative role, i.e. user directory admin, category
   * admin or CIRCABC admin.
   *
   * @param userName the user name to check
   * @return {@code true} if the user has any such administrative role, {@code false} otherwise
   */
  @Override
  public boolean isUserDirAdminOrCategoryAdminOrCircabcAdmin(String userName) {
    return circabcDaoService.selectCountAdminDByUserName(userName) > 0L;
  }

  /**
   * Retrieves the interest group record corresponding to the given interest group node.
   *
   * @param interestGroupNodeRef the Alfresco node reference of the interest group root
   * @return the {@link InterestGroupResult} loaded from the database
   */
  @Override
  public InterestGroupResult getInterestGroup(NodeRef interestGroupNodeRef) {
    long igID = (Long) nodeService.getProperty(
      interestGroupNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.selectIgByID(igID);
  }

  /**
   * Indicates whether the given user is a member of the specified interest group.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param userName the user name to check
   * @return {@code true} if the user is a member of the group, {@code false} otherwise
   */
  @Override
  public boolean isUserMember(NodeRef igNodeRef, String userName) {
    long igID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.getIsMemberOfGroup(igID, userName) > 0;
  }

  /**
   * Indicates whether the given user is a category administrator of the category that owns the
   * specified interest group.
   *
   * @param interestGroupNodeRef the Alfresco node reference of the interest group root
   * @param userName the user name to check
   * @return {@code true} if the user administers the interest group's parent category,
   *     {@code false} otherwise
   */
  @Override
  public boolean isCategoryAdminOfInterestGroup(
    NodeRef interestGroupNodeRef,
    String userName
  ) {
    long interestGroupID = (Long) nodeService.getProperty(
      interestGroupNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return (
      circabcDaoService.getIsCategoryAdminOfInterestGroup(
        interestGroupID,
        userName
      ) >
      0
    );
  }

  /**
   * Indicates whether the given user is an external (non-EU institution) user.
   *
   * @param userName the user name to check
   * @return {@code true} if the user is external, {@code false} otherwise
   */
  @Override
  public boolean isExternalUser(String userName) {
    return circabcDaoService.getisExternalUser(userName) > 0;
  }

  /**
   * Returns all interest group memberships of the given user, enriched with category and interest
   * group names/titles and the resolved profile display name.
   *
   * <p>Any error during retrieval is logged and results in an empty (or partial) list rather than
   * an exception.
   *
   * @param userName the user name whose memberships are requested
   * @return the list of {@link UserIGMembershipRecord}s; never {@code null}
   */
  @Override
  public List<UserIGMembershipRecord> getInterestGroups(String userName) {
    List<UserIGMembershipRecord> result = new ArrayList<>();
    try {
      List<UserIGMembership> list = circabcDaoService.selectInterestGroups(
        userName
      );
      Map<String, Long> allLocales = circabcDaoService.getAllAlfrescoLocale();
      for (UserIGMembership membership : list) {
        result.add(buildMembershipRecord(membership, allLocales));
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error getInterestGroups : ", e);
      }
    }
    return result;
  }

  private UserIGMembershipRecord buildMembershipRecord(
    UserIGMembership membership,
    Map<String, Long> allLocales
  ) {
    NodeRef categoNodeRef = new NodeRef(membership.getCatNodeRef());
    NodeRef igRootNodeRef = new NodeRef(membership.getIgNodeRef());

    String category = capitalize(
      (String) nodeService.getProperty(categoNodeRef, ContentModel.PROP_NAME)
    );
    String categoryTitle = extractTitle(
      nodeService.getProperty(categoNodeRef, ContentModel.PROP_TITLE)
    );
    String interestGroup = capitalize(
      (String) nodeService.getProperty(igRootNodeRef, ContentModel.PROP_NAME)
    );
    String interestGroupTitle = extractTitle(
      nodeService.getProperty(igRootNodeRef, ContentModel.PROP_TITLE)
    );
    String profileNodeRefId =
      membership.getProfileNodeRefId() != null
        ? new NodeRef(membership.getProfileNodeRefId()).getId()
        : "";
    String profileDisplayName = resolveProfileDisplayName(
      membership,
      allLocales
    );

    return new UserIGMembershipRecord(
      igRootNodeRef.getId(),
      interestGroup,
      categoNodeRef.getId(),
      category,
      membership.getProfileName(),
      categoryTitle,
      interestGroupTitle,
      profileDisplayName,
      membership.getAlfrescoGroup(),
      profileNodeRefId
    );
  }

  private String extractTitle(Serializable propTitle) {
    if (propTitle instanceof MLText mlText) {
      return capitalize(mlText.getDefaultValue());
    } else if (propTitle instanceof String s) {
      return s;
    }
    return "";
  }

  private String resolveProfileDisplayName(
    UserIGMembership membership,
    Map<String, Long> allLocales
  ) {
    List<TranslationEntry> profileTitles =
      circabcDaoService.selectProfileTitles(membership.getProfileId());
    if (profileTitles.size() == 1) {
      return profileTitles.get(0).getTranslation();
    }
    if (profileTitles.size() > 1) {
      Map<Long, String> trans = new HashMap<>();
      for (TranslationEntry t : profileTitles) {
        trans.put(t.getAlfLocaleId(), t.getTranslation());
      }
      return findBestTranslation(
        trans,
        allLocales,
        membership.getProfileName()
      );
    }
    return membership.getProfileName();
  }

  private String findBestTranslation(
    Map<Long, String> trans,
    Map<String, Long> allLocales,
    String defaultValue
  ) {
    String[] localeKeys = { EN_US_UNDERSCORE, "en_", EN_GB_UNDERSCORE };
    for (String key : localeKeys) {
      if (
        allLocales.containsKey(key) && trans.containsKey(allLocales.get(key))
      ) {
        return trans.get(allLocales.get(key));
      }
    }
    return defaultValue;
  }

  /**
   * Returns the categories the given user has access to, mapped to lightweight membership records.
   *
   * <p>The category display name is capitalized and, when available, derived from the localized
   * title. Any error during retrieval is logged and yields an empty (or partial) list.
   *
   * @param userName the user name whose categories are requested
   * @return the list of {@link UserCategoryMembershipRecord}s; never {@code null}
   */
  @Override
  public List<UserCategoryMembershipRecord> getCategories(String userName) {
    List<UserCategoryMembershipRecord> result = new ArrayList<>();
    try {
      List<String> list = circabcDaoService.selectCategories(userName);
      for (String item : list) {
        NodeRef categoNodeRef = new NodeRef(item);
        String category = capitalize(
          (String) nodeService.getProperty(
            categoNodeRef,
            ContentModel.PROP_NAME
          )
        );

        Serializable categoryTitle = nodeService.getProperty(
          categoNodeRef,
          ContentModel.PROP_TITLE
        );
        if (categoryTitle instanceof String s) {
          if (categoryTitle != null && !s.isEmpty()) {
            category = capitalize(s);
          }
        } else if (categoryTitle instanceof MLText mlText) {
          category = capitalize(mlText.getDefaultValue());
        }

        result.add(
          new UserCategoryMembershipRecord(category, "", categoNodeRef.getId())
        );
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error getCategories : ", e);
      }
    }
    return result;
  }

  private String capitalize(final String text) {
    if (text == null || text.isEmpty()) {
      return text;
    } else {
      return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
  }

  /**
   * Removes the given user's category administrator role for the specified category.
   *
   * @param categoryNodeRef the Alfresco node reference of the category
   * @param userName the user name whose category admin role is to be removed
   */
  @Override
  public void removeCategoryAdmin(NodeRef categoryNodeRef, String userName) {
    long categoryID = (Long) nodeService.getProperty(
      categoryNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    deleteCategoryAdmin(categoryID, userName);
  }

  private void deleteCategoryAdmin(final long categoryID, String userName) {
    final NodeRef person = personService.getPerson(userName);
    final Map<QName, Serializable> personProperties = nodeService.getProperties(
      person
    );
    final long personID = (Long) personProperties.get(
      ContentModel.PROP_NODE_DBID
    );
    CategoryAdmin catAdmin = new CategoryAdmin(personID, categoryID);
    try {
      circabcDaoService.deleteCategoryAdmin(catAdmin);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error deleteCategoryAdmin : " + catAdmin.toString(), e);
      }
    }
  }

  /**
   * Adds the given user to the named profile of the specified interest group.
   *
   * <p>If no matching profile/Alfresco group is found for the interest group, a warning is logged
   * and no change is made.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param userName the user name to add
   * @param profileName the name of the target profile within the interest group
   */
  @Override
  public void addPersonToProfile(
    NodeRef igNodeRef,
    String userName,
    String profileName
  ) {
    long interestGroupID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    String alfrescoGroup = getAlfrescoGroupByInterestGroupIDAndProfileName(
      interestGroupID,
      profileName
    );
    if (!alfrescoGroup.isEmpty()) {
      insertProfileUser(alfrescoGroup, userName);
    } else {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Error can not found profile " +
            profileName +
            " for interest group  " +
            igNodeRef.toString()
        );
      }
    }
  }

  private String getAlfrescoGroupByInterestGroupIDAndProfileName(
    long interestGroupID,
    String profileName
  ) {
    String result = "";
    io.swagger.model.db.Profile profile = new io.swagger.model.db.Profile();
    profile.setInterestGroupID(interestGroupID);
    profile.setName(profileName);
    try {
      io.swagger.model.db.Profile resultProfile =
        circabcDaoService.selectProfileByInterestGroupIDProfileName(profile);
      if (resultProfile != null) {
        result = resultProfile.getAlfrescoGroup();
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error getProfileIDByInterestGroupIDAndProfileName : " +
            profile.toString(),
          e
        );
      }
    }
    return result;
  }

  /**
   * Moves the given user to a different profile within the specified interest group by removing the
   * existing profile membership and inserting the new one.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param userName the user name whose profile is being changed
   * @param profileName the name of the new target profile
   */
  @Override
  public void changePersonProfile(
    NodeRef igNodeRef,
    String userName,
    String profileName
  ) {
    long interestGroupID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    int result = 0;
    try {
      result = circabcDaoService.deleteProfileByInterestGroupUserName(
        interestGroupID,
        userName
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error deleteProfileByInterestGroupUserName: ig id" +
            interestGroupID +
            " user name " +
            userName,
          e
        );
      }
    }
    if (result == 1) {
      long userID = getUserIDByUserName(userName);
      String alfrescoGroup = getAlfrescoGroupByInterestGroupIDAndProfileName(
        interestGroupID,
        profileName
      );
      ProfileUser profileUser = new ProfileUser(userID, alfrescoGroup);
      try {
        circabcDaoService.insertProfileUser(profileUser);
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error("Error insertProfileUse : " + profileUser.toString(), e);
        }
      }
    }
  }

  /**
   * Deletes the named profile (and its title translations) from the specified interest group.
   *
   * <p>If the profile cannot be found, the method does nothing.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param profileName the name of the profile to delete
   */
  @Override
  public void deleteProfile(NodeRef igNodeRef, String profileName) {
    long interestGroupID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    final io.swagger.model.db.Profile profile =
      getProfileByInterestGroupIDAndProfileName(interestGroupID, profileName);
    if (profile != null) {
      final long profileID = profile.getId();
      try {
        circabcDaoService.deleteProfileTitleTranslationsByID(profileID);
        circabcDaoService.deleteProfileByID(profileID);
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error("Error deleteProfileByID : " + profileID, e);
        }
      }
    }
  }

  private io.swagger.model.db.Profile getProfileByInterestGroupIDAndProfileName(
    long interestGroupID,
    String profileName
  ) {
    io.swagger.model.db.Profile result = null;
    io.swagger.model.db.Profile profile = new io.swagger.model.db.Profile();
    profile.setInterestGroupID(interestGroupID);
    profile.setName(profileName);
    try {
      result = circabcDaoService.selectProfileByInterestGroupIDProfileName(
        profile
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error getProfileIDByInterestGroupIDAndProfileName : " +
            profile.toString(),
          e
        );
      }
    }
    return result;
  }

  /**
   * Recomputes and persists the "can apply for membership" flag of the specified interest group
   * based on its current Alfresco properties.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   */
  @Override
  public void updateCanApplyForMemberhip(NodeRef igNodeRef) {
    long interestGroupID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    updateCanApplyForMembership(igNodeRef, interestGroupID);
  }

  /**
   * Persists (inserts or updates) the given profile for the specified interest group, including its
   * title translations, and applies any special handling for the {@code guest} and {@code EVERYONE}
   * profiles (public/registered visibility and membership application flags).
   *
   * <p>Errors are caught and logged rather than propagated.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param profileName the profile name (retained for API compatibility; the persisted name is
   *     derived from the profile's Alfresco properties)
   * @param profile the profile definition to persist
   */
  @Override
  public void updateProfile(
    NodeRef igNodeRef,
    String profileName,
    Profile profile
  ) {
    long interestGroupID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    io.swagger.model.db.Profile prof = buildDbProfile(interestGroupID, profile);

    try {
      saveProfile(prof);
      handleSpecialProfiles(
        prof.getName(),
        interestGroupID,
        prof.isVisible(),
        igNodeRef
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error updating profile : " + prof.toString(), e);
      }
    }
  }

  private io.swagger.model.db.Profile buildDbProfile(
    long interestGroupID,
    Profile profile
  ) {
    NodeRef nodeRef = profile.getNodeRef();
    Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

    String name = (String) properties.get(
      ProfileModel.PROP_IG_ROOT_PROFILE_NAME
    );
    String title = extractProfileTitle(properties, name);
    long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
    boolean isVisible = "visibility".equalsIgnoreCase(
      profile.getPermissions().get("visibility")
    );
    String alfrescoGroup = profile.getGroupName().replaceFirst("^GROUP_", "");
    String igFromNodeRef = Boolean.TRUE.equals(profile.getImported())
      ? profile.getImportedRef()
      : null;

    return new io.swagger.model.db.Profile(
      interestGroupID,
      id,
      alfrescoGroup,
      name,
      title,
      profile.getPermissions().get("members"),
      profile.getPermissions().get("information"),
      profile.getPermissions().get("library"),
      profile.getPermissions().get("newsgroups"),
      profile.getPermissions().get("events"),
      profile.getExported(),
      profile.getImported(),
      isVisible,
      nodeRef.toString(),
      igFromNodeRef
    );
  }

  private String extractProfileTitle(
    Map<QName, Serializable> properties,
    String defaultName
  ) {
    Serializable propertyTitle = properties.get(ContentModel.PROP_TITLE);
    if (propertyTitle instanceof String s) {
      return s;
    } else if (propertyTitle instanceof MLText mlText) {
      return mlText.getDefaultValue();
    }
    return defaultName;
  }

  private void saveProfile(io.swagger.model.db.Profile prof)
    throws ProfilePersistenceException {
    try {
      if (circabcDaoService.profileExists(prof.getId())) {
        circabcDaoService.updateProfile(prof);
      } else {
        circabcDaoService.insertProfile(prof);
      }
      circabcDaoService.updateProfileTitles(prof.getId());
    } catch (Exception e) {
      throw new ProfilePersistenceException("Failed to save profile", e);
    }
  }

  private void handleSpecialProfiles(
    String name,
    long interestGroupID,
    boolean isVisible,
    NodeRef igNodeRef
  ) {
    try {
      if ("guest".equals(name)) {
        circabcDaoService.updateInterestGroupPublic(interestGroupID, isVisible);
        updateCanApplyForMembership(igNodeRef, interestGroupID);
      } else if ("EVERYONE".equals(name)) {
        circabcDaoService.updateInterestGroupRegistered(
          interestGroupID,
          isVisible
        );
        updateCanApplyForMembership(igNodeRef, interestGroupID);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error updating special profile visibility: ", e);
      }
    }
  }

  private void updateCanApplyForMembership(
    NodeRef igNodeRef,
    long interestGroupID
  ) {
    boolean canRegisteredApply = true;
    final Serializable property = nodeService.getProperty(
      igNodeRef,
      CircabcModel.PROP_CAN_REGISTERED_APPLY
    );
    if (property != null) {
      canRegisteredApply = (Boolean) property;
    }
    try {
      circabcDaoService.updateInterestGroupApplyForMemberhip(
        interestGroupID,
        canRegisteredApply
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error updateInterestGroupApplyForMemberhip : ", e);
      }
    }
  }

  /**
   * Inserts a category into the database under the given header and refreshes the category's stored
   * properties.
   *
   * @param headerNodeRef the Alfresco node reference of the parent header
   * @param categoryNodeRef the Alfresco node reference of the category to add
   */
  @Override
  public void addCategoryNode(NodeRef headerNodeRef, NodeRef categoryNodeRef) {
    String nodeRef = categoryNodeRef.toString();
    long id = (Long) nodeService.getProperty(
      categoryNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    String name = (String) nodeService.getProperty(
      categoryNodeRef,
      ContentModel.PROP_NAME
    );
    String title = null;
    Serializable titleObj = nodeService.getProperty(
      categoryNodeRef,
      ContentModel.PROP_TITLE
    );
    if (titleObj instanceof String s) {
      title = s;
    } else if (titleObj instanceof MLText mltext) {
      title = mltext.getDefaultValue();
    }

    if (Objects.equals(title, "") || title == null) {
      title = name;
    }
    long headerID = (Long) nodeService.getProperty(
      headerNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    Category category = new Category(id, name, title, nodeRef, headerID);
    try {
      circabcDaoService.insertCategory(category);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error insertCategory : " + category.toString(), e);
      }
    }

    updateCategoryProperties(categoryNodeRef);
  }

  /**
   * Inserts the given user (identified by its person node) into the CIRCABC database, resolving the
   * user's preferred UI locale.
   *
   * <p>If the Alfresco locale map cannot be loaded the error is logged and no user is inserted.
   *
   * @param userNodeRef the Alfresco node reference of the person to add
   */
  @Override
  public void addUser(NodeRef userNodeRef) {
    Map<String, Long> allAlfrescoLocale = null;
    try {
      allAlfrescoLocale = circabcDaoService.getAllAlfrescoLocale();
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(ERROR_GET_ALL_ALFRESCO_LOCALE, e);
      }
    }
    if (allAlfrescoLocale != null) {
      insertUser(userNodeRef, allAlfrescoLocale);
    }
  }

  private void insertUser(
    NodeRef nodeRef,
    Map<String, Long> allAlfrescoLocale
  ) {
    final Map<QName, Serializable> properties = nodeService.getProperties(
      nodeRef
    );
    final long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
    final String userName = (String) properties.get(ContentModel.PROP_USERNAME);
    final String firstName = (String) properties.get(
      ContentModel.PROP_FIRSTNAME
    );
    final String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
    final String emailName = (String) properties.get(ContentModel.PROP_EMAIL);

    final String domain = (String) properties.get(UserModel.PROP_DOMAIN);

    final String ecasUserName = (String) properties.get(
      UserModel.PROP_ECAS_USER_NAME
    );

    final Boolean globalNotification = (Boolean) properties.get(
      UserModel.PROP_GLOBAL_NOTIFICATION
    );
    final Boolean visibility = (Boolean) properties.get(
      UserModel.PROP_VISISBILITY
    );

    String localeStr = getUILanguage(nodeRef);
    final Long localeID = allAlfrescoLocale.get(localeStr);

    User user = new User(
      id,
      userName,
      firstName,
      lastName,
      emailName,
      nodeRef.toString(),
      localeID,
      ecasUserName,
      domain,
      visibility,
      globalNotification
    );
    try {
      circabcDaoService.insertUser(user);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error insertUser : " + user.toString(), e);
      }
    }
  }

  private String getUILanguage(NodeRef nodeRef) {
    NodeRef prefRef = null;
    if (!nodeService.hasAspect(nodeRef, ApplicationModel.ASPECT_CONFIGURABLE)) {
      return null;
    }

    // target of the assoc is the configurations folder ref
    NodeRef configRef = configurableService.getConfigurationFolder(nodeRef);
    if (configRef == null) {
      return null;
    }

    String xpath = NamespaceService.APP_MODEL_PREFIX + ":" + "preferences";

    List<NodeRef> nodes = searchService.selectNodes(
      configRef,
      xpath,
      null,
      namespaceService,
      false
    );

    if (nodes.size() == 1) {
      prefRef = nodes.get(0);
    } else {
      return null;
    }

    final String userInterfaceLang = (String) nodeService.getProperty(
      prefRef,
      UserService.PREF_INTERFACE_LANGUAGE
    );
    if (userInterfaceLang != null) {
      Locale locale = Locale.of(userInterfaceLang);
      return DefaultTypeConverter.INSTANCE.convert(String.class, locale);
    } else {
      return null;
    }
  }

  /**
   * Updates the stored name and title of the specified interest group in the database, including its
   * title translations.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   */
  @Override
  public void updateIntestGroupProperties(NodeRef igNodeRef) {
    long interestGroupID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    String name = (String) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NAME
    );

    Object titleObj = nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_TITLE
    );
    String title = "";
    if (titleObj instanceof String) {
      title = (String) nodeService.getProperty(
        igNodeRef,
        ContentModel.PROP_TITLE
      );
    } else if (titleObj instanceof MLText) {
      title = (
        (MLText) nodeService.getProperty(igNodeRef, ContentModel.PROP_TITLE)
      ).getDefaultValue();
    }

    try {
      final InterestGroup interestGroup = new InterestGroup();
      interestGroup.setId(interestGroupID);
      interestGroup.setName(name);
      interestGroup.setTitle(title);
      circabcDaoService.updateInterestGroup(interestGroup);
      circabcDaoService.updateInterestGroupTitles(interestGroupID);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error updateIntestGroupTitles : ", e);
      }
    }
  }

  /**
   * Updates the stored name and title of the specified category in the database, including its title
   * translations (multilingual when the Alfresco title is an {@link MLText}).
   *
   * @param catNodeRef the Alfresco node reference of the category
   */
  @Override
  public void updateCategoryProperties(NodeRef catNodeRef) {
    long categoryID = (Long) nodeService.getProperty(
      catNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    String name = (String) nodeService.getProperty(
      catNodeRef,
      ContentModel.PROP_NAME
    );
    String title = null;
    Serializable titleObj = nodeService.getProperty(
      catNodeRef,
      ContentModel.PROP_TITLE
    );
    if (titleObj instanceof String s) {
      title = s;
    } else if (titleObj instanceof MLText mltext) {
      title = mltext.getDefaultValue();
    }

    if (Objects.equals(title, "") || title == null) {
      title = name;
    }

    try {
      final Category category = new Category();
      category.setId(categoryID);
      category.setName(name);
      category.setTitle(title);
      circabcDaoService.updateCategory(category);
      if (titleObj instanceof String) {
        circabcDaoService.updateCategoryTitles(categoryID);
      } else if (titleObj instanceof MLText mltext2) {
        circabcDaoService.updateCategoryTitles(
          categoryID,
          Converter.toI18NProperty(mltext2)
        );
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error updateCategoryTitles : ", e);
      }
    }
  }

  /**
   * Deletes the category (resolved by its node reference) from the database.
   *
   * @param catNodeRef the Alfresco node reference of the category to delete
   */
  @Override
  public void deleteCategory(NodeRef catNodeRef) {
    long categoryID = circabcDaoService.selectCategoryIDByNodeRef(
      catNodeRef.toString()
    );
    try {
      circabcDaoService.deleteCategory(categoryID);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error deleteCategory : ", e);
      }
    }
  }

  /**
   * Deletes the specified interest group (resolved from its node's database id) from the database.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   */
  @Override
  public void deleteIntestGroup(NodeRef igNodeRef) {
    long interestGroupID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    try {
      circabcDaoService.deleteInterestGroup(interestGroupID);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error deleteInterestGroup : ", e);
      }
    }
  }

  /**
   * Deletes the interest group with the given database id.
   *
   * @param interestGroupID the database id of the interest group to delete
   */
  @Override
  public void deleteIntestGroupByID(Long interestGroupID) {
    try {
      circabcDaoService.deleteInterestGroup(interestGroupID);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error deleteInterestGroup : ", e);
      }
    }
  }

  /**
   * Inserts the given header node into the database.
   *
   * @param headerNodeRef the Alfresco node reference of the header to add
   */
  @Override
  public void addHeaderNode(NodeRef headerNodeRef) {
    insertHeader(headerNodeRef);
  }

  private void insertHeader(NodeRef nodeRef) {
    final Map<QName, Serializable> properties = nodeService.getProperties(
      nodeRef
    );
    final long headerID = (Long) properties.get(ContentModel.PROP_NODE_DBID);
    final String name = (String) properties.get(ContentModel.PROP_NAME);
    final String description = (String) properties.get(
      ContentModel.PROP_DESCRIPTION
    );
    Header header = new Header(headerID, name, description, nodeRef.toString());
    try {
      circabcDaoService.insertHeader(header);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error insertHeader : " + header.toString(), e);
      }
    }
  }

  /**
   * Deletes the header (resolved by its node reference) from the database.
   *
   * @param nodeRef the Alfresco node reference of the header to delete
   */
  @Override
  public void deleteHeader(NodeRef nodeRef) {
    try {
      circabcDaoService.deleteHeader(nodeRef.toString());
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error deleteHeader : ", e);
      }
    }
  }

  /**
   * Returns the interest groups within a category that are visible/accessible to the given user.
   *
   * <p>Errors are logged and yield an empty (or partial) list.
   *
   * @param categoryRef the Alfresco node reference of the category
   * @param userName the user name whose accessible interest groups are requested
   * @return the list of {@link InterestGroupItem}s; never {@code null}
   */
  @Override
  public List<InterestGroupItem> getInterestGroupByCategoryUser(
    NodeRef categoryRef,
    String userName
  ) {
    List<InterestGroupItem> result = new ArrayList<>();
    long categoryID = (Long) nodeService.getProperty(
      categoryRef,
      ContentModel.PROP_NODE_DBID
    );
    try {
      final List<InterestGroupResult> selectIgByCategoryIDUserName =
        circabcDaoService.selectIgByCategoryIDUserName(categoryID, userName);
      for (InterestGroupResult interestGroupResult : selectIgByCategoryIDUserName) {
        InterestGroupItem item;
        item = new InterestGroupItem(interestGroupResult);
        result.add(item);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error getInterestGroupByCategoryUser : ", e);
      }
    }

    return result;
  }

  /**
   * Updates the stored record of the given user (name, email, locale, domain, notification and
   * visibility flags) in the database from its current Alfresco properties.
   *
   * @param userNodeRef the Alfresco node reference of the person to update
   */
  @Override
  public void updateUser(NodeRef userNodeRef) {
    final Map<QName, Serializable> properties = nodeService.getProperties(
      userNodeRef
    );
    final long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
    final String userName = (String) properties.get(ContentModel.PROP_USERNAME);
    final String firstName = (String) properties.get(
      ContentModel.PROP_FIRSTNAME
    );
    final String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
    final String emailName = (String) properties.get(ContentModel.PROP_EMAIL);

    final String domain = (String) properties.get(UserModel.PROP_DOMAIN);

    final String ecasUserName = (String) properties.get(
      UserModel.PROP_ECAS_USER_NAME
    );

    final Boolean globalNotification = (Boolean) properties.get(
      UserModel.PROP_GLOBAL_NOTIFICATION
    );
    final Boolean visibility = (Boolean) properties.get(
      UserModel.PROP_VISISBILITY
    );

    String localeStr = getUILanguage(userNodeRef);
    Map<String, Long> allAlfrescoLocale = null;
    try {
      allAlfrescoLocale = circabcDaoService.getAllAlfrescoLocale();
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(ERROR_GET_ALL_ALFRESCO_LOCALE, e);
      }
    }
    Long localeID = 0L;
    if (allAlfrescoLocale != null) {
      localeID = allAlfrescoLocale.get(localeStr);
    }

    User user = new User(
      id,
      userName,
      firstName,
      lastName,
      emailName,
      userNodeRef.toString(),
      localeID,
      ecasUserName,
      domain,
      visibility,
      globalNotification
    );
    try {
      circabcDaoService.updateUser(user);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error insertUser : " + user.toString(), e);
      }
    }
  }

  /**
   * Fully resynchronizes the database representation of an interest group with Alfresco.
   *
   * <p>The interest group is deleted and reinserted under its parent category, its logo reference is
   * refreshed, all profiles and their members are reinserted, and multilingual properties are
   * repopulated. Does nothing if the node does not carry the interest group root aspect.
   *
   * @param nodeRef the Alfresco node reference of the interest group root to resynchronize
   */
  @Override
  public void resyncInterestGroup(NodeRef nodeRef) {
    if (!nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
      return;
    }

    deleteIntestGroup(nodeRef);
    NodeRef categoryNodeRef = nodeService
      .getParentAssocs(nodeRef)
      .iterator()
      .next()
      .getParentRef();
    long categoryID = circabcDaoService.selectCategoryIDByNodeRef(
      categoryNodeRef.toString()
    );
    long interestGroupNodeDBID = (Long) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_NODE_DBID
    );
    insertInterestGroup(categoryID, nodeRef);

    updateLogoRef(nodeRef, interestGroupNodeDBID);
    insertAllProfiles(nodeRef, interestGroupNodeDBID);
    circabcDaoService.insertMultilingualPropertiesByInterestGroupID(
      interestGroupNodeDBID
    );
  }

  private void updateLogoRef(NodeRef nodeRef, long interestGroupNodeDBID) {
    try {
      DefaultLogoConfiguration logoDefinition =
        logoPreferencesService.getDefault(nodeRef);
      String logoId = "";
      if (logoDefinition != null && logoDefinition.getLogo() != null) {
        NodeRef logoRef = logoDefinition.getLogo().getReference();
        if (logoRef != null && nodeService.exists(logoRef)) {
          logoId = logoRef.toString();
        }
      }
      circabcDaoService.updateLogoIdForGroup(interestGroupNodeDBID, logoId);
    } catch (CustomizationException e) {
      logger.error("Impossible to update the logo ref in the group resync", e);
    }
  }

  private void insertAllProfiles(NodeRef nodeRef, long interestGroupNodeDBID) {
    List<io.swagger.model.db.Profile> profiles = profileService.getProfiles(
      nodeRef
    );
    for (io.swagger.model.db.Profile profile : profiles) {
      profile.setInterestGroupID(interestGroupNodeDBID);
      insertProfile(nodeRef, profile);
    }
  }

  private void insertProfile(
    NodeRef interestGroup,
    io.swagger.model.db.Profile profile
  ) {
    if (profile == null) {
      return;
    }
    persistProfile(profile);
    processProfileUsers(interestGroup, profile);
  }

  private void persistProfile(io.swagger.model.db.Profile profile) {
    try {
      circabcDaoService.insertProfile(profile);
      circabcDaoService.updateProfileTitles(profile.getId());
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error insertProfile : " + profile.toString(), e);
      }
    }
  }

  private void processProfileUsers(
    NodeRef interestGroup,
    io.swagger.model.db.Profile profile
  ) {
    if (isExcludedGroup(profile.getAlfrescoGroup()) || profile.isImported()) {
      return;
    }
    final Set<String> personInProfile = profileService.getPersonInProfile(
      interestGroup,
      profile.getName()
    );
    for (String userName : personInProfile) {
      if (!profile.getAlfrescoGroup().equalsIgnoreCase("ALL_CIRCA_USERS")) {
        insertProfileUser(profile.getAlfrescoGroup(), userName);
      }
    }
  }

  private boolean isExcludedGroup(String alfrescoGroup) {
    return alfrescoGroup.equals("guest") || alfrescoGroup.equals("EVERYONE");
  }

  private void insertProfileUser(final String alfrescoGroup, String userName) {
    final NodeRef person = personService.getPerson(userName);
    final Map<QName, Serializable> personProperties = nodeService.getProperties(
      person
    );
    final long personID = (Long) personProperties.get(
      ContentModel.PROP_NODE_DBID
    );
    ProfileUser profileUser = new ProfileUser(personID, alfrescoGroup);
    try {
      circabcDaoService.insertProfileUser(profileUser);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error insertProfileUse : " + profileUser.toString(), e);
      }
    }
  }

  private long insertInterestGroup(long categoryID, NodeRef interestGroup) {
    final Map<QName, Serializable> properties = nodeService.getProperties(
      interestGroup
    );

    String title = "";
    Object titleProp = properties.get(ContentModel.PROP_TITLE);
    if (titleProp instanceof MLText mltext) {
      title = mltext.getDefaultValue();
    } else if (titleProp instanceof String s) {
      title = s;
    }

    String name = (String) properties.get(ContentModel.PROP_NAME);
    Boolean isApplyForMembership = false;
    Boolean isPublic = profileService.hasGuestVisibility(interestGroup);
    Boolean isRegistered = false;
    try {
      isRegistered = profileService.hasAllCircabcUsersVisibility(interestGroup);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error when call hasAllCircabcUsersVisibility for ig nodeRef : " +
            interestGroup.toString(),
          e
        );
      }
    }

    if (Boolean.TRUE.equals(isRegistered)) {
      isApplyForMembership = (Boolean) properties.get(
        CircabcModel.PROP_CAN_REGISTERED_APPLY
      );
      if (isApplyForMembership == null) {
        isApplyForMembership = true;
      }
    }

    long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
    String logoRef = "";

    InterestGroup ig = new InterestGroup(
      categoryID,
      id,
      name,
      title,
      interestGroup.toString(),
      isPublic,
      isRegistered,
      isApplyForMembership,
      logoRef
    );
    try {
      circabcDaoService.insertInterestGroup(ig);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error insertInterestGroup : " + ig.toString(), e);
      }
    }
    return id;
  }

  /**
   * Returns the database locale id associated with the given user.
   *
   * @param userName the user name whose locale id is requested
   * @return the locale id, or {@code 1L} (the default locale) if none is stored
   */
  @Override
  public Long getUserLocaleID(String userName) {
    final Long localeID = circabcDaoService.selectLocaleIDByUserName(userName);
    if (localeID == null) {
      // default locale
      return 1L;
    } else {
      return localeID;
    }
  }

  /**
   * Returns the members of an interest group matching a free-text filter, using the default result
   * ordering.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param localeID the locale id used to resolve profile titles
   * @param alfrescoGroup optional Alfresco group (profile) to restrict the result to
   * @param text free-text filter applied to the users
   * @return the matching list of {@link UserWithProfile}; empty if the node is not an interest group
   *     root
   */
  @Override
  public List<UserWithProfile> getFilteredUsers(
    NodeRef igNodeRef,
    long localeID,
    String alfrescoGroup,
    String text
  ) {
    return getFilteredUsers(igNodeRef, localeID, alfrescoGroup, text, null);
  }

  /**
   * Returns the members of an interest group matching a free-text filter, with an explicit result
   * ordering.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param localeID the locale id used to resolve profile titles
   * @param alfrescoGroup optional Alfresco group (profile) to restrict the result to
   * @param text free-text filter applied to the users
   * @param order the ordering to apply to the results
   * @return the matching list of {@link UserWithProfile}; empty if the node is not an interest group
   *     root
   */
  @Override
  public List<UserWithProfile> getFilteredUsers(
    NodeRef igNodeRef,
    long localeID,
    String alfrescoGroup,
    String text,
    String order
  ) {
    List<UserWithProfile> result = Collections.emptyList();
    if (nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
      long igID = (Long) nodeService.getProperty(
        igNodeRef,
        ContentModel.PROP_NODE_DBID
      );

      result = circabcDaoService.selectUsersProfiles(
        igID,
        localeID,
        alfrescoGroup,
        text,
        order
      );
    }
    return result;
  }

  /**
   * Returns the members of an interest group matching per-field filters (first name, last name and
   * email), with an explicit result ordering.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param localeID the locale id used to resolve profile titles
   * @param alfrescoGroup optional Alfresco group (profile) to restrict the result to
   * @param firstName filter applied to the user's first name
   * @param lastName filter applied to the user's last name
   * @param email filter applied to the user's email
   * @param order the ordering to apply to the results
   * @return the matching list of {@link UserWithProfile}; empty if the node is not an interest group
   *     root
   */
  @Override
  public List<UserWithProfile> getFilteredUsers(
    NodeRef igNodeRef,
    long localeID,
    String alfrescoGroup,
    String firstName,
    String lastName,
    String email,
    String order
  ) {
    List<UserWithProfile> result = Collections.emptyList();
    if (nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
      long igID = (Long) nodeService.getProperty(
        igNodeRef,
        ContentModel.PROP_NODE_DBID
      );

      result = circabcDaoService.selectUsersProfilesFiltered(
        igID,
        localeID,
        alfrescoGroup,
        firstName,
        lastName,
        email,
        order
      );
    }
    return result;
  }

  /**
   * Grants the CIRCABC administrator role to the given user.
   *
   * @param userName the user name to promote to CIRCABC administrator
   */
  @Override
  public void addCircabcAdmin(String userName) {
    insertCircabcAdmin(userName);
  }

  private void insertCircabcAdmin(String userName) {
    final NodeRef person = personService.getPerson(userName);
    final Map<QName, Serializable> personProperties = nodeService.getProperties(
      person
    );
    final long personID = (Long) personProperties.get(
      ContentModel.PROP_NODE_DBID
    );
    CircabcAdmin circabcAdmin = new CircabcAdmin(personID);

    try {
      circabcDaoService.insertCircabcAdmin(circabcAdmin);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error insertCircabcAdmin : " + circabcAdmin.toString(),
          e
        );
      }
    }
  }

  /**
   * Revokes the CIRCABC administrator role from the given user.
   *
   * @param userName the user name to demote from CIRCABC administrator
   */
  @Override
  public void removeCircabcAdmin(String userName) {
    deleteCircabcAdmin(userName);
  }

  private void deleteCircabcAdmin(String userName) {
    final NodeRef person = personService.getPerson(userName);
    final Map<QName, Serializable> personProperties = nodeService.getProperties(
      person
    );
    final long personID = (Long) personProperties.get(
      ContentModel.PROP_NODE_DBID
    );
    CircabcAdmin circabcAdmin = new CircabcAdmin(personID);
    try {
      circabcDaoService.deleteCircabcAdmin(circabcAdmin);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error deleteCircabcAdmin : " + circabcAdmin.toString(),
          e
        );
      }
    }
  }

  /**
   * Returns the user names of the administrators of the specified category.
   *
   * @param categoryNodeRef the Alfresco node reference of the category
   * @return the list of category administrator user names
   */
  @Override
  public List<String> getCategoryAdmins(NodeRef categoryNodeRef) {
    long categID = (Long) nodeService.getProperty(
      categoryNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.selectCategoryAdmins(categID);
  }

  /**
   * Removes the given user from all profiles of the specified interest group, effectively deleting
   * their membership.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param userName the user name to remove from the group
   */
  @Override
  public void deletePersonFromGroup(NodeRef igNodeRef, String userName) {
    Long groupId = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );

    List<ProfileUser> userProfile = circabcDaoService.getUserProfileInGroup(
      groupId,
      userName
    );
    for (ProfileUser membership : userProfile) {
      circabcDaoService.deleteUserInGroup(membership);
    }
  }

  /**
   * Returns the localized title translations of the specified interest group.
   *
   * @param igRef the Alfresco node reference of the interest group root
   * @return a map of locale code to translated title
   */
  @Override
  public Map<String, String> getInterestGroupTitle(NodeRef igRef) {
    long igID = (Long) nodeService.getProperty(
      igRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.getGroupTitleTranslations(igID);
  }

  /**
   * Returns the localized title translations of the specified profile.
   *
   * @param profileNodeRef the Alfresco node reference of the profile
   * @return a map of locale code to translated title
   */
  @Override
  public Map<String, String> getProfileTitle(NodeRef profileNodeRef) {
    long profileId = (Long) nodeService.getProperty(
      profileNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.getProfileTitleTranslations(profileId);
  }

  /**
   * Returns the localized title translations of the specified category.
   *
   * @param categRef the Alfresco node reference of the category
   * @return a map of locale code to translated title
   */
  @Override
  public Map<String, String> getCategoryTitle(NodeRef categRef) {
    long categId = (Long) nodeService.getProperty(
      categRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.getCategoryTitleTranslations(categId);
  }

  /**
   * Indicates whether a user with the given user name exists in the CIRCABC database.
   *
   * <p>Any error during lookup is logged and treated as "does not exist".
   *
   * @param userName the user name to check
   * @return {@code true} if the user exists, {@code false} otherwise
   */
  @Override
  public boolean isUserExists(String userName) {
    try {
      long id = getUserIDByUserName(userName);
      return (id > 0L);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error in isUserExists", e);
      }
      return false;
    }
  }

  private long getUserIDByUserName(String userName) {
    long result = 0;
    try {
      result = circabcDaoService.selectUserIDByUserName(userName);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error selectUserIDByUserName : " + userName, e);
      }
    }
    return result;
  }

  /**
   * Updates the "public" visibility flag of the specified interest group. No action is taken if
   * either argument is {@code null}.
   *
   * @param igRef the Alfresco node reference of the interest group root
   * @param isPublic the new public visibility value
   */
  @Override
  public void updateInterestGroupPublic(NodeRef igRef, Boolean isPublic) {
    if (igRef != null && isPublic != null) {
      long igId = (Long) nodeService.getProperty(
        igRef,
        ContentModel.PROP_NODE_DBID
      );
      circabcDaoService.updateInterestGroupPublic(igId, isPublic);
    }
  }

  /**
   * Updates the "registered users" visibility flag of the specified interest group. No action is
   * taken if either argument is {@code null}.
   *
   * @param igRef the Alfresco node reference of the interest group root
   * @param isRegistered the new registered-visibility value
   */
  @Override
  public void updateInterestGroupRegistered(
    NodeRef igRef,
    Boolean isRegistered
  ) {
    if (igRef != null && isRegistered != null) {
      long igId = (Long) nodeService.getProperty(
        igRef,
        ContentModel.PROP_NODE_DBID
      );
      circabcDaoService.updateInterestGroupRegistered(igId, isRegistered);
    }
  }

  /**
   * Updates the "can apply for membership" flag of the specified interest group. No action is taken
   * if either argument is {@code null}.
   *
   * @param igRef the Alfresco node reference of the interest group root
   * @param applicationAllowed whether membership applications are allowed
   */
  @Override
  public void updateInterestGroupApplication(
    NodeRef igRef,
    Boolean applicationAllowed
  ) {
    if (igRef != null && applicationAllowed != null) {
      long igId = (Long) nodeService.getProperty(
        igRef,
        ContentModel.PROP_NODE_DBID
      );
      circabcDaoService.updateInterestGroupApplyForMemberhip(
        igId,
        applicationAllowed
      );
    }
  }

  /**
   * Deletes all CIRCABC data from the database. Intended for full reinitialization/resynchronization
   * scenarios; use with caution as this is irreversible.
   */
  @Override
  public void deleteAll() {
    circabcDaoService.deleteAll();
  }

  /**
   * Counts the number of members in the interest group identified by the given short id.
   *
   * @param id the short node id of the interest group root
   * @return the number of users in the interest group
   */
  @Override
  public int countMembersInIg(String id) {
    NodeRef igNodeRef = Converter.createNodeRefFromId(id);

    long igDBId = (long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );

    return circabcDaoService.countUsersInIg(igDBId);
  }

  /**
   * Returns the set of user names that are members of the interest group identified by the given
   * short id.
   *
   * @param interestGroupId the short node id of the interest group root
   * @return the set of member user names
   */
  @Override
  public Set<String> getUserIds(String interestGroupId) {
    NodeRef igNodeRef = Converter.createNodeRefFromId(interestGroupId);
    long igDBId = (long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.getUserIds(igDBId);
  }

  /**
   * Inserts the user with the given user name into the CIRCABC database, resolving their person
   * node first. Does nothing if the person cannot be found.
   *
   * @param userName the user name of the person to add
   */
  @Override
  public void addUser(String userName) {
    NodeRef userRef = personService.getPerson(userName);
    if (userRef != null) {
      addUser(userRef);
    }
  }

  /**
   * Returns the interest groups in the given category, excluding the current one, that the specified
   * user can see.
   *
   * @param categoryNodeRef the Alfresco node reference of the category
   * @param currentIgNodeRef the Alfresco node reference of the interest group to exclude
   * @param username the user name whose accessible interest groups are requested
   * @return the list of matching {@link IGData}
   */
  @Override
  public List<IGData> getCategoryIGsExceptCurrent(
    NodeRef categoryNodeRef,
    NodeRef currentIgNodeRef,
    String username
  ) {
    long categoryID = (Long) nodeService.getProperty(
      categoryNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    long interestGroupID = (Long) nodeService.getProperty(
      currentIgNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.getCategoryIGsExceptCurrent(
      categoryID,
      interestGroupID,
      username
    );
  }

  /**
   * Returns the interest groups in the given category, excluding the current one, that the specified
   * user can access through directory access.
   *
   * @param categoryNodeRef the Alfresco node reference of the category
   * @param currentIgNodeRef the Alfresco node reference of the interest group to exclude
   * @param username the user name whose accessible interest groups are requested
   * @return the list of matching {@link IGData}
   */
  @Override
  public List<IGData> getCategoryIGsExceptCurrentWithDirectoryAccess(
    NodeRef categoryNodeRef,
    NodeRef currentIgNodeRef,
    String username
  ) {
    long categoryID = (Long) nodeService.getProperty(
      categoryNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    long interestGroupID = (Long) nodeService.getProperty(
      currentIgNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    return circabcDaoService.getCategoryIGsExceptCurrentWithDirectoryAccess(
      categoryID,
      interestGroupID,
      username
    );
  }

  /**
   * Returns the name of the profile the given user holds in the specified interest group.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param userName the user name whose profile is requested
   * @return the profile name
   */
  @Override
  public String getPersonProfile(NodeRef igNodeRef, String userName) {
    io.swagger.model.db.Profile profile =
      circabcDaoService.selectProfileByInterestGroupNodeRefUserName(
        igNodeRef.toString(),
        userName
      );
    return profile.getName();
  }

  /**
   * Returns the Alfresco group name backing the profile the given user holds in the specified
   * interest group.
   *
   * @param igNodeRef the Alfresco node reference of the interest group root
   * @param userName the user name whose profile group is requested
   * @return the Alfresco group name of the user's profile
   */
  @Override
  public String getPersonProfileGroupName(NodeRef igNodeRef, String userName) {
    io.swagger.model.db.Profile profile =
      circabcDaoService.selectProfileByInterestGroupNodeRefUserName(
        igNodeRef.toString(),
        userName
      );
    return profile.getAlfrescoGroup();
  }

  /**
   * Returns the email addresses of the administrators of the specified category.
   *
   * @param categoryRef the Alfresco node reference of the category
   * @return the list of category administrator email addresses
   */
  @Override
  public List<String> getCategoryAdminEmails(NodeRef categoryRef) {
    return circabcDaoService.selectCategoryAdminEmails(categoryRef.toString());
  }

  /**
   * Returns the email addresses of the administrators of the specified interest group.
   *
   * @param interestGroupRef the Alfresco node reference of the interest group root
   * @return the list of interest group administrator email addresses
   */
  @Override
  public List<String> getInterestGroupAdminEmails(NodeRef interestGroupRef) {
    return circabcDaoService.selectInterestGroupAdminEmails(
      interestGroupRef.toString()
    );
  }

  /**
   * Returns the node references of all categories stored in the CIRCABC database.
   *
   * @return the list of category {@link NodeRef}s
   */
  @Override
  public List<NodeRef> getCategories() {
    return circabcDaoService
      .selectCategoriesNodeRef()
      .stream()
      .map(NodeRef::new)
      .toList();
  }

  /**
   * Deletes the given user from all CIRCABC database tables.
   *
   * @param userName the user name to delete
   * @throws UserDeletionException if the deletion fails
   */
  @Override
  public void deleteUserFromDatabase(String userName) {
    try {
      circabcDaoService.deleteUserFromAllTables(userName);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error deleting user from database: " + userName, e);
      }
      throw new UserDeletionException("Failed to delete user from database", e);
    }
  }
}
