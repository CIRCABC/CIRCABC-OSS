package eu.europa.ec.digit.circabc.rest.service.statistic.global;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.report.ReportDaoService;
import io.swagger.api.CircabcApi;
import io.swagger.exception.CircabcRuntimeException;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class GlobalStatisticsServiceTest {

  private GlobalStatisticsServiceImpl service;
  private NodeService nodeService;
  private SearchService searchService;
  private FileFolderService fileFolderService;
  private PersonService personService;
  private ReportDaoService reportDaoService;
  private LogService logService;
  private CircabcApi circabcApi;
  private CircabcService circabcService;
  private NamespacePrefixResolver namespacePrefixResolver;

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
    logService = mock(LogService.class);
    circabcApi = mock(CircabcApi.class);
    circabcService = mock(CircabcService.class);
    namespacePrefixResolver = mock(NamespacePrefixResolver.class);

    setField("nodeService", nodeService);
    setField("searchService", searchService);
    setField("fileFolderService", fileFolderService);
    setField("personService", personService);
    setField("reportDaoService", reportDaoService);
    setField("logService", logService);
    setField("circabcApi", circabcApi);
    setField("circabcService", circabcService);
    setField("namespacePrefixResolver", namespacePrefixResolver);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GlobalStatisticsServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testIsReportSaveFolderExisting_whenFolderExists_thenReturnsTrue()
    throws Exception {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    NodeRef statRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "stat"
    );
    NodeRef reportRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "report"
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
    ).thenReturn(circabcRef);
    when(
      nodeService.getChildByName(
        circabcRef,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(statRef);
    when(
      nodeService.getChildByName(
        statRef,
        ContentModel.ASSOC_CONTAINS,
        "reports"
      )
    ).thenReturn(reportRef);

    assertTrue(service.isReportSaveFolderExisting());
  }

  @Test
  public void testIsReportSaveFolderExisting_whenFolderMissing_thenReturnsFalse()
    throws Exception {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
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
    ).thenReturn(circabcRef);
    when(
      nodeService.getChildByName(
        circabcRef,
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

  @Test
  public void testGetLastLoginDateOfUser_whenUserNotFound_thenReturnsNull() {
    when(logService.getLastLoginDateOfUser("unknown")).thenReturn(null);

    assertNull(service.getLastLoginDateOfUser("unknown"));
  }

  @Test
  public void testGetListOfCircabcInterestGroups_whenCategoriesHaveIGs_thenReturnsAll() {
    NodeRef cat1 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat1"
    );
    NodeRef ig1 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig1");
    NodeRef ig2 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig2");
    NodeRef nonIg = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "nonIg"
    );

    when(circabcService.getCategories()).thenReturn(
      Collections.singletonList(cat1)
    );

    ChildAssociationRef assoc1 = mock(ChildAssociationRef.class);
    ChildAssociationRef assoc2 = mock(ChildAssociationRef.class);
    ChildAssociationRef assoc3 = mock(ChildAssociationRef.class);
    when(assoc1.getChildRef()).thenReturn(ig1);
    when(assoc2.getChildRef()).thenReturn(ig2);
    when(assoc3.getChildRef()).thenReturn(nonIg);

    when(nodeService.getChildAssocs(cat1)).thenReturn(
      Arrays.asList(assoc1, assoc2, assoc3)
    );
    when(nodeService.hasAspect(ig1, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(nodeService.hasAspect(ig2, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(nodeService.hasAspect(nonIg, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );

    List<NodeRef> result = service.getListOfCircabcInterestGroups();

    assertEquals(2, result.size());
    assertTrue(result.contains(ig1));
    assertTrue(result.contains(ig2));
    assertFalse(result.contains(nonIg));
  }

  @Test
  public void testGetListOfCircabcInterestGroups_whenNoCategories_thenReturnsEmpty() {
    when(circabcService.getCategories()).thenReturn(Collections.emptyList());

    List<NodeRef> result = service.getListOfCircabcInterestGroups();

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetListOfReportFiles_whenFolderHasFiles_thenReturnsList()
    throws Exception {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    NodeRef statRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "stat"
    );
    NodeRef reportRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "report"
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
    ).thenReturn(circabcRef);
    when(
      nodeService.getChildByName(
        circabcRef,
        ContentModel.ASSOC_CONTAINS,
        "statistics"
      )
    ).thenReturn(statRef);
    when(
      nodeService.getChildByName(
        statRef,
        ContentModel.ASSOC_CONTAINS,
        "reports"
      )
    ).thenReturn(reportRef);

    FileInfo file1 = mock(FileInfo.class);
    FileInfo file2 = mock(FileInfo.class);
    when(fileFolderService.listFiles(reportRef)).thenReturn(
      Arrays.asList(file1, file2)
    );

    List<FileInfo> result = service.getListOfReportFiles();

    assertEquals(2, result.size());
  }

  @Test(expected = CircabcRuntimeException.class)
  public void testSaveStatsToExcel_whenDestinationNotFolder_thenThrows()
    throws Exception {
    NodeRef dest = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "not-folder"
    );
    when(nodeService.getType(dest)).thenReturn(ContentModel.TYPE_CONTENT);

    service.saveStatsToExcel(dest, new HashMap<>());
  }

  @Test
  public void testGetCategoryReportsFolderRecipient_whenFolderExists_thenReturnsNodeRef()
    throws Exception {
    NodeRef dicoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dico"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc"
    );
    NodeRef categStatRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "categ-stat"
    );
    NodeRef categoryFolder = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "my-category"
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
    ).thenReturn(circabcRef);
    when(
      nodeService.getChildByName(
        circabcRef,
        ContentModel.ASSOC_CONTAINS,
        "category_statistics"
      )
    ).thenReturn(categStatRef);
    when(
      nodeService.getChildByName(
        categStatRef,
        ContentModel.ASSOC_CONTAINS,
        "TestCategory"
      )
    ).thenReturn(categoryFolder);

    NodeRef result = service.getCategoryReportsFolderRecipient("TestCategory");

    assertEquals(categoryFolder, result);
  }
}
