package eu.europa.ec.digit.circabc.rest.service.migration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.*;
import eu.cec.digit.circabc.migration.entities.generated.permissions.*;
import eu.cec.digit.circabc.migration.entities.generated.properties.*;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyType;
import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.keyword.Keyword;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordsService;
import eu.europa.ec.digit.circabc.rest.service.profile.ProfileService;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.DossierModel;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class IgExportServiceImplTest {

  private IgExportServiceImpl service;
  private NodeService nodeService;
  private ContentService contentService;
  private ProfileService profileService;
  private PersonService personService;
  private KeywordsService keywordsService;
  private DynamicPropertyService dynamicPropertyService;
  private EventService eventService;
  private VersionService versionService;
  private MultilingualContentService multilingualContentService;

  private static final NodeRef IG_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "ig-id"
  );
  private static final NodeRef CATEGORY_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "category-id"
  );
  private static final NodeRef HEADER_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "header-id"
  );
  private static final NodeRef LIBRARY_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "library-id"
  );
  private static final NodeRef DOC_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "doc-id"
  );
  private static final NodeRef FOLDER_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "folder-id"
  );
  private static final NodeRef NG_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "newsgroups-id"
  );
  private static final NodeRef INFO_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "information-id"
  );
  private static final NodeRef EVENTS_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "events-id"
  );
  private static final NodeRef KEYWORD_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "keyword-1"
  );

  @Before
  public void setUp() throws Exception {
    service = new IgExportServiceImpl();
    nodeService = mock(NodeService.class);
    contentService = mock(ContentService.class);
    profileService = mock(ProfileService.class);
    personService = mock(PersonService.class);
    keywordsService = mock(KeywordsService.class);
    dynamicPropertyService = mock(DynamicPropertyService.class);
    eventService = mock(EventService.class);
    versionService = mock(VersionService.class);
    multilingualContentService = mock(MultilingualContentService.class);

    setField("nodeService", nodeService);
    setField("contentService", contentService);
    setField("profileService", profileService);
    setField("personService", personService);
    setField("keywordsService", keywordsService);
    setField("dynamicPropertyService", dynamicPropertyService);
    setField("eventService", eventService);
    setField("versionService", versionService);
    setField("multilingualContentService", multilingualContentService);

    // Mock IG node properties
    when(nodeService.getProperty(IG_REF, ContentModel.PROP_NAME)).thenReturn(
      "TestIG"
    );
    when(nodeService.getProperty(IG_REF, ContentModel.PROP_TITLE)).thenReturn(
      "Test Interest Group"
    );
    when(
      nodeService.getProperty(IG_REF, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("A test IG");

    // Category structure
    ChildAssociationRef igParent = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      CATEGORY_REF,
      ContentModel.ASSOC_CONTAINS,
      IG_REF
    );
    when(nodeService.getPrimaryParent(IG_REF)).thenReturn(igParent);
    when(nodeService.exists(CATEGORY_REF)).thenReturn(true);
    when(
      nodeService.getProperty(CATEGORY_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestCategory");

    ChildAssociationRef catParent = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      HEADER_REF,
      ContentModel.ASSOC_CONTAINS,
      CATEGORY_REF
    );
    when(nodeService.getPrimaryParent(CATEGORY_REF)).thenReturn(catParent);
    when(nodeService.exists(HEADER_REF)).thenReturn(true);
    when(
      nodeService.getProperty(HEADER_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestHeader");

    // Default: no children, no profiles, no keywords, no dynamic properties
    when(nodeService.getChildAssocs(IG_REF)).thenReturn(
      Collections.emptyList()
    );
    when(profileService.getProfiles(IG_REF)).thenReturn(
      Collections.emptyList()
    );
    when(keywordsService.getKeywords(IG_REF)).thenReturn(
      Collections.emptyList()
    );
    when(dynamicPropertyService.getDynamicProperties(IG_REF)).thenReturn(
      Collections.emptyList()
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = IgExportServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  // ==================== Basic Export Tests ====================

  @Test
  public void testExportIg_basicMetadata() {
    ImportRoot root = service.exportIg(IG_REF);

    assertNotNull(root);
    assertNotNull(root.getCircabc());
    assertNotNull(root.getPersons());
    assertEquals(1, root.getCircabc().getCategoryHeaders().size());

    CategoryHeader header = root.getCircabc().getCategoryHeaders().get(0);
    assertEquals("TestHeader", header.getName().getValue().toString());
    assertEquals(1, header.getCategories().size());

    Category cat = header.getCategories().get(0);
    assertEquals("TestCategory", cat.getName().getValue().toString());
    assertEquals(1, cat.getInterestGroups().size());

    InterestGroup ig = cat.getInterestGroups().get(0);
    assertEquals("TestIG", ig.getName().getValue().toString());
    assertEquals("Test Interest Group", ig.getTitle().getValue().toString());
    assertEquals("A test IG", ig.getDescription().getValue().toString());
  }

  @Test
  public void testExportIg_contactInformation() {
    MLText contactML = new MLText();
    contactML.put(Locale.ENGLISH, "Contact us at support@test.eu");
    contactML.put(Locale.FRENCH, "Contactez-nous");
    when(
      nodeService.getProperty(IG_REF, CircabcModel.PROP_CONTACT_INFORMATION)
    ).thenReturn(contactML);
    when(
      nodeService.getProperty(IG_REF, CircabcModel.PROP_CAN_REGISTERED_APPLY)
    ).thenReturn(Boolean.TRUE);

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertTrue(ig.isAllowApply());
    assertFalse(ig.getI18NContactInfos().isEmpty());
    assertEquals(2, ig.getI18NContactInfos().size());
  }

  @Test
  public void testExportIg_applications() {
    Map<String, Object> applicants = new HashMap<>();
    applicants.put("user1", new Object());
    applicants.put("user2", new Object());
    when(
      nodeService.getProperty(IG_REF, CircabcModel.PROP_APPLICANTS)
    ).thenReturn((Serializable) applicants);

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getApplications());
    assertEquals(2, ig.getApplications().getApplications().size());
  }

  // ==================== Keyword Definitions ====================

  @Test
  public void testExportIg_keywordDefinitions() {
    Keyword kw = mock(Keyword.class);
    MLText kwValues = new MLText();
    kwValues.put(Locale.ENGLISH, "Environment");
    kwValues.put(Locale.FRENCH, "Environnement");
    when(kw.getMLValues()).thenReturn(kwValues);
    when(kw.getId()).thenReturn(KEYWORD_REF);
    when(keywordsService.getKeywords(IG_REF)).thenReturn(List.of(kw));

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getKeywordDefinitions());
    assertEquals(1, ig.getKeywordDefinitions().getDefinitions().size());
    KeywordDefinition def = ig.getKeywordDefinitions().getDefinitions().get(0);
    assertEquals(2, def.getI18NValues().size());
  }

  // ==================== Dynamic Property Definitions ====================

  @Test
  public void testExportIg_dynamicPropertyDefinitions() {
    DynamicProperty dp = mock(DynamicProperty.class);
    when(dp.getIndex()).thenReturn(1L);
    when(dp.getType()).thenReturn(DynamicPropertyType.TEXT_FIELD);
    MLText label = new MLText();
    label.put(Locale.ENGLISH, "Project Code");
    when(dp.getLabel()).thenReturn(label);
    when(dp.getValidValues()).thenReturn(null);
    when(dynamicPropertyService.getDynamicProperties(IG_REF)).thenReturn(
      List.of(dp)
    );

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getDynamicPropertyDefinitions());
    assertEquals(1, ig.getDynamicPropertyDefinitions().getDefinitions().size());
    DynamicPropertyDefinition def = ig
      .getDynamicPropertyDefinitions()
      .getDefinitions()
      .get(0);
    assertEquals(Integer.valueOf(1), def.getId());
    assertEquals(1, def.getI18NValues().size());
  }

  @Test
  public void testExportIg_dynamicPropertyWithSelectionCases() {
    DynamicProperty dp = mock(DynamicProperty.class);
    when(dp.getIndex()).thenReturn(2L);
    when(dp.getType()).thenReturn(DynamicPropertyType.SELECTION);
    MLText label = new MLText();
    label.put(Locale.ENGLISH, "Status");
    when(dp.getLabel()).thenReturn(label);
    when(dp.getValidValues()).thenReturn("Active\nArchived\nDraft");
    when(dynamicPropertyService.getDynamicProperties(IG_REF)).thenReturn(
      List.of(dp)
    );

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    DynamicPropertyDefinition def = ig
      .getDynamicPropertyDefinitions()
      .getDefinitions()
      .get(0);
    assertEquals(3, def.getSelectionCases().size());
    assertTrue(def.getSelectionCases().contains("Active"));
    assertTrue(def.getSelectionCases().contains("Archived"));
    assertTrue(def.getSelectionCases().contains("Draft"));
  }

  // ==================== Directory / Profiles ====================

  @Test
  public void testExportIg_profilesAndMembers() {
    io.swagger.model.db.Profile leaderProfile =
      new io.swagger.model.db.Profile();
    leaderProfile.setName("Leader");
    leaderProfile.setLibraryPermission("LibAdmin");
    leaderProfile.setInformationPermission("InfAdmin");
    leaderProfile.setDirectoryPermission("DirAdmin");
    leaderProfile.setEventPermission("EveAdmin");
    leaderProfile.setNewsgroupPermission("NwsAdmin");

    io.swagger.model.db.Profile guestProfile =
      new io.swagger.model.db.Profile();
    guestProfile.setName("guest");
    guestProfile.setLibraryPermission("LibNoAccess");

    when(profileService.getProfiles(IG_REF)).thenReturn(
      List.of(leaderProfile, guestProfile)
    );
    when(profileService.getPersonInProfile(IG_REF, "Leader")).thenReturn(
      Set.of("user1", "user2")
    );
    when(profileService.getPersonInProfile(IG_REF, "guest")).thenReturn(
      Collections.emptySet()
    );

    // Mock person lookups
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );
    when(personService.personExists("user1")).thenReturn(true);
    when(personService.personExists("user2")).thenReturn(true);
    when(personService.getPerson("user1")).thenReturn(personRef);
    when(personService.getPerson("user2")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("john@test.eu");

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    Directory dir = ig.getDirectory();
    assertNotNull(dir);
    assertNotNull(dir.getGuest());
    assertEquals(1, dir.getAccessProfiles().size());
    AccessProfile ap = dir.getAccessProfiles().get(0);
    assertEquals("Leader", ap.getName());
    assertEquals(2, ap.getUsers().size());

    // Persons
    assertEquals(2, root.getPersons().getPersons().size());
  }

  // ==================== Library Export with Document Metadata ====================

  @Test
  public void testExportIg_libraryWithDocument() {
    // Setup IG children: Library
    ChildAssociationRef libChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      IG_REF,
      ContentModel.ASSOC_CONTAINS,
      LIBRARY_REF
    );
    when(nodeService.getChildAssocs(IG_REF)).thenReturn(List.of(libChild));
    when(
      nodeService.getProperty(LIBRARY_REF, ContentModel.PROP_NAME)
    ).thenReturn("Library");

    // Library has one document
    ChildAssociationRef docChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      LIBRARY_REF,
      ContentModel.ASSOC_CONTAINS,
      DOC_REF
    );
    when(nodeService.getChildAssocs(LIBRARY_REF)).thenReturn(List.of(docChild));
    when(nodeService.getType(DOC_REF)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.getProperty(DOC_REF, ContentModel.PROP_NAME)).thenReturn(
      "report.pdf"
    );
    when(nodeService.getProperty(DOC_REF, ContentModel.PROP_TITLE)).thenReturn(
      "Annual Report"
    );
    when(
      nodeService.getProperty(DOC_REF, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("2024 report");

    // Content data
    ContentData cd = new ContentData(
      "store://content/123",
      "application/pdf",
      1024,
      "UTF-8"
    );
    when(
      nodeService.getProperty(DOC_REF, ContentModel.PROP_CONTENT)
    ).thenReturn(cd);

    // Document metadata
    when(
      nodeService.getProperty(DOC_REF, DocumentModel.PROP_STATUS)
    ).thenReturn("FINAL");
    when(
      nodeService.getProperty(DOC_REF, DocumentModel.PROP_SECURITY_RANKING)
    ).thenReturn("PUBLIC");
    Date issueDate = new Date();
    when(
      nodeService.getProperty(DOC_REF, DocumentModel.PROP_ISSUE_DATE)
    ).thenReturn(issueDate);
    when(
      nodeService.getProperty(DOC_REF, DocumentModel.PROP_REFERENCE)
    ).thenReturn("REF-001");
    when(nodeService.getProperty(DOC_REF, ContentModel.PROP_AUTHOR)).thenReturn(
      "Jane Smith"
    );

    // Audit
    Date created = new Date();
    when(
      nodeService.getProperty(DOC_REF, ContentModel.PROP_CREATED)
    ).thenReturn(created);
    when(
      nodeService.getProperty(DOC_REF, ContentModel.PROP_CREATOR)
    ).thenReturn("admin");
    when(
      nodeService.getProperty(DOC_REF, ContentModel.PROP_MODIFIED)
    ).thenReturn(created);
    when(
      nodeService.getProperty(DOC_REF, ContentModel.PROP_MODIFIER)
    ).thenReturn("admin");

    // No discussions, no keywords, no versions for this test
    when(
      nodeService.hasAspect(DOC_REF, ForumModel.ASPECT_DISCUSSABLE)
    ).thenReturn(false);

    when(nodeService.getType(DOC_REF)).thenReturn(ContentModel.TYPE_CONTENT);

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getLibrary());
    assertEquals(1, ig.getLibrary().getContents().size());

    Content content = ig.getLibrary().getContents().get(0);
    assertEquals("report.pdf", content.getName().getValue().toString());
    assertEquals("Annual Report", content.getTitle().getValue().toString());
    assertEquals("store://content/123", content.getUri());
    assertEquals("FINAL", content.getStatus().getValue().toString());
    assertEquals("PUBLIC", content.getSecurityRanking().getValue().toString());
    assertEquals("REF-001", content.getReference().getValue().toString());
    assertEquals("Jane Smith", content.getAuthor().getValue().toString());
    assertNotNull(content.getCreated());
    assertEquals("admin", content.getCreator().getValue().toString());
  }

  // ==================== Version History ====================

  @Test
  public void testExportIg_documentWithVersionHistory() {
    // Setup library with document
    setupLibraryWithSingleDoc();

    // Version history: 3 versions (1.0, 1.1, 2.0 - head)
    VersionHistory vh = mock(VersionHistory.class);
    Version v1 = mock(Version.class);
    Version v2 = mock(Version.class);
    Version v3 = mock(Version.class);
    NodeRef frozen1 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "frozen-1"
    );
    NodeRef frozen2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "frozen-2"
    );
    NodeRef frozen3 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "frozen-3"
    );

    when(v1.getVersionLabel()).thenReturn("1.0");
    when(v1.getFrozenStateNodeRef()).thenReturn(frozen1);
    when(v2.getVersionLabel()).thenReturn("1.1");
    when(v2.getFrozenStateNodeRef()).thenReturn(frozen2);
    when(v3.getVersionLabel()).thenReturn("2.0");
    when(v3.getFrozenStateNodeRef()).thenReturn(frozen3);

    // Alfresco returns newest first
    when(vh.getAllVersions()).thenReturn(List.of(v3, v2, v1));
    when(versionService.getVersionHistory(DOC_REF)).thenReturn(vh);

    ContentData cd1 = new ContentData(
      "store://v1",
      "application/pdf",
      100,
      "UTF-8"
    );
    ContentData cd2 = new ContentData(
      "store://v2",
      "application/pdf",
      200,
      "UTF-8"
    );
    when(
      nodeService.getProperty(frozen1, ContentModel.PROP_CONTENT)
    ).thenReturn(cd1);
    when(
      nodeService.getProperty(frozen2, ContentModel.PROP_CONTENT)
    ).thenReturn(cd2);

    ImportRoot root = service.exportIg(IG_REF);
    Content content = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0)
      .getLibrary()
      .getContents()
      .get(0);

    assertNotNull(content.getVersions());
    // Prior versions (1.0 and 1.1), head is 2.0
    assertEquals(2, content.getVersions().getVersions().size());
    assertEquals(
      "1.0",
      content
        .getVersions()
        .getVersions()
        .get(0)
        .getVersionLabel()
        .getValue()
        .toString()
    );
    assertEquals(
      "1.1",
      content
        .getVersions()
        .getVersions()
        .get(1)
        .getVersionLabel()
        .getValue()
        .toString()
    );
    assertEquals(
      "store://v1",
      content.getVersions().getVersions().get(0).getUri()
    );
    assertEquals("2.0", content.getVersionLabel().getValue().toString());
  }

  // ==================== Keyword References on Documents ====================

  @Test
  public void testExportIg_documentWithKeywords() {
    // Setup keyword definitions first
    Keyword kw = mock(Keyword.class);
    MLText kwValues = new MLText();
    kwValues.put(Locale.ENGLISH, "Climate");
    when(kw.getMLValues()).thenReturn(kwValues);
    when(kw.getId()).thenReturn(KEYWORD_REF);
    when(keywordsService.getKeywords(IG_REF)).thenReturn(List.of(kw));

    // Setup library with document
    setupLibraryWithSingleDoc();

    // Document has keyword reference
    when(
      nodeService.getProperty(DOC_REF, DocumentModel.PROP_KEYWORD)
    ).thenReturn((Serializable) List.of(KEYWORD_REF));

    ImportRoot root = service.exportIg(IG_REF);
    Content content = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0)
      .getLibrary()
      .getContents()
      .get(0);

    assertNotNull(content.getKeywords());
    assertEquals(1, content.getKeywords().getIds().size());
  }

  // ==================== Helper Methods ====================

  private void setupLibraryWithSingleDoc() {
    ChildAssociationRef libChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      IG_REF,
      ContentModel.ASSOC_CONTAINS,
      LIBRARY_REF
    );
    when(nodeService.getChildAssocs(IG_REF)).thenReturn(List.of(libChild));
    when(
      nodeService.getProperty(LIBRARY_REF, ContentModel.PROP_NAME)
    ).thenReturn("Library");

    ChildAssociationRef docChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      LIBRARY_REF,
      ContentModel.ASSOC_CONTAINS,
      DOC_REF
    );
    when(nodeService.getChildAssocs(LIBRARY_REF)).thenReturn(List.of(docChild));
    when(nodeService.getType(DOC_REF)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.getProperty(DOC_REF, ContentModel.PROP_NAME)).thenReturn(
      "doc.pdf"
    );

    ContentData cd = new ContentData(
      "store://content/doc",
      "application/pdf",
      500,
      "UTF-8"
    );
    when(
      nodeService.getProperty(DOC_REF, ContentModel.PROP_CONTENT)
    ).thenReturn(cd);
    when(
      nodeService.hasAspect(DOC_REF, ForumModel.ASPECT_DISCUSSABLE)
    ).thenReturn(false);
  }

  // ==================== Dossiers ====================

  @Test
  public void testExportIg_libraryWithDossier() {
    ChildAssociationRef libChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      IG_REF,
      ContentModel.ASSOC_CONTAINS,
      LIBRARY_REF
    );
    when(nodeService.getChildAssocs(IG_REF)).thenReturn(List.of(libChild));
    when(
      nodeService.getProperty(LIBRARY_REF, ContentModel.PROP_NAME)
    ).thenReturn("Library");

    NodeRef dossierRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "dossier-1"
    );
    ChildAssociationRef dossierChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      LIBRARY_REF,
      ContentModel.ASSOC_CONTAINS,
      dossierRef
    );
    when(nodeService.getChildAssocs(LIBRARY_REF)).thenReturn(
      List.of(dossierChild)
    );
    when(nodeService.getType(dossierRef)).thenReturn(
      DossierModel.TYPE_DOSSIER_SPACE
    );
    when(
      nodeService.getProperty(dossierRef, ContentModel.PROP_NAME)
    ).thenReturn("MyDossier");
    when(
      nodeService.getProperty(dossierRef, ContentModel.PROP_TITLE)
    ).thenReturn("Important Dossier");
    when(nodeService.getChildAssocs(dossierRef)).thenReturn(
      Collections.emptyList()
    );

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getLibrary());
    assertEquals(1, ig.getLibrary().getDossiers().size());
    assertEquals(
      "MyDossier",
      ig.getLibrary().getDossiers().get(0).getName().getValue().toString()
    );
  }

  // ==================== Discussions ====================

  @Test
  public void testExportIg_documentWithDiscussion() {
    setupLibraryWithSingleDoc();
    when(
      nodeService.hasAspect(DOC_REF, ForumModel.ASPECT_DISCUSSABLE)
    ).thenReturn(true);

    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "disc-forum"
    );
    NodeRef topicRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "disc-topic"
    );
    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "disc-post"
    );

    ChildAssociationRef discAssoc = new ChildAssociationRef(
      ForumModel.ASSOC_DISCUSSION,
      DOC_REF,
      ForumModel.ASSOC_DISCUSSION,
      forumRef
    );
    when(
      nodeService.getChildAssocs(
        DOC_REF,
        ForumModel.ASSOC_DISCUSSION,
        ForumModel.ASSOC_DISCUSSION
      )
    ).thenReturn(List.of(discAssoc));

    ChildAssociationRef topicAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      forumRef,
      ContentModel.ASSOC_CONTAINS,
      topicRef
    );
    when(nodeService.getChildAssocs(forumRef)).thenReturn(List.of(topicAssoc));
    when(nodeService.getType(topicRef)).thenReturn(ForumModel.TYPE_TOPIC);
    when(nodeService.getProperty(topicRef, ContentModel.PROP_NAME)).thenReturn(
      "Discussion Topic"
    );

    ChildAssociationRef postAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      topicRef,
      ContentModel.ASSOC_CONTAINS,
      postRef
    );
    when(nodeService.getChildAssocs(topicRef)).thenReturn(List.of(postAssoc));
    when(nodeService.getType(postRef)).thenReturn(ForumModel.TYPE_POST);

    ContentReader reader = mock(ContentReader.class);
    when(reader.exists()).thenReturn(true);
    when(reader.getContentString()).thenReturn("This is a comment");
    when(
      contentService.getReader(postRef, ContentModel.PROP_CONTENT)
    ).thenReturn(reader);

    ImportRoot root = service.exportIg(IG_REF);
    Content content = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0)
      .getLibrary()
      .getContents()
      .get(0);

    assertNotNull(content.getDiscussions());
    assertEquals(1, content.getDiscussions().getTopics().size());
    assertEquals(
      1,
      content.getDiscussions().getTopics().get(0).getMessages().size()
    );
    assertEquals(
      "This is a comment",
      content
        .getDiscussions()
        .getTopics()
        .get(0)
        .getMessages()
        .get(0)
        .getContent()
    );
  }

  // ==================== Newsgroups ====================

  @Test
  public void testExportIg_newsgroups() {
    ChildAssociationRef ngChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      IG_REF,
      ContentModel.ASSOC_CONTAINS,
      NG_REF
    );
    when(nodeService.getChildAssocs(IG_REF)).thenReturn(List.of(ngChild));
    when(nodeService.getProperty(NG_REF, ContentModel.PROP_NAME)).thenReturn(
      "Newsgroups"
    );

    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ng-forum"
    );
    ChildAssociationRef forumChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      NG_REF,
      ContentModel.ASSOC_CONTAINS,
      forumRef
    );
    when(nodeService.getChildAssocs(NG_REF)).thenReturn(List.of(forumChild));
    when(nodeService.getType(forumRef)).thenReturn(ForumModel.TYPE_FORUM);
    when(nodeService.getProperty(forumRef, ContentModel.PROP_NAME)).thenReturn(
      "General"
    );
    when(nodeService.getChildAssocs(forumRef)).thenReturn(
      Collections.emptyList()
    );

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getNewsgroups());
    assertEquals(1, ig.getNewsgroups().getFora().size());
    assertEquals(
      "General",
      ig.getNewsgroups().getFora().get(0).getName().getValue().toString()
    );
  }

  // ==================== Information with News ====================

  @Test
  public void testExportIg_informationWithNews() {
    ChildAssociationRef infoChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      IG_REF,
      ContentModel.ASSOC_CONTAINS,
      INFO_REF
    );
    when(nodeService.getChildAssocs(IG_REF)).thenReturn(List.of(infoChild));
    when(nodeService.getProperty(INFO_REF, ContentModel.PROP_NAME)).thenReturn(
      "Information"
    );

    NodeRef newsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "news-1"
    );
    ChildAssociationRef newsChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      INFO_REF,
      ContentModel.ASSOC_CONTAINS,
      newsRef
    );
    when(nodeService.getChildAssocs(INFO_REF)).thenReturn(List.of(newsChild));
    when(nodeService.getType(newsRef)).thenReturn(
      CircabcModel.TYPE_INFORMATION_NEWS
    );
    when(
      nodeService.hasAspect(newsRef, CircabcModel.ASPECT_INFORMATION_NEWS)
    ).thenReturn(true);
    when(nodeService.getProperty(newsRef, ContentModel.PROP_NAME)).thenReturn(
      "Breaking News"
    );
    when(nodeService.getProperty(newsRef, ContentModel.PROP_TITLE)).thenReturn(
      "Important Update"
    );
    when(
      nodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_CONTENT)
    ).thenReturn("<p>Hello</p>");
    when(
      nodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_PATTERN)
    ).thenReturn("pattern1");
    when(
      nodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_LAYOUT)
    ).thenReturn("layout1");
    when(
      nodeService.getProperty(newsRef, CircabcModel.PROP_NEWS_SIZE)
    ).thenReturn(3);
    when(nodeService.getChildAssocs(newsRef)).thenReturn(
      Collections.emptyList()
    );

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getInformation());
    assertEquals(1, ig.getInformation().getInfNews().size());
    InfNews news = ig.getInformation().getInfNews().get(0);
    assertEquals("Breaking News", news.getName().getValue().toString());
    assertEquals("<p>Hello</p>", news.getNewsContent());
    assertEquals("pattern1", news.getNewsPattern());
    assertEquals("layout1", news.getNewsLayout());
    assertEquals(Integer.valueOf(3), news.getNewsSize());
  }

  // ==================== Events ====================

  @Test
  public void testExportIg_events() {
    ChildAssociationRef evtChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      IG_REF,
      ContentModel.ASSOC_CONTAINS,
      EVENTS_REF
    );
    when(nodeService.getChildAssocs(IG_REF)).thenReturn(List.of(evtChild));
    when(
      nodeService.getProperty(EVENTS_REF, ContentModel.PROP_NAME)
    ).thenReturn("Events");

    // Mock eventService
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-root"
    );
    when(eventService.getIGsEventRoot("ig-id")).thenReturn(eventRoot);

    // One meeting under event root
    NodeRef meetingRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "meeting-1"
    );
    ChildAssociationRef meetingChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      eventRoot,
      ContentModel.ASSOC_CONTAINS,
      meetingRef
    );
    when(nodeService.getChildAssocs(eventRoot)).thenReturn(
      List.of(meetingChild)
    );
    when(
      nodeService.hasAspect(meetingRef, CircabcModel.ASPECT_EVENT)
    ).thenReturn(true);

    String nsUri = "http://www.cc.cec/circabc/model/events/1.0";
    when(
      nodeService.getProperty(
        meetingRef,
        QName.createQName(nsUri, "meetingType")
      )
    ).thenReturn("FaceToFace");
    when(
      nodeService.getProperty(
        meetingRef,
        QName.createQName(nsUri, "eventTitle")
      )
    ).thenReturn("Weekly Sync");
    when(
      nodeService.getProperty(meetingRef, QName.createQName(nsUri, "location"))
    ).thenReturn("Room 101");
    when(
      nodeService.getProperty(
        meetingRef,
        QName.createQName(nsUri, "availability")
      )
    ).thenReturn("Private");

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getEvents());
    assertEquals(1, ig.getEvents().getMeetings().size());
    Meeting meeting = ig.getEvents().getMeetings().get(0);
    assertEquals("Weekly Sync", meeting.getAppointmentTitle());
    assertEquals("Room 101", meeting.getLocation());
  }

  // ==================== Folders (recursive Space export) ====================

  @Test
  public void testExportIg_libraryWithFolders() {
    ChildAssociationRef libChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      IG_REF,
      ContentModel.ASSOC_CONTAINS,
      LIBRARY_REF
    );
    when(nodeService.getChildAssocs(IG_REF)).thenReturn(List.of(libChild));
    when(
      nodeService.getProperty(LIBRARY_REF, ContentModel.PROP_NAME)
    ).thenReturn("Library");

    ChildAssociationRef folderChild = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      LIBRARY_REF,
      ContentModel.ASSOC_CONTAINS,
      FOLDER_REF
    );
    when(nodeService.getChildAssocs(LIBRARY_REF)).thenReturn(
      List.of(folderChild)
    );
    when(nodeService.getType(FOLDER_REF)).thenReturn(ContentModel.TYPE_FOLDER);
    when(
      nodeService.getProperty(FOLDER_REF, ContentModel.PROP_NAME)
    ).thenReturn("SubFolder");
    when(
      nodeService.getProperty(FOLDER_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Sub Folder");
    when(nodeService.getChildAssocs(FOLDER_REF)).thenReturn(
      Collections.emptyList()
    );

    ImportRoot root = service.exportIg(IG_REF);
    InterestGroup ig = root
      .getCircabc()
      .getCategoryHeaders()
      .get(0)
      .getCategories()
      .get(0)
      .getInterestGroups()
      .get(0);

    assertNotNull(ig.getLibrary());
    assertEquals(1, ig.getLibrary().getSpaces().size());
    assertEquals(
      "SubFolder",
      ig.getLibrary().getSpaces().get(0).getName().getValue().toString()
    );
  }
}
