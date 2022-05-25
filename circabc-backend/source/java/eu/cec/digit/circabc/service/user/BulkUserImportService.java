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
package eu.cec.digit.circabc.service.user;

import eu.cec.digit.circabc.repo.user.InvalidBulkImportFileFormatException;
import eu.cec.digit.circabc.service.profile.Profile;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.List;
import java.util.Map;

/** @author beaurpi */
public interface BulkUserImportService {

    /**
     * List members of one Interest group
     *
     * @param igRef
     * @return
     */
    List<BulkImportUserData> listMembers(NodeRef igRef, Boolean igNameAsProfile);

    /**
     * to download current work
     *
     * @param model
     * @return XLS workbook
     */
    HSSFWorkbook saveWork(List<BulkImportUserData> model);

    /**
     * to download current work
     *
     * @param book
     * @param fileName
     * @return
     * @throws InvalidBulkImportFileFormatException
     */
    List<BulkImportUserData> loadWork(HSSFWorkbook book, String fileName)
            throws InvalidBulkImportFileFormatException;

    /**
     * Add list of user to model, this method simply check if user is not present twice. If yes only
     * first is taken in account
     *
     * @param model
     * @param newValues
     */
    void addAll(
            List<BulkImportUserData> model, List<BulkImportUserData> newValues, NodeRef currentIgRef);

    /**
     * This method should be called in order to update list of users. According to the to helpers, we
     * will create new access profiles in the target Interest Group.
     *
     * @param model
     * @param createIgProfileHelper
     * @param createDepartmentNumberProfileHelper
     * @param profilesToBeCreated
     */
    void parseProfilesToBeCreated(
            List<BulkImportUserData> model,
            Boolean createIgProfileHelper,
            Boolean createDepartmentNumberProfileHelper,
            List<String> profilesToBeCreated);

    /**
     * return the list of profile for the specified group
     *
     * @param igRef
     * @return
     */
    List<Profile> listGroupProfiles(NodeRef igRef);

    /**
     * final step of this interface, it will invite all people in the target group
     *
     * @param model
     * @param igRef
     * @param igProfiles
     */
    void inviteUsers(
            List<BulkImportUserData> model,
            NodeRef igRef,
            Map<String, String> igProfiles,
            Boolean notify);
}
