package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.profile.ProfileService;
import io.swagger.api.GroupsApi;
import io.swagger.api.ProfilesApi;
import io.swagger.model.BulkImportUserData;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.db.Profile;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Before;
import org.junit.Test;

public class BulkUserImportServiceTest {

  private BulkUserImportService service;
  private BulkUserImportServiceImpl serviceImpl;
  private PersonService personService;
  private LdapUserService ldapUserService;
  private NodeService nodeService;
  private ProfileService profileService;
  private CircabcService circabcService;
  private GroupsApi groupsApi;
  private ProfilesApi profilesApi;
  private UserService userService;
  private NotificationService notificationService;
  private NodeRef igRef;

  @Before
  public void setUp() throws Exception {
    serviceImpl = new BulkUserImportServiceImpl();
    service = serviceImpl;

    personService = mock(PersonService.class);
    ldapUserService = mock(LdapUserService.class);
    nodeService = mock(NodeService.class);
    profileService = mock(ProfileService.class);
    circabcService = mock(CircabcService.class);
    groupsApi = mock(GroupsApi.class);
    profilesApi = mock(ProfilesApi.class);
    userService = mock(UserService.class);
    notificationService = mock(NotificationService.class);

    setField("personService", personService);
    setField("ldapUserService", ldapUserService);
    setField("nodeService", nodeService);
    setField("profileService", profileService);
    setField("circabcService", circabcService);
    setField("groupsApi", groupsApi);
    setField("profilesApi", profilesApi);
    setField("userService", userService);
    setField("notificationService", notificationService);

    igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BulkUserImportServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(serviceImpl, value);
  }

  // --- listMembers ---

  @Test
  public void testListMembers_whenMultipleMembers_thenReturnsAll() {
    Map<String, Profile> members = new LinkedHashMap<>();
    members.put("user1", new Profile());
    members.put("user2", new Profile());

    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(members);
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "Title"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "name"
    );

    when(ldapUserService.getLDAPUserDataByUid("user1")).thenReturn(
      new CircabcUserDataBean()
    );
    when(ldapUserService.getLDAPUserDataByUid("user2")).thenReturn(
      new CircabcUserDataBean()
    );

    List<BulkImportUserData> result = service.listMembers(igRef, false);

    assertEquals(2, result.size());
    assertEquals("user1", result.get(0).getExpectedUsername());
    assertEquals("user2", result.get(1).getExpectedUsername());
  }

  @Test
  public void testListMembers_whenIgNameAsProfileTrue_thenUsesTitle() {
    Map<String, Profile> members = new LinkedHashMap<>();
    members.put("user1", new Profile());

    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(members);
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "IG Title"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "ig-name"
    );
    when(ldapUserService.getLDAPUserDataByUid("user1")).thenReturn(
      new CircabcUserDataBean()
    );

    List<BulkImportUserData> result = service.listMembers(igRef, true);

    assertEquals("IG Title", result.get(0).getIgName());
    assertEquals("IG Title", result.get(0).getProfile());
  }

  // --- saveWork ---

  @Test
  public void testSaveWork_whenMultipleUsers_thenAllRowsCreated() {
    List<BulkImportUserData> model = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      BulkImportUserData data = new BulkImportUserData();
      CircabcUserDataBean bean = new CircabcUserDataBean();
      bean.setEcasUserName("user" + i);
      bean.setFirstName("First" + i);
      bean.setLastName("Last" + i);
      bean.setEmail("user" + i + "@ec.eu");
      data.setUser(bean);
      data.setProfile("Profile" + i);
      model.add(data);
    }

    HSSFWorkbook wb = service.saveWork(model);

    // header + 3 data rows
    assertNotNull(wb.getSheetAt(0).getRow(0));
    assertNotNull(wb.getSheetAt(0).getRow(3));
    assertNull(wb.getSheetAt(0).getRow(4));
  }

  @Test
  public void testSaveWork_headerColumnsAreCorrect() {
    HSSFWorkbook wb = service.saveWork(new ArrayList<>());
    Row header = wb.getSheetAt(0).getRow(0);

    assertEquals("username", header.getCell(0).getStringCellValue());
    assertEquals("firstname", header.getCell(1).getStringCellValue());
    assertEquals("lastname", header.getCell(2).getStringCellValue());
    assertEquals("email", header.getCell(3).getStringCellValue());
    assertEquals("profile", header.getCell(4).getStringCellValue());
  }

  // --- loadWork ---

  @Test
  public void testLoadWork_whenNoSheets_thenReturnsEmptyList()
    throws Exception {
    HSSFWorkbook wb = new HSSFWorkbook();
    List<BulkImportUserData> result = service.loadWork(wb, "file.xls");
    assertTrue(result.isEmpty());
  }

  @Test
  public void testLoadWork_whenSheetHasNoRows_thenReturnsEmptyList()
    throws Exception {
    HSSFWorkbook wb = new HSSFWorkbook();
    wb.createSheet();
    List<BulkImportUserData> result = service.loadWork(wb, "file.xls");
    assertTrue(result.isEmpty());
  }

  @Test(
    expected = io.swagger.exception.InvalidBulkImportFileFormatException.class
  )
  public void testLoadWork_whenFewerThanFiveColumns_thenThrows()
    throws Exception {
    HSSFWorkbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet();
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("username");
    header.createCell(1).setCellValue("firstname");
    header.createCell(2).setCellValue("lastname");

    service.loadWork(wb, "file.xls");
  }

  @Test
  public void testLoadWork_whenUserNotFoundInLdap_thenStatusIgnore()
    throws Exception {
    HSSFWorkbook wb = createValidWorkbook(
      "unknown",
      "John",
      "Doe",
      "j@e.eu",
      "P"
    );

    when(
      ldapUserService.getLDAPUserIDByIdMonikerEmailCn(
        "",
        "unknown",
        "j@e.eu",
        "Doe",
        true
      )
    ).thenReturn(Collections.emptyList());

    List<BulkImportUserData> result = service.loadWork(wb, "test.xls");

    assertEquals(1, result.size());
    assertEquals(BulkImportUserData.STATUS_IGNORE, result.get(0).getStatus());
  }

  @Test
  public void testLoadWork_whenMultipleSheets_thenParsesAll() throws Exception {
    HSSFWorkbook wb = new HSSFWorkbook();
    for (int i = 0; i < 2; i++) {
      Sheet sheet = wb.createSheet("Sheet" + i);
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("username");
      header.createCell(1).setCellValue("firstname");
      header.createCell(2).setCellValue("lastname");
      header.createCell(3).setCellValue("email");
      header.createCell(4).setCellValue("profile");
      Row data = sheet.createRow(1);
      data.createCell(0).setCellValue("user" + i);
      data.createCell(1).setCellValue("F" + i);
      data.createCell(2).setCellValue("L" + i);
      data.createCell(3).setCellValue("u" + i + "@e.eu");
      data.createCell(4).setCellValue("Prof");
    }

    when(
      ldapUserService.getLDAPUserIDByIdMonikerEmailCn(
        eq(""),
        anyString(),
        anyString(),
        anyString(),
        eq(true)
      )
    ).thenReturn(Collections.emptyList());

    List<BulkImportUserData> result = service.loadWork(wb, "multi.xls");

    assertEquals(2, result.size());
  }

  // --- addAll ---

  @Test
  public void testAddAll_whenUserHasNullUser_thenStillAdded() {
    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(
      Collections.emptyMap()
    );

    BulkImportUserData newUser = new BulkImportUserData();
    newUser.setUser(null);

    List<BulkImportUserData> model = new ArrayList<>();
    service.addAll(model, Arrays.asList(newUser), igRef);

    assertEquals(1, model.size());
    assertEquals(BulkImportUserData.STATUS_ERROR, model.get(0).getStatus());
  }

  @Test
  public void testAddAll_whenEmptyNewValues_thenModelUnchanged() {
    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(
      Collections.emptyMap()
    );

    List<BulkImportUserData> model = new ArrayList<>();
    service.addAll(model, new ArrayList<>(), igRef);

    assertTrue(model.isEmpty());
  }

  // --- parseProfilesToBeCreated ---

  @Test
  public void testParseProfilesToBeCreated_whenNoIgRefAndNoDept_thenProfileUnchanged() {
    BulkImportUserData user = new BulkImportUserData();
    user.setProfile("Original");

    List<String> profilesToBeCreated = new ArrayList<>();
    service.parseProfilesToBeCreated(
      Arrays.asList(user),
      false,
      false,
      profilesToBeCreated
    );

    assertEquals("Original", user.getProfile());
    assertTrue(profilesToBeCreated.isEmpty());
  }

  @Test
  public void testParseProfilesToBeCreated_whenDuplicateIgNames_thenAddedOnce() {
    BulkImportUserData user1 = new BulkImportUserData();
    user1.setIgRef(igRef);
    user1.setIgName("SameIG");

    BulkImportUserData user2 = new BulkImportUserData();
    user2.setIgRef(igRef);
    user2.setIgName("SameIG");

    List<String> profilesToBeCreated = new ArrayList<>();
    service.parseProfilesToBeCreated(
      Arrays.asList(user1, user2),
      true,
      false,
      profilesToBeCreated
    );

    assertEquals(1, profilesToBeCreated.size());
    assertEquals("SameIG", profilesToBeCreated.get(0));
  }

  // --- inviteUsers ---

  @Test
  public void testInviteUsers_whenUserIsNull_thenSkipped() {
    BulkImportUserData user = new BulkImportUserData();
    user.setUser(null);
    user.setStatus(BulkImportUserData.STATUS_OK);

    service.inviteUsers(Arrays.asList(user), igRef, new HashMap<>(), false);

    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testInviteUsers_whenEmptyModel_thenNoInteraction() {
    service.inviteUsers(new ArrayList<>(), igRef, new HashMap<>(), false);
    verifyNoInteractions(groupsApi);
    verifyNoInteractions(circabcService);
  }

  @Test
  public void testInviteUsers_whenProfileNotInMap_thenCreatesProfile() {
    CircabcUserDataBean userBean = new CircabcUserDataBean();
    userBean.setUserName("newuser");

    BulkImportUserData user = new BulkImportUserData();
    user.setUser(userBean);
    user.setStatus(BulkImportUserData.STATUS_OK);
    user.setProfile("NewProfile");

    when(circabcService.isUserMember(igRef, "newuser")).thenReturn(false);
    when(personService.personExists("newuser")).thenReturn(true);

    io.swagger.model.Profile createdProfile = new io.swagger.model.Profile();
    createdProfile.setName("NewProfile_generated");
    when(profilesApi.groupsIdProfilesPost(eq(igRef), any())).thenReturn(
      createdProfile
    );

    Profile dbProfile = new Profile();
    dbProfile.setAlfrescoGroup("GROUP_NEW");
    when(profileService.getProfile(igRef, "NewProfile")).thenReturn(dbProfile);

    Map<String, String> igProfiles = new HashMap<>();
    service.inviteUsers(Arrays.asList(user), igRef, igProfiles, false);

    verify(profilesApi).groupsIdProfilesPost(eq(igRef), any());
    verify(groupsApi).groupsIdMembersPost(eq(igRef), any());
    assertTrue(igProfiles.containsKey("NewProfile"));
  }

  @Test
  public void testInviteUsers_whenPersonDoesNotExist_thenCreatesUser() {
    CircabcUserDataBean userBean = new CircabcUserDataBean();
    userBean.setUserName("brandnew");

    BulkImportUserData user = new BulkImportUserData();
    user.setUser(userBean);
    user.setStatus(BulkImportUserData.STATUS_OK);
    user.setProfile("Access");

    when(circabcService.isUserMember(igRef, "brandnew")).thenReturn(false);
    when(personService.personExists("brandnew")).thenReturn(false);

    Profile dbProfile = new Profile();
    dbProfile.setAlfrescoGroup("GROUP_ACCESS");
    when(profileService.getProfile(igRef, "Access")).thenReturn(dbProfile);

    Map<String, String> igProfiles = new HashMap<>();
    igProfiles.put("Access", "Access");

    service.inviteUsers(Arrays.asList(user), igRef, igProfiles, false);

    verify(userService).createUser(userBean, true);
    verify(groupsApi).groupsIdMembersPost(eq(igRef), any());
  }

  // --- listGroupProfiles ---

  @Test
  public void testListGroupProfiles_delegatesToProfileService() {
    List<Profile> profiles = Arrays.asList(new Profile(), new Profile());
    when(profileService.getProfiles(igRef)).thenReturn(profiles);

    List<Profile> result = service.listGroupProfiles(igRef);

    assertEquals(2, result.size());
    verify(profileService).getProfiles(igRef);
  }

  // --- helpers ---

  private HSSFWorkbook createValidWorkbook(
    String username,
    String firstname,
    String lastname,
    String email,
    String profile
  ) {
    HSSFWorkbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet();
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("username");
    header.createCell(1).setCellValue("firstname");
    header.createCell(2).setCellValue("lastname");
    header.createCell(3).setCellValue("email");
    header.createCell(4).setCellValue("profile");
    Row data = sheet.createRow(1);
    data.createCell(0).setCellValue(username);
    data.createCell(1).setCellValue(firstname);
    data.createCell(2).setCellValue(lastname);
    data.createCell(3).setCellValue(email);
    data.createCell(4).setCellValue(profile);
    return wb;
  }
}
