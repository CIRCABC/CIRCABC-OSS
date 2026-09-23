package eu.europa.ec.digit.circabc.rest.service.app;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.I18nProperty;
import io.swagger.model.db.*;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class CircabcDaoServiceImplTest {

  private CircabcDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() throws Exception {
    service = new CircabcDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(sqlSessionTemplate);

    // Initialize AuthenticationUtil for tests
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");
  }

  // --- getAllAlfrescoLocale ---

  @Test
  public void testGetAllAlfrescoLocale_whenResults_thenReturnsMap() {
    KeyValue kv1 = new KeyValue();
    kv1.setKey("en_");
    kv1.setValue(1L);
    KeyValue kv2 = new KeyValue();
    kv2.setKey("fr_");
    kv2.setValue(2L);

    when(sqlSessionTemplate.selectList("Circabc.select_alf_locale")).thenReturn(
      Arrays.asList(kv1, kv2)
    );

    Map<String, Long> result = service.getAllAlfrescoLocale();

    assertEquals(2, result.size());
    assertEquals(Long.valueOf(1L), result.get("en_"));
    assertEquals(Long.valueOf(2L), result.get("fr_"));
  }

  @Test
  public void testGetAllAlfrescoLocale_whenEmpty_thenReturnsEmptyMap() {
    when(sqlSessionTemplate.selectList("Circabc.select_alf_locale")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Long> result = service.getAllAlfrescoLocale();

    assertTrue(result.isEmpty());
  }

  // --- selectUserIDByUserName ---

  @Test
  public void testSelectUserIDByUserName_whenFound_thenReturnsId() {
    when(
      sqlSessionTemplate.selectOne(
        "Circabc.select_user_id_by_user_name",
        "john"
      )
    ).thenReturn(42L);

    long result = service.selectUserIDByUserName("john");

    assertEquals(42L, result);
  }

  @Test
  public void testSelectUserIDByUserName_whenNotFound_thenReturnsZero() {
    when(
      sqlSessionTemplate.selectOne(
        "Circabc.select_user_id_by_user_name",
        "unknown"
      )
    ).thenReturn(null);

    long result = service.selectUserIDByUserName("unknown");

    assertEquals(0L, result);
  }

  // --- profileExists ---

  @Test
  public void testProfileExists_whenExists_thenReturnsTrue() {
    when(
      sqlSessionTemplate.selectOne(
        "Circabc.select_profil_count_by_profile_id",
        10L
      )
    ).thenReturn(1);

    assertTrue(service.profileExists(10L));
  }

  @Test
  public void testProfileExists_whenNotExists_thenReturnsFalse() {
    when(
      sqlSessionTemplate.selectOne(
        "Circabc.select_profil_count_by_profile_id",
        10L
      )
    ).thenReturn(0);

    assertFalse(service.profileExists(10L));
  }

  // --- selectLocaleIDByUserName ---

  @Test
  public void testSelectLocaleIDByUserName_whenValidUser_thenReturnsLocaleId() {
    when(
      sqlSessionTemplate.selectOne(
        "Circabc.select_locale_id_by_user_name",
        "john"
      )
    ).thenReturn(5L);

    Long result = service.selectLocaleIDByUserName("john");

    assertEquals(Long.valueOf(5L), result);
  }

  @Test
  public void testSelectLocaleIDByUserName_whenNullUser_thenUsesAdmin() {
    when(
      sqlSessionTemplate.selectOne(
        eq("Circabc.select_locale_id_by_user_name"),
        anyString()
      )
    ).thenReturn(1L);

    Long result = service.selectLocaleIDByUserName(null);

    assertNotNull(result);
  }

  @Test
  public void testSelectLocaleIDByUserName_whenEmptyUser_thenUsesAdmin() {
    when(
      sqlSessionTemplate.selectOne(
        eq("Circabc.select_locale_id_by_user_name"),
        anyString()
      )
    ).thenReturn(1L);

    Long result = service.selectLocaleIDByUserName("");

    assertNotNull(result);
  }

  // --- deleteInterestGroup ---

  @Test
  public void testDeleteInterestGroup_thenDeletesAllRelated() {
    service.deleteInterestGroup(100L);

    verify(sqlSessionTemplate).delete(
      "Circabc.delete_profile_titles_by_ig_id",
      100L
    );
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_profile_users_by_ig_id",
      100L
    );
    verify(sqlSessionTemplate).delete("Circabc.delete_profiles_by_ig_id", 100L);
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_ig_title_trans_by_ig_id",
      100L
    );
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_group_statistics_by_ig_id",
      100L
    );
    verify(sqlSessionTemplate).delete("Circabc.delete_ig_by_id", 100L);
  }

  // --- selectCountAdminDByUserName ---

  @Test
  public void testSelectCountAdminDByUserName_whenFound_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne(
        "Circabc.select_count_admin_by_user_name",
        "admin"
      )
    ).thenReturn(3L);

    assertEquals(3L, service.selectCountAdminDByUserName("admin"));
  }

  @Test
  public void testSelectCountAdminDByUserName_whenNull_thenReturnsZero() {
    when(
      sqlSessionTemplate.selectOne(
        "Circabc.select_count_admin_by_user_name",
        "nobody"
      )
    ).thenReturn(null);

    assertEquals(0L, service.selectCountAdminDByUserName("nobody"));
  }

  // --- deleteUserFromAllTables ---

  @Test
  public void testDeleteUserFromAllTables_thenDeletesFromAllTables() {
    service.deleteUserFromAllTables("john");

    verify(sqlSessionTemplate).delete(
      eq("Circabc.delete_user_from_profile_users"),
      anyMap()
    );
    verify(sqlSessionTemplate).delete(
      eq("Circabc.delete_user_from_circabc_admins"),
      anyMap()
    );
    verify(sqlSessionTemplate).delete(
      eq("Circabc.delete_user_from_category_admins"),
      anyMap()
    );
    verify(sqlSessionTemplate).delete(
      eq("Circabc.delete_user_from_users"),
      anyMap()
    );
  }

  // --- getGroupTitleTranslations ---

  @Test
  public void testGetGroupTitleTranslations_whenResults_thenReturnsMap() {
    KeyValueString kvs = new KeyValueString();
    kvs.setKey("en_");
    kvs.setValue("English Title");

    when(
      sqlSessionTemplate.selectList(eq("select_group_titles"), anyMap())
    ).thenReturn(Collections.singletonList(kvs));

    Map<String, String> result = service.getGroupTitleTranslations(1L);

    assertEquals(1, result.size());
    assertEquals("English Title", result.get("en"));
  }

  @Test
  public void testGetGroupTitleTranslations_whenNullValue_thenSkipped() {
    KeyValueString kvs = new KeyValueString();
    kvs.setKey("fr_");
    kvs.setValue(null);

    when(
      sqlSessionTemplate.selectList(eq("select_group_titles"), anyMap())
    ).thenReturn(Collections.singletonList(kvs));

    Map<String, String> result = service.getGroupTitleTranslations(1L);

    assertTrue(result.isEmpty());
  }

  // --- selectIgByCategoryIDUserName ---

  @Test
  public void testSelectIgByCategoryIDUserName_whenGuest_thenSelectsPublic() {
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList(eq("Circabc.select_public_ig_by_cat_user"), anyMap());

    service.selectIgByCategoryIDUserName(1L, "guest");

    verify(sqlSessionTemplate).selectList(
      eq("Circabc.select_public_ig_by_cat_user"),
      anyMap()
    );
  }

  @Test
  public void testSelectIgByCategoryIDUserName_whenCategoryAdmin_thenSelectsAll() {
    when(
      sqlSessionTemplate.selectOne(
        eq("Circabc.select_is_user_cat_admin"),
        anyMap()
      )
    ).thenReturn(1);
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList(eq("Circabc.select_ig_by_cat_admin"), anyMap());

    service.selectIgByCategoryIDUserName(1L, "catadmin");

    verify(sqlSessionTemplate).selectList(
      eq("Circabc.select_ig_by_cat_admin"),
      anyMap()
    );
  }

  @Test
  public void testSelectIgByCategoryIDUserName_whenRegularUser_thenSelectsByUser() {
    when(
      sqlSessionTemplate.selectOne(
        eq("Circabc.select_is_user_cat_admin"),
        anyMap()
      )
    ).thenReturn(0);
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList(eq("Circabc.select_ig_by_cat_user"), anyMap());

    service.selectIgByCategoryIDUserName(1L, "regularuser");

    verify(sqlSessionTemplate).selectList(
      eq("Circabc.select_ig_by_cat_user"),
      anyMap()
    );
  }

  // --- insertHeader / updateHeader ---

  @Test
  public void testInsertHeader_thenCallsInsert() {
    Header header = new Header();
    service.insertHeader(header);
    verify(sqlSessionTemplate).insert("Circabc.insert_header", header);
  }

  @Test
  public void testUpdateHeader_thenCallsInsert() {
    Header header = new Header();
    service.updateHeader(header);
    verify(sqlSessionTemplate).insert("Circabc.update_header_by_ref", header);
  }

  // --- deleteCategory ---

  @Test
  public void testDeleteCategory_whenHasIGs_thenDeletesIGsFirst() {
    when(
      sqlSessionTemplate.selectList("Circabc.select_ig_ids_by_cat_id", 5L)
    ).thenReturn(Arrays.asList(10L, 20L));

    service.deleteCategory(5L);

    // Verify IGs deleted
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_profile_titles_by_ig_id",
      10L
    );
    verify(sqlSessionTemplate).delete("Circabc.delete_ig_by_id", 10L);
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_profile_titles_by_ig_id",
      20L
    );
    verify(sqlSessionTemplate).delete("Circabc.delete_ig_by_id", 20L);
    // Verify category deleted
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_cat_title_trans_by_cat_id",
      5L
    );
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_cat_admins_by_cat_id",
      5L
    );
    verify(sqlSessionTemplate).delete("Circabc.delete_cat_by_id", 5L);
  }

  // --- updateCategoryTitles with I18nProperty ---

  @Test
  public void testUpdateCategoryTitles_withI18nProperty_thenInsertsTranslations() {
    KeyValue kv = new KeyValue();
    kv.setKey("en_");
    kv.setValue(1L);
    when(sqlSessionTemplate.selectList("Circabc.select_alf_locale")).thenReturn(
      Collections.singletonList(kv)
    );

    I18nProperty title = new I18nProperty();
    title.put("en", "English");

    service.updateCategoryTitles(5L, title);

    verify(sqlSessionTemplate).delete(
      "Circabc.delete_cat_title_translations_by_id",
      5L
    );
    verify(sqlSessionTemplate).insert(
      eq("Circabc.insert_category_title"),
      anyMap()
    );
  }

  @Test
  public void testUpdateCategoryTitles_whenLocaleNotFound_thenSkips() {
    KeyValue kv = new KeyValue();
    kv.setKey("en_");
    kv.setValue(1L);
    when(sqlSessionTemplate.selectList("Circabc.select_alf_locale")).thenReturn(
      Collections.singletonList(kv)
    );

    I18nProperty title = new I18nProperty();
    title.put("xx", "Unknown");

    service.updateCategoryTitles(5L, title);

    verify(sqlSessionTemplate).delete(
      "Circabc.delete_cat_title_translations_by_id",
      5L
    );
    verify(sqlSessionTemplate, never()).insert(
      eq("Circabc.insert_category_title"),
      anyMap()
    );
  }

  // --- moveInterestGroup ---

  @Test
  public void testMoveInterestGroup_thenCallsUpdate() {
    service.moveInterestGroup(10L, 1L, 2L);
    verify(sqlSessionTemplate).update(eq("Circabc.update_cat_id"), anyMap());
  }

  // --- updateInterestGroup ---

  @Test
  public void testUpdateInterestGroup_thenCallsUpdate() {
    InterestGroup ig = new InterestGroup();
    service.updateInterestGroup(ig);
    verify(sqlSessionTemplate).update("Circabc.update_ig_name_title_by_id", ig);
  }

  // --- updateCategory ---

  @Test
  public void testUpdateCategory_thenCallsUpdate() {
    Category cat = new Category();
    service.updateCategory(cat);
    verify(sqlSessionTemplate).update(
      "Circabc.update_cat_name_title_by_id",
      cat
    );
  }

  // --- updateProfileTitles ---

  @Test
  public void testUpdateProfileTitles_deletesAndInserts() {
    service.updateProfileTitles(42L);
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_profile_title_translations_by_id",
      42L
    );
    verify(sqlSessionTemplate).insert(
      "Circabc.insert_profile_titles_by_id",
      42L
    );
  }

  // --- updateUserUILangauge ---

  @Test
  public void testUpdateUserUILangauge_thenCallsUpdate() {
    service.updateUserUILangauge("john", "fr");
    verify(sqlSessionTemplate).update(
      eq("Circabc.update_user_ui_lang"),
      anyMap()
    );
  }

  // --- updateInterestGroupTitles ---

  @Test
  public void testUpdateInterestGroupTitles_deletesAndInserts() {
    service.updateInterestGroupTitles(100L);
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_ig_title_translations_by_id",
      100L
    );
    verify(sqlSessionTemplate).insert("Circabc.insert_ig_titles_by_id", 100L);
  }

  // --- updateCategoryTitles (long) ---

  @Test
  public void testUpdateCategoryTitles_long_deletesAndInserts() {
    service.updateCategoryTitles(50L);
    verify(sqlSessionTemplate).delete(
      "Circabc.delete_cat_title_translations_by_id",
      50L
    );
    verify(sqlSessionTemplate).insert("Circabc.insert_cat_titles_by_id", 50L);
  }

  // --- selectCategoryIDByNodeRef ---

  @Test
  public void testSelectCategoryIDByNodeRef_returnsId() {
    when(
      sqlSessionTemplate.selectOne(
        "Circabc.select_cat_id_by_node_ref",
        "workspace://SpacesStore/cat"
      )
    ).thenReturn(99L);
    assertEquals(
      99L,
      service.selectCategoryIDByNodeRef("workspace://SpacesStore/cat")
    );
  }

  // --- updateProfileExported ---

  @Test
  public void testUpdateProfileExported_thenCallsUpdate() {
    service.updateProfileExported(10L, true);
    verify(sqlSessionTemplate).update(
      eq("Circabc.update_profile_export"),
      anyMap()
    );
  }

  // --- insertMultilingualProperties ---

  @Test
  public void testInsertMultilingualProperties_callsThreeInserts() {
    service.insertMultilingualProperties();
    verify(sqlSessionTemplate).insert("Circabc.insert_cat_titles");
    verify(sqlSessionTemplate).insert("Circabc.insert_ig_titles");
    verify(sqlSessionTemplate).insert("Circabc.insert_profile_titles");
  }

  // --- deleteProfileByInterestGroupUserName ---

  @Test
  public void testDeleteProfileByInterestGroupUserName_returnsCount() {
    when(
      sqlSessionTemplate.delete(
        eq("Circabc.delete_profile_user_by_ig_id_user_name"),
        anyMap()
      )
    ).thenReturn(1);
    assertEquals(
      1,
      service.deleteProfileByInterestGroupUserName(100L, "user1")
    );
  }

  // --- selectIgByID ---

  @Test
  public void testSelectIgByID_returnsResult() {
    InterestGroupResult expected = new InterestGroupResult();
    when(
      sqlSessionTemplate.selectOne(eq("Circabc.select_ig_by_id"), anyMap())
    ).thenReturn(expected);
    assertSame(expected, service.selectIgByID(42L));
  }

  // --- selectProfileTitles ---

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectProfileTitles_thenReturnsList() {
    TranslationEntry entry = new TranslationEntry();
    List result = Collections.singletonList(entry);
    when(
      sqlSessionTemplate.selectList(
        eq("Circabc.select_titles_by_profile_id"),
        any()
      )
    ).thenReturn(result);
    List<TranslationEntry> actual = service.selectProfileTitles(100L);
    assertEquals(1, actual.size());
  }

  // --- selectCategoryAdmins ---

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectCategoryAdmins_thenReturnsList() {
    List result = Arrays.asList("admin1", "admin2");
    when(
      sqlSessionTemplate.selectList(eq("Circabc.select_category_admins"), any())
    ).thenReturn(result);
    List<String> actual = service.selectCategoryAdmins(10L);
    assertEquals(2, actual.size());
  }

  // --- getUserProfileInGroup ---

  @SuppressWarnings("unchecked")
  @Test
  public void testGetUserProfileInGroup_thenReturnsList() {
    ProfileUser pu = new ProfileUser(1L, "GROUP_test");
    List result = Collections.singletonList(pu);
    when(
      sqlSessionTemplate.selectList(eq("select_user_profile_in_group"), any())
    ).thenReturn(result);
    List<ProfileUser> actual = service.getUserProfileInGroup(1L, "user1");
    assertEquals(1, actual.size());
  }

  // --- deleteUserInGroup ---

  @Test
  public void testDeleteUserInGroup_thenCallsDelete() {
    ProfileUser pu = new ProfileUser(42L, "GROUP_test");
    service.deleteUserInGroup(pu);
    verify(sqlSessionTemplate).delete(
      eq("delete_user_profile_in_group"),
      any()
    );
  }

  // --- updateLogoIdForCategory ---

  @Test
  public void testUpdateLogoIdForCategory_thenCallsUpdate() {
    service.updateLogoIdForCategory(10L, "logo-ref");
    verify(sqlSessionTemplate).update(eq("update_logo_id_for_category"), any());
  }

  // --- updateLogoIdForGroup ---

  @Test
  public void testUpdateLogoIdForGroup_thenCallsUpdate() {
    service.updateLogoIdForGroup(5L, "logo-ref");
    verify(sqlSessionTemplate).update(eq("update_logo_id_for_group"), any());
  }

  // --- getProfileTitleTranslations ---

  @SuppressWarnings("unchecked")
  @Test
  public void testGetProfileTitleTranslations_thenReturnsMap() {
    when(
      sqlSessionTemplate.selectList(eq("select_profile_titles"), any())
    ).thenReturn(Collections.emptyList());
    Map<String, String> result = service.getProfileTitleTranslations(100L);
    assertNotNull(result);
  }

  // --- updateProfileTitle ---

  @SuppressWarnings("unchecked")
  @Test
  public void testUpdateProfileTitle_thenDeletesOldTitles() {
    when(sqlSessionTemplate.selectList("Circabc.select_alf_locale")).thenReturn(
      Collections.emptyList()
    );

    I18nProperty title = new I18nProperty();
    title.put("en", "English");

    service.updateProfileTitle(100L, title);
    verify(sqlSessionTemplate).delete(eq("delete_profile_titles"), any());
  }

  // --- getCategoryTitleTranslations ---

  @SuppressWarnings("unchecked")
  @Test
  public void testGetCategoryTitleTranslations_thenReturnsMap() {
    when(
      sqlSessionTemplate.selectList(eq("select_category_titles"), any())
    ).thenReturn(Collections.emptyList());
    Map<String, String> result = service.getCategoryTitleTranslations(10L);
    assertNotNull(result);
  }

  // --- selectCategoryAdminEmails ---

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectCategoryAdminEmails_thenReturnsList() {
    List result = Arrays.asList("admin@test.com");
    when(
      sqlSessionTemplate.selectList(
        eq("Circabc.select_category_admin_emails"),
        any()
      )
    ).thenReturn(result);
    List<String> actual = service.selectCategoryAdminEmails(
      "workspace://SpacesStore/cat-1"
    );
    assertEquals(1, actual.size());
  }

  // --- selectInterestGroupAdminEmails ---

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectInterestGroupAdminEmails_thenReturnsList() {
    List result = Arrays.asList("leader@test.com");
    when(
      sqlSessionTemplate.selectList(
        eq("Circabc.select_interest_group_admin_emails"),
        any()
      )
    ).thenReturn(result);
    List<String> actual = service.selectInterestGroupAdminEmails(
      "workspace://SpacesStore/ig-1"
    );
    assertEquals(1, actual.size());
  }

  // --- selectCategoriesNodeRef ---

  @SuppressWarnings("unchecked")
  @Test
  public void testSelectCategoriesNodeRef_thenReturnsList() {
    List result = Arrays.asList("workspace://SpacesStore/cat-1");
    when(
      sqlSessionTemplate.selectList("Circabc.select_categories_node_ref")
    ).thenReturn(result);
    List<String> actual = service.selectCategoriesNodeRef();
    assertEquals(1, actual.size());
  }

  // --- updateIgToBeDeleted ---

  @Test
  public void testUpdateIgToBeDeleted_thenCallsUpdate() {
    service.updateIgToBeDeleted(5L, true);
    verify(sqlSessionTemplate).update(
      eq("Circabc.update_to_be_deleted_by_id"),
      any()
    );
  }

  // --- insertCircabcAdmin ---

  @Test
  public void testInsertCircabcAdmin_thenCallsInsert() {
    CircabcAdmin admin = new CircabcAdmin(1L);
    service.insertCircabcAdmin(admin);
    verify(sqlSessionTemplate).insert("Circabc.insert_circabc_admin", admin);
  }

  // --- deleteCircabcAdmin ---

  @Test
  public void testDeleteCircabcAdmin_thenCallsDelete() {
    CircabcAdmin admin = new CircabcAdmin(1L);
    service.deleteCircabcAdmin(admin);
    verify(sqlSessionTemplate).delete("Circabc.delete_circabc_admin", admin);
  }

  // --- deleteAllCircabcAdmins ---

  @Test
  public void testDeleteAllCircabcAdmins_thenCallsDelete() {
    service.deleteAllCircabcAdmins();
    verify(sqlSessionTemplate).delete("Circabc.delete_all_circabc_admins");
  }

  // --- getIsCircabcAdmin ---

  @Test
  public void testGetIsCircabcAdmin_whenAdmin_thenReturnsPositive() {
    when(
      sqlSessionTemplate.selectOne(
        eq("Circabc.select_counter_username_in_circabc"),
        any()
      )
    ).thenReturn(1);
    assertEquals(1, service.getIsCircabcAdmin("admin"));
  }

  // --- getCategoryIGsExceptCurrent ---

  @SuppressWarnings("unchecked")
  @Test
  public void testGetCategoryIGsExceptCurrent_thenReturnsList() {
    IGData ig = new IGData();
    List result = Collections.singletonList(ig);
    when(
      sqlSessionTemplate.selectList(
        eq("Circabc.select_category_igs_except_current"),
        any()
      )
    ).thenReturn(result);
    List<IGData> actual = service.getCategoryIGsExceptCurrent(10L, 5L, "user1");
    assertEquals(1, actual.size());
  }
}
