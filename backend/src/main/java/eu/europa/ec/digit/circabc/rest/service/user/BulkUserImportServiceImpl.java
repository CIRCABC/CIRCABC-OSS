/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.user;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationType;
import eu.europa.ec.digit.circabc.rest.service.profile.ProfileService;
import io.swagger.api.GroupsApi;
import io.swagger.api.ProfilesApi;
import io.swagger.exception.InvalidBulkImportFileFormatException;
import io.swagger.exception.ProfileException;
import io.swagger.model.BulkImportUserData;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.I18nProperty;
import io.swagger.model.MembershipPostDefinition;
import io.swagger.model.NotifiableUser;
import io.swagger.model.NotifiableUserImpl;
import io.swagger.model.User;
import io.swagger.model.UserProfile;
import io.swagger.model.db.Profile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.model.permissions.VisibilityPermissions;
import java.io.Serializable;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link BulkUserImportService}.
 *
 * <p>Provides the business logic supporting the bulk (spreadsheet-based) user import and export
 * flow for an Interest Group (IG). Its responsibilities include:
 *
 * <ul>
 *   <li>Exporting the current members of an IG to an Excel (HSSF) workbook.
 *   <li>Parsing an uploaded Excel workbook into {@link BulkImportUserData} entries, resolving each
 *       row against the LDAP/directory service to identify matching accounts.
 *   <li>Merging parsed users into a working model while filtering out users that are already
 *       members of the IG.
 *   <li>Determining which membership profiles must be created (based on IG name or department
 *       number helpers).
 *   <li>Inviting the resolved users into the IG, creating any missing user accounts and access
 *       profiles, and optionally notifying the invited users.
 * </ul>
 *
 * <p>The expected spreadsheet layout is a header row followed by user rows, using the fixed columns
 * declared in {@link #xlsDefinedColumns}: {@code username}, {@code firstname}, {@code lastname},
 * {@code email} and {@code profile}.
 *
 * @author beaurpi
 */
public class BulkUserImportServiceImpl implements BulkUserImportService {

  /**
   * Ordered list of the mandatory spreadsheet column headers, in the exact order they are expected
   * to appear. Used both when exporting the header row and when validating an uploaded workbook.
   */
  private static final List<String> xlsDefinedColumns = new ArrayList<>(
    Arrays.asList("username", "firstname", "lastname", "email", "profile")
  );

  @Autowired
  private PersonService personService;

  /** Directory service used to look up user accounts by uid or by email/name/moniker criteria. */
  @Autowired
  @Qualifier("ldapOrLuceneUserService")
  private LdapUserService ldapUserService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private UserService userService;

  @Autowired
  private ProfileService profileService;

  /** Logger for this service. */
  private Log logger = LogFactory.getLog(BulkUserImportServiceImpl.class);

  @Autowired
  private ProfilesApi profilesApi;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private GroupsApi groupsApi;

  /**
   * Lists the current invited members of an Interest Group as bulk-import entries, typically used to
   * pre-populate an export.
   *
   * <p>Each returned entry is enriched with the member's directory (LDAP) data and marked with the
   * {@link BulkImportUserData#STATUS_OK} status.
   *
   * @param igRef the node reference of the Interest Group whose members are listed
   * @param igNameAsProfile when {@code true}, the IG title (or name) is set as the profile on each
   *     returned entry
   * @return the list of members of the group as {@link BulkImportUserData} entries
   */
  @Override
  public List<BulkImportUserData> listMembers(
    NodeRef igRef,
    Boolean igNameAsProfile
  ) {
    Map<String, Profile> members = profileService.getInvitedUsersProfiles(
      igRef
    );

    List<BulkImportUserData> result = new ArrayList<>();

    String igTitle = nodeService
      .getProperty(igRef, ContentModel.PROP_TITLE)
      .toString();
    String igName = nodeService
      .getProperty(igRef, ContentModel.PROP_NAME)
      .toString();

    for (String member : members.keySet()) {
      BulkImportUserData user = new BulkImportUserData();

      user.setExpectedUsername(member);
      user.setIgRef(igRef);
      user.setIgName((igTitle != null ? igTitle : igName));
      user.setUser(ldapUserService.getLDAPUserDataByUid(member));
      user.setStatus(BulkImportUserData.STATUS_OK);

      if (Boolean.TRUE.equals(igNameAsProfile)) {
        user.setProfile((igTitle != null ? igTitle : igName));
      }

      result.add(user);
    }

    return result;
  }

  /**
   * Lists the membership profiles defined on the given Interest Group.
   *
   * @param igRef the node reference of the Interest Group
   * @return the profiles defined on the group
   */
  public List<io.swagger.model.db.Profile> listGroupProfiles(NodeRef igRef) {
    return profileService.getProfiles(igRef);
  }

  /**
   * Builds an Excel (HSSF) workbook representing the given bulk-import model, suitable for download.
   *
   * <p>The first row contains the styled header defined by {@link #xlsDefinedColumns}. Each
   * subsequent row corresponds to one {@link BulkImportUserData} entry: resolved users are written
   * with their directory data, while unresolved entries only carry the expected username.
   *
   * @param model the list of user entries to export
   * @return the generated workbook
   */
  @Override
  public HSSFWorkbook saveWork(List<BulkImportUserData> model) {
    // create a new workbook
    HSSFWorkbook wb = new HSSFWorkbook();
    // create a new sheet
    Sheet s = wb.createSheet();
    // declare a row object reference
    Row r = null;
    // declare a cell object reference
    Cell c = null;

    wb.setSheetName(0, "UserExport");

    Integer i = 0;
    Integer j = 0;

    r = s.createRow(i);

    CellStyle header = wb.createCellStyle();
    header.setBorderBottom(BorderStyle.THIN);
    header.setBorderRight(BorderStyle.THIN);
    header.setBorderTop(BorderStyle.THIN);
    header.setBorderLeft(BorderStyle.THIN);

    HSSFFont font = wb.createFont();
    font.setBold(true);

    header.setFont(font);

    // init the column names
    for (String col : xlsDefinedColumns) {
      c = r.createCell(j);
      c.setCellStyle(header);
      c.setCellValue(col);

      j++;
    }

    i++;

    // add all user to export file
    for (BulkImportUserData user : model) {
      r = s.createRow(i);

      if (user.getUser() != null) {
        c = r.createCell(0);
        c.setCellValue(user.getUser().getEcasUserName());
        c = r.createCell(1);
        c.setCellValue(user.getUser().getFirstName());
        c = r.createCell(2);
        c.setCellValue(user.getUser().getLastName());
        c = r.createCell(3);
        c.setCellValue(user.getUser().getEmail());
        c = r.createCell(4);
        c.setCellValue(user.getProfile());
      } else {
        c = r.createCell(0);
        c.setCellValue(user.getExpectedUsername());
        c = r.createCell(1);
        c.setCellValue("");
        c = r.createCell(2);
        c.setCellValue("");
        c = r.createCell(3);
        c.setCellValue("");
        c = r.createCell(4);
        c.setCellValue("");
      }

      i++;
    }

    return wb;
  }

  /**
   * Parses an uploaded Excel workbook into a list of bulk-import user entries.
   *
   * <p>Every sheet in the workbook is processed; each data row is resolved against the directory
   * service to try to match an existing account. Matched rows are marked
   * {@link BulkImportUserData#STATUS_OK}, unmatched (or ambiguous) rows
   * {@link BulkImportUserData#STATUS_IGNORE}.
   *
   * @param book the workbook to read
   * @param fileName the name of the source file, recorded on each parsed entry
   * @return the list of parsed user entries (possibly empty)
   * @throws InvalidBulkImportFileFormatException if a sheet does not match the expected column layout
   */
  @Override
  public List<BulkImportUserData> loadWork(HSSFWorkbook book, String fileName)
    throws InvalidBulkImportFileFormatException {
    List<BulkImportUserData> result = new ArrayList<>();
    if (book.getNumberOfSheets() < 1) return result;
    for (int iSheet = 0; iSheet < book.getNumberOfSheets(); iSheet++) {
      processSheet(book.getSheetAt(iSheet), iSheet, fileName, result);
    }
    return result;
  }

  private void processSheet(
    Sheet s,
    int iSheet,
    String fileName,
    List<BulkImportUserData> result
  ) throws InvalidBulkImportFileFormatException {
    Row rTmp = s.getRow(0);
    if (rTmp == null) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Sheet number" +
            iSheet +
            "of the template file being read has no users to invite. Please check it."
        );
      }
      return;
    }
    if (Boolean.FALSE.equals(validateSheetColumns(s))) {
      throw new InvalidBulkImportFileFormatException();
    }
    int iCeel = rTmp.getFirstCellNum();
    int nbRows = s.getPhysicalNumberOfRows();
    for (int i = s.getFirstRowNum() + 1; i < nbRows; i++) {
      Row r = s.getRow(i);
      if (r != null) {
        processRow(r, iCeel, fileName, result);
      }
    }
  }

  private void processRow(
    Row r,
    int iCeel,
    String fileName,
    List<BulkImportUserData> result
  ) {
    String username = getCellValue(r, iCeel);
    String lastname = getCellValue(r, iCeel + 2);
    String email = getCellValue(r, iCeel + 3);
    String profile = getCellValue(r, iCeel + 4);
    if (username.isEmpty() && lastname.isEmpty() && email.isEmpty()) {
      logger.debug("Ignoring blank line in bulk user invitation");
      return;
    }
    BulkImportUserData tmpUser = new BulkImportUserData();
    tmpUser.setFromFile(fileName);
    tmpUser.setProfile(profile);
    List<String> possibleUsers =
      ldapUserService.getLDAPUserIDByIdMonikerEmailCn(
        "",
        username,
        email,
        lastname,
        true
      );
    if (possibleUsers != null && possibleUsers.size() == 1) {
      tmpUser.setUser(
        ldapUserService.getLDAPUserDataByUid(possibleUsers.get(0))
      );
      tmpUser.setStatus(BulkImportUserData.STATUS_OK);
    } else {
      tmpUser.setUser(new CircabcUserDataBean());
      tmpUser.getUser().setEcasUserName(username);
      tmpUser.getUser().setEmail(email);
      tmpUser.setStatus(BulkImportUserData.STATUS_IGNORE);
    }
    result.add(tmpUser);
  }

  private String getCellValue(Row r, int cellIndex) {
    return r.getCell(cellIndex) != null
      ? r.getCell(cellIndex).getStringCellValue()
      : "";
  }

  private Boolean validateSheetColumns(Sheet s) {
    int iRow = s.getFirstRowNum();
    Row r = s.getRow(iRow);
    int iCellFirst = r.getFirstCellNum();

    if (r.getPhysicalNumberOfCells() < 5) {
      return false;
    }

    return (
      validateColumn(r, iCellFirst, 0) &&
      validateColumn(r, iCellFirst + 1, 1) &&
      validateColumn(r, iCellFirst + 2, 2) &&
      validateColumn(r, iCellFirst + 3, 3) &&
      validateColumn(r, iCellFirst + 4, 4)
    );
  }

  private boolean validateColumn(
    Row r,
    int cellIndex,
    int expectedColumnIndex
  ) {
    Cell cell = r.getCell(cellIndex);
    if (cell == null) {
      return false;
    }
    return cell
      .getStringCellValue()
      .equals(xlsDefinedColumns.get(expectedColumnIndex));
  }

  /**
   * Merges newly parsed entries into an existing working model.
   *
   * <p>Entries already members of the target IG are flagged accordingly, and resolved users already
   * present in the model (matched by email) are skipped to avoid duplicates.
   *
   * @param model the current working model to add to
   * @param newValues the newly parsed entries to merge (a {@code null} value is ignored)
   * @param currentIgRef the node reference of the target Interest Group, used to detect existing
   *     members
   */
  public void addAll(
    List<BulkImportUserData> model,
    List<BulkImportUserData> newValues,
    NodeRef currentIgRef
  ) {
    if (newValues == null) {
      return;
    }

    filterAlreadyMember(newValues, currentIgRef);

    for (BulkImportUserData tmpUser : newValues) {
      if (shouldAddUser(tmpUser, model)) {
        model.add(tmpUser);
      }
    }
  }

  private boolean shouldAddUser(
    BulkImportUserData tmpUser,
    List<BulkImportUserData> model
  ) {
    if (tmpUser.getUser() == null) {
      return true;
    }
    return !isUserAlreadyInModel(tmpUser, model);
  }

  private boolean isUserAlreadyInModel(
    BulkImportUserData tmpUser,
    List<BulkImportUserData> model
  ) {
    for (BulkImportUserData existingUser : model) {
      if (
        existingUser.getUser() != null &&
        tmpUser
          .getUser()
          .getEmail()
          .equalsIgnoreCase(existingUser.getUser().getEmail())
      ) {
        return true;
      }
    }
    return false;
  }

  private void filterAlreadyMember(
    List<BulkImportUserData> newValues,
    NodeRef currentIgRef
  ) {
    Map<String, Profile> members = profileService.getInvitedUsersProfiles(
      currentIgRef
    );

    for (BulkImportUserData bTmp : newValues) {
      if (bTmp.getUser() != null) {
        if (members.containsKey(bTmp.getUser().getUserName())) {
          bTmp.setStatus(BulkImportUserData.STATUS_ALREADY_MEMBER);
        }
      } else {
        bTmp.setStatus(BulkImportUserData.STATUS_ERROR);
      }
    }
  }

  /**
   * Determines and assigns the membership profiles to be created for each entry in the model.
   *
   * <p>Depending on the helper flags, the profile of each user is derived from the IG name and/or
   * the user's department number, and any newly required profile name is accumulated into
   * {@code profilesToBeCreated}.
   *
   * @param model the working model of users to process
   * @param createIgProfileHelper when {@code true}, use the IG name as the profile
   * @param createDepartmentNumberProfileHelper when {@code true}, use the department number as the
   *     profile
   * @param profilesToBeCreated the accumulating list of profile names that need to be created
   */
  @Override
  public void parseProfilesToBeCreated(
    List<BulkImportUserData> model,
    Boolean createIgProfileHelper,
    Boolean createDepartmentNumberProfileHelper,
    List<String> profilesToBeCreated
  ) {
    for (BulkImportUserData tmpUser : model) {
      processIgProfile(tmpUser, createIgProfileHelper, profilesToBeCreated);
      processDepartmentProfile(
        tmpUser,
        createDepartmentNumberProfileHelper,
        profilesToBeCreated
      );
    }
  }

  private void processIgProfile(
    BulkImportUserData tmpUser,
    Boolean createIgProfileHelper,
    List<String> profilesToBeCreated
  ) {
    if (tmpUser.getIgRef() == null) {
      return;
    }
    if (
      Boolean.TRUE.equals(createIgProfileHelper) && tmpUser.getIgName() != null
    ) {
      tmpUser.setProfile(tmpUser.getIgName());
      if (!profilesToBeCreated.contains(tmpUser.getIgName())) {
        profilesToBeCreated.add(tmpUser.getIgName());
      }
    } else if (Boolean.FALSE.equals(createIgProfileHelper)) {
      tmpUser.setProfile("");
      if (tmpUser.getIgName() != null) {
        profilesToBeCreated.remove(tmpUser.getIgName());
      }
    }
  }

  private void processDepartmentProfile(
    BulkImportUserData tmpUser,
    Boolean createDepartmentNumberProfileHelper,
    List<String> profilesToBeCreated
  ) {
    if (tmpUser.getDepartmentNumber() == null) {
      return;
    }
    if (Boolean.TRUE.equals(createDepartmentNumberProfileHelper)) {
      tmpUser.setProfile(tmpUser.getDepartmentNumber());
      if (!profilesToBeCreated.contains(tmpUser.getDepartmentNumber())) {
        profilesToBeCreated.add(tmpUser.getDepartmentNumber());
      }
    } else if (Boolean.FALSE.equals(createDepartmentNumberProfileHelper)) {
      tmpUser.setProfile("");
      profilesToBeCreated.remove(tmpUser.getDepartmentNumber());
    }
  }

  /**
   * Invites the eligible users of the model into the Interest Group.
   *
   * <p>Only users that are resolved, marked {@link BulkImportUserData#STATUS_OK} and not already
   * members are invited. Missing user accounts and missing access profiles are created on demand,
   * and, when requested, invited users are notified.
   *
   * @param model the working model of users to invite
   * @param igRef the node reference of the target Interest Group
   * @param igProfiles a mutable map of existing profile title to profile (group) name; updated when
   *     new profiles are created
   * @param notify when {@code true}, send an invitation notification to each invited user
   */
  public void inviteUsers(
    List<BulkImportUserData> model,
    NodeRef igRef,
    Map<String, String> igProfiles,
    Boolean notify
  ) {
    for (BulkImportUserData user : model) {
      if (canInviteUser(user, igRef)) {
        inviteUserWithProfile(user, igRef, igProfiles, notify);
      }
    }
  }

  private boolean canInviteUser(BulkImportUserData user, NodeRef igRef) {
    return (
      user.getUser() != null &&
      user.getStatus().equals(BulkImportUserData.STATUS_OK) &&
      !this.circabcService.isUserMember(igRef, user.getUser().getUserName())
    );
  }

  private void inviteUserWithProfile(
    BulkImportUserData user,
    NodeRef igRef,
    Map<String, String> igProfiles,
    Boolean notify
  ) {
    try {
      if (!igProfiles.keySet().contains(user.getProfile())) {
        Map<String, String> newProfile = createNewAccessProfile(
          igRef,
          user.getProfile()
        );
        igProfiles.putAll(newProfile);
      }
      inviteUserToGroup(igRef, igProfiles, user, notify);
    } catch (ProfileException e) {
      logInviteError(user, e);
    }
  }

  private void logInviteError(BulkImportUserData user, ProfileException e) {
    if (logger.isErrorEnabled()) {
      logger.error(
        "BulkUserImport: Error during adding user: " +
          user.getUser().getUserName() +
          " to profile :" +
          user.getProfile(),
        e
      );
    }
  }

  /**
   * @param igRef
   * @param igProfiles
   * @param user
   * @param notify
   */
  private void inviteUserToGroup(
    NodeRef igRef,
    Map<String, String> igProfiles,
    BulkImportUserData user,
    Boolean notify
  ) {
    if (!personService.personExists(user.getUser().getUserName())) {
      userService.createUser(user.getUser(), true);
    }

    MembershipPostDefinition body = new MembershipPostDefinition();
    UserProfile membershipItem = new UserProfile();
    membershipItem.setUser(new User());
    membershipItem.setProfile(new io.swagger.model.Profile());

    Profile profile = profileService.getProfile(igRef, user.getProfile());

    membershipItem.getUser().setUserId(user.getUser().getUserName());
    membershipItem.getProfile().setName(igProfiles.get(user.getProfile()));
    if (profile != null) {
      membershipItem
        .getProfile()
        .setGroupName("GROUP_" + profile.getAlfrescoGroup());
    }

    body.addMembershipsItem(membershipItem);
    this.groupsApi.groupsIdMembersPost(igRef, body);

    if (Boolean.TRUE.equals(notify)) {
      Set<NotifiableUser> users = new HashSet<>();

      NodeRef person = personService.getPerson(user.getUser().getUserName());
      Map<QName, Serializable> properties = nodeService.getProperties(person);

      Serializable langObject = userService.getPreference(
        person,
        UserService.PREF_INTERFACE_LANGUAGE
      );

      Locale locale;

      if (langObject == null) {
        locale = null;
      } else if (langObject instanceof Locale loc) {
        locale = loc;
      } else {
        locale = Locale.of(langObject.toString());
      }

      users.add(new NotifiableUserImpl(person, locale, properties));

      try {
        notificationService.notify(
          igRef,
          users,
          NotificationType.NOTIFY_USER_INVITATION,
          null,
          null
        );
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "Error during notification of user: " +
              user.getUser().getUserName() +
              " in the bulk user importation.",
            e
          );
        }
      }
    }
  }

  private Map<String, String> createNewAccessProfile(
    NodeRef igRef,
    String profileTitle
  ) {
    Map<String, String> resultProfile = new HashMap<>();
    io.swagger.model.Profile body = new io.swagger.model.Profile();

    body.setImported(false);
    body.setExported(false);
    body.setName(profileTitle);
    body.setTitle(new I18nProperty("en", profileTitle));
    Map<String, String> permissions = new HashMap<>();

    permissions.put("newsgroups", NewsGroupPermissions.NWSNOACCESS.toString());
    permissions.put("events", EventPermissions.EVENOACCESS.toString());
    permissions.put(
      "information",
      InformationPermissions.INFNOACCESS.toString()
    );
    permissions.put("library", LibraryPermissions.LIBNOACCESS.toString());
    permissions.put("members", DirectoryPermissions.DIRNOACCESS.toString());
    permissions.put("visibility", VisibilityPermissions.VISIBILITY.toString());
    body.setPermissions(permissions);

    io.swagger.model.Profile newProfile = profilesApi.groupsIdProfilesPost(
      igRef,
      body
    );
    resultProfile.put(profileTitle, newProfile.getName());
    return resultProfile;
  }
}
