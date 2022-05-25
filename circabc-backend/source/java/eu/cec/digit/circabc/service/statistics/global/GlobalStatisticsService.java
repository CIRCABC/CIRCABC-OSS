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
package eu.cec.digit.circabc.service.statistics.global;

import eu.cec.digit.circabc.repo.statistics.ig.IgDescriptor;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** @author beaurpi */
public interface GlobalStatisticsService {

    /**
     * prepare the space where reports will be stored dataDictionary/Circabc/statistics/reports/
     *
     * @throws Exception
     */
    void prepareFolderRecipient();

    /** @return list of data */
    Map<String, Object> makeGlobalStats();

    /**
     * make and save stats into the designed folder
     *
     * @param destinationFolder
     * @param lData
     * @return noderef of the saved file
     */
    NodeRef saveStatsToExcel(NodeRef destinationFolder, Map<String, Object> lData);

    /** */
    NodeRef getReportSaveFolder();

    /**
     * Verify if report folder for statistics exists
     *
     * @return
     */
    Boolean isReportSaveFolderExisting();

    /**
     * @param username
     * @return
     */
    Date getLastLoginDateOfUser(String username);

    /** @return Map<String, String> String: File Title, String, download link */
    List<FileInfo> getListOfReportFiles();

    /** look and zip files in report folder */
    void cleanAndZipPreviousReportFiles();

    /** report for more details about all IGs */
    Map<String, Object> makeDetailedIgStats();

    /**
     * *
     *
     * @param reportSaveFolder
     * @param makeDetailedIgStats
     * @return
     */
    NodeRef saveDetailedIgStatsToExcel(NodeRef reportSaveFolder, Map<String, Object> igData);

    List<NodeRef> getListOfCircabcInterestGroups();

    List<FileInfo> getCategoryGroupStatsFiles(String categoryName, NodeRef categoryRef);

    void prepareCategoryReportsFolderRecipient(String categoryName, NodeRef categoryRef);

    NodeRef getCategoryReportsFolderRecipient(String categoryName);

    List<IgDescriptor> computeCategoryGroupStatistics(NodeRef categoryRef);

    NodeRef saveCategoryGroupStatistics(
            List<IgDescriptor> computedCategoryGroupStatistics, String categoryName);
}
