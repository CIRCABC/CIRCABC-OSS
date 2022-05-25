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
package eu.cec.digit.circabc.repo.statistics.global;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.log.LogCountResultDAO;
import eu.cec.digit.circabc.repo.statistics.ig.IgDescriptor;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.compress.ZipService;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.profile.*;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.service.report.ReportDaoService;
import eu.cec.digit.circabc.service.statistics.global.GlobalStatisticsService;
import eu.cec.digit.circabc.service.statistics.ig.IgStatisticsService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import io.swagger.api.ProfilesApi;
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
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/** @author beaurpi */
public class GlobalStatisticsServiceImpl implements GlobalStatisticsService {

    static final Log logger = LogFactory.getLog(GlobalStatisticsServiceImpl.class);
    private static final String CIRCABC_SITE = "CIRCABC Site";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String circabcDictionaryFolderName = "CircaBC";
    private static String statisticsDictionaryFolderName = "statistics";
    private static String categoryStatisticsReportsDictionaryFolderName = "category_statistics";
    private static String reportsDictionaryFolderName = "reports";
    private static String jobConfigFileName = "statsJobConfig.properties";
    private static List<String> detailedListHeader =
            Arrays.asList(
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
                    "Public Outputs");
    private ManagementService managementService;
    private NodeService nodeService;
    private SearchService searchService;
    private FileFolderService fileFolderService;
    private PersonService personService;
    private ReportDaoService reportDaoService;
    private TransactionService transactionService;
    private ContentService contentService;
    private IGRootProfileManagerService igRootProfileManagerService;
    private ProfileManagerServiceFactory profileManagerServiceFactory;
    private LogService logService;
    private ZipService zipService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private IgStatisticsService igStatisticsService;
    private ProfilesApi profilesApi;
    private NamespacePrefixResolver namespacePrefixResolver;

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /** @param profilesApi the profilesApi to set */
    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    private Integer getNumberOfCircabcHeaders() {
        return getListOfCircabcHeaders().size();
    }

    private List<NodeRef> getListOfCircabcHeaders() {

        NodeRef rootHeader = getManagementService().getRootCategoryHeader();
        List<ChildAssociationRef> lHeadersAssoc = nodeService.getChildAssocs(rootHeader);
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

        return getManagementService().getCategories();
    }

    private Integer getNumberOfCircabcInterestGroups() {

        return getListOfCircabcInterestGroups().size();
    }

    public List<NodeRef> getListOfCircabcInterestGroups() {

        List<NodeRef> lIg = new ArrayList<>();
        for (NodeRef categ : getListOfCircabcCategories()) {
            for (ChildAssociationRef c : nodeService.getChildAssocs(categ)) {
                if (nodeService.hasAspect(c.getChildRef(), CircabcModel.ASPECT_IGROOT)) {
                    lIg.add(c.getChildRef());
                }
            }
        }
        return lIg;
    }

    private Integer getNumberOfUsers() {

        List<PersonService.PersonInfo> personInfos =
                personService
                        .getPeople(null, null, null, new PagingRequest(Integer.MAX_VALUE, null))
                        .getPage();
        return personInfos.size();
    }

    private Map<NodeRef, Integer> getNumberOfCircabcInterestGroupsPerCategory() {

        Map<NodeRef, Integer> nbIgPerCateg = new HashMap<>();
        Map<NodeRef, List<NodeRef>> lIgPerCateg = getListOfCircabcInterestGroupsPerCategory();
        for (Entry<NodeRef, List<NodeRef>> entry : lIgPerCateg.entrySet()) {
            nbIgPerCateg.put(entry.getKey(), entry.getValue().size());
        }

        return nbIgPerCateg;
    }

    private Map<NodeRef, List<NodeRef>> getListOfCircabcInterestGroupsPerCategory() {

        Map<NodeRef, List<NodeRef>> lIgPerCateg = new HashMap<>();
        for (NodeRef categ : getListOfCircabcCategories()) {
            lIgPerCateg.put(categ, getListOfCircabcInterestGroupsForCategory(categ));
        }
        return lIgPerCateg;
    }

    private List<NodeRef> getListOfCircabcInterestGroupsForCategory(NodeRef categNodeRef) {

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
                logger.error("Error during SQL query for number of documents in CIRCABC:", e);
            }
        }

        return nbDocs;
    }

    public void prepareFolderRecipient() {

        NodeRef dicoNodeRef = getDicoNodeRef();

        NodeRef circabcFolderNodeRef =
                nodeService.getChildByName(
                        dicoNodeRef, ContentModel.ASSOC_CONTAINS, circabcDictionaryFolderName);

        FileInfo statisticsFolder = null;

        if (nodeService.getChildByName(
                circabcFolderNodeRef, ContentModel.ASSOC_CONTAINS, statisticsDictionaryFolderName)
                == null) {
            statisticsFolder =
                    fileFolderService.create(
                            circabcFolderNodeRef, statisticsDictionaryFolderName, ContentModel.TYPE_FOLDER);
            fileFolderService.create(
                    statisticsFolder.getNodeRef(), reportsDictionaryFolderName, ContentModel.TYPE_FOLDER);
            fileFolderService.create(
                    statisticsFolder.getNodeRef(), jobConfigFileName, ContentModel.TYPE_CONTENT);

            NodeRef statisticsFolderRef =
                    nodeService.getChildByName(
                            circabcFolderNodeRef, ContentModel.ASSOC_CONTAINS, statisticsDictionaryFolderName);

            if (statisticsFolderRef != null) {
                permissionService.setInheritParentPermissions(statisticsFolderRef, false);

                final String prefixedUserGroupName =
                        PermissionService.GROUP_PREFIX
                                + getProfileManagerServiceFactory()
                                .getCircabcRootProfileManagerService()
                                .getInvitedUsersGroupName(getManagementService().getCircabcNodeRef());
                permissionService.setPermission(
                        statisticsFolderRef, prefixedUserGroupName, PermissionService.CONTRIBUTOR, true);
            }
        }
    }

    /** @return */
    private NodeRef getDicoNodeRef() {
        String dicoPath = managementService.getAlfrescoDictionaryLucenePath();

        List<NodeRef> nodes =
                searchService.selectNodes(
                        nodeService.getRootNode(Repository.getStoreRef()),
                        dicoPath,
                        null,
                        namespacePrefixResolver,
                        false);

        return nodes.get(0);
    }

    public Map<String, Object> makeGlobalStats() {

        Map<String, Object> globalStats = new HashMap<>();

        Integer nbHeaders = getNumberOfCircabcHeaders();
        globalStats.put("numberOfCategoryHeaders", nbHeaders);

        Integer nbCategories = getNumberOfCircabcCategories();
        globalStats.put("numberOfCategories", nbCategories);

        Integer nbInterestGroups = getNumberOfCircabcInterestGroups();
        globalStats.put("numberOfIgs", nbInterestGroups);

        Map<NodeRef, Integer> nbIgPC = getNumberOfCircabcInterestGroupsPerCategory();
        Map<String, Integer> lIgPC = new HashMap<>();

        for (Entry<NodeRef, Integer> entry : nbIgPC.entrySet()) {
            lIgPC.put(getNameOrTitleOfNodeRefHelper(entry.getKey()), entry.getValue());
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

    public NodeRef saveStatsToExcel(final NodeRef destinationFolder, Map<String, Object> lData) {

        Calendar cFile = new GregorianCalendar();
        cFile.setTime(new Date());
        final String fileName =
                "StatisticReport"
                        + cFile.get(Calendar.DAY_OF_MONTH)
                        + "-"
                        + (cFile.get(Calendar.MONTH) + 1)
                        + "-"
                        + cFile.get(Calendar.YEAR)
                        + "-"
                        + cFile.get(Calendar.HOUR_OF_DAY)
                        + "-"
                        + cFile.get(Calendar.MINUTE)
                        + "-"
                        + cFile.get(Calendar.MILLISECOND)
                        + ".xls";

        if (nodeService.getType(destinationFolder).equals(ContentModel.TYPE_FOLDER)) {
            final HSSFWorkbook wb = builtExcelFromData(lData);

            RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
            return helper.doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                        public NodeRef execute() throws Throwable {
                            return writeFile(destinationFolder, fileName, fileName, wb);
                        }
                    },
                    false,
                    true);

        } else {
            throw new CircabcRuntimeException(
                    "destination noderef:" + destinationFolder.toString() + " is not a valid folder");
        }
    }

    /**
     * @param lData
     * @return
     */
    @SuppressWarnings("unchecked")
    private HSSFWorkbook builtExcelFromData(Map<String, Object> lData) {
        final HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheetNumbers = wb.createSheet("numbers");

        Integer iRow = 0;

        for (Entry<String, Object> entry : lData.entrySet()) {
            final String key = entry.getKey();
            if (!key.matches("list.*")) {
                switch (key) {
                    case "numberOfIgsPerCategory": {
                        HSSFRow row = sheetNumbers.createRow(iRow);
                        HSSFCell cell = row.createCell(0);
                        cell.setCellValue(new HSSFRichTextString(key));

                        iRow++;

                        Map<String, Integer> lnbs = (Map<String, Integer>) entry.getValue();

                        for (Entry<String, Integer> item : lnbs.entrySet()) {
                            HSSFRow row2 = sheetNumbers.createRow(iRow);
                            HSSFCell cell2 = row2.createCell(0);
                            cell2.setCellValue(new HSSFRichTextString(item.getKey()));

                            HSSFCell cell3 = row2.createCell(1);
                            cell3.setCellValue(item.getValue());

                            iRow++;
                        }

                        break;
                    }
                    case "actionCountForYesterDay": {
                        HSSFRow row = sheetNumbers.createRow(iRow);
                        HSSFCell cell = row.createCell(0);
                        cell.setCellValue(new HSSFRichTextString(key));

                        HSSFCell cell2 = row.createCell(1);

                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(new Date());

                        cell2.setCellValue(
                                new HSSFRichTextString(
                                        ""
                                                + (gc.get(Calendar.DAY_OF_MONTH) - 1)
                                                + "-"
                                                + (gc.get(Calendar.MONTH) + 1)
                                                + "-"
                                                + gc.get(Calendar.YEAR)));

                        iRow++;

                        for (LogCountResultDAO logEntry : (List<LogCountResultDAO>) entry.getValue()) {
                            HSSFRow row2 = sheetNumbers.createRow(iRow);

                            HSSFCell cellDate = row2.createCell(0);
                            cellDate.setCellValue(
                                    new HSSFRichTextString(
                                            logEntry.getHourPeriod() + "h-" + (logEntry.getHourPeriod() + 1) + "h"));

                            HSSFCell cellNumber = row2.createCell(1);
                            cellNumber.setCellValue(logEntry.getNumberOfActions());

                            iRow++;
                        }

                        break;
                    }
                    default: {
                        HSSFRow row = sheetNumbers.createRow(iRow);
                        HSSFCell cell = row.createCell(0);
                        cell.setCellValue(new HSSFRichTextString(key));

                        HSSFCell cell2 = row.createCell(1);
                        cell2.setCellValue((Integer) entry.getValue());
                        break;
                    }
                }

                iRow++;
            }
        }

        iRow = 0;
        int iCol = 0;

        HSSFSheet sheetLists = wb.createSheet("lists");

        HSSFRow row = sheetLists.createRow(iRow);
        HSSFCell cell = row.createCell(iCol);
        cell.setCellValue(new HSSFRichTextString("Category"));

        HSSFCell cell2 = row.createCell((iCol + 1));
        cell2.setCellValue(new HSSFRichTextString("IG Title"));

        HSSFCell cell3 = row.createCell((iCol + 2));
        cell3.setCellValue(new HSSFRichTextString("IG Name"));

        HSSFCell cell4 = row.createCell((iCol + 3));
        cell4.setCellValue(new HSSFRichTextString("Leaders"));

        HSSFCell cell5 = row.createCell((iCol + 4));
        cell5.setCellValue(new HSSFRichTextString("Link"));

        HSSFCell cell6 = row.createCell((iCol + 5));
        cell6.setCellValue(new HSSFRichTextString("New Link"));

        iRow++;

        String rootUrl = CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);

        for (CategoryDescriptor c : (List<CategoryDescriptor>) lData.get("listOfCircabcStructure")) {
            HSSFRow rowCateg = sheetLists.createRow(iRow);
            HSSFCell cCateg1 = rowCateg.createCell(iCol);
            cCateg1.setCellValue(
                    new HSSFRichTextString((c.getTitle().equals("") ? c.getName() : c.getTitle())));

            HSSFCell cCateg2 = rowCateg.createCell((iCol + 1));
            cCateg2.setCellValue(new HSSFRichTextString("-----"));

            HSSFCell cCateg3 = rowCateg.createCell((iCol + 2));
            cCateg3.setCellValue(new HSSFRichTextString("-----"));

            HSSFCell cCateg4 = rowCateg.createCell((iCol + 3));
            cCateg4.setCellValue(
                    new HSSFRichTextString(c.getListOfAdmins().toString().replace("[", "").replace("]", "")));

            HSSFCell cCateg5 = rowCateg.createCell((iCol + 4));
            cCateg5.setCellValue(new HSSFRichTextString(rootUrl + "/w/browse/" + c.getRef().getId()));

            HSSFCell cCateg6 = rowCateg.createCell((iCol + 5));
            cCateg6.setCellValue(
                    new HSSFRichTextString(
                            rootUrl
                                    + CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_CONTEXT)
                                    + "category/"
                                    + c.getRef().getId()
                                    + "/details"));

            iRow++;

            for (IgDescriptor ig : c.getListOfIgs()) {
                HSSFRow row2 = sheetLists.createRow(iRow);
                HSSFCell c1 = row2.createCell(iCol);
                c1.setCellValue(
                        new HSSFRichTextString((c.getTitle().equals("") ? c.getName() : c.getTitle())));

                HSSFCell c2 = row2.createCell((iCol + 1));
                c2.setCellValue(
                        new HSSFRichTextString((ig.getTitle().equals("") ? ig.getName() : ig.getTitle())));

                HSSFCell c3 = row2.createCell((iCol + 2));
                c3.setCellValue(new HSSFRichTextString(ig.getName()));

                HSSFCell c4 = row2.createCell((iCol + 3));
                c4.setCellValue(
                        new HSSFRichTextString(
                                ig.getSetOfLeaders().toString().replace("[", "").replace("]", "")));

                HSSFCell c5 = row2.createCell((iCol + 4));
                c5.setCellValue(new HSSFRichTextString(rootUrl + "/w/browse/" + ig.getRef().getId()));

                HSSFCell c6 = row2.createCell((iCol + 5));
                c6.setCellValue(
                        new HSSFRichTextString(
                                rootUrl
                                        + CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_CONTEXT)
                                        + "group/"
                                        + ig.getRef().getId()));

                iRow++;
            }
        }
        return wb;
    }

    /**
     * @param lData
     * @return
     */
    @SuppressWarnings("unchecked")
    private HSSFWorkbook builtExcelFromDetailedData(Map<String, Object> lData) {
        final HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheetNumbers = wb.createSheet("detailed list of IGs");

        List<CategoryDescriptor> lCateg = (List<CategoryDescriptor>) lData.get("detailedIgList");

        int iRow = 0;
        int iCol = 0;

        HSSFRow rowHeader = sheetNumbers.createRow(iRow);
        CellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setRotation((short) 90);

        for (String header : detailedListHeader) {

            HSSFCell cell = rowHeader.createCell(iCol);
            cell.setCellValue(new HSSFRichTextString(header));
            cell.setCellStyle(style);

            iCol++;
        }

        iRow++;

        for (CategoryDescriptor categ : lCateg) {
            for (IgDescriptor ig : categ.getListOfIgs()) {
                if (ig.getPublicVisibility()) {
                    HSSFRow row = sheetNumbers.createRow(iRow);
                    HSSFCell cellOwner = row.createCell(0);
                    cellOwner.setCellValue(new HSSFRichTextString(ig.getSetOfLeaders().toString()));

                    HSSFCell cellType = row.createCell(1);
                    cellType.setCellValue(new HSSFRichTextString(CIRCABC_SITE));

                    HSSFCell cellOwnerMail = row.createCell(2);
                    cellOwnerMail.setCellValue(new HSSFRichTextString(ig.getSetOfLeaders().toString()));

                    HSSFCell cellDg = row.createCell(3);
                    cellDg.setCellValue(
                            new HSSFRichTextString(
                                    !categ.getTitle().isEmpty() ? categ.getTitle() : categ.getName()));

                    HSSFCell cellRequestedDate = row.createCell(4);
                    cellRequestedDate.setCellValue(ig.getCreationDate());

                    HSSFCell cellTitle = row.createCell(5);
                    cellTitle.setCellValue(!ig.getTitle().isEmpty() ? ig.getTitle() : ig.getName());

                    HSSFCell cellLightDesc = row.createCell(6);
                    cellLightDesc.setCellValue(
                            ig.getLightDescription() != null ? ig.getLightDescription() : "");

                    HSSFCell cellSiteUrl = row.createCell(11);
                    String rootUrl = CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);
                    String url = "";
                    if (rootUrl.endsWith("/")) {
                        url = rootUrl + "w/browse/" + ig.getRef().getId();
                    } else {
                        url = rootUrl + "/w/browse/" + ig.getRef().getId();
                    }
                    cellSiteUrl.setCellValue(url);

                    HSSFCell cellReleaseDate = row.createCell(13);
                    cellReleaseDate.setCellValue(ig.getCreationDate());

                    HSSFCell cellFullSiteUrl = row.createCell(18);
                    cellFullSiteUrl.setCellValue(cellSiteUrl.getStringCellValue());

                    HSSFCell cellFunctionalities = row.createCell(20);
                    cellFunctionalities.setCellValue(ig.getAvailableServices().toString());

                    HSSFCell cellPublicRegistered = row.createCell(28);
                    if (ig.getPublicEnabled()) {
                        cellPublicRegistered.setCellValue("Visible to public");
                    } else if (!ig.getPublicEnabled() && ig.getRegisteredEnabled()) {
                        cellPublicRegistered.setCellValue("Visible to registered users");
                    }

                    iRow++;
                }
            }
        }

        return wb;
    }

    /** GETTERS & SETTERS */

    /** @return the managementService */
    public ManagementService getManagementService() {
        return managementService;
    }

    /** @param managementService the managementService to set */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /** @return the nodeService */
    public NodeService getNodeService() {
        return nodeService;
    }

    /** @param nodeService the nodeService to set */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /** @return the searchService */
    public SearchService getSearchService() {
        return searchService;
    }

    /** @param searchService the searchService to set */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /** @return the fileFolderService */
    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    /** @param fileFolderService the fileFolderService to set */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    /** @return the personService */
    public PersonService getPersonService() {
        return personService;
    }

    /** @param personService the personService to set */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /** @return the reportDaoService */
    public ReportDaoService getReportDaoService() {
        return reportDaoService;
    }

    /** @param reportDaoService the reportDaoService to set */
    public void setReportDaoService(ReportDaoService reportDaoService) {
        this.reportDaoService = reportDaoService;
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
        return (title.length() > 0
                ? title
                : nodeService.getProperty(node, ContentModel.PROP_NAME).toString());
    }

    /**
     * title if exists, name if not
     *
     * @param nodes list of nodes
     * @return
     */
    @SuppressWarnings("unused")
    private List<String> getListOfNameFromListOfNodeRefHelper(List<NodeRef> nodes) {
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

    public NodeRef getReportSaveFolder() {
        NodeRef dicoNodeRef = getDicoNodeRef();

        NodeRef circabcFolderNodeRef =
                nodeService.getChildByName(
                        dicoNodeRef, ContentModel.ASSOC_CONTAINS, circabcDictionaryFolderName);

        NodeRef statFolderNodeRef =
                nodeService.getChildByName(
                        circabcFolderNodeRef, ContentModel.ASSOC_CONTAINS, statisticsDictionaryFolderName);

        NodeRef reportNode = null;

        if (statFolderNodeRef != null) {
            reportNode =
                    nodeService.getChildByName(
                            statFolderNodeRef, ContentModel.ASSOC_CONTAINS, reportsDictionaryFolderName);
        }

        return reportNode;
    }

    private Set<String> getListOfLeadersForInterestGroup(NodeRef igNodeRef) {

        Set<String> usernames = new HashSet<>();

        List<Profile> lProfiles = igRootProfileManagerService.getProfiles(igNodeRef);
        for (Profile p : lProfiles) {
            if (p.isAdmin()
                    || p.getServicePermissions(CircabcServices.DIRECTORY.name())
                    .contains(DirectoryPermissions.DIRADMIN.toString())) {
                usernames.addAll(
                        igRootProfileManagerService.getPersonInProfile(igNodeRef, p.getProfileName()));
            }
        }

        Set<String> usernamesAndEmails = new HashSet<>();
        for (String uname : usernames) {
            usernamesAndEmails.add(
                    nodeService
                            .getProperty(personService.getPerson(uname), ContentModel.PROP_EMAIL)
                            .toString());
        }

        return usernamesAndEmails;
    }

    /** @return the igRootProfileManagerService */
    public IGRootProfileManagerService getIgRootProfileManagerService() {
        return igRootProfileManagerService;
    }

    /** @param igRootProfileManagerService the igRootProfileManagerService to set */
    public void setIgRootProfileManagerService(
            IGRootProfileManagerService igRootProfileManagerService) {
        this.igRootProfileManagerService = igRootProfileManagerService;
    }

    private List<CategoryDescriptor> getCircabcStructure(Boolean detailed) {

        List<CategoryDescriptor> lCategs = new ArrayList<>();
        for (NodeRef nCateg : getListOfCircabcCategories()) {
            CategoryDescriptor c = new CategoryDescriptor();
            c.setRef(nCateg);
            c.setName(nodeService.getProperty(nCateg, ContentModel.PROP_NAME).toString());
            c.setTitle(
                    (nodeService.getProperties(nCateg).containsKey(ContentModel.PROP_TITLE)
                            ? nodeService.getProperty(nCateg, ContentModel.PROP_TITLE).toString()
                            : ""));
            c.setListOfAdmins(getListOfAdminsForCategory(nCateg));

            List<IgDescriptor> lIgDesc = new ArrayList<>();

            for (NodeRef nIg : getListOfCircabcInterestGroupsForCategory(nCateg)) {
                IgDescriptor ig = new IgDescriptor();
                ig.setRef(nIg);
                ig.setName(nodeService.getProperty(nIg, ContentModel.PROP_NAME).toString());
                ig.setTitle(
                        (nodeService.getProperties(nIg).containsKey(ContentModel.PROP_TITLE)
                                ? nodeService.getProperty(nIg, ContentModel.PROP_TITLE).toString()
                                : ""));
                ig.setSetOfLeaders(getListOfLeadersForInterestGroup(nIg));

                if (detailed) {
                    ig.setDescription(
                            (nodeService.getProperties(nIg).containsKey(ContentModel.PROP_DESCRIPTION)
                                    ? nodeService.getProperty(nIg, ContentModel.PROP_DESCRIPTION).toString()
                                    : ""));
                    ig.setLightDescription(
                            (nodeService.getProperties(nIg).containsKey(CircabcModel.PROP_LIGHT_DESCRIPTION)
                                    ? nodeService.getProperty(nIg, CircabcModel.PROP_LIGHT_DESCRIPTION).toString()
                                    : ""));

                    if (nodeService.getProperties(nIg).containsKey(ContentModel.PROP_CREATED)) {
                        Date d = (Date) nodeService.getProperty(nIg, ContentModel.PROP_CREATED);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        ig.setCreationDate(sdf.format(d));
                    } else {
                        ig.setCreationDate("");
                    }

                    Profile profileGuest =
                            igRootProfileManagerService.getProfile(nIg, CircabcConstant.GUEST_AUTHORITY);
                    Profile profileRegistered =
                            igRootProfileManagerService.getProfile(nIg, CircabcConstant.REGISTERED_AUTHORITY);
                    ig.setPublicVisibility(
                            computeVisibility(profileGuest) || computeVisibility(profileRegistered));
                    ig.setPublicEnabled(computeVisibility(profileGuest));
                    ig.setRegisteredEnabled(computeVisibility(profileRegistered));

                    Set<String> services = new HashSet<>();

                    for (Profile profile : igRootProfileManagerService.getProfiles(nIg)) {
                        final Map<String, Set<String>> permissions = profile.getServicesPermissions();
                        final Set<String> infServicePermissions =
                                permissions.get(CircabcServices.INFORMATION.toString());
                        final Set<String> libServicePermissions =
                                permissions.get(CircabcServices.LIBRARY.toString());
                        final Set<String> dirServicePermissions =
                                permissions.get(CircabcServices.DIRECTORY.toString());
                        final Set<String> newServicePermissions =
                                permissions.get(CircabcServices.NEWSGROUP.toString());
                        final Set<String> eveServicePermissions =
                                permissions.get(CircabcServices.EVENT.toString());

                        if (infServicePermissions != null) {
                            if (infServicePermissions.size() > 0) {
                                if (!infServicePermissions
                                        .iterator()
                                        .next()
                                        .equals(InformationPermissions.INFNOACCESS.toString())) {
                                    services.add(CircabcServices.INFORMATION.toString());
                                }
                            }
                        }
                        if (libServicePermissions != null) {
                            if (libServicePermissions.size() > 0) {
                                if (!libServicePermissions
                                        .iterator()
                                        .next()
                                        .equals(LibraryPermissions.LIBNOACCESS.toString())) {
                                    services.add(CircabcServices.LIBRARY.toString());
                                }
                            }
                        }
                        if (dirServicePermissions != null) {
                            if (dirServicePermissions.size() > 0) {
                                if (!dirServicePermissions
                                        .iterator()
                                        .next()
                                        .equals(DirectoryPermissions.DIRNOACCESS.toString())) {
                                    services.add(CircabcServices.DIRECTORY.toString());
                                }
                            }
                        }
                        if (newServicePermissions != null) {
                            if (newServicePermissions.size() > 0) {
                                if (!newServicePermissions
                                        .iterator()
                                        .next()
                                        .equals(NewsGroupPermissions.NWSNOACCESS.toString())) {
                                    services.add(CircabcServices.NEWSGROUP.toString());
                                }
                            }
                        }
                        if (eveServicePermissions != null) {
                            if (eveServicePermissions.size() > 0) {
                                if (!eveServicePermissions
                                        .iterator()
                                        .next()
                                        .equals(EventPermissions.EVENOACCESS.toString())) {
                                    services.add(CircabcServices.EVENT.toString());
                                }
                            }
                        }
                    }

                    ig.setAvailableServices(services);
                }

                lIgDesc.add(ig);
            }

            c.setListOfIgs(lIgDesc);
            lCategs.add(c);
        }

        return lCategs;
    }

    private boolean computeVisibility(final Profile profile) {
        final Set<String> servicePermissions =
                profile.getServicePermissions(CircabcServices.VISIBILITY.toString());
        if (servicePermissions == null || servicePermissions.size() == 0) {
            return false;
        } else {
            final String visibility = servicePermissions.iterator().next();
            return VisibilityPermissions.VISIBILITY.toString().equals(visibility);
        }
    }

    private Set<String> getListOfAdminsForCategory(NodeRef categoryNodeRef) {

        Set<String> usernames = new HashSet<>();

        if (categoryNodeRef == null) {
            return usernames;
        }

        ProfileManagerService profileManagerService =
                profileManagerServiceFactory.getProfileManagerService(categoryNodeRef);

        if (profileManagerService == null) {
            return usernames;
        }

        Set<String> invitedUsers = profileManagerService.getInvitedUsers(categoryNodeRef);

        if (invitedUsers == null) {
            return usernames;
        }

        usernames.addAll(invitedUsers);

        Set<String> usernamesAndEmails = new HashSet<>();
        for (String uname : usernames) {
            usernamesAndEmails.add(
                    nodeService
                            .getProperty(personService.getPerson(uname), ContentModel.PROP_EMAIL)
                            .toString());
        }

        return usernamesAndEmails;
    }

    public Date getLastLoginDateOfUser(String username) {

        return logService.getLastLoginDateOfUser(username);
    }

    public List<FileInfo> getListOfReportFiles() {
        NodeRef folderNodeRef = getReportSaveFolder();

        return fileFolderService.listFiles(folderNodeRef);
    }

    /** @return the logService */
    public LogService getLogService() {
        return logService;
    }

    /** @param logService the logService to set */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    private List<LogCountResultDAO> getCountOfActionsForYesterday() {

        List<LogCountResultDAO> listOfActionsCompleted = new ArrayList<>();
        List<LogCountResultDAO> listOfActionsFromDB = logService.getNumberOfActionsYesterdayPerHour();

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

    public void cleanAndZipPreviousReportFiles() {

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        final String archiveName =
                "archive-"
                        + gc.get(Calendar.DAY_OF_MONTH)
                        + "-"
                        + (gc.get(Calendar.MONTH) + 1)
                        + "-"
                        + gc.get(Calendar.YEAR)
                        + "-"
                        + gc.get(Calendar.MILLISECOND)
                        + ".zip";
        final NodeRef reportFolder = getReportSaveFolder();
        final File tempZipFile =
                TempFileProvider.createTempFile(archiveName, ".tmp", TempFileProvider.getTempDir());

        List<FileInfo> listOfReports =
                fileFolderService.search(reportFolder, "*.xls", true, false, false);
        List<NodeRef> listOfReportsNodeRefs = new ArrayList<>();

        for (FileInfo file : listOfReports) {
            listOfReportsNodeRefs.add(file.getNodeRef());
        }

        zipService.addingFileIntoArchive(listOfReportsNodeRefs, tempZipFile);

        for (FileInfo file : listOfReports) {

            fileFolderService.delete(file.getNodeRef());
        }

        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        helper.doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                    public NodeRef execute() throws Throwable {

                        NodeRef node =
                                nodeService.getChildByName(reportFolder, ContentModel.ASSOC_CONTAINS, archiveName);

                        if (node == null) {
                            final FileInfo fileInfo =
                                    fileFolderService.create(reportFolder, archiveName, ContentModel.TYPE_CONTENT);
                            final NodeRef createdNodeRef = fileInfo.getNodeRef();

                            node = createdNodeRef;

                            // get a writer for the content and put the file
                            final ContentWriter writer =
                                    contentService.getWriter(createdNodeRef, ContentModel.PROP_CONTENT, true);

                            writer.setMimetype(MimetypeMap.MIMETYPE_ZIP);

                            writer.putContent(tempZipFile);
                        }

                        return node;
                    }
                },
                false,
                true);

        boolean isDeleted = tempZipFile.delete();

        if (!isDeleted && logger.isWarnEnabled()) {
            try {
                logger.warn("Unable to delete file : " + tempZipFile.getCanonicalPath());
            } catch (IOException e) {
                logger.warn("Unable to get getCanonicalPath for : " + tempZipFile.getPath(), e);
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

    public Boolean isReportSaveFolderExisting() {
        Boolean result = false;
        if (getReportSaveFolder() != null) {
            result = true;
        }
        return result;
    }

    @Override
    public Map<String, Object> makeDetailedIgStats() {

        Map<String, Object> globalStats = new HashMap<>();
        globalStats.put("detailedIgList", getCircabcStructure(true));

        return globalStats;
    }

    @Override
    public NodeRef saveDetailedIgStatsToExcel(
            final NodeRef reportSaveFolder, Map<String, Object> igData) {

        Calendar cFile = new GregorianCalendar();
        cFile.setTime(new Date());
        final String fileName =
                "DetailsIgListReport"
                        + cFile.get(Calendar.DAY_OF_MONTH)
                        + "-"
                        + (cFile.get(Calendar.MONTH) + 1)
                        + "-"
                        + cFile.get(Calendar.YEAR)
                        + "-"
                        + cFile.get(Calendar.HOUR_OF_DAY)
                        + "-"
                        + cFile.get(Calendar.MINUTE)
                        + "-"
                        + cFile.get(Calendar.MILLISECOND)
                        + ".xls";

        if (nodeService.getType(reportSaveFolder).equals(ContentModel.TYPE_FOLDER)) {
            final HSSFWorkbook wb = builtExcelFromDetailedData(igData);

            RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
            return helper.doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                        public NodeRef execute() throws Throwable {
                            return writeFile(reportSaveFolder, fileName, fileName, wb);
                        }
                    },
                    false,
                    true);

        } else {
            throw new CircabcRuntimeException(
                    "destination noderef:" + reportSaveFolder.toString() + " is not a valid folder");
        }
    }

    private NodeRef writeFile(
            NodeRef reportSaveFolder, String fileName, String title, HSSFWorkbook wb) throws IOException {
        NodeRef node =
                nodeService.getChildByName(reportSaveFolder, ContentModel.ASSOC_CONTAINS, fileName);

        if (node == null) {
            final FileInfo fileInfo =
                    fileFolderService.create(reportSaveFolder, fileName, ContentModel.TYPE_CONTENT);
            final NodeRef createdNodeRef = fileInfo.getNodeRef();
            nodeService.setProperty(createdNodeRef, ContentModel.PROP_TITLE, title);
            File tempFile = TempFileProvider.createTempFile(fileName, "tmp");
            FileOutputStream fileWriter = null;
            try {
                fileWriter = new FileOutputStream(tempFile);
                wb.write(fileWriter);
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }

            // get a writer for the content and put the file
            final ContentWriter writer =
                    contentService.getWriter(createdNodeRef, ContentModel.PROP_CONTENT, true);

            writer.setMimetype(MimetypeMap.MIMETYPE_EXCEL);

            writer.putContent(tempFile);

            boolean isDeleted = tempFile.delete();
            if (!isDeleted && logger.isWarnEnabled()) {
                try {
                    logger.warn("Unable to delete file : " + tempFile.getCanonicalPath());
                } catch (IOException e) {
                    logger.warn("Unable to get getCanonicalPath for ." + tempFile.getPath(), e);
                }
            }

            node = createdNodeRef;
        }

        return node;
    }

    /** @param namespacePrefixResolver the namespacePrefixResolver to set */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /** @return the profileManagerServiceFactory */
    public ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return profileManagerServiceFactory;
    }

    /** @param profileManagerServiceFactory the profileManagerServiceFactory to set */
    public void setProfileManagerServiceFactory(
            ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Override
    public List<FileInfo> getCategoryGroupStatsFiles(String categoryName, NodeRef categoryRef) {
        NodeRef folderNodeRef = getCategoryReportSaveFolder(categoryName, categoryRef);
        return fileFolderService.listFiles(folderNodeRef);
    }

    private NodeRef getCategoryReportSaveFolder(String categoryName, NodeRef categoryRef) {

        prepareCategoryReportsFolderRecipient(categoryName, categoryRef);
        NodeRef reportFolder = getCategoryReportsFolderRecipient(categoryName);

        return reportFolder;
    }

    public void prepareCategoryReportsFolderRecipient(String categoryName, NodeRef categoryRef) {

        String username = AuthenticationUtil.getFullyAuthenticatedUser();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        NodeRef dicoNodeRef = getDicoNodeRef();

        NodeRef circabcFolderNodeRef =
                nodeService.getChildByName(
                        dicoNodeRef, ContentModel.ASSOC_CONTAINS, circabcDictionaryFolderName);

        if (nodeService.getChildByName(
                circabcFolderNodeRef,
                ContentModel.ASSOC_CONTAINS,
                categoryStatisticsReportsDictionaryFolderName)
                == null) {
            fileFolderService.create(
                    circabcFolderNodeRef,
                    categoryStatisticsReportsDictionaryFolderName,
                    ContentModel.TYPE_FOLDER);
        }

        NodeRef categStatFolder =
                nodeService.getChildByName(
                        circabcFolderNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        categoryStatisticsReportsDictionaryFolderName);

        NodeRef statFolder =
                nodeService.getChildByName(categStatFolder, ContentModel.ASSOC_CONTAINS, categoryName);

        if (statFolder == null) {
            statFolder =
                    fileFolderService
                            .create(categStatFolder, categoryName, ContentModel.TYPE_FOLDER)
                            .getNodeRef();
        }

        if (permissionService.getInheritParentPermissions(statFolder)) {
            permissionService.setInheritParentPermissions(statFolder, false);
            String authority =
                    nodeService
                            .getProperty(categoryRef, CircabcModel.PROP_CATEGORY_INVITED_USER_GROUP)
                            .toString();
            String permission = "Contributor";
            permissionService.setPermission(statFolder, authority, permission, true);
        }

        AuthenticationUtil.setFullyAuthenticatedUser(username);
    }

    public NodeRef getCategoryReportsFolderRecipient(String categoryName) {

        NodeRef dicoNodeRef = getDicoNodeRef();

        NodeRef circabcFolderNodeRef =
                nodeService.getChildByName(
                        dicoNodeRef, ContentModel.ASSOC_CONTAINS, circabcDictionaryFolderName);

        NodeRef categStatFolder =
                nodeService.getChildByName(
                        circabcFolderNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        categoryStatisticsReportsDictionaryFolderName);

        return nodeService.getChildByName(categStatFolder, ContentModel.ASSOC_CONTAINS, categoryName);
    }

    public List<FileInfo> getListOfCategoryReportFiles(String categoryName) {
        NodeRef folderNodeRef = getCategoryReportsFolderRecipient(categoryName);

        return fileFolderService.listFiles(folderNodeRef);
    }

    public List<IgDescriptor> computeCategoryGroupStatistics(NodeRef categoryRef) {
        List<NodeRef> lGroups = getListOfCircabcInterestGroupsForCategory(categoryRef);
        List<IgDescriptor> results = new ArrayList<IgDescriptor>();
        Integer i = 1;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (NodeRef nIg : lGroups) {
            logger.info(
                    "CategoryStatisticsJob: "
                            + categoryRef.getId()
                            + " Started IG: "
                            + i
                            + "/"
                            + lGroups.size());
            IgDescriptor ig = new IgDescriptor();
            ig.setRef(nIg);
            ig.setName(nodeService.getProperty(nIg, ContentModel.PROP_NAME).toString());
            ig.setTitle(
                    (nodeService.getProperties(nIg).containsKey(ContentModel.PROP_TITLE)
                            ? nodeService.getProperty(nIg, ContentModel.PROP_TITLE).toString()
                            : ""));
            ig.setSetOfLeaders(getListOfLeadersForInterestGroup(nIg));
            Serializable propertyCreated = nodeService.getProperty(nIg, ContentModel.PROP_CREATED);
            Date created = DefaultTypeConverter.INSTANCE.convert(Date.class, propertyCreated);
            ig.setCreationDate(simpleDateFormat.format(created));

            ig.setLastAccessDate(findLastAccessToIg(nIg, simpleDateFormat));
            ig.setLastUpdateDate(findLastUpdateToIg(nIg, simpleDateFormat));
            igStatisticsService.buildStatsData(nIg);
            ig.setNbDocuments(igStatisticsService.getNumberOfLibraryDocuments());
            ig.setNbMembers(
                    profilesApi
                            .groupsIdProfilesGet(categoryRef.getId(), "", false)
                            .size() /* igStatisticsService.getNumberOfUsers(nIg) */);
            ig.setNbEvents(
                    igStatisticsService.getNumbetOfEvents() + igStatisticsService.getNumbetOfMeetings());
            ig.setNbPosts(igStatisticsService.getNumberOfPosts());
            ig.setLibraryDocSize(
                    String.format("%.1f", igStatisticsService.getContentSizeOfLibrary() / Math.pow(1024, 2)));
            ig.setInformationInfoSize(
                    String.format(
                            "%.1f", igStatisticsService.getContentSizeOfInformation() / Math.pow(1024, 2)));
            ig.setDeepness(igStatisticsService.getMaxLevel());
            results.add(ig);
            logger.info(
                    "CategoryStatisticsJob: "
                            + categoryRef.getId()
                            + " Finished IG: "
                            + i
                            + "/"
                            + lGroups.size());
            i++;
        }

        return results;
    }

    private String findLastUpdateToIg(NodeRef nIg, SimpleDateFormat sdf) {

        Long igDbId = (Long) nodeService.getProperty(nIg, ContentModel.PROP_NODE_DBID);
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

        Long igDbId = (Long) nodeService.getProperty(nIg, ContentModel.PROP_NODE_DBID);
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

    @Override
    public NodeRef saveCategoryGroupStatistics(
            List<IgDescriptor> computedCategoryGroupStatistics, String categoryName) {

        Calendar cFile = new GregorianCalendar();
        cFile.setTime(new Date());
        final String fileName =
                "CategoryReport"
                        + cFile.get(Calendar.DAY_OF_MONTH)
                        + "-"
                        + (cFile.get(Calendar.MONTH) + 1)
                        + "-"
                        + cFile.get(Calendar.YEAR)
                        + "-"
                        + cFile.get(Calendar.HOUR_OF_DAY)
                        + "-"
                        + cFile.get(Calendar.MINUTE)
                        + "-"
                        + cFile.get(Calendar.MILLISECOND)
                        + ".xls";

        final NodeRef destinationFolder = getCategoryReportsFolderRecipient(categoryName);

        if (nodeService.getType(destinationFolder).equals(ContentModel.TYPE_FOLDER)) {
            final HSSFWorkbook wb = builtCategoryExcelFromData(computedCategoryGroupStatistics);

            RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
            return helper.doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
                        public NodeRef execute() throws Throwable {
                            return writeFile(destinationFolder, fileName, fileName, wb);
                        }
                    },
                    false,
                    true);

        } else {
            throw new CircabcRuntimeException(
                    "destination noderef:" + destinationFolder.toString() + " is not a valid folder");
        }
    }

    private HSSFWorkbook builtCategoryExcelFromData(
            List<IgDescriptor> computedCategoryGroupStatistics) {
        final HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheetNumbers = wb.createSheet("ig-statistics");

        Integer iRow = 0;

        HSSFRow row = sheetNumbers.createRow(iRow);
        row.createCell(0).setCellValue("IgName");
        row.createCell(1).setCellValue("Link");
        row.createCell(2).setCellValue("Creation date");
        row.createCell(3).setCellValue("Last accessed");
        row.createCell(4).setCellValue("Last updated");
        row.createCell(5).setCellValue("Nb documents");
        row.createCell(6).setCellValue("Size of documents (Mb)");
        row.createCell(7).setCellValue("Size of information files (Mb)");
        row.createCell(8).setCellValue("Nb events/meetings");
        row.createCell(9).setCellValue("Nb posts");
        row.createCell(10).setCellValue("Deepness");
        row.createCell(11).setCellValue("Nb Members");
        row.createCell(12).setCellValue("Leaders");
        row.createCell(13).setCellValue("Hyperlink");
        // HYPERLINK(link, IgName).

        iRow++;

        String rootUrl = CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);

        for (IgDescriptor ig : computedCategoryGroupStatistics) {
            HSSFRow rowTmp = sheetNumbers.createRow(iRow);

            rowTmp.createCell(0).setCellValue(ig.getTitle());
            rowTmp.createCell(1).setCellValue(rootUrl + "/w/browse/" + ig.getRef().getId());
            rowTmp.createCell(2).setCellValue(ig.getCreationDate());
            rowTmp.createCell(3).setCellValue(ig.getLastAccessDate());
            rowTmp.createCell(4).setCellValue(ig.getLastUpdateDate());
            rowTmp.createCell(5).setCellValue(ig.getNbDocuments());
            rowTmp.createCell(6).setCellValue(ig.getLibraryDocSize());
            rowTmp.createCell(7).setCellValue(ig.getInformationInfoSize());
            rowTmp.createCell(8).setCellValue(ig.getNbEvents());
            rowTmp.createCell(9).setCellValue(ig.getNbPosts());
            rowTmp.createCell(10).setCellValue(ig.getDeepness());
            rowTmp.createCell(11).setCellValue(ig.getNbMembers());
            rowTmp
                    .createCell(12)
                    .setCellValue(ig.getSetOfLeaders().toString().replace("[", "").replace("]", ""));
            final HSSFCell cell = rowTmp.createCell(13);
            cell.setCellType(2);
            cell.setCellFormula(
                    "HYPERLINK(B" + String.valueOf(iRow + 1) + ",A" + String.valueOf(iRow + 1) + ")");
            iRow++;
        }

        return wb;
    }
}
