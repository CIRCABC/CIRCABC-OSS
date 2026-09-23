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
package eu.europa.ec.digit.circabc.rest.service.statistic.global;

import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgDescriptor;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service responsible for computing, persisting and retrieving CIRCABC-wide (global) usage
 * statistics as well as per-category and per-Interest-Group (IG) statistics.
 *
 * <p>The service gathers aggregated metrics about the platform (global stats and detailed IG
 * stats), renders them into Excel reports, and stores those reports in dedicated folders within
 * the Alfresco repository (typically under {@code dataDictionary/Circabc/statistics/reports/}). It
 * also exposes helpers to prepare and locate the report folders, list previously generated report
 * files, archive old reports, and query user login information.
 *
 * <p>Implementations interact with the Alfresco repository and therefore work with {@link NodeRef}
 * and {@link FileInfo} instances to reference and describe repository nodes.
 *
 * @author beaurpi
 */
public interface GlobalStatisticsService {
  /**
   * Prepares the repository folder where global statistics reports will be stored, creating it if
   * necessary. The target location is {@code dataDictionary/Circabc/statistics/reports/}.
   */
  void prepareFolderRecipient();

  /**
   * Computes the aggregated, platform-wide statistics.
   *
   * @return a map of statistic names to their computed values describing the global state of the
   *     platform
   */
  Map<String, Object> makeGlobalStats();

  /**
   * Renders the supplied global statistics into an Excel workbook and saves it into the given
   * destination folder.
   *
   * @param destinationFolder the repository folder in which the generated report is stored
   * @param lData the previously computed global statistics to write into the report
   * @return the {@link NodeRef} of the saved Excel report file
   */
  NodeRef saveStatsToExcel(
    NodeRef destinationFolder,
    Map<String, Object> lData
  );

  /**
   * Returns the repository folder used to store statistics reports.
   *
   * @return the {@link NodeRef} of the report save folder, or {@code null} if it does not exist
   */
  NodeRef getReportSaveFolder();

  /**
   * Verifies whether the report folder used to store statistics already exists.
   *
   * @return {@code true} if the report save folder exists, {@code false} otherwise
   */
  Boolean isReportSaveFolderExisting();

  /**
   * Retrieves the last login date recorded for the given user.
   *
   * @param username the username whose last login date is requested
   * @return the {@link Date} of the user's last login, or {@code null} if unknown
   */
  Date getLastLoginDateOfUser(String username);

  /**
   * Lists the statistics report files previously generated and stored in the report folder.
   *
   * @return a list of {@link FileInfo} describing the available report files
   */
  List<FileInfo> getListOfReportFiles();

  /**
   * Archives previously generated report files by zipping them and cleaning up the report folder.
   */
  void cleanAndZipPreviousReportFiles();

  /**
   * Computes detailed statistics for all Interest Groups (IGs) on the platform.
   *
   * @return a map of statistic names to their computed values describing detailed IG usage
   */
  Map<String, Object> makeDetailedIgStats();

  /**
   * Renders the supplied detailed Interest Group statistics into an Excel workbook and saves it
   * into the given folder.
   *
   * @param reportSaveFolder the repository folder in which the generated report is stored
   * @param igData the previously computed detailed IG statistics to write into the report
   * @return the {@link NodeRef} of the saved Excel report file
   */
  NodeRef saveDetailedIgStatsToExcel(
    NodeRef reportSaveFolder,
    Map<String, Object> igData
  );

  /**
   * Retrieves all Interest Groups defined in CIRCABC.
   *
   * @return a list of {@link NodeRef} pointing to every CIRCABC Interest Group
   */
  List<NodeRef> getListOfCircabcInterestGroups();

  /**
   * Lists the group statistics report files stored for the given category.
   *
   * @param categoryName the name of the category
   * @param categoryRef the {@link NodeRef} of the category node
   * @return a list of {@link FileInfo} describing the category's group statistics report files
   */
  List<FileInfo> getCategoryGroupStatsFiles(
    String categoryName,
    NodeRef categoryRef
  );

  /**
   * Prepares the repository folder that will hold the statistics reports for the given category,
   * creating it if necessary.
   *
   * @param categoryName the name of the category
   * @param categoryRef the {@link NodeRef} of the category node
   */
  void prepareCategoryReportsFolderRecipient(
    String categoryName,
    NodeRef categoryRef
  );

  /**
   * Returns the repository folder used to store the statistics reports of the given category.
   *
   * @param categoryName the name of the category
   * @return the {@link NodeRef} of the category's report folder
   */
  NodeRef getCategoryReportsFolderRecipient(String categoryName);

  /**
   * Computes the group statistics for every Interest Group belonging to the given category.
   *
   * @param categoryRef the {@link NodeRef} of the category node
   * @return a list of {@link IgDescriptor} holding the computed statistics for each Interest Group
   *     in the category
   */
  List<IgDescriptor> computeCategoryGroupStatistics(NodeRef categoryRef);

  /**
   * Renders the previously computed category group statistics into a report and saves it for the
   * given category.
   *
   * @param computedCategoryGroupStatistics the computed per-IG statistics to persist
   * @param categoryName the name of the category the statistics belong to
   * @return the {@link NodeRef} of the saved statistics report file
   */
  NodeRef saveCategoryGroupStatistics(
    List<IgDescriptor> computedCategoryGroupStatistics,
    String categoryName
  );
}
