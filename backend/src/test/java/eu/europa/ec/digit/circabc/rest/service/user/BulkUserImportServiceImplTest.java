package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.profile.ProfileService;
import io.swagger.model.BulkImportUserData;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.db.Profile;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

public class BulkUserImportServiceImplTest {

  private BulkUserImportServiceImpl service;
  private PersonService personService;
  private LdapUserService ldapUserService;
  private org.alfresco.service.cmr.repository.NodeService nodeService;
  private ProfileService profileService;
  private CircabcService circabcService;
  private io.swagger.api.GroupsApi groupsApi;
  private io.swagger.api.ProfilesApi profilesApi;
  private UserService userService;
  private eu.europa.ec.digit.circabc.rest.service.notification.NotificationService notificationService;

  private NodeRef igRef;

  @Before
  public void setUp() throws Exception {
    service = new BulkUserImportServiceImpl();
    personService = mock(PersonService.class);
    ldapUserService = mock(LdapUserService.class);
    nodeService = mock(org.alfresco.service.cmr.repository.NodeService.class);
    profileService = mock(ProfileService.class);
    circabcService = mock(CircabcService.class);
    groupsApi = mock(io.swagger.api.GroupsApi.class);
    profilesApi = mock(io.swagger.api.ProfilesApi.class);
    userService = mock(UserService.class);
    notificationService = mock(
      eu.europa.ec.digit.circabc.rest.service.notification
        .NotificationService.class
    );

    setField("personService", personService);
    setField("ldapUserService", ldapUserService);
    setField("nodeService", nodeService);
    setField("profileService", profileService);
    setField("circabcService", circabcService);
    setField("groupsApi", groupsApi);
    setField("profilesApi", profilesApi);
    setField("userService", userService);
    setField("notificationService", notificationService);

    igRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-ig-id");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BulkUserImportServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  // --- listMembers tests ---

  @Test
  public void testListMembers_whenMembersExist_thenReturnsUserDataList() {
    Map<String, Profile> members = new LinkedHashMap<>();
    members.put("user1", new Profile());

    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(members);
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "IG Title"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "ig-name"
    );

    CircabcUserDataBean userData = new CircabcUserDataBean();
    userData.setEcasUserName("user1");
    when(ldapUserService.getLDAPUserDataByUid("user1")).thenReturn(userData);

    List<BulkImportUserData> result = service.listMembers(igRef, false);

    assertEquals(1, result.size());
    assertEquals("user1", result.get(0).getExpectedUsername());
    assertEquals("IG Title", result.get(0).getIgName());
    assertEquals(BulkImportUserData.STATUS_OK, result.get(0).getStatus());
    assertNull(result.get(0).getProfile());
  }

  @Test
  public void testListMembers_whenIgNameAsProfileTrue_thenProfileSetToIgTitle() {
    Map<String, Profile> members = new LinkedHashMap<>();
    members.put("user1", new Profile());

    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(members);
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "IG Title"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "ig-name"
    );

    CircabcUserDataBean userData = new CircabcUserDataBean();
    when(ldapUserService.getLDAPUserDataByUid("user1")).thenReturn(userData);

    List<BulkImportUserData> result = service.listMembers(igRef, true);

    assertEquals("IG Title", result.get(0).getProfile());
  }

  @Test
  public void testListMembers_whenNoMembers_thenReturnsEmptyList() {
    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(
      Collections.emptyMap()
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "Title"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "name"
    );

    List<BulkImportUserData> result = service.listMembers(igRef, false);

    assertTrue(result.isEmpty());
  }

  // --- saveWork tests ---

  @Test
  public void testSaveWork_whenModelHasUsers_thenCreatesWorkbook() {
    List<BulkImportUserData> model = new ArrayList<>();
    BulkImportUserData userData = new BulkImportUserData();
    CircabcUserDataBean user = new CircabcUserDataBean();
    user.setEcasUserName("jdoe");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("jdoe@ec.europa.eu");
    userData.setUser(user);
    userData.setProfile("Admin");
    model.add(userData);

    HSSFWorkbook wb = service.saveWork(model);

    assertNotNull(wb);
    assertEquals("UserExport", wb.getSheetName(0));
    // Header row
    assertEquals(
      "username",
      wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue()
    );
    // Data row
    assertEquals(
      "jdoe",
      wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()
    );
    assertEquals(
      "John",
      wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue()
    );
    assertEquals(
      "Doe",
      wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue()
    );
    assertEquals(
      "jdoe@ec.europa.eu",
      wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue()
    );
    assertEquals(
      "Admin",
      wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue()
    );
  }

  @Test
  public void testSaveWork_whenUserIsNull_thenUsesExpectedUsername() {
    List<BulkImportUserData> model = new ArrayList<>();
    BulkImportUserData userData = new BulkImportUserData();
    userData.setUser(null);
    userData.setExpectedUsername("unknown_user");
    model.add(userData);

    HSSFWorkbook wb = service.saveWork(model);

    assertEquals(
      "unknown_user",
      wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue()
    );
  }

  @Test
  public void testSaveWork_whenEmptyModel_thenOnlyHeaderRow() {
    HSSFWorkbook wb = service.saveWork(new ArrayList<>());

    assertNotNull(wb.getSheetAt(0).getRow(0));
    assertNull(wb.getSheetAt(0).getRow(1));
  }

  // --- addAll tests ---

  @Test
  public void testAddAll_whenNewValuesNull_thenModelUnchanged() {
    List<BulkImportUserData> model = new ArrayList<>();
    service.addAll(model, null, igRef);
    assertTrue(model.isEmpty());
  }

  @Test
  public void testAddAll_whenNewUser_thenAddsToModel() {
    List<BulkImportUserData> model = new ArrayList<>();

    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(
      Collections.emptyMap()
    );

    BulkImportUserData newUser = new BulkImportUserData();
    CircabcUserDataBean userBean = new CircabcUserDataBean();
    userBean.setEmail("new@ec.europa.eu");
    userBean.setUserName("newuser");
    newUser.setUser(userBean);
    newUser.setStatus(BulkImportUserData.STATUS_OK);

    service.addAll(model, Arrays.asList(newUser), igRef);

    assertEquals(1, model.size());
  }

  @Test
  public void testAddAll_whenDuplicateEmail_thenNotAdded() {
    CircabcUserDataBean existingBean = new CircabcUserDataBean();
    existingBean.setEmail("same@ec.europa.eu");

    BulkImportUserData existing = new BulkImportUserData();
    existing.setUser(existingBean);

    List<BulkImportUserData> model = new ArrayList<>();
    model.add(existing);

    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(
      Collections.emptyMap()
    );

    CircabcUserDataBean newBean = new CircabcUserDataBean();
    newBean.setEmail("same@ec.europa.eu");
    newBean.setUserName("newuser");

    BulkImportUserData newUser = new BulkImportUserData();
    newUser.setUser(newBean);
    newUser.setStatus(BulkImportUserData.STATUS_OK);

    service.addAll(model, Arrays.asList(newUser), igRef);

    assertEquals(1, model.size());
  }

  @Test
  public void testAddAll_whenAlreadyMember_thenStatusSetToAlreadyMember() {
    Map<String, Profile> members = new HashMap<>();
    members.put("existinguser", new Profile());
    when(profileService.getInvitedUsersProfiles(igRef)).thenReturn(members);

    CircabcUserDataBean userBean = new CircabcUserDataBean();
    userBean.setEmail("existing@ec.europa.eu");
    userBean.setUserName("existinguser");

    BulkImportUserData newUser = new BulkImportUserData();
    newUser.setUser(userBean);
    newUser.setStatus(BulkImportUserData.STATUS_OK);

    List<BulkImportUserData> model = new ArrayList<>();
    service.addAll(model, Arrays.asList(newUser), igRef);

    assertEquals(1, model.size());
    assertEquals(
      BulkImportUserData.STATUS_ALREADY_MEMBER,
      model.get(0).getStatus()
    );
  }

  // --- parseProfilesToBeCreated tests ---

  @Test
  public void testParseProfilesToBeCreated_whenIgProfileEnabled_thenSetsProfileToIgName() {
    BulkImportUserData user = new BulkImportUserData();
    user.setIgRef(igRef);
    user.setIgName("MyIG");

    List<BulkImportUserData> model = Arrays.asList(user);
    List<String> profilesToBeCreated = new ArrayList<>();

    service.parseProfilesToBeCreated(model, true, false, profilesToBeCreated);

    assertEquals("MyIG", user.getProfile());
    assertTrue(profilesToBeCreated.contains("MyIG"));
  }

  @Test
  public void testParseProfilesToBeCreated_whenDepartmentProfileEnabled_thenSetsProfileToDept() {
    BulkImportUserData user = new BulkImportUserData();
    user.setDepartmentNumber("DIGIT.B3");

    List<BulkImportUserData> model = Arrays.asList(user);
    List<String> profilesToBeCreated = new ArrayList<>();

    service.parseProfilesToBeCreated(model, false, true, profilesToBeCreated);

    assertEquals("DIGIT.B3", user.getProfile());
    assertTrue(profilesToBeCreated.contains("DIGIT.B3"));
  }

  @Test
  public void testParseProfilesToBeCreated_whenIgProfileDisabled_thenClearsProfile() {
    BulkImportUserData user = new BulkImportUserData();
    user.setIgRef(igRef);
    user.setIgName("MyIG");
    user.setProfile("MyIG");

    List<BulkImportUserData> model = Arrays.asList(user);
    List<String> profilesToBeCreated = new ArrayList<>(Arrays.asList("MyIG"));

    service.parseProfilesToBeCreated(model, false, false, profilesToBeCreated);

    assertEquals("", user.getProfile());
    assertFalse(profilesToBeCreated.contains("MyIG"));
  }

  // --- inviteUsers tests ---

  @Test
  public void testInviteUsers_whenUserNotEligible_thenSkipped() {
    BulkImportUserData user = new BulkImportUserData();
    user.setStatus(BulkImportUserData.STATUS_IGNORE);
    user.setUser(new CircabcUserDataBean());

    List<BulkImportUserData> model = Arrays.asList(user);
    Map<String, String> igProfiles = new HashMap<>();

    service.inviteUsers(model, igRef, igProfiles, false);

    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testInviteUsers_whenUserAlreadyMember_thenSkipped() {
    CircabcUserDataBean userBean = new CircabcUserDataBean();
    userBean.setUserName("existinguser");

    BulkImportUserData user = new BulkImportUserData();
    user.setUser(userBean);
    user.setStatus(BulkImportUserData.STATUS_OK);

    when(circabcService.isUserMember(igRef, "existinguser")).thenReturn(true);

    List<BulkImportUserData> model = Arrays.asList(user);
    Map<String, String> igProfiles = new HashMap<>();

    service.inviteUsers(model, igRef, igProfiles, false);

    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testInviteUsers_whenEligibleUser_thenInvitesUser() {
    CircabcUserDataBean userBean = new CircabcUserDataBean();
    userBean.setUserName("newuser");

    BulkImportUserData user = new BulkImportUserData();
    user.setUser(userBean);
    user.setStatus(BulkImportUserData.STATUS_OK);
    user.setProfile("Access");

    when(circabcService.isUserMember(igRef, "newuser")).thenReturn(false);
    when(personService.personExists("newuser")).thenReturn(true);

    Profile profile = new Profile();
    profile.setAlfrescoGroup("GROUP_ACCESS");
    when(profileService.getProfile(igRef, "Access")).thenReturn(profile);

    Map<String, String> igProfiles = new HashMap<>();
    igProfiles.put("Access", "Access");

    service.inviteUsers(model(user), igRef, igProfiles, false);

    verify(groupsApi).groupsIdMembersPost(eq(igRef), any());
  }

  // --- loadWork tests ---

  @Test
  public void testLoadWork_whenEmptyWorkbook_thenReturnsEmptyList()
    throws Exception {
    HSSFWorkbook wb = new HSSFWorkbook();
    List<BulkImportUserData> result = service.loadWork(wb, "test.xls");
    assertTrue(result.isEmpty());
  }

  @Test(
    expected = io.swagger.exception.InvalidBulkImportFileFormatException.class
  )
  public void testLoadWork_whenInvalidColumns_thenThrowsException()
    throws Exception {
    HSSFWorkbook wb = new HSSFWorkbook();
    org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet();
    org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("wrong1");
    header.createCell(1).setCellValue("wrong2");
    header.createCell(2).setCellValue("wrong3");
    header.createCell(3).setCellValue("wrong4");
    header.createCell(4).setCellValue("wrong5");

    service.loadWork(wb, "test.xls");
  }

  @Test
  public void testLoadWork_whenValidData_thenParsesUsers() throws Exception {
    HSSFWorkbook wb = new HSSFWorkbook();
    org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet();
    org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("username");
    header.createCell(1).setCellValue("firstname");
    header.createCell(2).setCellValue("lastname");
    header.createCell(3).setCellValue("email");
    header.createCell(4).setCellValue("profile");

    org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(1);
    dataRow.createCell(0).setCellValue("jdoe");
    dataRow.createCell(1).setCellValue("John");
    dataRow.createCell(2).setCellValue("Doe");
    dataRow.createCell(3).setCellValue("jdoe@ec.europa.eu");
    dataRow.createCell(4).setCellValue("Admin");

    CircabcUserDataBean userData = new CircabcUserDataBean();
    userData.setEcasUserName("jdoe");
    when(
      ldapUserService.getLDAPUserIDByIdMonikerEmailCn(
        "",
        "jdoe",
        "jdoe@ec.europa.eu",
        "Doe",
        true
      )
    ).thenReturn(Arrays.asList("jdoe"));
    when(ldapUserService.getLDAPUserDataByUid("jdoe")).thenReturn(userData);

    List<BulkImportUserData> result = service.loadWork(wb, "test.xls");

    assertEquals(1, result.size());
    assertEquals("test.xls", result.get(0).getFromFile());
    assertEquals("Admin", result.get(0).getProfile());
    assertEquals(BulkImportUserData.STATUS_OK, result.get(0).getStatus());
  }

  // --- helper ---

  private List<BulkImportUserData> model(BulkImportUserData... users) {
    return new ArrayList<>(Arrays.asList(users));
  }
}
