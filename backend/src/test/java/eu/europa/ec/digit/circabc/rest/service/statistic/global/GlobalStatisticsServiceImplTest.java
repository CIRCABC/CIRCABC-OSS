package eu.europa.ec.digit.circabc.rest.service.statistic.global;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.compress.ZipService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.report.ReportDaoService;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgStatisticsParameter;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgStatisticsService;
import io.swagger.api.CircabcApi;
import io.swagger.api.GroupsApi;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.CircabcRuntimeException;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.LogCountResultDAO;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class GlobalStatisticsServiceImplTest {

  private GlobalStatisticsServiceImpl service;

  private NodeService nodeService;
  private SearchService searchService;
  private FileFolderService fileFolderService;
  private PersonService personService;
  private ReportDaoService reportDaoService;
  private TransactionService transactionService;
  private ContentService contentService;
  private LogService logService;
  private ZipService zipService;
  private PermissionService permissionService;
  private IgStatisticsService igStatisticsService;
  private NamespacePrefixResolver namespacePrefixResolver;
  private CircabcConfig circabcConfig;
  private CircabcApi circabcApi;
  private GroupsApi groupsApi;
  private CircabcService circabcService;

  @Before
  public void setUp() throws Exception {
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

    service = new GlobalStatisticsServiceImpl();

    nodeService = mock(NodeService.class);
    searchService = mock(SearchService.class);
    fileFolderService = mock(FileFolderService.class);
    personService = mock(PersonService.class);
    reportDaoService = mock(ReportDaoService.class);
    transactionService = mock(TransactionService.class);
    contentService = mock(ContentService.class);
    logService = mock(LogService.class);
    zipService = mock(ZipService.class);
    permissionService = mock(PermissionService.class);
    igStatisticsService = mock(IgStatisticsService.class);
    namespacePrefixResolver = mock(NamespacePrefixResolver.class);
    circabcConfig = mock(CircabcConfig.class);
    circabcApi = mock(CircabcApi.class);
    groupsApi = mock(GroupsApi.class);
    circabcService = mock(CircabcService.class);

    setField("nodeService", nodeService);
    setField("searchService", searchService);
    setField("fileFolderService", fileFolderService);
    setField("personService", personService);
    setField("reportDaoService", reportDaoService);
    setField("transactionService", transactionService);
    setField("contentService", contentService);
    setField("logService", logService);
    setField("zipService", zipService);
    setField("permissionService", permissionService);
    setField("igStatisticsService", igStatisticsService);
    setField("namespacePrefixResolver", namespacePrefixResolver);
    setField("circabcConfig", circabcConfig);
    setField("circabcApi", circabcApi);
    setField("groupsApi", groupsApi);
    setField("circabcService", circabcService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GlobalStatisticsServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testMakeGlobalStats_whenDataAvailable_thenReturnsAllKeys() {
    NodeRef rootHeader = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root-header"
    );
    when(circabcApi.getRootCategoryHeader()).thenReturn(rootHeader);
    when(nodeService.getChildAssocs(rootHeader)).thenReturn(
      Collections.emptyList()
    );

    when(circabcService.getCategories()).thenReturn(Collections.emptyList());

    @SuppressWarnings("unchecked")
    PagingResults<PersonService.PersonInfo> pagingResults = mock(
      PagingResults.class
    );
    when(pagingResults.getPage()).thenReturn(Collections.emptyList());
    when(
      personService.getPeople(
        isNull(),
        isNull(),
        isNull(),
        any(PagingRequest.class)
      )
    ).thenReturn(pagingResults);

    when(reportDaoService.queryDbForNumberOfDocuments()).thenReturn(42);

    List<LogCountResultDAO> logCounts = new ArrayList<>();
    when(logService.getNumberOfActionsYesterdayPerHour()).thenReturn(logCounts);

    Map<String, Object> result = service.makeGlobalStats();

    assertNotNull(result);
    assertTrue(result.containsKey("numberOfCategoryHeaders"));
    assertTrue(result.containsKey("numberOfCategories"));
    assertTrue(result.containsKey("numberOfIgs"));
    assertTrue(result.containsKey("numberOfUsers"));
    assertTrue(result.containsKey("numberOfDocuments"));
    assertTrue(result.containsKey("listOfCircabcStructure"));
    assertTrue(result.containsKey("actionCountForYesterDay"));
    assertEquals(0, result.get("numberOfCategoryHeaders"));
    assertEquals(0, result.get("numberOfCategories"));
    assertEquals(0, result.get("numberOfIgs"));
    assertEquals(0, result.get("numberOfUsers"));
    assertEquals(42, result.get("numberOfDocuments"));
  }

  @Test
  public void testGetListOfCircabcInterestGroups_whenCategoriesHaveIgs_thenReturnsIgNodes() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    NodeRef nonIgRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "non-ig"
    );

    when(circabcService.getCategories()).thenReturn(
      Collections.singletonList(catRef)
    );

    ChildAssociationRef igAssoc = mock(ChildAssociationRef.class);
    when(igAssoc.getChildRef()).thenReturn(igRef);
    ChildAssociationRef nonIgAssoc = mock(ChildAssociationRef.class);
    when(nonIgAssoc.getChildRef()).thenReturn(nonIgRef);

    when(nodeService.getChildAssocs(catRef)).thenReturn(
      Arrays.asList(igAssoc, nonIgAssoc)
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(
      nodeService.hasAspect(nonIgRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    List<NodeRef> result = service.getListOfCircabcInterestGroups();

    assertEquals(1, result.size());
    assertEquals(igRef, result.get(0));
  }

  @Test
  public void testIsReportSaveFolderExisting_whenFolderExists_thenReturnsTrue() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-folder"
    );
    NodeRef statFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "stat-folder"
    );
    NodeRef reportFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "report-folder"
    );

    when(
      searchService.selectNodes(
        any(NodeRef.class),
        anyString(),
        isNull(),
        any(NamespacePrefixResolver.class),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
    ).thenReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "root"));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcFolder);
    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(statFolder);
    when(
      nodeService.getChildByName(
        statFolder,
        ContentModel.ASSOC_CONTAINS,
        "reports"
      )
    ).thenReturn(reportFolder);

    assertTrue(service.isReportSaveFolderExisting());
  }

  @Test
  public void testIsReportSaveFolderExisting_whenFolderMissing_thenReturnsFalse() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-folder"
    );

    when(
      nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
    ).thenReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "root"));
    when(
      searchService.selectNodes(
        any(NodeRef.class),
        anyString(),
        isNull(),
        any(NamespacePrefixResolver.class),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcFolder);
    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(null);

    assertFalse(service.isReportSaveFolderExisting());
  }

  @Test
  public void testGetLastLoginDateOfUser_whenCalled_thenDelegatesToLogService() {
    Date expected = new Date();
    when(logService.getLastLoginDateOfUser("admin")).thenReturn(expected);

    Date result = service.getLastLoginDateOfUser("admin");

    assertEquals(expected, result);
    verify(logService).getLastLoginDateOfUser("admin");
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testSaveStatsToExcel_whenDestinationNotFolder_thenThrows() {
    NodeRef dest = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "not-folder"
    );
    when(nodeService.getType(dest)).thenReturn(ContentModel.TYPE_CONTENT);

    service.saveStatsToExcel(dest, new HashMap<>());
  }

  @Test
  public void testMakeDetailedIgStats_whenCalled_thenReturnsDetailedIgList() {
    when(circabcService.getCategories()).thenReturn(Collections.emptyList());

    Map<String, Object> result = service.makeDetailedIgStats();

    assertNotNull(result);
    assertTrue(result.containsKey("detailedIgList"));
    assertTrue(((List<?>) result.get("detailedIgList")).isEmpty());
  }

  // --- getListOfCircabcInterestGroups with multiple categories ---

  @Test
  public void testGetListOfCircabcInterestGroups_whenMultipleCategories_thenAggregates() {
    NodeRef cat1 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    NodeRef cat2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-2"
    );
    NodeRef ig1 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-1");
    NodeRef ig2 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-2");

    when(circabcService.getCategories()).thenReturn(Arrays.asList(cat1, cat2));

    ChildAssociationRef assoc1 = mock(ChildAssociationRef.class);
    when(assoc1.getChildRef()).thenReturn(ig1);
    when(nodeService.getChildAssocs(cat1)).thenReturn(
      Collections.singletonList(assoc1)
    );
    when(nodeService.hasAspect(ig1, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    ChildAssociationRef assoc2 = mock(ChildAssociationRef.class);
    when(assoc2.getChildRef()).thenReturn(ig2);
    when(nodeService.getChildAssocs(cat2)).thenReturn(
      Collections.singletonList(assoc2)
    );
    when(nodeService.hasAspect(ig2, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    List<NodeRef> result = service.getListOfCircabcInterestGroups();

    assertEquals(2, result.size());
    assertTrue(result.contains(ig1));
    assertTrue(result.contains(ig2));
  }

  // --- isReportSaveFolderExisting edge cases ---

  @Test
  public void testIsReportSaveFolderExisting_whenCircabcFolderNull_thenReturnsFalse() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );

    when(
      nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
    ).thenReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "root"));
    when(
      searchService.selectNodes(
        any(NodeRef.class),
        anyString(),
        isNull(),
        any(NamespacePrefixResolver.class),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(null);

    assertFalse(service.isReportSaveFolderExisting());
  }

  // --- saveStatsToExcel with valid folder ---

  @Test(expected = CircabcRuntimeException.class)
  public void testSaveStatsToExcel_whenDestinationNotFolder_thenThrows2() {
    NodeRef dest = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "not-folder-2"
    );
    when(nodeService.getType(dest)).thenReturn(ContentModel.TYPE_CONTENT);

    service.saveStatsToExcel(dest, new HashMap<>());
  }

  // --- getReportSaveFolder ---

  @Test
  public void testGetReportSaveFolder_whenFoldersExist_thenReturnsReportFolder() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    NodeRef statFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "stats"
    );
    NodeRef reportFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "reports"
    );

    when(
      searchService.selectNodes(
        any(),
        eq("/app:company_home/app:dictionary"),
        isNull(),
        any(),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcFolder);
    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(statFolder);
    when(
      nodeService.getChildByName(
        statFolder,
        ContentModel.ASSOC_CONTAINS,
        "reports"
      )
    ).thenReturn(reportFolder);

    NodeRef result = service.getReportSaveFolder();
    assertEquals(reportFolder, result);
  }

  @Test
  public void testGetReportSaveFolder_whenStatFolderNull_thenReturnsNull() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );

    when(
      searchService.selectNodes(
        any(),
        eq("/app:company_home/app:dictionary"),
        isNull(),
        any(),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcFolder);
    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(null);

    NodeRef result = service.getReportSaveFolder();
    assertNull(result);
  }

  // --- getListOfReportFiles ---

  @Test
  public void testGetListOfReportFiles_whenFilesExist_thenReturnsList() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    NodeRef statFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "stats"
    );
    NodeRef reportFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "reports"
    );

    when(
      searchService.selectNodes(
        any(),
        eq("/app:company_home/app:dictionary"),
        isNull(),
        any(),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcFolder);
    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(statFolder);
    when(
      nodeService.getChildByName(
        statFolder,
        ContentModel.ASSOC_CONTAINS,
        "reports"
      )
    ).thenReturn(reportFolder);

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileFolderService.listFiles(reportFolder)).thenReturn(
      Collections.singletonList(fileInfo)
    );

    List<FileInfo> result = service.getListOfReportFiles();
    assertEquals(1, result.size());
  }

  // --- prepareFolderRecipient ---

  @Test
  public void testPrepareFolderRecipient_whenStatFolderExists_thenDoesNotCreate() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    NodeRef statFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "stats"
    );

    when(
      searchService.selectNodes(
        any(),
        eq("/app:company_home/app:dictionary"),
        isNull(),
        any(),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcFolder);
    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(statFolder);

    service.prepareFolderRecipient();

    verify(fileFolderService, never()).create(any(), anyString(), any());
  }

  @Test
  public void testPrepareFolderRecipient_whenStatFolderMissing_thenCreatesIt() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    NodeRef newStatFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-stats"
    );

    when(
      searchService.selectNodes(
        any(),
        eq("/app:company_home/app:dictionary"),
        isNull(),
        any(),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcFolder);
    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(null);

    FileInfo statFolderInfo = mock(FileInfo.class);
    when(statFolderInfo.getNodeRef()).thenReturn(newStatFolder);
    when(
      fileFolderService.create(
        circabcFolder,
        "statistics",
        ContentModel.TYPE_FOLDER
      )
    ).thenReturn(statFolderInfo);

    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    )
      .thenReturn(null)
      .thenReturn(newStatFolder);
    when(circabcApi.getInvitedUsersGroupName()).thenReturn("GROUP_invited");

    service.prepareFolderRecipient();

    verify(fileFolderService).create(
      circabcFolder,
      "statistics",
      ContentModel.TYPE_FOLDER
    );
    verify(fileFolderService).create(
      newStatFolder,
      "reports",
      ContentModel.TYPE_FOLDER
    );
  }

  // --- saveStatsToExcel happy path ---

  @Test
  public void testSaveStatsToExcel_whenValidFolder_thenDelegatesToTransaction() {
    NodeRef dest = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder"
    );
    when(nodeService.getType(dest)).thenReturn(ContentModel.TYPE_FOLDER);

    org.alfresco.repo.transaction.RetryingTransactionHelper helper = mock(
      org.alfresco.repo.transaction.RetryingTransactionHelper.class
    );
    when(transactionService.getRetryingTransactionHelper()).thenReturn(helper);
    NodeRef resultRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "result"
    );
    when(helper.doInTransaction(any(), eq(false), eq(true))).thenReturn(
      resultRef
    );

    when(circabcConfig.getWebRootUrl()).thenReturn("http://localhost");
    when(circabcConfig.getNewUiContext()).thenReturn("/ui/");
    when(circabcService.getCategories()).thenReturn(Collections.emptyList());
    when(logService.getNumberOfActionsYesterdayPerHour()).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> data = new HashMap<>();
    data.put("numberOfCategoryHeaders", 1);
    data.put("numberOfCategories", 2);
    data.put("numberOfIgs", 3);
    data.put("numberOfUsers", 10);
    data.put("numberOfDocuments", 100);
    data.put("numberOfIgsPerCategory", new HashMap<String, Integer>());
    data.put("actionCountForYesterDay", new ArrayList<LogCountResultDAO>());
    data.put("listOfCircabcStructure", new ArrayList<>());

    NodeRef result = service.saveStatsToExcel(dest, data);
    assertEquals(resultRef, result);
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testSaveDetailedIgStatsToExcel_whenNotFolder_thenThrows() {
    NodeRef dest = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "not-folder"
    );
    when(nodeService.getType(dest)).thenReturn(ContentModel.TYPE_CONTENT);

    service.saveDetailedIgStatsToExcel(dest, new HashMap<>());
  }

  @Test
  public void testSaveDetailedIgStatsToExcel_whenValidFolder_thenDelegatesToTransaction() {
    NodeRef dest = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder"
    );
    when(nodeService.getType(dest)).thenReturn(ContentModel.TYPE_FOLDER);

    org.alfresco.repo.transaction.RetryingTransactionHelper helper = mock(
      org.alfresco.repo.transaction.RetryingTransactionHelper.class
    );
    when(transactionService.getRetryingTransactionHelper()).thenReturn(helper);
    NodeRef resultRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "result"
    );
    when(helper.doInTransaction(any(), eq(false), eq(true))).thenReturn(
      resultRef
    );

    Map<String, Object> data = new HashMap<>();
    data.put("detailedIgList", new ArrayList<>());

    NodeRef result = service.saveDetailedIgStatsToExcel(dest, data);
    assertEquals(resultRef, result);
  }

  // --- computeCategoryGroupStatistics ---

  @Test
  public void testComputeCategoryGroupStatistics_whenIgsExist_thenReturnsDescriptors() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );

    ChildAssociationRef igAssoc = mock(ChildAssociationRef.class);
    when(igAssoc.getChildRef()).thenReturn(igRef);
    when(nodeService.getChildAssocs(catRef)).thenReturn(
      Collections.singletonList(igAssoc)
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    Map<org.alfresco.service.namespace.QName, java.io.Serializable> props =
      new HashMap<>();
    props.put(ContentModel.PROP_NAME, "test-ig");
    props.put(ContentModel.PROP_TITLE, "Test IG");
    props.put(ContentModel.PROP_CREATED, new Date());
    props.put(ContentModel.PROP_NODE_DBID, 123L);
    when(nodeService.getProperties(igRef)).thenReturn(props);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "test-ig"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "Test IG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_CREATED)).thenReturn(
      new Date()
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(123L);

    when(circabcService.getInterestGroupAdminEmails(igRef)).thenReturn(
      Collections.singletonList("admin@test.com")
    );

    IgStatisticsParameter stats = mock(IgStatisticsParameter.class);
    when(stats.getLibraryDocumentCount()).thenReturn(10);
    when(stats.getNbUsers()).thenReturn(5);
    when(stats.getEventCount()).thenReturn(2);
    when(stats.getMeetingCount()).thenReturn(1);
    when(stats.getPostCount()).thenReturn(3);
    when(stats.getLibrarySize()).thenReturn(1048576L);
    when(stats.getInformationSize()).thenReturn(524288L);
    when(stats.getMaxLevel()).thenReturn(3);
    when(igStatisticsService.buildStatsData(igRef)).thenReturn(stats);

    when(logService.getLastAccessOnInterestGroup(123L)).thenReturn(new Date());
    when(logService.getLastUpdateOnInterestGroup(123L)).thenReturn(new Date());

    List<
      eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgDescriptor
    > result = service.computeCategoryGroupStatistics(catRef);

    assertEquals(1, result.size());
    assertEquals("test-ig", result.get(0).getName());
    assertEquals(
      Integer.valueOf(10),
      Integer.valueOf(result.get(0).getNbDocuments())
    );
    assertEquals(
      Integer.valueOf(5),
      Integer.valueOf(result.get(0).getNbMembers())
    );
  }

  @Test
  public void testComputeCategoryGroupStatistics_whenNoIgs_thenReturnsEmpty() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-empty"
    );
    when(nodeService.getChildAssocs(catRef)).thenReturn(
      Collections.emptyList()
    );

    List<
      eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgDescriptor
    > result = service.computeCategoryGroupStatistics(catRef);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testComputeCategoryGroupStatistics_whenLastAccessNull_thenEmptyString() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );

    ChildAssociationRef igAssoc = mock(ChildAssociationRef.class);
    when(igAssoc.getChildRef()).thenReturn(igRef);
    when(nodeService.getChildAssocs(catRef)).thenReturn(
      Collections.singletonList(igAssoc)
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    Map<org.alfresco.service.namespace.QName, java.io.Serializable> props =
      new HashMap<>();
    props.put(ContentModel.PROP_NAME, "ig-no-access");
    props.put(ContentModel.PROP_CREATED, new Date());
    props.put(ContentModel.PROP_NODE_DBID, 456L);
    when(nodeService.getProperties(igRef)).thenReturn(props);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "ig-no-access"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_CREATED)).thenReturn(
      new Date()
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(456L);

    when(circabcService.getInterestGroupAdminEmails(igRef)).thenReturn(
      Collections.emptyList()
    );

    IgStatisticsParameter stats = mock(IgStatisticsParameter.class);
    when(stats.getLibraryDocumentCount()).thenReturn(0);
    when(stats.getNbUsers()).thenReturn(0);
    when(stats.getEventCount()).thenReturn(0);
    when(stats.getMeetingCount()).thenReturn(0);
    when(stats.getPostCount()).thenReturn(0);
    when(stats.getLibrarySize()).thenReturn(0L);
    when(stats.getInformationSize()).thenReturn(0L);
    when(stats.getMaxLevel()).thenReturn(0);
    when(igStatisticsService.buildStatsData(igRef)).thenReturn(stats);

    when(logService.getLastAccessOnInterestGroup(456L)).thenReturn(null);
    when(logService.getLastUpdateOnInterestGroup(456L)).thenReturn(null);

    List<
      eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgDescriptor
    > result = service.computeCategoryGroupStatistics(catRef);

    assertEquals(1, result.size());
    assertEquals("", result.get(0).getLastAccessDate());
    assertEquals("", result.get(0).getLastUpdateDate());
  }

  // --- saveCategoryGroupStatistics ---

  @Test(expected = CircabcRuntimeException.class)
  public void testSaveCategoryGroupStatistics_whenNotFolder_thenThrows() {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    NodeRef categStatFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "categ-stat"
    );
    NodeRef catFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-folder"
    );

    when(
      nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
    ).thenReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "root"));
    when(
      searchService.selectNodes(any(), anyString(), isNull(), any(), eq(false))
    ).thenReturn(Collections.singletonList(dicoRef));
    when(
      nodeService.getChildByName(
        dicoRef,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcFolder);
    when(
      nodeService.getChildByName(
        circabcFolder,
        ContentModel.ASSOC_CONTAINS,
        "category_statistics"
      )
    ).thenReturn(categStatFolder);
    when(
      nodeService.getChildByName(
        categStatFolder,
        ContentModel.ASSOC_CONTAINS,
        "TestCat"
      )
    ).thenReturn(catFolder);
    when(nodeService.getType(catFolder)).thenReturn(ContentModel.TYPE_CONTENT);

    service.saveCategoryGroupStatistics(new ArrayList<>(), "TestCat");
  }

  // --- setters/getters ---

  @Test
  public void testSettersAndGetters() {
    TransactionService ts = mock(TransactionService.class);
    service.setTransactionService(ts);
    assertEquals(ts, service.getTransactionService());

    ContentService cs = mock(ContentService.class);
    service.setContentService(cs);
    assertEquals(cs, service.getContentService());

    ZipService zs = mock(ZipService.class);
    service.setZipService(zs);
    assertEquals(zs, service.getZipService());

    IgStatisticsService iss = mock(IgStatisticsService.class);
    service.setIgStatisticsService(iss);
    assertEquals(iss, service.getIgStatisticsService());
  }

  // --- makeGlobalStats with categories ---

  @Test
  public void testMakeGlobalStats_whenCategoriesExist_thenCountsThem() {
    NodeRef rootHeader = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root-header"
    );
    when(circabcApi.getRootCategoryHeader()).thenReturn(rootHeader);

    NodeRef headerChild = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "header-1"
    );
    ChildAssociationRef headerAssoc = mock(ChildAssociationRef.class);
    when(headerAssoc.getChildRef()).thenReturn(headerChild);
    when(nodeService.getChildAssocs(rootHeader)).thenReturn(
      Collections.singletonList(headerAssoc)
    );

    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    ChildAssociationRef catAssoc = mock(ChildAssociationRef.class);
    when(catAssoc.getChildRef()).thenReturn(catRef);
    when(nodeService.getChildAssocs(headerChild)).thenReturn(
      Collections.singletonList(catAssoc)
    );
    when(
      nodeService.hasAspect(catRef, CircabcModel.ASPECT_CATEGORY)
    ).thenReturn(true);

    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    ChildAssociationRef igAssoc = mock(ChildAssociationRef.class);
    when(igAssoc.getChildRef()).thenReturn(igRef);
    when(nodeService.getChildAssocs(catRef)).thenReturn(
      Collections.singletonList(igAssoc)
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    when(circabcService.getCategories()).thenReturn(Collections.emptyList());

    @SuppressWarnings("unchecked")
    PagingResults<PersonService.PersonInfo> pagingResults = mock(
      PagingResults.class
    );
    when(pagingResults.getPage()).thenReturn(Collections.emptyList());
    when(
      personService.getPeople(
        isNull(),
        isNull(),
        isNull(),
        any(PagingRequest.class)
      )
    ).thenReturn(pagingResults);

    when(logService.getNumberOfActionsYesterdayPerHour()).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> result = service.makeGlobalStats();

    assertNotNull(result);
    assertEquals(1, result.get("numberOfCategoryHeaders"));
    assertTrue(result.containsKey("numberOfCategories"));
    assertTrue(result.containsKey("numberOfIgs"));
    assertTrue(result.containsKey("numberOfUsers"));
    assertTrue(result.containsKey("actionCountForYesterDay"));
  }
}
