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

import io.swagger.exception.InvalidBulkImportFileFormatException;
import io.swagger.model.BulkImportUserData;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Service contract for the bulk user import workflow of an Interest Group (IG).
 *
 * <p>This service supports the end-to-end process of importing many users into an IG at once:
 * listing the current members, exporting the working set to an XLS spreadsheet, loading a
 * (possibly edited) spreadsheet back into the working model, merging additional user entries while
 * de-duplicating them, resolving the access profiles that must be created in the target IG, and
 * finally inviting all the collected users into the group.
 *
 * <p>The working data is manipulated as a mutable {@link List} of {@link BulkImportUserData} rows
 * that is typically round-tripped through an {@link HSSFWorkbook} so an administrator can edit it
 * offline before the users are invited.
 *
 * @author beaurpi
 */
public interface BulkUserImportService {
  /**
   * Lists the current members of a single Interest Group as bulk-import rows.
   *
   * @param igRef the {@link NodeRef} of the Interest Group whose members should be listed
   * @param igNameAsProfile when {@code true}, the IG name is used as the profile label for each
   *     returned member instead of the raw profile identifier
   * @return the list of {@link BulkImportUserData} rows representing the group's members
   */
  List<BulkImportUserData> listMembers(NodeRef igRef, Boolean igNameAsProfile);

  /**
   * Exports the current working model to an XLS workbook so it can be downloaded and edited offline.
   *
   * @param model the working list of {@link BulkImportUserData} rows to serialize
   * @return the generated {@link HSSFWorkbook} (XLS) containing the supplied rows
   */
  HSSFWorkbook saveWork(List<BulkImportUserData> model);

  /**
   * Loads a previously exported (and possibly edited) XLS workbook back into the working model.
   *
   * @param book the {@link HSSFWorkbook} to parse
   * @param fileName the original file name, used for validation and error reporting
   * @return the list of {@link BulkImportUserData} rows parsed from the workbook
   * @throws InvalidBulkImportFileFormatException if the workbook does not match the expected
   *     bulk-import format
   */
  List<BulkImportUserData> loadWork(HSSFWorkbook book, String fileName)
    throws InvalidBulkImportFileFormatException;

  /**
   * Merges a list of new user entries into the working model.
   *
   * <p>This method simply checks that a user is not present twice; if a user appears more than once,
   * only the first occurrence is taken into account.
   *
   * @param model the working list to merge the new values into (modified in place)
   * @param newValues the list of {@link BulkImportUserData} rows to add
   * @param currentIgRef the {@link NodeRef} of the Interest Group the entries are being added to
   */
  void addAll(
    List<BulkImportUserData> model,
    List<BulkImportUserData> newValues,
    NodeRef currentIgRef
  );

  /**
   * Determines which access profiles must be created in the target Interest Group before the users
   * can be invited, based on the working model and the supplied creation helpers.
   *
   * @param model the working list of {@link BulkImportUserData} rows to inspect
   * @param createIgProfileHelper when {@code true}, profiles derived from the IG are eligible for
   *     creation
   * @param createDepartmentNumberProfileHelper when {@code true}, profiles derived from the user
   *     department number are eligible for creation
   * @param profilesToBeCreated the list that will be populated with the identifiers of the profiles
   *     to be created (modified in place)
   */
  void parseProfilesToBeCreated(
    List<BulkImportUserData> model,
    Boolean createIgProfileHelper,
    Boolean createDepartmentNumberProfileHelper,
    List<String> profilesToBeCreated
  );

  /**
   * Returns the list of access profiles defined for the specified Interest Group.
   *
   * @param igRef the {@link NodeRef} of the Interest Group
   * @return the list of {@link io.swagger.model.db.Profile} entries for the group
   */
  List<io.swagger.model.db.Profile> listGroupProfiles(NodeRef igRef);

  /**
   * Final step of the workflow: invites all users in the working model into the target Interest
   * Group.
   *
   * @param model the working list of {@link BulkImportUserData} rows to invite
   * @param igRef the {@link NodeRef} of the target Interest Group
   * @param igProfiles a map associating each user's profile key with the target IG profile to assign
   * @param notify when {@code true}, invited users are notified (e.g. by email)
   */
  void inviteUsers(
    List<BulkImportUserData> model,
    NodeRef igRef,
    Map<String, String> igProfiles,
    Boolean notify
  );
}
