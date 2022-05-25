package eu.cec.digit.circabc.repo.app;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.repo.app.model.*;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeader;
import io.swagger.model.I18nProperty;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.mybatis.spring.SqlSessionTemplate;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.MySQLCodec;
import org.owasp.esapi.codecs.MySQLCodec.Mode;
import org.owasp.esapi.codecs.OracleCodec;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class CircabcDaoServiceImpl {

    private Codec codec = null;
    private SqlSessionTemplate sqlSessionTemplate = null;

    public CircabcDaoServiceImpl() {

        super();

        // Initialize the codec to be used for ESAPI
        String dbDialect = CircabcConfiguration.getProperty("hibernate.dialect");

        if (dbDialect != null && dbDialect.toLowerCase().contains("mysql")) {
            codec = new MySQLCodec(Mode.STANDARD);
        }
        // else if (db != null && db.toLowerCase().contains("postgre")) {
        // still no codec for postres
        // }
        else {
            codec = new OracleCodec();
        }
    }

    public void init() {
    }

    public void insertHeader(Header header) {
        sqlSessionTemplate.insert("Circabc.insert_header", header);
    }

    public void updateHeader(Header header) {
        sqlSessionTemplate.insert("Circabc.update_header_by_ref", header);
    }

    public void insertCategory(Category category) {
        sqlSessionTemplate.insert("Circabc.insert_category", category);
    }

    public void insertCategoryHeader(CategoryHeader categoryHeaderIDs) {
        sqlSessionTemplate.insert("Circabc.insert_category_header", categoryHeaderIDs);
    }

    public void insertUser(User user) {
        sqlSessionTemplate.insert("Circabc.insert_user", user);
    }

    public void insertCategoryAdmin(CategoryAdmin catAdmin) {
        sqlSessionTemplate.insert("Circabc.insert_categegory_admin", catAdmin);
    }

    public void deleteCategoryAdmin(CategoryAdmin catAdmin) {
        sqlSessionTemplate.delete("Circabc.delete_categegory_admin", catAdmin);
    }

    public void insertInterestGroup(InterestGroup ig) {
        sqlSessionTemplate.insert("Circabc.insert_interest_group", ig);
    }

    public void insertProfile(eu.cec.digit.circabc.repo.app.model.Profile profile) {
        sqlSessionTemplate.insert("Circabc.insert_profile", profile);
    }

    public void insertProfileUser(ProfileUser profileUser) {
        sqlSessionTemplate.insert("Circabc.insert_profile_user", profileUser);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> getAllAlfrescoLocale() {

        final List<KeyValue> selectList =
                (List<KeyValue>) sqlSessionTemplate.selectList("Circabc.select_alf_locale");
        Map<String, Long> result = new HashMap<>(selectList.size());
        for (KeyValue keyValue : selectList) {
            result.put(keyValue.getKey(), keyValue.getValue());
        }
        return result;
    }

    public void insertMultilingualProperties() {
        sqlSessionTemplate.insert("Circabc.insert_cat_titles");
        sqlSessionTemplate.insert("Circabc.insert_ig_titles");
        sqlSessionTemplate.insert("Circabc.insert_profile_titles");
    }

    public void insertMultilingualPropertiesByInterestGroupID(long interestGroupID) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("id", interestGroupID);
        sqlSessionTemplate.insert("Circabc.insert_ig_titles_by_id", params);
        // disabled since it throws a Foreign key exception
        // sqlSessionTemplate.insert("Circabc.insert_profile_titles_by_ig_id", params);

    }

    public int deleteProfileByInterestGroupUserName(long interestGroupID, String userName) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("userName", userName);
        params.put("interestGroupID", interestGroupID);
        return sqlSessionTemplate.delete("Circabc.delete_profile_user_by_ig_id_user_name", params);
    }

    public long selectUserIDByUserName(String userName) {
        Object id = sqlSessionTemplate.selectOne("Circabc.select_user_id_by_user_name", userName);
        return (id != null ? (Long) id : 0L);
    }

    public void updateProfile(eu.cec.digit.circabc.repo.app.model.Profile profile) {
        sqlSessionTemplate.update("Circabc.update_profile", profile);
    }

    public void updateInterestGroupPublic(long interestGroupID, boolean isPublic) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("id", interestGroupID);
        params.put("isPublic", isPublic);
        sqlSessionTemplate.update("Circabc.update_ig_public", params);
    }

    public void updateInterestGroupRegistered(long interestGroupID, boolean isRegistered) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("id", interestGroupID);
        params.put("isRegistered", isRegistered);
        sqlSessionTemplate.update("Circabc.update_ig_registered", params);
    }

    public void updateInterestGroupApplyForMemberhip(
            long interestGroupID, boolean isApplyForMembership) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("id", interestGroupID);
        params.put("isApplyForMembership", isApplyForMembership);
        sqlSessionTemplate.update("Circabc.update_ig_apply_for_membership", params);
    }

    public void updateProfileExported(long profileID, boolean export) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("id", profileID);
        params.put("isExported", export);
        sqlSessionTemplate.update("Circabc.update_profile_export", params);
    }

    public eu.cec.digit.circabc.repo.app.model.Profile selectProfileByInterestGroupIDProfileName(
            eu.cec.digit.circabc.repo.app.model.Profile profile) {
        return (eu.cec.digit.circabc.repo.app.model.Profile)
                sqlSessionTemplate.selectOne("Circabc.select_profile_by_ig_id_profile_name", profile);
    }

    public void deleteProfileByID(long profileID) {
        sqlSessionTemplate.delete("Circabc.delete_profile_by_id", profileID);
    }

    public void deleteProfileTitleTranslationsByID(long profileID) {
        sqlSessionTemplate.delete("Circabc.delete_profile_title_translations_by_id", profileID);
    }

    public boolean profileExists(long id) {
        final int count =
                (Integer) sqlSessionTemplate.selectOne("Circabc.select_profil_count_by_profile_id", id);
        return (count == 1);
    }

    public void updateProfileTitles(long id) {
        sqlSessionTemplate.delete("Circabc.delete_profile_title_translations_by_id", id);
        sqlSessionTemplate.insert("Circabc.insert_profile_titles_by_id", id);
    }

    public void updateUserUILangauge(String userName, String language) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("userName", userName);
        params.put("language", language);
        sqlSessionTemplate.update("Circabc.update_user_ui_lang", params);
    }

    public void updateInterestGroupTitles(long interestGroupID) {
        sqlSessionTemplate.delete("Circabc.delete_ig_title_translations_by_id", interestGroupID);
        sqlSessionTemplate.insert("Circabc.insert_ig_titles_by_id", interestGroupID);
    }

    public void updateCategoryTitles(long categoryID) {
        sqlSessionTemplate.delete("Circabc.delete_cat_title_translations_by_id", categoryID);
        sqlSessionTemplate.insert("Circabc.insert_cat_titles_by_id", categoryID);
    }

    public void updateCategoryTitles(long categoryID, I18nProperty title) {
        sqlSessionTemplate.delete("Circabc.delete_cat_title_translations_by_id", categoryID);
        Map<String, Long> locales = getAllAlfrescoLocale();

        for (Entry<String, String> titleEntry : title.entrySet()) {
            String localeStr =
                    (titleEntry.getKey().contains("_") ? titleEntry.getKey() : titleEntry.getKey() + "_");
            if (locales.containsKey(localeStr)) {
                Map<String, Object> propsTitle = new HashMap<>();
                propsTitle.put("categoryId", categoryID);
                propsTitle.put("titleTrans", titleEntry.getValue());
                propsTitle.put("localeId", locales.get(localeStr));
                sqlSessionTemplate.insert("Circabc.insert_category_title", propsTitle);
            }
        }
    }

    public long selectCategoryIDByNodeRef(String nodeRef) {
        return (Long) sqlSessionTemplate.selectOne("Circabc.select_cat_id_by_node_ref", nodeRef);
    }

    public void deleteCategory(long categoryID) throws SQLException {
        @SuppressWarnings("unchecked") final List<Long> igIDs =
                (List<Long>) sqlSessionTemplate.selectList("Circabc.select_ig_ids_by_cat_id", categoryID);
        for (Long igID : igIDs) {
            deleteInterestGroup(igID);
        }
        sqlSessionTemplate.delete("Circabc.delete_cat_title_trans_by_cat_id", categoryID);
        sqlSessionTemplate.delete("Circabc.delete_cat_admins_by_cat_id", categoryID);
        sqlSessionTemplate.delete("Circabc.delete_cat_by_id", categoryID);
    }

    public void deleteInterestGroup(long interestGroupID) {
        sqlSessionTemplate.delete("Circabc.delete_profile_titles_by_ig_id", interestGroupID);
        sqlSessionTemplate.delete("Circabc.delete_profile_users_by_ig_id", interestGroupID);
        sqlSessionTemplate.delete("Circabc.delete_profiles_by_ig_id", interestGroupID);
        sqlSessionTemplate.delete("Circabc.delete_ig_title_trans_by_ig_id", interestGroupID);
        sqlSessionTemplate.delete("Circabc.delete_ig_by_id", interestGroupID);
    }

    public void moveInterestGroup(long interestGroupID, long oldCategoryID, long newCategoryID) {
        HashMap<String, Object> params = new HashMap<>(3);
        params.put("igID", interestGroupID);
        params.put("oldCatID", oldCategoryID);
        params.put("newCatID", newCategoryID);
        sqlSessionTemplate.update("Circabc.update_cat_id", params);
    }

    public void updateInterestGroup(InterestGroup interestGroup) {
        sqlSessionTemplate.update("Circabc.update_ig_name_title_by_id", interestGroup);
    }

    public void updateCategory(Category category) {
        sqlSessionTemplate.update("Circabc.update_cat_name_title_by_id", category);
    }

    @SuppressWarnings("unchecked")
    public List<InterestGroupResult> selectIgByCategoryIDUserName(long categoryID, String userName)
            throws SQLException {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("catID", categoryID);
        params.put("userName", userName);
        final List<InterestGroupResult> result;

        if (userName.equals("guest")) {
            result =
                    (List<InterestGroupResult>)
                            sqlSessionTemplate.selectList("Circabc.select_public_ig_by_cat_user", params);
        } else if (isCategoryAdmin(categoryID, userName)) {
            result =
                    (List<InterestGroupResult>)
                            sqlSessionTemplate.selectList("Circabc.select_ig_by_cat_admin", params);
        } else {
            result =
                    (List<InterestGroupResult>)
                            sqlSessionTemplate.selectList("Circabc.select_ig_by_cat_user", params);
        }
        return result;
    }

    public InterestGroupResult selectIgByID(long interestGroupID) {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("ID", interestGroupID);
        final InterestGroupResult result;
        result = (InterestGroupResult) sqlSessionTemplate.selectOne("Circabc.select_ig_by_id", params);
        return result;
    }

    private boolean isCategoryAdmin(final long categoryID, final String userName) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("catID", categoryID);
        params.put("userName", userName);
        int result = (Integer) sqlSessionTemplate.selectOne("Circabc.select_is_user_cat_admin", params);

        return (result == 1);
    }

    public List<ExportedProfileItem> selectExpProfilesByCategoryIDInterestGroupID(
            long categoryID, long interestGroupID) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("catID", categoryID);
        params.put("igID", interestGroupID);
        @SuppressWarnings("unchecked")
        List<ExportedProfileItem> result =
                (List<ExportedProfileItem>)
                        sqlSessionTemplate.selectList("Circabc.select_exp_profiles_by_cat_id_ig_id", params);
        return result;
    }

    public List<ExportedProfileItem> selectExpProfilesByCategoryID(long categoryID) {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("catID", categoryID);
        @SuppressWarnings("unchecked")
        List<ExportedProfileItem> result =
                (List<ExportedProfileItem>)
                        sqlSessionTemplate.selectList("Circabc.select_exp_profiles_by_cat_id", params);
        return result;
    }

    public List<String> selectImporetedAlfGroups(long interestGroupID) {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("igID", interestGroupID);
        @SuppressWarnings("unchecked")
        List<String> result =
                (List<String>)
                        sqlSessionTemplate.selectList("Circabc.select_imp_alf_group_by_ig_id", params);
        return result;
    }

    public Set<String> selectAlfrescoGroupNotInIneterstGroup(long categoryID, long interestGroupID) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("catID", categoryID);
        params.put("igID", interestGroupID);
        @SuppressWarnings("unchecked")
        List<String> result =
                (List<String>) sqlSessionTemplate.selectList("Circabc.select_alf_group_not_in_ig", params);
        return new HashSet<>(result);
    }

    public List<UserIGMembership> selectInterestGroups(String userName) {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("userName", userName);
        @SuppressWarnings("unchecked")
        List<UserIGMembership> result =
                (List<UserIGMembership>)
                        sqlSessionTemplate.selectList("Circabc.select_ig_node_ref_by_user_name", params);
        return result;
    }

    public List<String> selectCategories(String userName) {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("userName", userName);
        @SuppressWarnings("unchecked")
        List<String> result =
                (List<String>)
                        sqlSessionTemplate.selectList("Circabc.select_cat_node_ref_by_user_name", params);
        return result;
    }

    public List<String> selectAllUserNodeRef() {
        @SuppressWarnings("unchecked")
        List<String> result =
                (List<String>) sqlSessionTemplate.selectList("Circabc.select_all_user_node_ref");
        return result;
    }

    public void updateUser(User user) {
        sqlSessionTemplate.update("Circabc.update_user", user);
    }

    /**
     * @param sqlSessionTemplate the sqlSessionTemplate to set
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public void deleteHeader(String headerRef) {
        sqlSessionTemplate.delete("Circabc.delete_header_by_ref", headerRef);
    }

    public List<HeaderCategory> selectHeadersCategories() {
        @SuppressWarnings("unchecked")
        List<HeaderCategory> result =
                (List<HeaderCategory>) sqlSessionTemplate.selectList("Circabc.select_headers_categories");
        return result;
    }

    public List<ProfileWithUsersCount> selectProfiles(long interestGroupID, long localeID) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("igID", interestGroupID);
        params.put("localeID", localeID);
        @SuppressWarnings("unchecked")
        List<ProfileWithUsersCount> result =
                (List<ProfileWithUsersCount>)
                        sqlSessionTemplate.selectList("Circabc.select_profiles_by_ig_id_and_locale_id", params);
        return result;
    }

    public Long selectLocaleIDByUserName(String userName) {
        if (userName == null || userName.length() == 0) {
            // Add as default 'admin' locale in case the userName is not given
            userName = AuthenticationUtil.getAdminUserName();
        }
        return (Long) sqlSessionTemplate.selectOne("Circabc.select_locale_id_by_user_name", userName);
    }

    public List<UserWithProfile> selectUsersProfiles(
            long igID, long localeID, String alfrescoGroup, String text) {
        HashMap<String, Object> params = new HashMap<>(4);
        params.put("igID", igID);
        params.put("localeID", localeID);
        if (alfrescoGroup.isEmpty()) {
            params.put("alfrescoGroup", null);
        } else {
            params.put("alfrescoGroup", alfrescoGroup);
        }

        if (text.isEmpty()) {
            params.put("text", null);
        } else {
            params.put("text", "%" + sanitizeSQL(text.toLowerCase()) + "%");
        }

        params.put("orderBy", null);

        @SuppressWarnings("unchecked")
        List<UserWithProfile> result =
                (List<UserWithProfile>)
                        sqlSessionTemplate.selectList(
                                "Circabc.select_users_by_ig_id_and_locale_id_alf_group_like_text", params);
        return result;
    }

    public List<eu.cec.digit.circabc.repo.app.model.Profile> selectProfileTitleAlfGroupByIgId(
            long igID, long localeID, String searchQuery) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("igID", igID);
        params.put("localeID", localeID);
        params.put(
                "text",
                searchQuery != null && searchQuery.isEmpty()
                        ? null
                        : searchQuery == null ? null : "%" + sanitizeSQL(searchQuery.toLowerCase()) + "%");

        @SuppressWarnings("unchecked")
        List<eu.cec.digit.circabc.repo.app.model.Profile> result =
                (List<eu.cec.digit.circabc.repo.app.model.Profile>)
                        sqlSessionTemplate.selectList(
                                "Circabc.select_profile_alf_group_title_by_ig_id", params);
        return result;
    }

    /**
     * Uses the ESAPI to sanitize the search text to avoid SQL injection and security problems.
     */
    private String sanitizeSQL(String string) {
        return ESAPI.encoder().encodeForSQL(codec, string);
    }

    public Integer getCountOfIGAdmins(long igID) {
        return (Integer) sqlSessionTemplate.selectOne("Circabc.select_admin_count_by_ig_id", igID);
    }

    public List<UserWithProfile> selectUsersProfiles(
            long igID, long localeID, String alfrescoGroup, String text, String orderBy) {

        HashMap<String, Object> params = new HashMap<>(4);
        params.put("igID", igID);
        params.put("localeID", localeID);
        if (alfrescoGroup.isEmpty()) {
            params.put("alfrescoGroup", null);
        } else {
            params.put("alfrescoGroup", alfrescoGroup);
        }

        params.put("text", null);
        params.put("email", null);
        params.put("firstName", null);
        params.put("lastName", null);

        if (text.isEmpty()) {
            params.put("text", null);
        } else if (text.contains("@")) {
            params.put("email", "%" + sanitizeSQL(text.toLowerCase()) + "%");
        } else if (text.trim().contains(" ")) {
            String[] words = text.trim().split(" ");
            params.put("firstName", "%" + sanitizeSQL(words[0].toLowerCase()) + "%");
            params.put("lastName", "%" + sanitizeSQL(words[1].toLowerCase()) + "%");
        } else {
            params.put("text", "%" + sanitizeSQL(text.toLowerCase()) + "%");
        }

        if (orderBy == null) {
            params.put("orderByColumn", "firstName");
            params.put("orderByType", "ASC");
        } else if (orderBy.isEmpty()) {
            params.put("orderByColumn", "firstName");
            params.put("orderByType", "ASC");
        } else {
            params.put("orderByColumn", orderBy.split("_")[0]);
            params.put("orderByType", orderBy.split("_")[1]);
        }

        @SuppressWarnings("unchecked")
        List<UserWithProfile> result =
                (List<UserWithProfile>)
                        sqlSessionTemplate.selectList(
                                "Circabc.select_users_by_ig_id_and_locale_id_alf_group_like_text", params);
        return result;
    }

    public int countUsersInIg(long igDBId) {
        return (int) sqlSessionTemplate.selectOne("Circabc.count_users_in_ig", igDBId);
    }

    public Set<String> getUserIds(long igDBId) {
        return new HashSet((List<String>) sqlSessionTemplate.selectList("Circabc.users_in_ig", igDBId));
    }

    public Integer getIsMemberOfGroup(long igID, String username) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("igID", igID);
        params.put("username", username);

        return (Integer) sqlSessionTemplate.selectOne("Circabc.select_counter_username_in_ig", params);
    }

    public List<Header> selectHeaders() throws SQLException {
        final List<Header> result;

        result = (List<Header>) sqlSessionTemplate.selectList("Circabc.select_headers");

        return result;
    }

    public List<Category> selectCategoriesByHeaderLocale(long headerID, long localeID)
            throws SQLException {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("headerID", headerID);
        params.put("localeID", localeID);
        final List<Category> result;
        result =
                (List<Category>)
                        sqlSessionTemplate.selectList("Circabc.select_categories_by_header_locale", params);
        return result;
    }

    public int getIsCategoryAdmin(long categoryID, String userName) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("categoryID", categoryID);
        params.put("username", userName);

        return (Integer)
                sqlSessionTemplate.selectOne("Circabc.select_counter_username_in_category", params);
    }

    public int getIsCategoryAdminOfInterestGroup(long interestGroupID, String userName) {
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("interestGroupID", interestGroupID);
        params.put("username", userName);

        return (Integer)
                sqlSessionTemplate.selectOne("Circabc.select_count_cat_admin_by_username_ig", params);
    }

    public int getIsCircabcAdmin(String userName) {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("username", userName);
        return (Integer)
                sqlSessionTemplate.selectOne("Circabc.select_counter_username_in_circabc", params);
    }

    public void insertCircabcAdmin(CircabcAdmin circabcAdmin) {
        sqlSessionTemplate.insert("Circabc.insert_circabc_admin", circabcAdmin);
    }

    public void deleteAllCircabcAdmins() {
        sqlSessionTemplate.delete("Circabc.delete_all_circabc_admins");
    }

    public void deleteCircabcAdmin(CircabcAdmin circabcAdmin) {
        sqlSessionTemplate.delete("Circabc.delete_circabc_admin", circabcAdmin);
    }

    public List<TranslationEntry> selectProfileTitles(long profileID) throws SQLException {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("profileID", profileID);
        final List<TranslationEntry> result;
        result =
                (List<TranslationEntry>)
                        sqlSessionTemplate.selectList("Circabc.select_titles_by_profile_id", params);
        return result;
    }

    public List<String> selectCategoryAdmins(long categID) {
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("categID", categID);
        return (List<String>) sqlSessionTemplate.selectList("Circabc.select_category_admins", params);
    }

    public List<ProfileUser> getUserProfileInGroup(Long groupId, String username) {

        Map<String, Object> props = new HashMap<>();
        props.put("groupId", groupId);
        props.put("username", username);

        return (List<ProfileUser>) sqlSessionTemplate.selectList("select_user_profile_in_group", props);
    }

    public void deleteUserInGroup(ProfileUser membership) {
        Map<String, Object> props = new HashMap<>();
        props.put("alfGroup", membership.getAlfrescoGroup());
        props.put("userId", membership.getUserID());
        sqlSessionTemplate.delete("delete_user_profile_in_group", props);
    }

    public void updateLogoIdForCategory(Long categID, String logoId) {
        Map<String, Object> props = new HashMap<>();
        props.put("categId", categID);
        props.put("logoId", logoId);
        sqlSessionTemplate.update("update_logo_id_for_category", props);
    }

    public void updateLogoIdForGroup(Long igID, String logoRef) {
        Map<String, Object> props = new HashMap<>();
        props.put("igId", igID);
        props.put("logoId", logoRef);
        sqlSessionTemplate.update("update_logo_id_for_group", props);
    }

    public Map<String, String> getGroupTitleTranslations(long igID) {
        Map<String, Object> props = new HashMap<>();
        props.put("igId", igID);
        List<KeyValueString> titles =
                (List<KeyValueString>) sqlSessionTemplate.selectList("select_group_titles", props);

        // need to following to remove the "_" from the title;
        Map<String, String> result = new HashMap<String, String>();

        for (KeyValueString title : titles) {
            if (title.getValue() != null) {
                result.put(title.getKey().toString().substring(0, 2), title.getValue().toString());
            }
        }

        return result;
    }

    public Map<String, String> getProfileTitleTranslations(long profileID) {
        Map<String, Object> props = new HashMap<>();
        props.put("profileID", profileID);
        List<KeyValueString> titles =
                (List<KeyValueString>) sqlSessionTemplate.selectList("select_profile_titles", props);

        // need to following to remove the "_" from the title;
        Map<String, String> result = new HashMap<String, String>();

        for (KeyValueString title : titles) {
            if (title.getValue() != null) {
                result.put(title.getKey().toString().substring(0, 2), title.getValue().toString());
            }
        }

        return result;
    }

    public void updateProfileTitle(long profileID, I18nProperty title) {

        Map<String, Long> locales = getAllAlfrescoLocale();
        Map<String, Object> props = new HashMap<>();
        props.put("profileID", profileID);
        sqlSessionTemplate.delete("delete_profile_titles", props);

        for (Entry<String, String> titleEntry : title.entrySet()) {
            String localeStr =
                    (titleEntry.getKey().contains("_") ? titleEntry.getKey() : titleEntry.getKey() + "_");
            if (locales.containsKey(localeStr)) {
                Map<String, Object> propsTitle = new HashMap<>();
                propsTitle.put("profileID", profileID);
                propsTitle.put("titleTrans", titleEntry.getValue());
                propsTitle.put("localeId", locales.get(localeStr));
                sqlSessionTemplate.insert("insert_profile_title", propsTitle);
            }
        }
    }

    public Map<String, String> getCategoryTitleTranslations(long categId) {
        Map<String, Object> props = new HashMap<>();
        props.put("categId", categId);
        List<KeyValueString> titles =
                (List<KeyValueString>) sqlSessionTemplate.selectList("select_category_titles", props);

        // need to following to remove the "_" from the title;
        Map<String, String> result = new HashMap<String, String>();

        for (KeyValueString title : titles) {
            if (title.getValue() != null) {
                result.put(title.getKey().toString().substring(0, 2), title.getValue().toString());
            }
        }

        return result;
    }

    public void deleteAll() {
        sqlSessionTemplate.delete("Circabc.delete_all");
    }

    public List<InterestGroupResult> getAllInterestGroups() {
        return (List<InterestGroupResult>) sqlSessionTemplate.selectList("select_all_groups");
    }

    public long selectCountAdminDByUserName(String userName) {
        Object id = sqlSessionTemplate.selectOne("Circabc.select_count_admin_by_user_name", userName);
        return (id != null ? (Long) id : 0L);
    }
}

