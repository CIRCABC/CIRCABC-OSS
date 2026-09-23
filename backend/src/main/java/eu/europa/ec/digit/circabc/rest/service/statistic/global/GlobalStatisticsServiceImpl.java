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

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.compress.ZipService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.profile.permissions.CircabcServices;
import eu.europa.ec.digit.circabc.rest.service.report.ReportDaoService;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.CategoryDescriptor;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgDescriptor;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgStatisticsParameter;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgStatisticsService;
import io.swagger.api.CircabcApi;
import io.swagger.api.GroupsApi;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.CircabcRuntimeException;
import io.swagger.model.InterestGroup;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.LogCountResultDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link GlobalStatisticsService}.
 *
 * <p>Computes platform-wide (global) and category-scoped usage statistics for CIRCABC and persists
 * them as Excel ({@code .xls}) reports inside the Alfresco Data Dictionary. Typical figures
 * gathered include the number of category headers, categories, Interest Groups, users and
 * documents, the breakdown of Interest Groups per category, and per-Interest-Group details such as
 * membership counts, storage sizes, document/event/post counts and last access/update dates.
 *
 * <p>Reports are written under the Data Dictionary using well-known folder names
 * ({@code CircaBC/statistics/reports} for global reports and {@code category_statistics/&lt;category&gt;}
 * for category reports). File creation is executed within Alfresco retrying transactions and the
 * workbooks are built with Apache POI (HSSF). This service is primarily invoked by scheduled
 * statistics jobs and by the REST layer exposing statistics endpoints.
 *
 * @author beaurpi
 */
public class GlobalStatisticsServiceImpl implements GlobalStatisticsService {

  static final Log logger = LogFactory.getLog(
    GlobalStatisticsServiceImpl.class
  );
  /** Label used for the site name in detailed IG report rows. */
  private static final String CIRCABC_SITE = "CIRCABC Site";
  /** Date pattern used when formatting timestamps in category statistics. */
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  /** Message prefix used when reporting an invalid destination folder. */
  private static final String DESTINATION_NODE_REF = "destination noderef:";
  /** Message suffix used when the destination node is not a folder. */
  private static final String IS_NOT_A_VALID_FOLDER = " is not a valid folder";
  /** Placeholder value written in cells that are not applicable for a given row. */
  private static final String DASHES = "-----";
  /** Name of the top-level CIRCABC folder inside the Alfresco Data Dictionary. */
  private static String circabcDictionaryFolderName = "CircaBC";
  /** Name of the statistics folder holding job configuration and reports. */
  private static String statisticsDictionaryFolderName = "statistics";
  /** Name of the Data Dictionary folder holding per-category statistics reports. */
  private static String categoryStatisticsReportsDictionaryFolderName =
    "category_statistics";
  /** Name of the sub-folder where generated report files are stored. */
  private static String reportsDictionaryFolderName = "reports";
  /** File name of the statistics scheduled-job configuration properties. */
  private static String jobConfigFileName = "statsJobConfig.properties";
  /** Column headers used for the detailed list of Interest Groups sheet. */
  private static List<String> detailedListHeader = Arrays.asList(
    "Owner",
    "Content Type",
    "Owner's e-mail",
    "DG",
    "Request Date",
    "Workspace Title",
    "Workspace description",
    "Blog Short Description",
    "Justification",
    "Keywords",
    "Usage All",
    "Site Url",
    "Quota (MB)",
    "Release Date",
    "End Date",
    "Archived",
    "Deleted",
    "Visibility",
    "Full Site Url",
    "Target Audiences",
    "Functionalities",
    "Categories",
    "Site Languages",
    "Authentication Method (Blog)",
    "Authentication Method (Forum)",
    "Contact",
    "Related Workspaces",
    "Document Management",
    "Public Outputs"
  );

  @Autowired
  private NodeService nodeService;

  @Autowired
  private SearchService searchService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private PersonService personService;

  @Autowired
  private ReportDaoService reportDaoService;

  @Autowired
  private TransactionService transactionService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private LogService logService;

  @Autowired
  private ZipService zipService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private IgStatisticsService igStatisticsService;

  @Autowired
  @Qualifier("namespaceService")
  private NamespacePrefixResolver namespacePrefixResolver;

  @Autowired
  private CircabcConfig circabcConfig;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private GroupsApi groupsApi;

  @Autowired
  private CircabcService circabcService;

  private Integer getNumberOfCircabcHeaders() {
    return getListOfCircabcHeaders().size();
  }

  private List<NodeRef> getListOfCircabcHeaders() {
    NodeRef rootHeader = circabcApi.getRootCategoryHeader();
    List<ChildAssociationRef> lHeadersAssoc = nodeService.getChildAssocs(
      rootHeader
    );
    List<NodeRef> lHeadersNoderef = new ArrayList<>();
    for (ChildAssociationRef c : lHeadersAssoc) {
      lHeadersNoderef.add(c.getChildRef());
    }

    return lHeadersNoderef;
  }

  private Integer getNumberOfCircabcCategories() {
    return getListOfCircabcCategories().size();
  }

  private List<NodeRef> getListOfCircabcCategories() {
    return circabcService.getCategories();
  }

  private Integer getNumberOfCircabcInterestGroups() {
    return getListOfCircabcInterestGroups().size();
  }

  /**
   * Returns every Interest Group node in the platform, across all categories.
   *
   * @return the list of Interest Group root {@link NodeRef}s
   */
  public List<NodeRef> getListOfCircabcInterestGroups() {
    List<NodeRef> lIg = new ArrayList<>();
    for (NodeRef categ : getListOfCircabcCategories()) {
      for (ChildAssociationRef c : nodeService.getChildAssocs(categ)) {
        if (
          nodeService.hasAspect(c.getChildRef(), CircabcModel.ASPECT_IGROOT)
        ) {
          lIg.add(c.getChildRef());
        }
      }
    }
    return lIg;
  }

  private Integer getNumberOfUsers() {
    List<PersonService.PersonInfo> personInfos = personService
      .getPeople(null, null, null, new PagingRequest(Integer.MAX_VALUE, null))
      .getPage();
    return personInfos.size();
  }

  private Map<NodeRef, Integer> getNumberOfCircabcInterestGroupsPerCategory() {
    Map<NodeRef, Integer> nbIgPerCateg = new HashMap<>();
    Map<NodeRef, List<NodeRef>> lIgPerCateg =
      getListOfCircabcInterestGroupsPerCategory();
    for (Entry<NodeRef, List<NodeRef>> entry : lIgPerCateg.entrySet()) {
      nbIgPerCateg.put(entry.getKey(), entry.getValue().size());
    }

    return nbIgPerCateg;
  }

  private Map<
    NodeRef,
    List<NodeRef>
  > getListOfCircabcInterestGroupsPerCategory() {
    Map<NodeRef, List<NodeRef>> lIgPerCateg = new HashMap<>();
    for (NodeRef categ : getListOfCircabcCategories()) {
      lIgPerCateg.put(categ, getListOfCircabcInterestGroupsForCategory(categ));
    }
    return lIgPerCateg;
  }

  private List<NodeRef> getListOfCircabcInterestGroupsForCategory(
    NodeRef categNodeRef
  ) {
    List<NodeRef> lIg = new ArrayList<>();
    for (ChildAssociationRef c : nodeService.getChildAssocs(categNodeRef)) {
      if (nodeService.hasAspect(c.getChildRef(), CircabcModel.ASPECT_IGROOT)) {
        lIg.add(c.getChildRef());
      }
    }
    return lIg;
  }

  private Integer getNumberOfDocumentsInCircabc() {
    Integer nbDocs = 0;

    try {
      nbDocs = reportDaoService.queryDbForNumberOfDocuments();
    } catch (NumberFormatException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error during getting number of documents in CIRCABC:", e);
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error during SQL query for number of documents in CIRCABC:",
          e
        );
      }
    }

    return nbDocs;
  }

  /**
   * Ensures the folder structure that stores statistics reports exists in the Data Dictionary.
   *
   * <p>Creates {@code CircaBC/statistics}, its {@code reports} sub-folder and the job
   * configuration file when the statistics folder is missing, and restricts permissions so that
   * only invited users have contributor access.
   */
  public void prepareFolderRecipient() {
    NodeRef dicoNodeRef = getDicoNodeRef();

    NodeRef circabcFolderNodeRef = nodeService.getChildByName(
      dicoNodeRef,
      ContentModel.ASSOC_CONTAINS,
      circabcDictionaryFolderName
    );

    FileInfo statisticsFolder = null;

    if (
      nodeService.getChildByName(
        circabcFolderNodeRef,
        ContentModel.ASSOC_CONTAINS,
        statisticsDictionaryFolderName
      ) ==
      null
    ) {
      statisticsFolder = fileFolderService.create(
        circabcFolderNodeRef,
        statisticsDictionaryFolderName,
        ContentModel.TYPE_FOLDER
      );
      fileFolderService.create(
        statisticsFolder.getNodeRef(),
        reportsDictionaryFolderName,
        ContentModel.TYPE_FOLDER
      );
      fileFolderService.create(
        statisticsFolder.getNodeRef(),
        jobConfigFileName,
        ContentModel.TYPE_CONTENT
      );

      NodeRef statisticsFolderRef = nodeService.getChildByName(
        circabcFolderNodeRef,
        ContentModel.ASSOC_CONTAINS,
        statisticsDictionaryFolderName
      );

      if (statisticsFolderRef != null) {
        permissionService.setInheritParentPermissions(
          statisticsFolderRef,
          false
        );

        final String prefixedUserGroupName =
          circabcApi.getInvitedUsersGroupName();

        permissionService.setPermission(
          statisticsFolderRef,
          prefixedUserGroupName,
          PermissionService.CONTRIBUTOR,
          true
        );
      }
    }
  }

  /** @return */
  // NOSONAR: The XPath expression is a well-known Alfresco path that cannot be parameterized.
  // This is a standard Alfresco repository path, not a user-controlled URI.
  private NodeRef getDicoNodeRef() {
    String dicoPath = "/app:company_home/app:dictionary"; // NOSONAR

    List<NodeRef> nodes = searchService.selectNodes(
      nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE),
      dicoPath,
      null,
      namespacePrefixResolver,
      false
    );

    return nodes.get(0);
  }

  /**
   * Computes the set of global (platform-wide) statistics.
   *
   * <p>The returned map contains the number of category headers, categories, Interest Groups,
   * users and documents, the number of Interest Groups per category, the full CIRCABC structure
   * and the per-hour action counts for the previous day.
   *
   * @return a map of statistic name to computed value
   */
  public Map<String, Object> makeGlobalStats() {
    Map<String, Object> globalStats = new HashMap<>();

    Integer nbHeaders = getNumberOfCircabcHeaders();
    globalStats.put("numberOfCategoryHeaders", nbHeaders);

    Integer nbCategories = getNumberOfCircabcCategories();
    globalStats.put("numberOfCategories", nbCategories);

    Integer nbInterestGroups = getNumberOfCircabcInterestGroups();
    globalStats.put("numberOfIgs", nbInterestGroups);

    Map<NodeRef, Integer> nbIgPC =
      getNumberOfCircabcInterestGroupsPerCategory();
    Map<String, Integer> lIgPC = new HashMap<>();

    for (Entry<NodeRef, Integer> entry : nbIgPC.entrySet()) {
      lIgPC.put(
        getNameOrTitleOfNodeRefHelper(entry.getKey()),
        entry.getValue()
      );
    }
    globalStats.put("numberOfIgsPerCategory", lIgPC);

    Integer nbUsers = getNumberOfUsers();
    globalStats.put("numberOfUsers", nbUsers);

    Integer nbDocs = getNumberOfDocumentsInCircabc();
    globalStats.put("numberOfDocuments", nbDocs);

    globalStats.put("listOfCircabcStructure", getCircabcStructure(false));

    globalStats.put("actionCountForYesterDay", getCountOfActionsForYesterday());

    return globalStats;
  }

  /**
   * Builds an Excel workbook from the supplied global statistics and stores it in the given
   * folder, within a retrying transaction.
   *
   * @param destinationFolder the target folder node; must be of type {@code cm:folder}
   * @param lData the global statistics data, typically produced by {@link #makeGlobalStats()}
   * @return the {@link NodeRef} of the created report file
   * @throws CircabcRuntimeException if {@code destinationFolder} is not a valid folder
   */
  public NodeRef saveStatsToExcel(
    final NodeRef destinationFolder,
    Map<String, Object> lData
  ) {
    Calendar cFile = new GregorianCalendar();
    cFile.setTime(new Date());
    final String fileName =
      "StatisticReport" +
      cFile.get(Calendar.DAY_OF_MONTH) +
      "-" +
      (cFile.get(Calendar.MONTH) + 1) +
      "-" +
      cFile.get(Calendar.YEAR) +
      "-" +
      cFile.get(Calendar.HOUR_OF_DAY) +
      "-" +
      cFile.get(Calendar.MINUTE) +
      "-" +
      cFile.get(Calendar.MILLISECOND) +
      ".xls";

    if (
      nodeService.getType(destinationFolder).equals(ContentModel.TYPE_FOLDER)
    ) {
      final HSSFWorkbook wb = builtExcelFromData(lData);

      RetryingTransactionHelper helper =
        transactionService.getRetryingTransactionHelper();
      return helper.doInTransaction(
        new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
          public NodeRef execute() throws Throwable {
            return writeFile(destinationFolder, fileName, fileName, wb);
          }
        },
        false,
        true
      );
    } else {
      throw new CircabcRuntimeException(
        DESTINATION_NODE_REF +
          destinationFolder.toString() +
          IS_NOT_A_VALID_FOLDER
      );
    }
  }

  /**
   * @param lData
   * @return
   */
  private HSSFWorkbook builtExcelFromData(Map<String, Object> lData) {
    final HSSFWorkbook wb = new HSSFWorkbook();
    HSSFCellStyle numberCellStyle = createNumberCellStyle(wb);
    buildNumbersSheet(wb, lData);
    buildListsSheet(wb, lData, numberCellStyle);
    return wb;
  }

  private HSSFCellStyle createNumberCellStyle(HSSFWorkbook wb) {
    HSSFCellStyle style = wb.createCellStyle();
    style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
    return style;
  }

  private void buildNumbersSheet(HSSFWorkbook wb, Map<String, Object> lData) {
    HSSFSheet sheet = wb.createSheet("numbers");
    int iRow = 0;
    for (Entry<String, Object> entry : lData.entrySet()) {
      String key = entry.getKey();
      if (key.matches("list.*")) continue;
      iRow = writeNumberEntry(sheet, iRow, key, entry.getValue());
      iRow++;
    }
  }

  @SuppressWarnings("unchecked")
  private int writeNumberEntry(
    HSSFSheet sheet,
    int iRow,
    String key,
    Object value
  ) {
    if ("numberOfIgsPerCategory".equals(key)) {
      return writeMapEntry(sheet, iRow, key, (Map<String, Integer>) value);
    } else if ("actionCountForYesterDay".equals(key)) {
      return writeActionCountEntry(
        sheet,
        iRow,
        key,
        (List<LogCountResultDAO>) value
      );
    } else {
      HSSFRow row = sheet.createRow(iRow);
      row.createCell(0).setCellValue(new HSSFRichTextString(key));
      row.createCell(1).setCellValue((Integer) value);
      return iRow;
    }
  }

  private int writeMapEntry(
    HSSFSheet sheet,
    int iRow,
    String key,
    Map<String, Integer> map
  ) {
    sheet
      .createRow(iRow)
      .createCell(0)
      .setCellValue(new HSSFRichTextString(key));
    iRow++;
    for (Entry<String, Integer> item : map.entrySet()) {
      HSSFRow row = sheet.createRow(iRow);
      row.createCell(0).setCellValue(new HSSFRichTextString(item.getKey()));
      row.createCell(1).setCellValue(item.getValue());
      iRow++;
    }
    return iRow;
  }

  private int writeActionCountEntry(
    HSSFSheet sheet,
    int iRow,
    String key,
    List<LogCountResultDAO> logs
  ) {
    HSSFRow row = sheet.createRow(iRow);
    row.createCell(0).setCellValue(new HSSFRichTextString(key));
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(new Date());
    row
      .createCell(1)
      .setCellValue(
        new HSSFRichTextString(
          (gc.get(Calendar.DAY_OF_MONTH) - 1) +
            "-" +
            (gc.get(Calendar.MONTH) + 1) +
            "-" +
            gc.get(Calendar.YEAR)
        )
      );
    iRow++;
    for (LogCountResultDAO logEntry : logs) {
      HSSFRow row2 = sheet.createRow(iRow);
      row2
        .createCell(0)
        .setCellValue(
          new HSSFRichTextString(
            logEntry.getHourPeriod() +
              "h-" +
              (logEntry.getHourPeriod() + 1) +
              "h"
          )
        );
      row2.createCell(1).setCellValue(logEntry.getNumberOfActions());
      iRow++;
    }
    return iRow;
  }

  @SuppressWarnings("unchecked")
  private void buildListsSheet(
    HSSFWorkbook wb,
    Map<String, Object> lData,
    HSSFCellStyle numberCellStyle
  ) {
    HSSFSheet sheetLists = wb.createSheet("lists");
    createListsHeader(sheetLists);
    int iRow = 1;
    String rootUrl = circabcConfig.getWebRootUrl();

    for (CategoryDescriptor c : (List<CategoryDescriptor>) lData.get(
      "listOfCircabcStructure"
    )) {
      iRow = writeCategoryRow(sheetLists, iRow, c, rootUrl, numberCellStyle);
      for (IgDescriptor ig : c.getListOfIgs()) {
        iRow = writeIgRow(sheetLists, iRow, c, ig, rootUrl, numberCellStyle);
      }
    }
  }

  private void createListsHeader(HSSFSheet sheet) {
    HSSFRow row = sheet.createRow(0);
    String[] headers = {
      "Category",
      "IG Title",
      "IG Name",
      "Leaders",
      "Link",
      "Size (MB)",
      "Nb Members",
      "Nb IGs",
    };
    for (int i = 0; i < headers.length; i++) {
      row.createCell(i).setCellValue(new HSSFRichTextString(headers[i]));
    }
  }

  private int writeCategoryRow(
    HSSFSheet sheet,
    int iRow,
    CategoryDescriptor c,
    String rootUrl,
    HSSFCellStyle numberCellStyle
  ) {
    HSSFRow row = sheet.createRow(iRow);
    row
      .createCell(0)
      .setCellValue(
        new HSSFRichTextString(
          c.getTitle().isEmpty() ? c.getName() : c.getTitle()
        )
      );
    row.createCell(1).setCellValue(new HSSFRichTextString(DASHES));
    row.createCell(2).setCellValue(new HSSFRichTextString(DASHES));
    row
      .createCell(3)
      .setCellValue(
        new HSSFRichTextString(
          c.getListOfAdmins().toString().replace("[", "").replace("]", "")
        )
      );
    row
      .createCell(4)
      .setCellValue(
        new HSSFRichTextString(
          rootUrl +
            circabcConfig.getNewUiContext() +
            "category/" +
            c.getRef().getId() +
            "/details"
        )
      );

    int nbIgs = 0;
    int nbMembers = 0;
    double size = 0;
    for (IgDescriptor ig : c.getListOfIgs()) {
      nbIgs++;
      IgStatisticsParameter stats = igStatisticsService.buildStatsData(
        ig.getRef()
      );
      ig.setNbMembers(stats.getNbUsers());
      nbMembers += stats.getNbUsers();
      ig.setLibraryDocSize(stats.getLibrarySize() / Math.pow(1024, 2));
      ig.setInformationInfoSize(stats.getInformationSize() / Math.pow(1024, 2));
      size += getIgSize(ig);
    }
    HSSFCell sizeCell = row.createCell(5, CellType.NUMERIC);
    sizeCell.setCellValue(size);
    sizeCell.setCellStyle(numberCellStyle);
    row.createCell(6).setCellValue(nbMembers);
    row.createCell(7).setCellValue(nbIgs);
    return iRow + 1;
  }

  private int writeIgRow(
    HSSFSheet sheet,
    int iRow,
    CategoryDescriptor c,
    IgDescriptor ig,
    String rootUrl,
    HSSFCellStyle numberCellStyle
  ) {
    HSSFRow row = sheet.createRow(iRow);
    row
      .createCell(0)
      .setCellValue(
        new HSSFRichTextString(
          c.getTitle().isEmpty() ? c.getName() : c.getTitle()
        )
      );
    row
      .createCell(1)
      .setCellValue(
        new HSSFRichTextString(
          ig.getTitle().isEmpty() ? ig.getName() : ig.getTitle()
        )
      );
    row.createCell(2).setCellValue(new HSSFRichTextString(ig.getName()));
    row
      .createCell(3)
      .setCellValue(
        new HSSFRichTextString(
          ig.getSetOfLeaders().toString().replace("[", "").replace("]", "")
        )
      );
    row
      .createCell(4)
      .setCellValue(
        new HSSFRichTextString(
          rootUrl +
            circabcConfig.getNewUiContext() +
            "group/" +
            ig.getRef().getId()
        )
      );
    double size = getIgSize(ig);
    HSSFCell sizeCell = row.createCell(5, CellType.NUMERIC);
    sizeCell.setCellValue(size);
    sizeCell.setCellStyle(numberCellStyle);
    row.createCell(6).setCellValue(ig.getNbMembers());
    row.createCell(7).setCellValue(new HSSFRichTextString(DASHES));
    return iRow + 1;
  }

  private double getIgSize(IgDescriptor ig) {
    double lib = ig.getLibraryDocSize() != null ? ig.getLibraryDocSize() : 0;
    double info =
      ig.getInformationInfoSize() != null ? ig.getInformationInfoSize() : 0;
    return lib + info;
  }

  /**
   * @param lData
   * @return
   */
  @SuppressWarnings("unchecked")
  private HSSFWorkbook builtExcelFromDetailedData(Map<String, Object> lData) {
    final HSSFWorkbook wb = new HSSFWorkbook();
    HSSFSheet sheetNumbers = wb.createSheet("detailed list of IGs");

    List<CategoryDescriptor> lCateg = (List<CategoryDescriptor>) lData.get(
      "detailedIgList"
    );

    int iRow = createHeaderRow(wb, sheetNumbers);

    for (CategoryDescriptor categ : lCateg) {
      iRow = processCategory(sheetNumbers, categ, iRow);
    }

    return wb;
  }

  private int createHeaderRow(HSSFWorkbook wb, HSSFSheet sheet) {
    HSSFRow rowHeader = sheet.createRow(0);
    CellStyle style = wb.createCellStyle();
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setRotation((short) 90);

    int iCol = 0;
    for (String header : detailedListHeader) {
      HSSFCell cell = rowHeader.createCell(iCol++);
      cell.setCellValue(new HSSFRichTextString(header));
      cell.setCellStyle(style);
    }
    return 1;
  }

  private int processCategory(
    HSSFSheet sheet,
    CategoryDescriptor categ,
    int iRow
  ) {
    for (IgDescriptor ig : categ.getListOfIgs()) {
      if (Boolean.TRUE.equals(ig.getPublicVisibility())) {
        createIgRow(sheet, categ, ig, iRow++);
      }
    }
    return iRow;
  }

  private void createIgRow(
    HSSFSheet sheet,
    CategoryDescriptor categ,
    IgDescriptor ig,
    int iRow
  ) {
    HSSFRow row = sheet.createRow(iRow);

    row
      .createCell(0)
      .setCellValue(new HSSFRichTextString(ig.getSetOfLeaders().toString()));
    row.createCell(1).setCellValue(new HSSFRichTextString(CIRCABC_SITE));
    row
      .createCell(2)
      .setCellValue(new HSSFRichTextString(ig.getSetOfLeaders().toString()));
    row
      .createCell(3)
      .setCellValue(
        new HSSFRichTextString(
          !categ.getTitle().isEmpty() ? categ.getTitle() : categ.getName()
        )
      );
    row.createCell(4).setCellValue(ig.getCreationDate());
    row
      .createCell(5)
      .setCellValue(!ig.getTitle().isEmpty() ? ig.getTitle() : ig.getName());
    row
      .createCell(6)
      .setCellValue(
        ig.getLightDescription() != null ? ig.getLightDescription() : ""
      );

    String url = buildIgUrl(ig);
    row.createCell(11).setCellValue(url);
    row.createCell(13).setCellValue(ig.getCreationDate());
    row.createCell(18).setCellValue(url);
    row.createCell(20).setCellValue(ig.getAvailableServices().toString());
    row.createCell(28).setCellValue(getVisibilityLabel(ig));
  }

  private String buildIgUrl(IgDescriptor ig) {
    String rootUrl = circabcConfig.getWebRootUrl();
    String separator = rootUrl.endsWith("/") ? "" : "/";
    return rootUrl + separator + "w/browse/" + ig.getRef().getId();
  }

  private String getVisibilityLabel(IgDescriptor ig) {
    if (Boolean.TRUE.equals(ig.getPublicEnabled())) {
      return "Visible to public";
    }
    if (
      Boolean.FALSE.equals(ig.getPublicEnabled()) &&
      Boolean.TRUE.equals(ig.getRegisteredEnabled())
    ) {
      return "Visible to registered users";
    }
    return "";
  }

  /**
   * title if exists, name if not
   *
   * @param node
   * @return
   */
  private String getNameOrTitleOfNodeRefHelper(NodeRef node) {
    String title = "";
    if (nodeService.getProperties(node).containsKey(ContentModel.PROP_TITLE)) {
      title = nodeService.getProperty(node, ContentModel.PROP_TITLE).toString();
    }
    return (
      !title.isEmpty()
        ? title
        : nodeService.getProperty(node, ContentModel.PROP_NAME).toString()
    );
  }

  /**
   * title if exists, name if not
   *
   * @param nodes list of nodes
   * @return
   */
  @SuppressWarnings("unused")
  private List<String> getListOfNameFromListOfNodeRefHelper(
    List<NodeRef> nodes
  ) {
    List<String> lNames = new ArrayList<>();

    for (NodeRef n : nodes) {
      lNames.add(getNameOrTitleOfNodeRefHelper(n));
    }

    return lNames;
  }

  /** @return the transactionService */
  public TransactionService getTransactionService() {
    return transactionService;
  }

  /** @param transactionService the transactionService to set */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /** @return the contentService */
  public ContentService getContentService() {
    return contentService;
  }

  /** @param contentService the contentService to set */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Resolves the Data Dictionary folder where global statistics reports are stored.
   *
   * @return the {@code CircaBC/statistics/reports} folder {@link NodeRef}, or {@code null} if the
   *     statistics folder does not exist
   */
  public NodeRef getReportSaveFolder() {
    NodeRef dicoNodeRef = getDicoNodeRef();

    NodeRef circabcFolderNodeRef = nodeService.getChildByName(
      dicoNodeRef,
      ContentModel.ASSOC_CONTAINS,
      circabcDictionaryFolderName
    );

    NodeRef statFolderNodeRef = nodeService.getChildByName(
      circabcFolderNodeRef,
      ContentModel.ASSOC_CONTAINS,
      statisticsDictionaryFolderName
    );

    NodeRef reportNode = null;

    if (statFolderNodeRef != null) {
      reportNode = nodeService.getChildByName(
        statFolderNodeRef,
        ContentModel.ASSOC_CONTAINS,
        reportsDictionaryFolderName
      );
    }

    return reportNode;
  }

  private Set<String> getListOfLeadersForInterestGroup(NodeRef igNodeRef) {
    return new HashSet<>(circabcService.getInterestGroupAdminEmails(igNodeRef));
  }

  private List<CategoryDescriptor> getCircabcStructure(Boolean detailed) {
    List<CategoryDescriptor> lCategs = new ArrayList<>();
    for (NodeRef nCateg : getListOfCircabcCategories()) {
      lCategs.add(buildCategoryDescriptor(nCateg, detailed));
    }
    return lCategs;
  }

  private CategoryDescriptor buildCategoryDescriptor(
    NodeRef nCateg,
    Boolean detailed
  ) {
    CategoryDescriptor c = new CategoryDescriptor();
    c.setRef(nCateg);
    c.setName(
      nodeService.getProperty(nCateg, ContentModel.PROP_NAME).toString()
    );
    c.setTitle(getPropertyOrEmpty(nCateg, ContentModel.PROP_TITLE));
    c.setListOfAdmins(getListOfAdminsForCategory(nCateg));
    c.setListOfIgs(buildIgDescriptorList(nCateg, detailed));
    return c;
  }

  private List<IgDescriptor> buildIgDescriptorList(
    NodeRef nCateg,
    Boolean detailed
  ) {
    List<IgDescriptor> lIgDesc = new ArrayList<>();
    for (NodeRef nIg : getListOfCircabcInterestGroupsForCategory(nCateg)) {
      lIgDesc.add(buildIgDescriptor(nIg, detailed));
    }
    return lIgDesc;
  }

  private IgDescriptor buildIgDescriptor(NodeRef nIg, Boolean detailed) {
    IgDescriptor ig = new IgDescriptor();
    ig.setRef(nIg);
    ig.setName(nodeService.getProperty(nIg, ContentModel.PROP_NAME).toString());
    ig.setTitle(getPropertyOrEmpty(nIg, ContentModel.PROP_TITLE));
    ig.setSetOfLeaders(getListOfLeadersForInterestGroup(nIg));

    if (Boolean.TRUE.equals(detailed)) {
      populateDetailedIgInfo(ig, nIg);
    }
    return ig;
  }

  private void populateDetailedIgInfo(IgDescriptor ig, NodeRef nIg) {
    ig.setDescription(getPropertyOrEmpty(nIg, ContentModel.PROP_DESCRIPTION));
    ig.setLightDescription(
      getPropertyOrEmpty(nIg, CircabcModel.PROP_LIGHT_DESCRIPTION)
    );
    ig.setCreationDate(formatCreationDate(nIg));

    InterestGroup group = groupsApi.getInterestGroupDetails(nIg);
    ig.setPublicVisibility(group.getIsPublic());
    ig.setPublicEnabled(group.getAllowApply());
    ig.setRegisteredEnabled(group.getIsRegistered());
    ig.setAvailableServices(getDefaultServices());
  }

  private String getPropertyOrEmpty(NodeRef nodeRef, QName property) {
    if (nodeService.getProperties(nodeRef).containsKey(property)) {
      Object value = nodeService.getProperty(nodeRef, property);
      return value != null ? value.toString() : "";
    }
    return "";
  }

  private String formatCreationDate(NodeRef nIg) {
    if (nodeService.getProperties(nIg).containsKey(ContentModel.PROP_CREATED)) {
      Date d = (Date) nodeService.getProperty(nIg, ContentModel.PROP_CREATED);
      return new SimpleDateFormat("dd/MM/yyyy").format(d);
    }
    return "";
  }

  private Set<String> getDefaultServices() {
    Set<String> services = new HashSet<>();
    services.add(CircabcServices.INFORMATION.toString());
    services.add(CircabcServices.LIBRARY.toString());
    services.add(CircabcServices.DIRECTORY.toString());
    services.add(CircabcServices.NEWSGROUP.toString());
    services.add(CircabcServices.EVENT.toString());
    return services;
  }

  private Set<String> getListOfAdminsForCategory(NodeRef categoryNodeRef) {
    return new HashSet<>(
      circabcService.getCategoryAdminEmails(categoryNodeRef)
    );
  }

  /**
   * Returns the last login date recorded for the given user.
   *
   * @param username the user login name
   * @return the last login {@link Date}, or {@code null} if none is recorded
   */
  public Date getLastLoginDateOfUser(String username) {
    return logService.getLastLoginDateOfUser(username);
  }

  /**
   * Lists the report files currently stored in the global statistics reports folder.
   *
   * @return the list of report {@link FileInfo} entries
   */
  public List<FileInfo> getListOfReportFiles() {
    NodeRef folderNodeRef = getReportSaveFolder();

    return fileFolderService.listFiles(folderNodeRef);
  }

  private List<LogCountResultDAO> getCountOfActionsForYesterday() {
    List<LogCountResultDAO> listOfActionsCompleted = new ArrayList<>();
    List<LogCountResultDAO> listOfActionsFromDB =
      logService.getNumberOfActionsYesterdayPerHour();

    /*
     * Fill new list to have all hours in a day
     */
    for (int i = 0; i < 24; i++) {
      LogCountResultDAO currentCount = new LogCountResultDAO();
      currentCount.setHourPeriod(i);
      currentCount.setNumberOfActions(0);

      for (LogCountResultDAO lCount : listOfActionsFromDB) {
        if (lCount.getHourPeriod() == i) {
          currentCount.setNumberOfActions(lCount.getNumberOfActions());
        }
      }

      listOfActionsCompleted.add(currentCount);
    }

    return listOfActionsCompleted;
  }

  /**
   * Archives previously generated global report files into a single ZIP.
   *
   * <p>All {@code *.xls} files found in the reports folder are added to a temporary ZIP archive,
   * the original files are deleted, and the archive is stored back into the reports folder within
   * a retrying transaction. The temporary file is removed afterwards.
   */
  public void cleanAndZipPreviousReportFiles() {
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(new Date());
    final String archiveName =
      "archive-" +
      gc.get(Calendar.DAY_OF_MONTH) +
      "-" +
      (gc.get(Calendar.MONTH) + 1) +
      "-" +
      gc.get(Calendar.YEAR) +
      "-" +
      gc.get(Calendar.MILLISECOND) +
      ".zip";
    final NodeRef reportFolder = getReportSaveFolder();
    final File tempZipFile = TempFileProvider.createTempFile(
      archiveName,
      ".tmp",
      TempFileProvider.getTempDir()
    );

    @SuppressWarnings("deprecation")
    List<FileInfo> listOfReports = fileFolderService.search(
      reportFolder,
      "*.xls",
      true,
      false,
      false
    );
    List<NodeRef> listOfReportsNodeRefs = new ArrayList<>();

    for (FileInfo file : listOfReports) {
      listOfReportsNodeRefs.add(file.getNodeRef());
    }

    zipService.addingFileIntoArchive(listOfReportsNodeRefs, tempZipFile);

    for (FileInfo file : listOfReports) {
      fileFolderService.delete(file.getNodeRef());
    }

    RetryingTransactionHelper helper =
      transactionService.getRetryingTransactionHelper();
    helper.doInTransaction(
      new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
        public Void execute() throws Throwable {
          NodeRef node = nodeService.getChildByName(
            reportFolder,
            ContentModel.ASSOC_CONTAINS,
            archiveName
          );

          if (node == null) {
            final NodeRef createdNodeRef = fileFolderService
              .create(reportFolder, archiveName, ContentModel.TYPE_CONTENT)
              .getNodeRef();

            // get a writer for the content and put the file
            final ContentWriter writer = contentService.getWriter(
              createdNodeRef,
              ContentModel.PROP_CONTENT,
              true
            );

            writer.setMimetype(MimetypeMap.MIMETYPE_ZIP);

            writer.putContent(tempZipFile);
          }

          return null;
        }
      },
      false,
      true
    );

    try {
      java.nio.file.Files.delete(tempZipFile.toPath());
    } catch (IOException e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Unable to delete file : " + tempZipFile.getPath(), e);
      }
    }
  }

  /** @return the zipService */
  public ZipService getZipService() {
    return zipService;
  }

  /** @param zipService the zipService to set */
  public void setZipService(ZipService zipService) {
    this.zipService = zipService;
  }

  /**
   * Indicates whether the global statistics reports folder exists.
   *
   * @return {@code true} if the reports folder exists, {@code false} otherwise
   */
  public Boolean isReportSaveFolderExisting() {
    Boolean result = false;
    if (getReportSaveFolder() != null) {
      result = true;
    }
    return result;
  }

  /**
   * Computes the detailed per-Interest-Group statistics for the whole platform.
   *
   * @return a map containing the detailed CIRCABC structure under the {@code detailedIgList} key
   */
  @Override
  public Map<String, Object> makeDetailedIgStats() {
    Map<String, Object> globalStats = new HashMap<>();
    globalStats.put("detailedIgList", getCircabcStructure(true));

    return globalStats;
  }

  /**
   * Builds an Excel workbook from the detailed Interest Group statistics and stores it in the
   * given folder, within a retrying transaction.
   *
   * @param reportSaveFolder the target folder node; must be of type {@code cm:folder}
   * @param igData the detailed statistics data, typically produced by {@link #makeDetailedIgStats()}
   * @return the {@link NodeRef} of the created report file
   * @throws CircabcRuntimeException if {@code reportSaveFolder} is not a valid folder
   */
  @Override
  public NodeRef saveDetailedIgStatsToExcel(
    final NodeRef reportSaveFolder,
    Map<String, Object> igData
  ) {
    Calendar cFile = new GregorianCalendar();
    cFile.setTime(new Date());
    final String fileName =
      "DetailsIgListReport" +
      cFile.get(Calendar.DAY_OF_MONTH) +
      "-" +
      (cFile.get(Calendar.MONTH) + 1) +
      "-" +
      cFile.get(Calendar.YEAR) +
      "-" +
      cFile.get(Calendar.HOUR_OF_DAY) +
      "-" +
      cFile.get(Calendar.MINUTE) +
      "-" +
      cFile.get(Calendar.MILLISECOND) +
      ".xls";

    if (
      nodeService.getType(reportSaveFolder).equals(ContentModel.TYPE_FOLDER)
    ) {
      final HSSFWorkbook wb = builtExcelFromDetailedData(igData);

      RetryingTransactionHelper helper =
        transactionService.getRetryingTransactionHelper();
      return helper.doInTransaction(
        new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
          public NodeRef execute() throws Throwable {
            return writeFile(reportSaveFolder, fileName, fileName, wb);
          }
        },
        false,
        true
      );
    } else {
      throw new CircabcRuntimeException(
        DESTINATION_NODE_REF +
          reportSaveFolder.toString() +
          IS_NOT_A_VALID_FOLDER
      );
    }
  }

  private NodeRef writeFile(
    NodeRef reportSaveFolder,
    String fileName,
    String title,
    HSSFWorkbook wb
  ) throws IOException {
    NodeRef node = nodeService.getChildByName(
      reportSaveFolder,
      ContentModel.ASSOC_CONTAINS,
      fileName
    );

    if (node == null) {
      final NodeRef createdNodeRef = fileFolderService
        .create(reportSaveFolder, fileName, ContentModel.TYPE_CONTENT)
        .getNodeRef();
      nodeService.setProperty(createdNodeRef, ContentModel.PROP_TITLE, title);
      File tempFile = TempFileProvider.createTempFile(fileName, "tmp");
      try (FileOutputStream fileWriter = new FileOutputStream(tempFile)) {
        wb.write(fileWriter);
      }

      // get a writer for the content and put the file
      final ContentWriter writer = contentService.getWriter(
        createdNodeRef,
        ContentModel.PROP_CONTENT,
        true
      );

      writer.setMimetype(MimetypeMap.MIMETYPE_EXCEL);

      writer.putContent(tempFile);

      try {
        java.nio.file.Files.delete(tempFile.toPath());
      } catch (IOException e) {
        if (logger.isWarnEnabled()) {
          logger.warn("Unable to delete file : " + tempFile.getPath(), e);
        }
      }

      node = createdNodeRef;
    }

    return node;
  }

  /**
   * Lists the statistics report files stored for a given category.
   *
   * <p>The lookup is performed as the Alfresco administrator, since the caller may not have direct
   * read access to the category statistics folder; the original authentication is restored before
   * returning.
   *
   * @param categoryName the category folder name under {@code category_statistics}
   * @param categoryRef the {@link NodeRef} of the category, used to prepare the folder if missing
   * @return the list of report {@link FileInfo} entries for the category
   */
  @Override
  public List<FileInfo> getCategoryGroupStatsFiles(
    String categoryName,
    NodeRef categoryRef
  ) {
    NodeRef folderNodeRef = getCategoryReportSaveFolder(
      categoryName,
      categoryRef
    );
    //We need to be alfresco admin to call the method
    String username = AuthenticationUtil.getFullyAuthenticatedUser();
    AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    List<FileInfo> results = fileFolderService.listFiles(folderNodeRef);
    AuthenticationUtil.setFullyAuthenticatedUser(username);
    return results;
  }

  private NodeRef getCategoryReportSaveFolder(
    String categoryName,
    NodeRef categoryRef
  ) {
    prepareCategoryReportsFolderRecipient(categoryName, categoryRef);
    return getCategoryReportsFolderRecipient(categoryName);
  }

  /**
   * Ensures the Data Dictionary folder structure for a category's statistics reports exists.
   *
   * <p>Runs as the Alfresco administrator to create the {@code category_statistics} folder (if
   * absent) and the per-category sub-folder, disabling permission inheritance and granting
   * contributor access to the category's invited-user group. The original authentication is
   * restored before returning.
   *
   * @param categoryName the category folder name to create/use
   * @param categoryRef the {@link NodeRef} of the category, used to resolve its invited-user group
   */
  public void prepareCategoryReportsFolderRecipient(
    String categoryName,
    NodeRef categoryRef
  ) {
    String username = AuthenticationUtil.getFullyAuthenticatedUser();
    AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

    NodeRef dicoNodeRef = getDicoNodeRef();

    NodeRef circabcFolderNodeRef = nodeService.getChildByName(
      dicoNodeRef,
      ContentModel.ASSOC_CONTAINS,
      circabcDictionaryFolderName
    );

    if (
      nodeService.getChildByName(
        circabcFolderNodeRef,
        ContentModel.ASSOC_CONTAINS,
        categoryStatisticsReportsDictionaryFolderName
      ) ==
      null
    ) {
      FileInfo statistics = fileFolderService.create(
        circabcFolderNodeRef,
        categoryStatisticsReportsDictionaryFolderName,
        ContentModel.TYPE_FOLDER
      );
      permissionService.setInheritParentPermissions(
        statistics.getNodeRef(),
        false
      );
    }

    NodeRef categStatFolder = nodeService.getChildByName(
      circabcFolderNodeRef,
      ContentModel.ASSOC_CONTAINS,
      categoryStatisticsReportsDictionaryFolderName
    );

    NodeRef statFolder = nodeService.getChildByName(
      categStatFolder,
      ContentModel.ASSOC_CONTAINS,
      categoryName
    );

    if (statFolder == null) {
      statFolder = fileFolderService
        .create(categStatFolder, categoryName, ContentModel.TYPE_FOLDER)
        .getNodeRef();
    }

    if (permissionService.getInheritParentPermissions(statFolder)) {
      permissionService.setInheritParentPermissions(statFolder, false);
      String authority =
        PermissionService.GROUP_PREFIX +
        nodeService
          .getProperty(
            categoryRef,
            CircabcModel.PROP_CATEGORY_INVITED_USER_GROUP
          )
          .toString();

      String permission = PermissionService.CONTRIBUTOR;
      permissionService.setPermission(statFolder, authority, permission, true);
    }

    AuthenticationUtil.setFullyAuthenticatedUser(username);
  }

  /**
   * Resolves the Data Dictionary folder holding a given category's statistics reports.
   *
   * @param categoryName the category folder name under {@code category_statistics}
   * @return the category reports folder {@link NodeRef}, or {@code null} if it does not exist
   */
  public NodeRef getCategoryReportsFolderRecipient(String categoryName) {
    NodeRef dicoNodeRef = getDicoNodeRef();

    NodeRef circabcFolderNodeRef = nodeService.getChildByName(
      dicoNodeRef,
      ContentModel.ASSOC_CONTAINS,
      circabcDictionaryFolderName
    );

    NodeRef categStatFolder = nodeService.getChildByName(
      circabcFolderNodeRef,
      ContentModel.ASSOC_CONTAINS,
      categoryStatisticsReportsDictionaryFolderName
    );

    return nodeService.getChildByName(
      categStatFolder,
      ContentModel.ASSOC_CONTAINS,
      categoryName
    );
  }

  /**
   * Lists the report files stored for a given category.
   *
   * @param categoryName the category folder name under {@code category_statistics}
   * @return the list of report {@link FileInfo} entries for the category
   */
  public List<FileInfo> getListOfCategoryReportFiles(String categoryName) {
    NodeRef folderNodeRef = getCategoryReportsFolderRecipient(categoryName);

    return fileFolderService.listFiles(folderNodeRef);
  }

  /**
   * Computes detailed statistics for every Interest Group belonging to the given category.
   *
   * <p>For each Interest Group this gathers title, name, leaders, creation date, last access and
   * update dates, document/event/post counts, membership count, storage sizes, folder depth and
   * contact information.
   *
   * @param categoryRef the {@link NodeRef} of the category whose Interest Groups are analysed
   * @return the list of computed {@link IgDescriptor}s, one per Interest Group
   */
  public List<IgDescriptor> computeCategoryGroupStatistics(
    NodeRef categoryRef
  ) {
    List<NodeRef> lGroups = getListOfCircabcInterestGroupsForCategory(
      categoryRef
    );
    List<IgDescriptor> results = new ArrayList<>();
    Integer i = 1;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

    for (NodeRef nIg : lGroups) {
      logger.info(
        "CategoryStatisticsJob: " +
          categoryRef.getId() +
          " Started IG: " +
          i +
          "/" +
          lGroups.size()
      );
      IgDescriptor ig = new IgDescriptor();
      ig.setRef(nIg);
      ig.setName(
        nodeService.getProperty(nIg, ContentModel.PROP_NAME).toString()
      );
      ig.setTitle(
        (nodeService.getProperties(nIg).containsKey(ContentModel.PROP_TITLE)
          ? nodeService.getProperty(nIg, ContentModel.PROP_TITLE).toString()
          : "")
      );
      ig.setSetOfLeaders(getListOfLeadersForInterestGroup(nIg));
      Serializable propertyCreated = nodeService.getProperty(
        nIg,
        ContentModel.PROP_CREATED
      );
      Date created = DefaultTypeConverter.INSTANCE.convert(
        Date.class,
        propertyCreated
      );
      ig.setCreationDate(simpleDateFormat.format(created));

      ig.setLastAccessDate(findLastAccessToIg(nIg, simpleDateFormat));
      ig.setLastUpdateDate(findLastUpdateToIg(nIg, simpleDateFormat));
      IgStatisticsParameter statistics = igStatisticsService.buildStatsData(
        nIg
      );
      ig.setNbDocuments(statistics.getLibraryDocumentCount());
      ig.setNbMembers(statistics.getNbUsers());

      ig.setNbEvents(statistics.getEventCount() + statistics.getMeetingCount());
      ig.setNbPosts(statistics.getPostCount());
      ig.setLibraryDocSize(statistics.getLibrarySize() / Math.pow(1024, 2));
      ig.setInformationInfoSize(
        statistics.getInformationSize() / Math.pow(1024, 2)
      );
      ig.setDeepness(statistics.getMaxLevel());

      ig.setContactInformation(
        nodeService
          .getProperties(nIg)
          .containsKey(CircabcModel.PROP_CONTACT_INFORMATION)
          ? nodeService
              .getProperty(nIg, CircabcModel.PROP_CONTACT_INFORMATION)
              .toString()
          : ""
      );

      results.add(ig);
      logger.info(
        "CategoryStatisticsJob: " +
          categoryRef.getId() +
          " Finished IG: " +
          i +
          "/" +
          lGroups.size()
      );
      i++;
    }

    return results;
  }

  private String findLastUpdateToIg(NodeRef nIg, SimpleDateFormat sdf) {
    Long igDbId = (Long) nodeService.getProperty(
      nIg,
      ContentModel.PROP_NODE_DBID
    );
    Date dResult = null;

    if (igDbId != null) {
      dResult = logService.getLastUpdateOnInterestGroup(igDbId);
    }

    if (dResult != null) {
      return sdf.format(dResult);
    } else {
      return "";
    }
  }

  private String findLastAccessToIg(NodeRef nIg, SimpleDateFormat sdf) {
    Long igDbId = (Long) nodeService.getProperty(
      nIg,
      ContentModel.PROP_NODE_DBID
    );
    Date dResult = null;

    if (igDbId != null) {
      dResult = logService.getLastAccessOnInterestGroup(igDbId);
    }

    if (dResult != null) {
      return sdf.format(dResult);
    } else {
      return "";
    }
  }

  /** @return the igStatisticsService */
  public IgStatisticsService getIgStatisticsService() {
    return igStatisticsService;
  }

  /** @param igStatisticsService the igStatisticsService to set */
  public void setIgStatisticsService(IgStatisticsService igStatisticsService) {
    this.igStatisticsService = igStatisticsService;
  }

  /**
   * Builds an Excel workbook from pre-computed category statistics and stores it in the category's
   * reports folder, within a retrying transaction.
   *
   * <p>The workbook includes one row per Interest Group plus a totals row that sums the numeric
   * columns via spreadsheet formulas.
   *
   * @param computedCategoryGroupStatistics the per-Interest-Group statistics to persist, typically
   *     produced by {@link #computeCategoryGroupStatistics(NodeRef)}
   * @param categoryName the category folder name used to resolve the destination folder and to
   *     prefix the generated file name
   * @return the {@link NodeRef} of the created report file
   * @throws CircabcRuntimeException if the resolved destination folder is not a valid folder
   */
  @Override
  public NodeRef saveCategoryGroupStatistics(
    List<IgDescriptor> computedCategoryGroupStatistics,
    String categoryName
  ) {
    Calendar cFile = new GregorianCalendar();
    cFile.setTime(new Date());
    final String fileName =
      (categoryName != null ? categoryName + "_" : "") +
      "CategoryReport" +
      cFile.get(Calendar.DAY_OF_MONTH) +
      "-" +
      (cFile.get(Calendar.MONTH) + 1) +
      "-" +
      cFile.get(Calendar.YEAR) +
      "-" +
      cFile.get(Calendar.HOUR_OF_DAY) +
      "-" +
      cFile.get(Calendar.MINUTE) +
      "-" +
      cFile.get(Calendar.MILLISECOND) +
      ".xls";

    final NodeRef destinationFolder = getCategoryReportsFolderRecipient(
      categoryName
    );

    if (
      nodeService.getType(destinationFolder).equals(ContentModel.TYPE_FOLDER)
    ) {
      final HSSFWorkbook wb = builtCategoryExcelFromData(
        computedCategoryGroupStatistics
      );

      RetryingTransactionHelper helper =
        transactionService.getRetryingTransactionHelper();
      return helper.doInTransaction(
        new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
          public NodeRef execute() throws Throwable {
            return writeFile(destinationFolder, fileName, fileName, wb);
          }
        },
        false,
        true
      );
    } else {
      throw new CircabcRuntimeException(
        DESTINATION_NODE_REF +
          destinationFolder.toString() +
          IS_NOT_A_VALID_FOLDER
      );
    }
  }

  private HSSFWorkbook builtCategoryExcelFromData(
    List<IgDescriptor> computedCategoryGroupStatistics
  ) {
    final HSSFWorkbook wb = new HSSFWorkbook();

    HSSFCellStyle numberCellStyle = wb.createCellStyle();
    HSSFDataFormat dataFormat = wb.createDataFormat();
    numberCellStyle.setDataFormat(dataFormat.getFormat("0.00"));

    HSSFSheet sheetNumbers = wb.createSheet("ig-statistics");

    Integer iRow = 0;

    HSSFRow row = sheetNumbers.createRow(iRow);
    String[] headers = {
      "IG Title",
      "IG Name",
      "Link",
      "Creation date",
      "Last accessed",
      "Last updated",
      "Nb documents",
      "Size of documents (Mb)",
      "Size of information files (Mb)",
      "Nb events/meetings",
      "Nb posts",
      "Deepness",
      "Nb Members",
      "Leaders",
      "Contact Information",
    };
    for (int i = 0; i < headers.length; i++) {
      row.createCell(i).setCellValue(headers[i]);
    }

    iRow++;

    String rootUrl = circabcConfig.getWebRootUrl();

    for (IgDescriptor ig : computedCategoryGroupStatistics) {
      HSSFRow rowTmp = sheetNumbers.createRow(iRow);

      //if the title is empty, we put take the name instead
      rowTmp
        .createCell(0)
        .setCellValue(ig.getTitle().equals("") ? ig.getName() : ig.getTitle());
      rowTmp.createCell(1).setCellValue(ig.getName());
      rowTmp
        .createCell(2)
        .setCellValue(rootUrl + "/ui/group/" + ig.getRef().getId());
      rowTmp.createCell(3).setCellValue(ig.getCreationDate());
      rowTmp.createCell(4).setCellValue(ig.getLastAccessDate());
      rowTmp.createCell(5).setCellValue(ig.getLastUpdateDate());
      rowTmp.createCell(6).setCellValue(ig.getNbDocuments());
      rowTmp.createCell(7).setCellValue(ig.getLibraryDocSize());
      rowTmp.getCell(7).setCellStyle(numberCellStyle);
      rowTmp.createCell(8).setCellValue(ig.getInformationInfoSize());
      rowTmp.getCell(8).setCellStyle(numberCellStyle);
      rowTmp.createCell(9).setCellValue(ig.getNbEvents());
      rowTmp.createCell(10).setCellValue(ig.getNbPosts());
      rowTmp.createCell(11).setCellValue(ig.getDeepness());
      rowTmp.createCell(12).setCellValue(ig.getNbMembers());
      rowTmp
        .createCell(13)
        .setCellValue(
          ig.getSetOfLeaders().toString().replace("[", "").replace("]", "")
        );
      rowTmp.createCell(14).setCellValue(ig.getContactInformation());

      iRow++;
    }

    //add row with the totals
    HSSFRow rowTmp = sheetNumbers.createRow(iRow);
    rowTmp.createCell(0).setCellValue("Total");
    for (int i : new int[] { 1, 2, 3, 4, 5, 11, 13, 14 }) {
      rowTmp.createCell(i).setCellValue("");
    }

    //if we have a least 1 Interest Group in the Category, calculate the sums
    if (iRow >= 2) {
      //nb documents
      rowTmp.createCell(6).setCellFormula("sum(G2:G" + iRow + ")");
      //size of documents
      rowTmp.createCell(7).setCellFormula("sum(H2:H" + iRow + ")");
      rowTmp.getCell(7).setCellStyle(numberCellStyle);
      //size of information
      rowTmp.createCell(8).setCellFormula("sum(I2:I" + iRow + ")");
      rowTmp.getCell(8).setCellStyle(numberCellStyle);
      //nb events
      rowTmp.createCell(9).setCellFormula("sum(J2:J" + iRow + ")");
      //nb posts
      rowTmp.createCell(10).setCellFormula("sum(K2:K" + iRow + ")");
      //column 11: Deepness, we do not sum that info
      //nb members
      rowTmp.createCell(12).setCellFormula("sum(M2:M" + iRow + ")");

      //execute all the formules
      wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
    } else {
      //nb documents
      rowTmp.createCell(6).setCellValue(0);
      //size of documents
      rowTmp.createCell(7).setCellValue(0);
      //size of information
      rowTmp.createCell(8).setCellValue(0);
      //nb events
      rowTmp.createCell(9).setCellValue(0);
      //nb posts
      rowTmp.createCell(10).setCellValue(0);
      //nb members
      rowTmp.createCell(12).setCellValue(0);
    }

    return wb;
  }
}
