package eu.europa.ec.digit.circabc.rest.service.app;

import io.swagger.model.I18nProperty;
import io.swagger.model.db.Category;
import io.swagger.model.db.CategoryAdmin;
import io.swagger.model.db.CircabcAdmin;
import io.swagger.model.db.ExportedProfileItem;
import io.swagger.model.db.Header;
import io.swagger.model.db.HeaderCategory;
import io.swagger.model.db.IGData;
import io.swagger.model.db.InterestGroup;
import io.swagger.model.db.InterestGroupResult;
import io.swagger.model.db.KeyValue;
import io.swagger.model.db.KeyValueString;
import io.swagger.model.db.Profile;
import io.swagger.model.db.ProfileUser;
import io.swagger.model.db.ProfileWithUsersCount;
import io.swagger.model.db.TranslationEntry;
import io.swagger.model.db.User;
import io.swagger.model.db.UserIGMembership;
import io.swagger.model.db.UserWithProfile;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;

// Removed ESAPI usage; MyBatis uses prepared statements for parameters.

/**
 * MyBatis-backed data access object (DAO) for CIRCABC's relational data model.
 *
 * <p>This service centralizes all direct SQL access to the CIRCABC database tables
 * (headers, categories, interest groups, profiles, users, admins and their
 * multilingual title translations). Every operation is delegated to a MyBatis
 * mapped statement executed through the injected {@link SqlSessionTemplate},
 * which relies on prepared statements so that user-supplied values cannot be
 * used for SQL injection.</p>
 *
 * <p>The class is wired as a Spring bean; its collaborators
 * ({@link #setSqlSessionTemplate(SqlSessionTemplate)} and
 * {@link #setHibernateDialect(String)}) are injected via setters and
 * {@link #init()} is invoked as the bean's initialization callback.</p>
 */
public class CircabcDaoServiceImpl {

  /** MyBatis parameter key for a category identifier ("categoryId"). */
  private static final String CATEGORY_ID = "categoryId";
  /** MyBatis parameter key for a profile identifier ("profileID"). */
  private static final String PROFILE_ID = "profileID";
  /** MyBatis parameter key for a user name ("userName"). */
  private static final String USER_NAME = "userName";
  /** MyBatis parameter key for a category identifier variant ("catID"). */
  private static final String CAT_ID = "catID";
  /** MyBatis parameter key for a locale identifier ("localeID"). */
  private static final String LOCALE_ID = "localeID";
  /** MyBatis parameter key for an Alfresco group name ("alfrescoGroup"). */
  private static final String ALFRESCO_GROUP = "alfrescoGroup";
  /** MyBatis parameter key for an email address ("email"). */
  private static final String EMAIL = "email";
  /** MyBatis parameter key for a first name ("firstName"). */
  private static final String FIRST_NAME = "firstName";
  /** MyBatis parameter key for a last name ("lastName"). */
  private static final String LAST_NAME = "lastName";
  /** MyBatis parameter key for the column to order by ("orderByColumn"). */
  private static final String ORDER_BY_COLUMN = "orderByColumn";
  /** MyBatis parameter key for the ordering direction ("orderByType"). */
  private static final String ORDER_BY_TYPE = "orderByType";
  /** MyBatis parameter key for a user name variant ("username"). */
  private static final String USERNAME = "username";
  /** Logger for this DAO service. */
  private static Log logger = LogFactory.getLog(CircabcDaoServiceImpl.class);

  /** MyBatis session template used to execute all mapped SQL statements. */
  private SqlSessionTemplate sqlSessionTemplate = null;
  /** Configured Hibernate/database dialect, used only for informational logging. */
  private String hibernateDialect = "";

  /**
   * Sets the configured database dialect (injected by Spring).
   *
   * @param hibernateDialect the dialect string to record
   */
  public void setHibernateDialect(String hibernateDialect) {
    this.hibernateDialect = hibernateDialect;
  }

  /**
   * Bean initialization callback that logs the configured database dialect
   * for diagnostic visibility. Performs no other setup.
   */
  public void init() {
    // Log the database dialect for visibility. No ESAPI initialization.
    String dbDialect = this.hibernateDialect;
    logger.info("database dialect used: ");
    logger.info(dbDialect);
  }

  /**
   * Inserts a new header row.
   *
   * @param header the header to persist
   */
  public void insertHeader(Header header) {
    sqlSessionTemplate.insert("Circabc.insert_header", header);
  }

  /**
   * Updates an existing header, matched by its node reference.
   *
   * @param header the header carrying the new values and target reference
   */
  public void updateHeader(Header header) {
    sqlSessionTemplate.insert("Circabc.update_header_by_ref", header);
  }

  /**
   * Inserts a new category row.
   *
   * @param category the category to persist
   */
  public void insertCategory(Category category) {
    sqlSessionTemplate.insert("Circabc.insert_category", category);
  }

  /**
   * Inserts a new user row.
   *
   * @param user the user to persist
   */
  public void insertUser(User user) {
    sqlSessionTemplate.insert("Circabc.insert_user", user);
  }

  /**
   * Grants category administration by inserting a category-admin association.
   *
   * @param catAdmin the category-admin association to persist
   */
  public void insertCategoryAdmin(CategoryAdmin catAdmin) {
    sqlSessionTemplate.insert("Circabc.insert_categegory_admin", catAdmin);
  }

  /**
   * Revokes category administration by deleting a category-admin association.
   *
   * @param catAdmin the category-admin association to remove
   */
  public void deleteCategoryAdmin(CategoryAdmin catAdmin) {
    sqlSessionTemplate.delete("Circabc.delete_categegory_admin", catAdmin);
  }

  /**
   * Inserts a new interest group row.
   *
   * @param ig the interest group to persist
   */
  public void insertInterestGroup(InterestGroup ig) {
    sqlSessionTemplate.insert("Circabc.insert_interest_group", ig);
  }

  /**
   * Inserts a new profile row.
   *
   * @param profile the profile to persist
   */
  public void insertProfile(io.swagger.model.db.Profile profile) {
    sqlSessionTemplate.insert("Circabc.insert_profile", profile);
  }

  /**
   * Associates a user with a profile by inserting a profile-user row.
   *
   * @param profileUser the profile-user association to persist
   */
  public void insertProfileUser(ProfileUser profileUser) {
    sqlSessionTemplate.insert("Circabc.insert_profile_user", profileUser);
  }

  /**
   * Retrieves all Alfresco locales known to the database.
   *
   * @return a map of locale code to its numeric locale identifier
   */
  public Map<String, Long> getAllAlfrescoLocale() {
    final List<KeyValue> selectList = sqlSessionTemplate.selectList(
      "Circabc.select_alf_locale"
    );
    Map<String, Long> result = HashMap.newHashMap(selectList.size());
    for (KeyValue keyValue : selectList) {
      result.put(keyValue.getKey(), keyValue.getValue());
    }
    return result;
  }

  /**
   * Populates multilingual title translations for all categories, interest
   * groups and profiles.
   */
  public void insertMultilingualProperties() {
    sqlSessionTemplate.insert("Circabc.insert_cat_titles");
    sqlSessionTemplate.insert("Circabc.insert_ig_titles");
    sqlSessionTemplate.insert("Circabc.insert_profile_titles");
  }

  /**
   * Populates multilingual title translations for a single interest group.
   *
   * @param interestGroupID the interest group identifier
   */
  public void insertMultilingualPropertiesByInterestGroupID(
    long interestGroupID
  ) {
    HashMap<String, Object> params = new HashMap<>();
    params.put("id", interestGroupID);
    sqlSessionTemplate.insert("Circabc.insert_ig_titles_by_id", params);
  }

  /**
   * Removes a user's profile membership within a given interest group.
   *
   * @param interestGroupID the interest group identifier
   * @param userName the user name whose membership is removed
   * @return the number of profile-user rows deleted
   */
  public int deleteProfileByInterestGroupUserName(
    long interestGroupID,
    String userName
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(USER_NAME, userName);
    params.put("interestGroupID", interestGroupID);
    return sqlSessionTemplate.delete(
      "Circabc.delete_profile_user_by_ig_id_user_name",
      params
    );
  }

  /**
   * Looks up the numeric user identifier for a given user name.
   *
   * @param userName the user name to resolve
   * @return the user identifier, or {@code 0} if the user does not exist
   */
  public long selectUserIDByUserName(String userName) {
    Object id = sqlSessionTemplate.selectOne(
      "Circabc.select_user_id_by_user_name",
      userName
    );
    return (id != null ? (Long) id : 0L);
  }

  /**
   * Updates an existing profile row.
   *
   * @param profile the profile carrying the new values
   */
  public void updateProfile(io.swagger.model.db.Profile profile) {
    sqlSessionTemplate.update("Circabc.update_profile", profile);
  }

  /**
   * Updates the "public" visibility flag of an interest group.
   *
   * @param interestGroupID the interest group identifier
   * @param isPublic {@code true} to mark the group public, {@code false} otherwise
   */
  public void updateInterestGroupPublic(
    long interestGroupID,
    boolean isPublic
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("id", interestGroupID);
    params.put("isPublic", isPublic);
    sqlSessionTemplate.update("Circabc.update_ig_public", params);
  }

  /**
   * Updates the "registered users" access flag of an interest group.
   *
   * @param interestGroupID the interest group identifier
   * @param isRegistered {@code true} to allow registered-user access, {@code false} otherwise
   */
  public void updateInterestGroupRegistered(
    long interestGroupID,
    boolean isRegistered
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("id", interestGroupID);
    params.put("isRegistered", isRegistered);
    sqlSessionTemplate.update("Circabc.update_ig_registered", params);
  }

  /**
   * Updates the "apply for membership" flag of an interest group.
   *
   * @param interestGroupID the interest group identifier
   * @param isApplyForMembership {@code true} to allow membership applications, {@code false} otherwise
   */
  public void updateInterestGroupApplyForMemberhip(
    long interestGroupID,
    boolean isApplyForMembership
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("id", interestGroupID);
    params.put("isApplyForMembership", isApplyForMembership);
    sqlSessionTemplate.update("Circabc.update_ig_apply_for_membership", params);
  }

  /**
   * Updates the "exported" flag of a profile.
   *
   * @param profileID the profile identifier
   * @param export {@code true} to mark the profile as exported, {@code false} otherwise
   */
  public void updateProfileExported(long profileID, boolean export) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("id", profileID);
    params.put("isExported", export);
    sqlSessionTemplate.update("Circabc.update_profile_export", params);
  }

  /**
   * Finds a profile by its interest group identifier and profile name.
   *
   * @param profile a profile instance carrying the interest group id and profile name to match
   * @return the matching profile, or {@code null} if none is found
   */
  public Profile selectProfileByInterestGroupIDProfileName(Profile profile) {
    return (Profile) sqlSessionTemplate.selectOne(
      "Circabc.select_profile_by_ig_id_profile_name",
      profile
    );
  }

  /**
   * Finds the profile a user holds within an interest group identified by its node reference.
   *
   * @param interestGroupNodeRef the interest group's Alfresco node reference
   * @param userName the user name to match
   * @return the matching profile, or {@code null} if none is found
   */
  public Profile selectProfileByInterestGroupNodeRefUserName(
    String interestGroupNodeRef,
    String userName
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("nodeRef", interestGroupNodeRef);
    params.put(USER_NAME, userName);

    return (Profile) sqlSessionTemplate.selectOne(
      "Circabc.select_profile_by_ig_ref_user_name",
      params
    );
  }

  /**
   * Deletes a profile by its identifier.
   *
   * @param profileID the profile identifier
   */
  public void deleteProfileByID(long profileID) {
    sqlSessionTemplate.delete("Circabc.delete_profile_by_id", profileID);
  }

  /**
   * Deletes all title translation rows for a given profile.
   *
   * @param profileID the profile identifier
   */
  public void deleteProfileTitleTranslationsByID(long profileID) {
    sqlSessionTemplate.delete(
      "Circabc.delete_profile_title_translations_by_id",
      profileID
    );
  }

  /**
   * Checks whether a profile with the given identifier exists.
   *
   * @param id the profile identifier
   * @return {@code true} if exactly one matching profile exists, {@code false} otherwise
   */
  public boolean profileExists(long id) {
    final int count = (Integer) sqlSessionTemplate.selectOne(
      "Circabc.select_profil_count_by_profile_id",
      id
    );
    return (count == 1);
  }

  /**
   * Refreshes the title translations for a profile by deleting the existing
   * translations and re-inserting them from the source data.
   *
   * @param id the profile identifier
   */
  public void updateProfileTitles(long id) {
    sqlSessionTemplate.delete(
      "Circabc.delete_profile_title_translations_by_id",
      id
    );
    sqlSessionTemplate.insert("Circabc.insert_profile_titles_by_id", id);
  }

  /**
   * Updates a user's preferred UI language.
   *
   * @param userName the user name
   * @param language the language code to set
   */
  public void updateUserUILangauge(String userName, String language) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(USER_NAME, userName);
    params.put("language", language);
    sqlSessionTemplate.update("Circabc.update_user_ui_lang", params);
  }

  /**
   * Refreshes the title translations for an interest group by deleting the
   * existing translations and re-inserting them from the source data.
   *
   * @param interestGroupID the interest group identifier
   */
  public void updateInterestGroupTitles(long interestGroupID) {
    sqlSessionTemplate.delete(
      "Circabc.delete_ig_title_translations_by_id",
      interestGroupID
    );
    sqlSessionTemplate.insert(
      "Circabc.insert_ig_titles_by_id",
      interestGroupID
    );
  }

  /**
   * Refreshes the title translations for a category by deleting the existing
   * translations and re-inserting them from the source data.
   *
   * @param categoryID the category identifier
   */
  public void updateCategoryTitles(long categoryID) {
    sqlSessionTemplate.delete(
      "Circabc.delete_cat_title_translations_by_id",
      categoryID
    );
    sqlSessionTemplate.insert("Circabc.insert_cat_titles_by_id", categoryID);
  }

  /**
   * Replaces the title translations of a category with the supplied localized values.
   *
   * <p>Existing translations are deleted, then each entry in {@code title} whose
   * locale is known to the database is inserted as a new category title row.</p>
   *
   * @param categoryID the category identifier
   * @param title the localized titles keyed by locale
   */
  public void updateCategoryTitles(long categoryID, I18nProperty title) {
    sqlSessionTemplate.delete(
      "Circabc.delete_cat_title_translations_by_id",
      categoryID
    );
    Map<String, Long> locales = getAllAlfrescoLocale();

    for (Entry<String, String> titleEntry : title.entrySet()) {
      String localeStr = (titleEntry.getKey().contains("_")
        ? titleEntry.getKey()
        : titleEntry.getKey() + "_");
      if (locales.containsKey(localeStr)) {
        Map<String, Object> propsTitle = new HashMap<>();
        propsTitle.put(CATEGORY_ID, categoryID);
        propsTitle.put("titleTrans", titleEntry.getValue());
        propsTitle.put("localeId", locales.get(localeStr));
        sqlSessionTemplate.insert("Circabc.insert_category_title", propsTitle);
      }
    }
  }

  /**
   * Resolves the numeric category identifier for an Alfresco node reference.
   *
   * @param nodeRef the category's Alfresco node reference
   * @return the category identifier
   */
  public long selectCategoryIDByNodeRef(String nodeRef) {
    return (Long) sqlSessionTemplate.selectOne(
      "Circabc.select_cat_id_by_node_ref",
      nodeRef
    );
  }

  /**
   * Deletes a category and all of its dependent data.
   *
   * <p>All interest groups belonging to the category are deleted first (via
   * {@link #deleteInterestGroup(long)}), followed by the category's title
   * translations, admin associations and finally the category row itself.</p>
   *
   * @param categoryID the category identifier
   */
  public void deleteCategory(long categoryID) {
    final List<Long> igIDs = sqlSessionTemplate.selectList(
      "Circabc.select_ig_ids_by_cat_id",
      categoryID
    );
    for (Long igID : igIDs) {
      deleteInterestGroup(igID);
    }
    sqlSessionTemplate.delete(
      "Circabc.delete_cat_title_trans_by_cat_id",
      categoryID
    );
    sqlSessionTemplate.delete(
      "Circabc.delete_cat_admins_by_cat_id",
      categoryID
    );
    sqlSessionTemplate.delete("Circabc.delete_cat_by_id", categoryID);
  }

  /**
   * Deletes an interest group and all of its dependent data (profile titles,
   * profile-user associations, profiles, title translations, statistics and
   * finally the interest group row itself).
   *
   * @param interestGroupID the interest group identifier
   */
  public void deleteInterestGroup(long interestGroupID) {
    sqlSessionTemplate.delete(
      "Circabc.delete_profile_titles_by_ig_id",
      interestGroupID
    );
    sqlSessionTemplate.delete(
      "Circabc.delete_profile_users_by_ig_id",
      interestGroupID
    );
    sqlSessionTemplate.delete(
      "Circabc.delete_profiles_by_ig_id",
      interestGroupID
    );
    sqlSessionTemplate.delete(
      "Circabc.delete_ig_title_trans_by_ig_id",
      interestGroupID
    );
    sqlSessionTemplate.delete(
      "Circabc.delete_group_statistics_by_ig_id",
      interestGroupID
    );
    sqlSessionTemplate.delete("Circabc.delete_ig_by_id", interestGroupID);
  }

  /**
   * Moves an interest group from one category to another.
   *
   * @param interestGroupID the interest group identifier
   * @param oldCategoryID the current (source) category identifier
   * @param newCategoryID the target (destination) category identifier
   */
  public void moveInterestGroup(
    long interestGroupID,
    long oldCategoryID,
    long newCategoryID
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(3);
    params.put("igID", interestGroupID);
    params.put("oldCatID", oldCategoryID);
    params.put("newCatID", newCategoryID);
    sqlSessionTemplate.update("Circabc.update_cat_id", params);
  }

  /**
   * Updates an interest group's name and title.
   *
   * @param interestGroup the interest group carrying the new name and title
   */
  public void updateInterestGroup(InterestGroup interestGroup) {
    sqlSessionTemplate.update(
      "Circabc.update_ig_name_title_by_id",
      interestGroup
    );
  }

  /**
   * Updates a category's name and title.
   *
   * @param category the category carrying the new name and title
   */
  public void updateCategory(Category category) {
    sqlSessionTemplate.update("Circabc.update_cat_name_title_by_id", category);
  }

  /**
   * Lists the interest groups within a category that are visible to a user.
   *
   * <p>The result depends on the user's role: the guest user sees only public
   * groups, a category administrator sees all groups, and any other user sees
   * the groups they are allowed to access.</p>
   *
   * @param categoryID the category identifier
   * @param userName the user name for which visibility is evaluated
   * @return the list of visible interest groups
   */
  public List<InterestGroupResult> selectIgByCategoryIDUserName(
    long categoryID,
    String userName
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(CAT_ID, categoryID);
    params.put(USER_NAME, userName);
    final List<InterestGroupResult> result;

    if (userName.equals("guest")) {
      result = sqlSessionTemplate.selectList(
        "Circabc.select_public_ig_by_cat_user",
        params
      );
    } else if (isCategoryAdmin(categoryID, userName)) {
      result = sqlSessionTemplate.selectList(
        "Circabc.select_ig_by_cat_admin",
        params
      );
    } else {
      result = sqlSessionTemplate.selectList(
        "Circabc.select_ig_by_cat_user",
        params
      );
    }
    return result;
  }

  /**
   * Retrieves a single interest group by its identifier.
   *
   * @param interestGroupID the interest group identifier
   * @return the matching interest group result, or {@code null} if none is found
   */
  public InterestGroupResult selectIgByID(long interestGroupID) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put("ID", interestGroupID);
    final InterestGroupResult result;
    result = (InterestGroupResult) sqlSessionTemplate.selectOne(
      "Circabc.select_ig_by_id",
      params
    );
    return result;
  }

  /**
   * Determines whether the given user is an administrator of the given category.
   *
   * @param categoryID the category identifier
   * @param userName the user name to check
   * @return {@code true} if the user administers the category, {@code false} otherwise
   */
  private boolean isCategoryAdmin(
    final long categoryID,
    final String userName
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(CAT_ID, categoryID);
    params.put(USER_NAME, userName);
    int result = (Integer) sqlSessionTemplate.selectOne(
      "Circabc.select_is_user_cat_admin",
      params
    );

    return (result == 1);
  }

  /**
   * Lists exported profiles for a specific interest group within a category.
   *
   * @param categoryID the category identifier
   * @param interestGroupID the interest group identifier
   * @return the list of exported profile items
   */
  public List<ExportedProfileItem> selectExpProfilesByCategoryIDInterestGroupID(
    long categoryID,
    long interestGroupID
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(CAT_ID, categoryID);
    params.put("igID", interestGroupID);
    return sqlSessionTemplate.selectList(
      "Circabc.select_exp_profiles_by_cat_id_ig_id",
      params
    );
  }

  /**
   * Lists exported profiles across all interest groups of a category.
   *
   * @param categoryID the category identifier
   * @return the list of exported profile items
   */
  public List<ExportedProfileItem> selectExpProfilesByCategoryID(
    long categoryID
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put(CAT_ID, categoryID);
    return sqlSessionTemplate.selectList(
      "Circabc.select_exp_profiles_by_cat_id",
      params
    );
  }

  /**
   * Lists the Alfresco groups imported into an interest group.
   *
   * @param interestGroupID the interest group identifier
   * @return the list of imported Alfresco group names
   */
  public List<String> selectImportedAlfGroups(long interestGroupID) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put("igID", interestGroupID);
    return sqlSessionTemplate.selectList(
      "Circabc.select_imp_alf_group_by_ig_id",
      params
    );
  }

  /**
   * Lists the Alfresco groups holding the leader role in an interest group.
   *
   * @param interestGroupID the interest group identifier
   * @return the list of leader Alfresco group names
   */
  public List<String> selectLeaderAlfGroups(long interestGroupID) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put("igID", interestGroupID);
    return sqlSessionTemplate.selectList(
      "Circabc.select_leader_alf_group_by_ig_id",
      params
    );
  }

  /**
   * Lists the email addresses of the leaders of an interest group.
   *
   * @param interestGroupID the interest group identifier
   * @return the list of leader email addresses
   */
  public List<String> selectLeaderEmails(long interestGroupID) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put("igID", interestGroupID);
    return sqlSessionTemplate.selectList(
      "Circabc.select_leader_email_by_ig_id",
      params
    );
  }

  /**
   * Lists the Alfresco groups of a category that are not yet part of a given
   * interest group.
   *
   * @param categoryID the category identifier
   * @param interestGroupID the interest group identifier
   * @return the set of Alfresco group names not associated with the interest group
   */
  public Set<String> selectAlfrescoGroupNotInInterestGroup(
    long categoryID,
    long interestGroupID
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put(CAT_ID, categoryID);
    params.put("igID", interestGroupID);
    List<String> result = sqlSessionTemplate.selectList(
      "Circabc.select_alf_group_not_in_ig",
      params
    );
    return new HashSet<>(result);
  }

  /**
   * Lists the interest group memberships of a user.
   *
   * @param userName the user name
   * @return the list of the user's interest group memberships
   */
  public List<UserIGMembership> selectInterestGroups(String userName) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put(USER_NAME, userName);
    return sqlSessionTemplate.selectList(
      "Circabc.select_ig_node_ref_by_user_name",
      params
    );
  }

  /**
   * Lists the node references of the categories a user can access.
   *
   * @param userName the user name
   * @return the list of category node references
   */
  public List<String> selectCategories(String userName) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put(USER_NAME, userName);
    return sqlSessionTemplate.selectList(
      "Circabc.select_cat_node_ref_by_user_name",
      params
    );
  }

  /**
   * Lists the Alfresco node references of all users.
   *
   * @return the list of user node references
   */
  public List<String> selectAllUserNodeRef() {
    return sqlSessionTemplate.selectList("Circabc.select_all_user_node_ref");
  }

  /**
   * Updates an existing user row.
   *
   * @param user the user carrying the new values
   */
  public void updateUser(User user) {
    sqlSessionTemplate.update("Circabc.update_user", user);
  }

  /**
   * @param sqlSessionTemplate the sqlSessionTemplate to set
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /**
   * Deletes a header, matched by its node reference.
   *
   * @param headerRef the header's Alfresco node reference
   */
  public void deleteHeader(String headerRef) {
    sqlSessionTemplate.delete("Circabc.delete_header_by_ref", headerRef);
  }

  /**
   * Retrieves the association between headers and their categories.
   *
   * @return the list of header-category associations
   */
  public List<HeaderCategory> selectHeadersCategories() {
    return sqlSessionTemplate.selectList("Circabc.select_headers_categories");
  }

  /**
   * Lists the profiles of an interest group, including a count of their users,
   * with titles rendered in the requested locale.
   *
   * @param interestGroupID the interest group identifier
   * @param localeID the locale identifier used for title translation
   * @return the list of profiles with their user counts
   */
  public List<ProfileWithUsersCount> selectProfiles(
    long interestGroupID,
    long localeID
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("igID", interestGroupID);
    params.put(LOCALE_ID, localeID);
    return sqlSessionTemplate.selectList(
      "Circabc.select_profiles_by_ig_id_and_locale_id",
      params
    );
  }

  /**
   * Resolves the locale identifier associated with a user.
   *
   * <p>If no user name is supplied, the administrator user's locale is used as
   * a default.</p>
   *
   * @param userName the user name, or {@code null}/empty to fall back to the admin user
   * @return the numeric locale identifier
   */
  public Long selectLocaleIDByUserName(String userName) {
    if (userName == null || userName.isEmpty()) {
      // Add as default 'admin' locale in case the userName is not given
      userName = AuthenticationUtil.getAdminUserName();
    }
    return (Long) sqlSessionTemplate.selectOne(
      "Circabc.select_locale_id_by_user_name",
      userName
    );
  }

  /**
   * Searches the users of an interest group, optionally filtered by Alfresco
   * group and a free-text term.
   *
   * <p>The free-text term is lower-cased, sanitized and wrapped as a SQL
   * {@code LIKE} pattern. No explicit ordering is applied.</p>
   *
   * @param igID the interest group identifier
   * @param localeID the locale identifier used for title translation
   * @param alfrescoGroup the Alfresco group to filter by; an empty string means no filter
   * @param text the free-text search term; an empty string means no filter
   * @return the list of matching users with their profile information
   */
  public List<UserWithProfile> selectUsersProfiles(
    long igID,
    long localeID,
    String alfrescoGroup,
    String text
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(4);
    params.put("igID", igID);
    params.put(LOCALE_ID, localeID);
    if (alfrescoGroup.isEmpty()) {
      params.put(ALFRESCO_GROUP, null);
    } else {
      params.put(ALFRESCO_GROUP, alfrescoGroup);
    }

    if (text.isEmpty()) {
      params.put("text", null);
    } else {
      params.put("text", "%" + sanitizeSQL(text.toLowerCase()) + "%");
    }

    params.put("orderBy", null);

    return sqlSessionTemplate.selectList(
      "Circabc.select_users_by_ig_id_and_locale_id_alf_group_like_text",
      params
    );
  }

  /**
   * Lists the profiles of an interest group with their Alfresco group and
   * localized title, optionally filtered by a search query.
   *
   * @param igID the interest group identifier
   * @param localeID the locale identifier used for title translation
   * @param searchQuery the free-text search query; {@code null} or empty means no filter
   * @return the list of matching profiles
   */
  public List<Profile> selectProfileTitleAlfGroupByIgId(
    long igID,
    long localeID,
    String searchQuery
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("igID", igID);
    params.put(LOCALE_ID, localeID);
    String textValue = null;
    if (searchQuery != null && !searchQuery.isEmpty()) {
      textValue = "%" + sanitizeSQL(searchQuery.toLowerCase()) + "%";
    }
    params.put("text", textValue);

    return sqlSessionTemplate.selectList(
      "Circabc.select_profile_alf_group_title_by_ig_id",
      params
    );
  }

  /**
   * Minimal sanitization for user-provided search strings used with MyBatis parameters.
   * MyBatis already uses prepared statements, so SQL injection is not possible here.
   * We only strip control characters to avoid log/garbage issues while preserving search intent.
   *
   * @param string the raw search string, may be {@code null}
   * @return the string with control characters removed, or {@code null} if the input was {@code null}
   */
  private String sanitizeSQL(String string) {
    if (string == null) {
      return null;
    }
    // Remove all control characters (including \r, \n, \t etc.)
    return string.replaceAll("[\\p{Cntrl}]", "");
  }

  /** Whitelist of column names permitted in an {@code orderBy} expression. */
  private static final Set<String> ALLOWED_ORDER_COLUMNS = new HashSet<>(
    Arrays.asList(FIRST_NAME, LAST_NAME, EMAIL, USER_NAME)
  );

  /** Whitelist of ordering directions permitted in an {@code orderBy} expression. */
  private static final Set<String> ALLOWED_ORDER_TYPES = new HashSet<>(
    Arrays.asList("ASC", "DESC")
  );

  /**
   * Parses an {@code orderBy} expression of the form {@code column_direction} and
   * stores validated ordering parameters into the supplied MyBatis parameter map.
   *
   * <p>Only whitelisted columns ({@link #ALLOWED_ORDER_COLUMNS}) and directions
   * ({@link #ALLOWED_ORDER_TYPES}) are accepted; invalid or missing values fall
   * back to ordering by first name ascending.</p>
   *
   * @param params the MyBatis parameter map to populate with ordering keys
   * @param orderBy the requested ordering expression, may be {@code null} or empty
   */
  private void applyOrderBy(Map<String, Object> params, String orderBy) {
    String column = FIRST_NAME;
    String type = "ASC";
    if (orderBy != null && !orderBy.isEmpty()) {
      String[] parts = orderBy.split("_");
      if (parts.length == 2) {
        String requestedColumn = parts[0];
        String requestedType = parts[1].toUpperCase(Locale.ROOT);
        if (ALLOWED_ORDER_COLUMNS.contains(requestedColumn)) {
          column = requestedColumn;
        }
        if (ALLOWED_ORDER_TYPES.contains(requestedType)) {
          type = requestedType;
        }
      }
    }
    params.put(ORDER_BY_COLUMN, column);
    params.put(ORDER_BY_TYPE, type);
  }

  /**
   * Counts the administrators of an interest group.
   *
   * @param igID the interest group identifier
   * @return the number of administrators
   */
  public Integer getCountOfIGAdmins(long igID) {
    return (Integer) sqlSessionTemplate.selectOne(
      "Circabc.select_admin_count_by_ig_id",
      igID
    );
  }

  /**
   * Searches the users of an interest group with a free-text term and explicit ordering.
   *
   * <p>The free-text term is interpreted heuristically: a value containing
   * {@code @} is treated as an email filter, a value containing a space is split
   * into first-name/last-name filters, and any other value is used as a generic
   * text filter. All values are lower-cased, sanitized and wrapped as SQL
   * {@code LIKE} patterns. Ordering is validated via {@link #applyOrderBy(Map, String)}.</p>
   *
   * @param igID the interest group identifier
   * @param localeID the locale identifier used for title translation
   * @param alfrescoGroup the Alfresco group to filter by; an empty string means no filter
   * @param text the free-text search term; an empty string means no filter
   * @param orderBy the ordering expression of the form {@code column_direction}
   * @return the list of matching users with their profile information
   */
  public List<UserWithProfile> selectUsersProfiles(
    long igID,
    long localeID,
    String alfrescoGroup,
    String text,
    String orderBy
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(4);
    params.put("igID", igID);
    params.put(LOCALE_ID, localeID);
    if (alfrescoGroup.isEmpty()) {
      params.put(ALFRESCO_GROUP, null);
    } else {
      params.put(ALFRESCO_GROUP, alfrescoGroup);
    }

    params.put("text", null);
    params.put(EMAIL, null);
    params.put(FIRST_NAME, null);
    params.put(LAST_NAME, null);

    if (text.isEmpty()) {
      params.put("text", null);
    } else if (text.contains("@")) {
      params.put(EMAIL, "%" + sanitizeSQL(text.toLowerCase()) + "%");
    } else if (text.trim().contains(" ")) {
      String[] words = text.trim().split(" ");
      params.put(FIRST_NAME, "%" + sanitizeSQL(words[0].toLowerCase()) + "%");
      params.put(LAST_NAME, "%" + sanitizeSQL(words[1].toLowerCase()) + "%");
    } else {
      params.put("text", "%" + sanitizeSQL(text.toLowerCase()) + "%");
    }

    applyOrderBy(params, orderBy);

    return sqlSessionTemplate.selectList(
      "Circabc.select_users_by_ig_id_and_locale_id_alf_group_like_text",
      params
    );
  }

  /**
   * Searches the users of an interest group using distinct first-name,
   * last-name and email filters, with explicit ordering.
   *
   * <p>Each supplied filter is lower-cased, sanitized and wrapped as a SQL
   * {@code LIKE} pattern; empty filters are ignored. Ordering is validated via
   * {@link #applyOrderBy(Map, String)}.</p>
   *
   * @param igID the interest group identifier
   * @param localeID the locale identifier used for title translation
   * @param alfrescoGroup the Alfresco group to filter by; an empty string means no filter
   * @param firstName the first-name filter; an empty string means no filter
   * @param lastName the last-name filter; an empty string means no filter
   * @param email the email filter; an empty string means no filter
   * @param orderBy the ordering expression of the form {@code column_direction}
   * @return the list of matching users with their profile information
   */
  public List<UserWithProfile> selectUsersProfilesFiltered(
    long igID,
    long localeID,
    String alfrescoGroup,
    String firstName,
    String lastName,
    String email,
    String orderBy
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(4);
    params.put("igID", igID);
    params.put(LOCALE_ID, localeID);
    if (alfrescoGroup.isEmpty()) {
      params.put(ALFRESCO_GROUP, null);
    } else {
      params.put(ALFRESCO_GROUP, alfrescoGroup);
    }

    params.put(EMAIL, null);
    params.put(FIRST_NAME, null);
    params.put(LAST_NAME, null);

    if (firstName.isEmpty()) {
      params.put(FIRST_NAME, null);
    } else if (firstName.trim().contains(" ")) {
      String[] words = firstName.trim().split(" ");
      params.put(FIRST_NAME, "%" + sanitizeSQL(words[0].toLowerCase()) + "%");
    } else {
      params.put(FIRST_NAME, "%" + sanitizeSQL(firstName.toLowerCase()) + "%");
    }

    if (lastName.isEmpty()) {
      params.put(LAST_NAME, null);
    } else if (lastName.trim().contains(" ")) {
      String[] words = lastName.trim().split(" ");
      params.put(LAST_NAME, "%" + sanitizeSQL(words[0].toLowerCase()) + "%");
    } else {
      params.put(LAST_NAME, "%" + sanitizeSQL(lastName.toLowerCase()) + "%");
    }

    if (email.isEmpty()) {
      params.put(EMAIL, null);
    } else if (email.contains("@")) {
      params.put(EMAIL, "%" + sanitizeSQL(email.toLowerCase()) + "%");
    } else if (email.trim().contains(" ")) {
      String[] words = email.trim().split(" ");
      params.put(EMAIL, "%" + sanitizeSQL(words[0].toLowerCase()) + "%");
    } else {
      params.put(EMAIL, "%" + sanitizeSQL(email.toLowerCase()) + "%");
    }

    applyOrderBy(params, orderBy);

    return sqlSessionTemplate.selectList(
      "Circabc.select_users_by_ig_id_and_locale_id_alf_group_like_firstname_lastname_email",
      params
    );
  }

  /**
   * Counts the users belonging to an interest group.
   *
   * @param igDBId the interest group database identifier
   * @return the number of users in the interest group
   */
  public int countUsersInIg(long igDBId) {
    return (int) sqlSessionTemplate.selectOne(
      "Circabc.count_users_in_ig",
      igDBId
    );
  }

  /**
   * Retrieves the identifiers of the users belonging to an interest group.
   *
   * @param igDBId the interest group database identifier
   * @return the set of user identifiers
   */
  public Set<String> getUserIds(long igDBId) {
    return new HashSet<>(
      sqlSessionTemplate.selectList("Circabc.users_in_ig", igDBId)
    );
  }

  /**
   * Checks whether a user is a member of an interest group.
   *
   * @param igID the interest group identifier
   * @param username the user name to check
   * @return the number of matching memberships (typically {@code 0} or {@code 1})
   */
  public Integer getIsMemberOfGroup(long igID, String username) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("igID", igID);
    params.put(USERNAME, username);

    return (Integer) sqlSessionTemplate.selectOne(
      "Circabc.select_counter_username_in_ig",
      params
    );
  }

  /**
   * Retrieves all headers.
   *
   * @return the list of headers
   */
  public List<Header> selectHeaders() {
    final List<Header> result;

    result = sqlSessionTemplate.selectList("Circabc.select_headers");

    return result;
  }

  /**
   * Lists the categories belonging to a header, with titles rendered in the
   * requested locale.
   *
   * @param headerID the header identifier
   * @param localeID the locale identifier used for title translation
   * @return the list of categories
   */
  public List<Category> selectCategoriesByHeaderLocale(
    long headerID,
    long localeID
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("headerID", headerID);
    params.put(LOCALE_ID, localeID);
    final List<Category> result;
    result = sqlSessionTemplate.selectList(
      "Circabc.select_categories_by_header_locale",
      params
    );
    return result;
  }

  /**
   * Checks whether a user is an administrator of a category.
   *
   * @param categoryID the category identifier
   * @param userName the user name to check
   * @return the number of matching admin associations (typically {@code 0} or {@code 1})
   */
  public int getIsCategoryAdmin(long categoryID, String userName) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("categoryID", categoryID);
    params.put(USERNAME, userName);

    return (Integer) sqlSessionTemplate.selectOne(
      "Circabc.select_counter_username_in_category",
      params
    );
  }

  /**
   * Checks whether a user administers the category that owns a given interest group.
   *
   * @param interestGroupID the interest group identifier
   * @param userName the user name to check
   * @return the number of matching admin associations (typically {@code 0} or {@code 1})
   */
  public int getIsCategoryAdminOfInterestGroup(
    long interestGroupID,
    String userName
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(2);
    params.put("interestGroupID", interestGroupID);
    params.put(USERNAME, userName);

    return (Integer) sqlSessionTemplate.selectOne(
      "Circabc.select_count_cat_admin_by_username_ig",
      params
    );
  }

  /**
   * Checks whether a user is a global CIRCABC administrator.
   *
   * @param userName the user name to check
   * @return the number of matching admin associations (typically {@code 0} or {@code 1})
   */
  public int getIsCircabcAdmin(String userName) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put(USERNAME, userName);
    return (Integer) sqlSessionTemplate.selectOne(
      "Circabc.select_counter_username_in_circabc",
      params
    );
  }

  /**
   * Grants global CIRCABC administration by inserting an admin association.
   *
   * @param circabcAdmin the CIRCABC-admin association to persist
   */
  public void insertCircabcAdmin(CircabcAdmin circabcAdmin) {
    sqlSessionTemplate.insert("Circabc.insert_circabc_admin", circabcAdmin);
  }

  /**
   * Removes all global CIRCABC administrator associations.
   */
  public void deleteAllCircabcAdmins() {
    sqlSessionTemplate.delete("Circabc.delete_all_circabc_admins");
  }

  /**
   * Revokes global CIRCABC administration by deleting an admin association.
   *
   * @param circabcAdmin the CIRCABC-admin association to remove
   */
  public void deleteCircabcAdmin(CircabcAdmin circabcAdmin) {
    sqlSessionTemplate.delete("Circabc.delete_circabc_admin", circabcAdmin);
  }

  /**
   * Retrieves the localized title translations of a profile.
   *
   * @param profileID the profile identifier
   * @return the list of translation entries for the profile title
   */
  public List<TranslationEntry> selectProfileTitles(long profileID) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put(PROFILE_ID, profileID);
    final List<TranslationEntry> result;
    result = sqlSessionTemplate.selectList(
      "Circabc.select_titles_by_profile_id",
      params
    );
    return result;
  }

  /**
   * Lists the administrators of a category.
   *
   * @param categID the category identifier
   * @return the list of administrator user names
   */
  public List<String> selectCategoryAdmins(long categID) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put("categID", categID);
    return sqlSessionTemplate.selectList(
      "Circabc.select_category_admins",
      params
    );
  }

  /**
   * Retrieves the profile-user associations for a user within a given group.
   *
   * @param groupId the group identifier
   * @param username the user name
   * @return the list of matching profile-user associations
   */
  public List<ProfileUser> getUserProfileInGroup(
    Long groupId,
    String username
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("groupId", groupId);
    props.put(USERNAME, username);

    return sqlSessionTemplate.selectList("select_user_profile_in_group", props);
  }

  /**
   * Removes a user's profile membership within a group.
   *
   * @param membership the profile-user association identifying the Alfresco group and user to remove
   */
  public void deleteUserInGroup(ProfileUser membership) {
    Map<String, Object> props = new HashMap<>();
    props.put("alfGroup", membership.getAlfrescoGroup());
    props.put("userId", membership.getUserID());
    sqlSessionTemplate.delete("delete_user_profile_in_group", props);
  }

  /**
   * Updates the logo reference associated with a category.
   *
   * @param categID the category identifier
   * @param logoId the logo node reference/identifier to set
   */
  public void updateLogoIdForCategory(Long categID, String logoId) {
    Map<String, Object> props = new HashMap<>();
    props.put("categId", categID);
    props.put("logoId", logoId);
    sqlSessionTemplate.update("update_logo_id_for_category", props);
  }

  /**
   * Updates the logo reference associated with an interest group.
   *
   * @param igID the interest group identifier
   * @param logoRef the logo node reference/identifier to set
   */
  public void updateLogoIdForGroup(Long igID, String logoRef) {
    Map<String, Object> props = new HashMap<>();
    props.put("igId", igID);
    props.put("logoId", logoRef);
    sqlSessionTemplate.update("update_logo_id_for_group", props);
  }

  /**
   * Retrieves the title translations of an interest group keyed by language code.
   *
   * @param igID the interest group identifier
   * @return a map of two-letter language codes to translated titles
   */
  public Map<String, String> getGroupTitleTranslations(long igID) {
    return getTitleTranslations("select_group_titles", "igId", igID);
  }

  /**
   * Retrieves the title translations of a profile keyed by language code.
   *
   * @param profileID the profile identifier
   * @return a map of two-letter language codes to translated titles
   */
  public Map<String, String> getProfileTitleTranslations(long profileID) {
    return getTitleTranslations("select_profile_titles", PROFILE_ID, profileID);
  }

  /**
   * Replaces the title translations of a profile with the supplied localized values.
   *
   * <p>Existing translations are deleted, then each entry in {@code title} whose
   * locale is known to the database is inserted as a new profile title row.</p>
   *
   * @param profileID the profile identifier
   * @param title the localized titles keyed by locale
   */
  public void updateProfileTitle(long profileID, I18nProperty title) {
    Map<String, Long> locales = getAllAlfrescoLocale();
    Map<String, Object> props = new HashMap<>();
    props.put(PROFILE_ID, profileID);
    sqlSessionTemplate.delete("delete_profile_titles", props);

    for (Entry<String, String> titleEntry : title.entrySet()) {
      String localeStr = (titleEntry.getKey().contains("_")
        ? titleEntry.getKey()
        : titleEntry.getKey() + "_");
      if (locales.containsKey(localeStr)) {
        Map<String, Object> propsTitle = new HashMap<>();
        propsTitle.put(PROFILE_ID, profileID);
        propsTitle.put("titleTrans", titleEntry.getValue());
        propsTitle.put("localeId", locales.get(localeStr));
        sqlSessionTemplate.insert("insert_profile_title", propsTitle);
      }
    }
  }

  /**
   * Retrieves the title translations of a category keyed by language code.
   *
   * @param categId the category identifier
   * @return a map of two-letter language codes to translated titles
   */
  public Map<String, String> getCategoryTitleTranslations(long categId) {
    return getTitleTranslations("select_category_titles", "categId", categId);
  }

  /**
   * Deletes all CIRCABC data (full wipe of the managed tables).
   */
  public void deleteAll() {
    sqlSessionTemplate.delete("Circabc.delete_all");
  }

  /**
   * Retrieves all interest groups.
   *
   * @return the list of all interest groups
   */
  public List<InterestGroupResult> getAllInterestGroups() {
    return sqlSessionTemplate.selectList("select_all_groups");
  }

  /**
   * Counts the administrator associations held by a user name.
   *
   * @param userName the user name
   * @return the count of admin associations, or {@code 0} if none
   */
  public long selectCountAdminDByUserName(String userName) {
    Object id = sqlSessionTemplate.selectOne(
      "Circabc.select_count_admin_by_user_name",
      userName
    );
    return (id != null ? (Long) id : 0L);
  }

  /**
   * Checks whether a user is an external user.
   *
   * @param userName the user name to check
   * @return the number of matching rows marking the user as external (typically {@code 0} or {@code 1})
   */
  public int getisExternalUser(String userName) {
    HashMap<String, Object> params = HashMap.newHashMap(1);
    params.put(USERNAME, userName);
    return (Integer) sqlSessionTemplate.selectOne(
      "Circabc.select_counter_username_is_external",
      params
    );
  }

  /**
   * Lists the interest groups of a category, excluding the current one, that
   * are visible to a user.
   *
   * @param categoryId the category identifier
   * @param interestGroupID the identifier of the interest group to exclude
   * @param userName the user name for which visibility is evaluated
   * @return the list of matching interest groups
   */
  public List<IGData> getCategoryIGsExceptCurrent(
    long categoryId,
    long interestGroupID,
    String userName
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(3);
    params.put(CATEGORY_ID, categoryId);
    params.put("interestGroupId", interestGroupID);
    params.put(USERNAME, userName);

    return sqlSessionTemplate.selectList(
      "Circabc.select_category_igs_except_current",
      params
    );
  }

  /**
   * Lists the interest groups of a category, excluding the current one, to
   * which a user has directory access.
   *
   * @param categoryId the category identifier
   * @param interestGroupId the identifier of the interest group to exclude
   * @param userName the user name for which directory access is evaluated
   * @return the list of matching interest groups
   */
  public List<IGData> getCategoryIGsExceptCurrentWithDirectoryAccess(
    long categoryId,
    long interestGroupId,
    String userName
  ) {
    HashMap<String, Object> params = HashMap.newHashMap(3);
    params.put(CATEGORY_ID, categoryId);
    params.put("interestGroupId", interestGroupId);
    params.put(USERNAME, userName);
    return sqlSessionTemplate.selectList(
      "Circabc.select_category_igs_except_current_with_directory_access",
      params
    );
  }

  /**
   * Lists the email addresses of the administrators of a category.
   *
   * @param categoryNodeRef the category's Alfresco node reference
   * @return the list of administrator email addresses
   */
  public List<String> selectCategoryAdminEmails(String categoryNodeRef) {
    return sqlSessionTemplate.selectList(
      "Circabc.select_category_admin_emails",
      categoryNodeRef
    );
  }

  /**
   * Lists the email addresses of the administrators of an interest group.
   *
   * @param interestGroupNodeRef the interest group's Alfresco node reference
   * @return the list of administrator email addresses
   */
  public List<String> selectInterestGroupAdminEmails(
    String interestGroupNodeRef
  ) {
    return sqlSessionTemplate.selectList(
      "Circabc.select_interest_group_admin_emails",
      interestGroupNodeRef
    );
  }

  /**
   * Lists the Alfresco node references of all categories.
   *
   * @return the list of category node references
   */
  public List<String> selectCategoriesNodeRef() {
    return sqlSessionTemplate.selectList("Circabc.select_categories_node_ref");
  }

  /**
   * Sets or clears the "to be deleted" flag on an interest group.
   *
   * @param igId the interest group identifier
   * @param isToBeDeleted {@code true} to mark the group for deletion, {@code false} to clear the flag
   */
  public void updateIgToBeDeleted(Long igId, boolean isToBeDeleted) {
    Map<String, Object> props = new HashMap<>();
    props.put("id", igId);
    props.put("toBeDeleted", isToBeDeleted);
    sqlSessionTemplate.update("Circabc.update_to_be_deleted_by_id", props);
  }

  /**
   * Helper method to retrieve and process title translations from the database.
   *
   * @param queryName The SQL query name to execute
   * @param idKey The parameter key for the ID value
   * @param idValue The ID value used in the query
   * @return A map of language codes to translated titles
   */
  private Map<String, String> getTitleTranslations(
    String queryName,
    String idKey,
    Object idValue
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put(idKey, idValue);
    List<KeyValueString> titles = sqlSessionTemplate.selectList(
      queryName,
      props
    );

    // Remove the "_" from the title keys
    Map<String, String> result = new HashMap<>();

    for (KeyValueString title : titles) {
      if (title.getValue() != null) {
        result.put(title.getKey().substring(0, 2), title.getValue());
      }
    }

    return result;
  }

  /**
   * Removes all traces of a user across the CIRCABC tables.
   *
   * <p>The user is deleted from the profile-user associations, the global
   * CIRCABC admins, the category admins and finally the users table.</p>
   *
   * @param userName the user name to purge
   */
  public void deleteUserFromAllTables(String userName) {
    Map<String, Object> params = new HashMap<>();
    params.put(USER_NAME, userName);

    // Delete from cbc_profile_users
    sqlSessionTemplate.delete("Circabc.delete_user_from_profile_users", params);

    // Delete from cbc_circabc_admins
    sqlSessionTemplate.delete(
      "Circabc.delete_user_from_circabc_admins",
      params
    );

    // Delete from cbc_category_admins
    sqlSessionTemplate.delete(
      "Circabc.delete_user_from_category_admins",
      params
    );

    // Delete from cbc_users
    sqlSessionTemplate.delete("Circabc.delete_user_from_users", params);
  }
}
