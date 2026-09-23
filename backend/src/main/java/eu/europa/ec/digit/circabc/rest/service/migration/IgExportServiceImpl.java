package eu.europa.ec.digit.circabc.rest.service.migration;

import eu.cec.digit.circabc.migration.entities.TypedProperty.*;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.Statistics;
import eu.cec.digit.circabc.migration.entities.generated.VersionHistory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.*;
import eu.cec.digit.circabc.migration.entities.generated.permissions.*;
import eu.cec.digit.circabc.migration.entities.generated.properties.*;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.keyword.Keyword;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordsService;
import eu.europa.ec.digit.circabc.rest.service.profile.ProfileService;
import io.swagger.api.ContentApi;
import io.swagger.api.GroupsApi;
import io.swagger.api.KeywordsApi;
import io.swagger.api.NotificationsApi;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.DossierModel;
import java.io.Serializable;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link IgExportService}.
 *
 * <p>Exports a complete Interest Group into the {@link ImportRoot} JAXB structure,
 * capturing all data that the import service can consume for a full round-trip.
 */
public class IgExportServiceImpl implements IgExportService {

  private static final Log logger = LogFactory.getLog(
    IgExportServiceImpl.class
  );
  private static final String GUEST = "guest";
  private static final String EVERYONE = "EVERYONE";
  private static final String PERM_NO_ACCESS = "NoAccess";
  private static final String EVENTS_MODEL_NS =
    "http://www.cc.cec/circabc/model/events/1.0";

  @Autowired
  @Qualifier("NodeService")
  private NodeService nodeService;

  @Autowired
  @Qualifier("ContentService")
  private ContentService contentService;

  @Autowired
  private ProfileService profileService;

  @Autowired
  @Qualifier("personService")
  private PersonService personService;

  @Autowired
  private KeywordsApi keywordsApi;

  @Autowired
  private NotificationsApi notificationsApi;

  @Autowired
  private GroupsApi groupsApi;

  @Autowired
  private ContentApi contentApi;

  @Autowired
  private KeywordsService keywordsService;

  @Autowired
  private DynamicPropertyService dynamicPropertyService;

  @Autowired
  private EventService eventService;

  @Autowired
  private VersionService versionService;

  @Autowired
  @Qualifier("MultilingualContentService")
  private MultilingualContentService multilingualContentService;

  /** Map of keyword NodeRef id -> integer id for export cross-referencing. */
  private Map<String, Integer> keywordIdMap;
  private int keywordIdSeq;

  @Override
  public ImportRoot exportIg(NodeRef igRef) {
    Circabc circabc = new Circabc();
    Persons persons = new Persons();
    ImportRoot root = new ImportRoot(
      circabc,
      persons,
      new LogFile(),
      new VersionHistory(),
      new Statistics()
    );

    // Initialize keyword map for cross-referencing
    keywordIdMap = new HashMap<>();
    keywordIdSeq = 1;

    InterestGroup ig = createIgExport(igRef);
    exportIgProperties(igRef, ig);
    exportDefinitions(igRef, ig);
    exportIgServices(igRef, ig);
    ig.setDirectory(exportDirectory(igRef));
    exportPersons(igRef, persons);
    buildCategoryStructure(igRef, ig, circabc);
    return root;
  }

  // ==================== IG-level properties ====================

  private InterestGroup createIgExport(NodeRef igRef) {
    InterestGroup ig = new InterestGroup();
    String name = (String) nodeService.getProperty(
      igRef,
      ContentModel.PROP_NAME
    );
    ig.setName(new NameProperty(name));
    String title = (String) nodeService.getProperty(
      igRef,
      ContentModel.PROP_TITLE
    );
    if (title != null) ig.setTitle(new TitleProperty(title));
    String desc = (String) nodeService.getProperty(
      igRef,
      ContentModel.PROP_DESCRIPTION
    );
    if (desc != null) ig.setDescription(new DescriptionProperty(desc));
    // Record the IG's source NodeRef so old deep links to the IG root (or one of
    // its service roots) can be resolved to the migrated IG on import.
    ig.setOriginalNodeRef(igRef);
    return ig;
  }

  private void exportIgProperties(NodeRef igRef, InterestGroup ig) {
    // Contact information
    try {
      Serializable contactRaw = nodeService.getProperty(
        igRef,
        CircabcModel.PROP_CONTACT_INFORMATION
      );
      if (contactRaw instanceof MLText mlText) {
        for (Map.Entry<Locale, String> entry : mlText.entrySet()) {
          ig
            .getI18NContactInfos()
            .add(new I18NProperty(entry.getKey(), entry.getValue()));
        }
      } else if (contactRaw instanceof String s && !s.isEmpty()) {
        ig.setContactInfo(new ContactInfoProperty(s));
      }
    } catch (Exception e) {
      logger.debug("Could not export contact info: " + e.getMessage());
    }

    // Allow apply flag
    try {
      Boolean allowApply = (Boolean) nodeService.getProperty(
        igRef,
        CircabcModel.PROP_CAN_REGISTERED_APPLY
      );
      if (allowApply != null) ig.setAllowApply(allowApply);
    } catch (Exception e) {
      logger.debug("Could not export allowApply: " + e.getMessage());
    }

    // Pending membership applications
    try {
      @SuppressWarnings("unchecked")
      Map<String, ?> applicants = (Map<String, ?>) nodeService.getProperty(
        igRef,
        CircabcModel.PROP_APPLICANTS
      );
      if (applicants != null && !applicants.isEmpty()) {
        Applications apps = new Applications();
        for (Map.Entry<String, ?> entry : applicants.entrySet()) {
          Application app = new Application();
          app.setUser(entry.getKey());
          app.setDate(new Date());
          app.setMessage("");
          apps.getApplications().add(app);
        }
        ig.setApplications(apps);
      }
    } catch (Exception e) {
      logger.debug("Could not export applications: " + e.getMessage());
    }
  }

  // ==================== Keyword & Dynamic Property Definitions ====================

  private void exportDefinitions(NodeRef igRef, InterestGroup ig) {
    exportKeywordDefinitions(igRef, ig);
    exportDynamicPropertyDefinitions(igRef, ig);
  }

  /** Exports the IG's keyword definitions onto the {@code interestGroup} element. */
  private void exportKeywordDefinitions(NodeRef igRef, InterestGroup ig) {
    try {
      List<Keyword> keywords = keywordsService.getKeywords(igRef);
      if (keywords != null && !keywords.isEmpty()) {
        KeywordDefinitions kwDefs = new KeywordDefinitions();
        for (Keyword kw : keywords) {
          kwDefs.getDefinitions().add(buildKeywordDefinition(kw));
        }
        ig.setKeywordDefinitions(kwDefs);
      }
    } catch (Exception e) {
      logger.debug("Could not export keyword definitions: " + e.getMessage());
    }
  }

  /** Builds one keyword definition, assigning its export id and recording the id mapping. */
  private eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition buildKeywordDefinition(
    Keyword kw
  ) {
    eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition def =
      new eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition();
    int id = keywordIdSeq++;
    def.setId(id);
    if (kw.getId() != null) {
      keywordIdMap.put(kw.getId().getId(), id);
    }
    if (kw.getMLValues() != null) {
      for (Map.Entry<Locale, String> entry : kw.getMLValues().entrySet()) {
        def
          .getI18NValues()
          .add(new I18NProperty(entry.getKey(), entry.getValue()));
      }
    } else if (kw.getValue() != null) {
      def.setValue(kw.getValue());
    }
    return def;
  }

  /** Exports the IG's dynamic property definitions onto the {@code interestGroup} element. */
  private void exportDynamicPropertyDefinitions(
    NodeRef igRef,
    InterestGroup ig
  ) {
    try {
      List<DynamicProperty> dynProps =
        dynamicPropertyService.getDynamicProperties(igRef);
      if (dynProps != null && !dynProps.isEmpty()) {
        DynamicPropertyDefinitions dpDefs = new DynamicPropertyDefinitions();
        for (DynamicProperty dp : dynProps) {
          dpDefs.getDefinitions().add(buildDynamicPropertyDefinition(dp));
        }
        ig.setDynamicPropertyDefinitions(dpDefs);
      }
    } catch (Exception e) {
      logger.debug(
        "Could not export dynamic property definitions: " + e.getMessage()
      );
    }
  }

  /** Builds one dynamic property definition from the repository model. */
  private DynamicPropertyDefinition buildDynamicPropertyDefinition(
    DynamicProperty dp
  ) {
    DynamicPropertyDefinition def = new DynamicPropertyDefinition();
    if (dp.getIndex() != null) {
      def.setId(dp.getIndex().intValue());
    }
    if (dp.getType() != null) {
      def.setType(
        parseOrDefault(
          () -> DynPropertyType.fromValue(dp.getType().name()),
          DynPropertyType.TEXT_FIELD
        )
      );
    }
    if (dp.getLabel() != null) {
      for (Map.Entry<Locale, String> entry : dp.getLabel().entrySet()) {
        def
          .getI18NValues()
          .add(new I18NProperty(entry.getKey(), entry.getValue()));
      }
    }
    if (dp.getValidValues() != null && !dp.getValidValues().isEmpty()) {
      String[] cases = dp
        .getValidValues()
        .split(DynamicPropertyService.MULTI_VALUES_SEPARATOR_STRING);
      for (String c : cases) {
        def.getSelectionCases().add(c);
      }
    }
    return def;
  }

  // ==================== Directory (profiles + members) ====================

  private Directory exportDirectory(NodeRef igRef) {
    Directory directory = new Directory();
    List<io.swagger.model.db.Profile> profiles = profileService.getProfiles(
      igRef
    );
    for (io.swagger.model.db.Profile p : profiles) {
      String profileName = p.getName();
      Set<String> members = profileService.getPersonInProfile(
        igRef,
        profileName
      );
      if (GUEST.equalsIgnoreCase(profileName)) {
        directory.setGuest(buildGuest(p));
      } else if (EVERYONE.equals(profileName)) {
        directory.setRegistredUsers(buildRegistredUsers(p));
      } else {
        AccessProfile ap = buildAccessProfile(p, members);
        directory.getAccessProfiles().add(ap);
      }
    }
    return directory;
  }

  private Guest buildGuest(io.swagger.model.db.Profile p) {
    Guest guest = new Guest();
    guest.setInformationPermission(toSimpleInf(p.getInformationPermission()));
    guest.setLibraryPermission(toSimpleLib(p.getLibraryPermission()));
    guest.setDirectoryPermission(toSimpleDir(p.getDirectoryPermission()));
    guest.setEventPermission(toSimpleEve(p.getEventPermission()));
    guest.setNewsgroupPermission(toSimpleNws(p.getNewsgroupPermission()));
    return guest;
  }

  private RegistredUsers buildRegistredUsers(io.swagger.model.db.Profile p) {
    RegistredUsers reg = new RegistredUsers();
    reg.setInformationPermission(toSimpleInf(p.getInformationPermission()));
    reg.setLibraryPermission(toSimpleLib(p.getLibraryPermission()));
    reg.setDirectoryPermission(toSimpleDir(p.getDirectoryPermission()));
    reg.setEventPermission(toSimpleEve(p.getEventPermission()));
    reg.setNewsgroupPermission(toSimpleNws(p.getNewsgroupPermission()));
    return reg;
  }

  private AccessProfile buildAccessProfile(
    io.swagger.model.db.Profile p,
    Set<String> members
  ) {
    AccessProfile ap = new AccessProfile();
    ap.setName(p.getName());
    if (p.getLibraryPermission() != null) ap.setLibraryPermission(
      LibraryPermissions.fromValue(p.getLibraryPermission())
    );
    if (p.getInformationPermission() != null) ap.setInformationPermission(
      InformationPermissions.fromValue(p.getInformationPermission())
    );
    if (p.getDirectoryPermission() != null) ap.setDirectoryPermission(
      DirectoryPermissions.fromValue(p.getDirectoryPermission())
    );
    if (p.getEventPermission() != null) ap.setEventPermission(
      EventPermissions.fromValue(p.getEventPermission())
    );
    if (p.getNewsgroupPermission() != null) ap.setNewsgroupPermission(
      NewsgroupPermissions.fromValue(p.getNewsgroupPermission())
    );
    if (members != null) ap.getUsers().addAll(members);
    return ap;
  }

  private SimpleInformationPermissions toSimpleInf(String perm) {
    if (
      perm != null && !perm.contains(PERM_NO_ACCESS)
    ) return SimpleInformationPermissions.INF_ACCESS;
    return SimpleInformationPermissions.INF_NO_ACCESS;
  }

  private SimpleLibraryPermissions toSimpleLib(String perm) {
    if (
      perm != null && !perm.contains(PERM_NO_ACCESS)
    ) return SimpleLibraryPermissions.LIB_ACCESS;
    return SimpleLibraryPermissions.LIB_NO_ACCESS;
  }

  private SimpleDirectoryPermissions toSimpleDir(String perm) {
    if (
      perm != null && !perm.contains(PERM_NO_ACCESS)
    ) return SimpleDirectoryPermissions.DIR_ACCESS;
    return SimpleDirectoryPermissions.DIR_NO_ACCESS;
  }

  private SimpleEventPermissions toSimpleEve(String perm) {
    if (
      perm != null && !perm.contains(PERM_NO_ACCESS)
    ) return SimpleEventPermissions.EVE_ACCESS;
    return SimpleEventPermissions.EVE_NO_ACCESS;
  }

  private SimpleNewsgroupPermissions toSimpleNws(String perm) {
    if (
      perm != null && !perm.contains(PERM_NO_ACCESS)
    ) return SimpleNewsgroupPermissions.NWS_ACCESS;
    return SimpleNewsgroupPermissions.NWS_NO_ACCESS;
  }

  // ==================== Persons ====================

  private void exportPersons(NodeRef igRef, Persons persons) {
    Set<String> allUsers = new HashSet<>();
    List<io.swagger.model.db.Profile> profiles = profileService.getProfiles(
      igRef
    );
    for (io.swagger.model.db.Profile p : profiles) {
      Set<String> members = profileService.getPersonInProfile(
        igRef,
        p.getName()
      );
      if (members != null) allUsers.addAll(members);
    }
    for (String userId : allUsers) {
      exportPerson(userId, persons);
    }
  }

  private void exportPerson(String userId, Persons persons) {
    try {
      if (!personService.personExists(userId)) return;
      NodeRef personRef = personService.getPerson(userId);
      Person person = new Person();
      person.setUserId(new UserIdProperty(userId));
      String fn = (String) nodeService.getProperty(
        personRef,
        ContentModel.PROP_FIRSTNAME
      );
      if (fn != null) person.setFirstName(new FirstNameProperty(fn));
      String ln = (String) nodeService.getProperty(
        personRef,
        ContentModel.PROP_LASTNAME
      );
      if (ln != null) person.setLastName(new LastNameProperty(ln));
      String em = (String) nodeService.getProperty(
        personRef,
        ContentModel.PROP_EMAIL
      );
      if (em != null) person.setEmail(new EmailProperty(em));
      persons.getPersons().add(person);
    } catch (Exception e) {
      logger.debug("Could not export person: " + userId);
    }
  }

  // ==================== IG Services ====================

  private void exportIgServices(NodeRef igRef, InterestGroup ig) {
    List<ChildAssociationRef> children = nodeService.getChildAssocs(igRef);
    for (ChildAssociationRef child : children) {
      NodeRef childRef = child.getChildRef();
      String childName = (String) nodeService.getProperty(
        childRef,
        ContentModel.PROP_NAME
      );
      if ("Library".equalsIgnoreCase(childName)) {
        Library library = new Library();
        exportLibraryChildren(childRef, library);
        ig.setLibrary(library);
      } else if (
        "Newsgroups".equalsIgnoreCase(childName) ||
        "Newsgroup".equalsIgnoreCase(childName)
      ) {
        Newsgroups newsgroups = new Newsgroups();
        exportNewsgroups(childRef, newsgroups);
        ig.setNewsgroups(newsgroups);
      } else if ("Information".equalsIgnoreCase(childName)) {
        Information information = new Information();
        exportInformation(childRef, information);
        ig.setInformation(information);
      } else if (
        "Events".equalsIgnoreCase(childName) ||
        "Event".equalsIgnoreCase(childName)
      ) {
        Events events = new Events();
        exportEvents(igRef, events);
        ig.setEvents(events);
      }
    }
  }

  // ==================== Category Structure ====================

  private void buildCategoryStructure(
    NodeRef igRef,
    InterestGroup ig,
    Circabc circabc
  ) {
    Category category = new Category();
    category.getInterestGroups().add(ig);
    CategoryHeader header = new CategoryHeader();
    NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
    if (categoryRef != null && nodeService.exists(categoryRef)) {
      String catName = (String) nodeService.getProperty(
        categoryRef,
        ContentModel.PROP_NAME
      );
      category.setName(new NameProperty(catName));
      NodeRef headerRef = nodeService
        .getPrimaryParent(categoryRef)
        .getParentRef();
      if (headerRef != null && nodeService.exists(headerRef)) {
        String headerName = (String) nodeService.getProperty(
          headerRef,
          ContentModel.PROP_NAME
        );
        header.setName(new NameProperty(headerName));
      }
    }
    header.getCategories().add(category);
    circabc.getCategoryHeaders().add(header);
  }

  // ==================== Library Export ====================

  private void exportLibraryChildren(NodeRef spaceRef, Library library) {
    for (ChildAssociationRef child : nodeService.getChildAssocs(spaceRef)) {
      NodeRef childRef = child.getChildRef();
      QName type = nodeService.getType(childRef);
      if (isMultilingualContainer(childRef)) {
        library.getMlContents().add(exportMlContent(childRef));
      } else if (DossierModel.TYPE_DOSSIER_SPACE.equals(type)) {
        library.getDossiers().add(exportDossier(childRef));
      } else if (ContentModel.TYPE_FOLDER.equals(type)) {
        library.getSpaces().add(exportSpace(childRef));
      } else if (ContentModel.TYPE_CONTENT.equals(type)) {
        library.getContents().add(exportContentFull(childRef));
      }
    }
  }

  private Space exportSpace(NodeRef spaceRef) {
    Space space = new Space();
    String name = (String) nodeService.getProperty(
      spaceRef,
      ContentModel.PROP_NAME
    );
    space.setName(new NameProperty(name));
    setTitleAndDesc(spaceRef, space);
    setAuditProps(spaceRef, space);

    for (ChildAssociationRef child : nodeService.getChildAssocs(spaceRef)) {
      NodeRef childRef = child.getChildRef();
      QName type = nodeService.getType(childRef);
      if (isMultilingualContainer(childRef)) {
        space.getMlContents().add(exportMlContent(childRef));
      } else if (DossierModel.TYPE_DOSSIER_SPACE.equals(type)) {
        space.getDossiers().add(exportDossier(childRef));
      } else if (ContentModel.TYPE_FOLDER.equals(type)) {
        space.getSpaces().add(exportSpace(childRef));
      } else if (ContentModel.TYPE_CONTENT.equals(type)) {
        space.getContents().add(exportContentFull(childRef));
      }
    }
    return space;
  }

  private Content exportContentFull(NodeRef ref) {
    Content content = new Content();
    String name = (String) nodeService.getProperty(ref, ContentModel.PROP_NAME);
    content.setName(new NameProperty(name));
    setTitleAndDesc(ref, content);
    setAuditProps(ref, content);

    // Content URL
    ContentData cd = (ContentData) nodeService.getProperty(
      ref,
      ContentModel.PROP_CONTENT
    );
    if (cd != null) content.setUri(cd.getContentUrl());

    // Document metadata
    setDocumentMetadata(ref, content);

    // Dynamic property values
    setDynamicPropertyValues(ref, content);

    // Keyword references
    setKeywordReferences(ref, content);

    // Version history
    setVersionHistory(ref, content);

    // Discussions
    Discussions disc = exportDiscussions(ref);
    if (disc != null) content.setDiscussions(disc);

    return content;
  }

  // ==================== Document Metadata ====================

  private void setDocumentMetadata(NodeRef ref, Content content) {
    try {
      Serializable status = nodeService.getProperty(
        ref,
        DocumentModel.PROP_STATUS
      );
      if (status != null) content.setStatus(
        new StatusProperty(status.toString())
      );

      Serializable secRanking = nodeService.getProperty(
        ref,
        DocumentModel.PROP_SECURITY_RANKING
      );
      if (secRanking != null) content.setSecurityRanking(
        new SecurityRankingProperty(secRanking.toString())
      );

      Serializable issueDate = nodeService.getProperty(
        ref,
        DocumentModel.PROP_ISSUE_DATE
      );
      if (issueDate instanceof Date d) content.setIssueDate(
        new IssueDateProperty(d)
      );

      Serializable expDate = nodeService.getProperty(
        ref,
        DocumentModel.PROP_EXPIRATION_DATE
      );
      if (expDate instanceof Date d) content.setExpirationDate(
        new ExpirationDateProperty(d)
      );

      Serializable reference = nodeService.getProperty(
        ref,
        DocumentModel.PROP_REFERENCE
      );
      if (
        reference != null && !reference.toString().isEmpty()
      ) content.setReference(new ReferenceProperty(reference.toString()));

      Serializable author = nodeService.getProperty(
        ref,
        ContentModel.PROP_AUTHOR
      );
      if (author != null && !author.toString().isEmpty()) content.setAuthor(
        new AuthorProperty(author.toString())
      );
    } catch (Exception e) {
      logger.debug(
        "Could not export document metadata for " + ref + ": " + e.getMessage()
      );
    }
  }

  // ==================== Dynamic Property Values ====================

  private void setDynamicPropertyValues(NodeRef ref, Content content) {
    try {
      List<QName> dynPropQNames = DocumentModel.ALL_DYN_PROPS;
      for (int i = 0; i < dynPropQNames.size() && i < 20; i++) {
        Serializable val = nodeService.getProperty(ref, dynPropQNames.get(i));
        if (val != null && !val.toString().isEmpty()) {
          setDynPropByIndex(content, i + 1, val.toString());
        }
      }
    } catch (Exception e) {
      logger.debug("Could not export dynamic properties for " + ref);
    }
  }

  @SuppressWarnings("java:S3776")
  private void setDynPropByIndex(Content content, int idx, String value) {
    switch (idx) {
      case 1 -> content.setDynamicProperty1(new DynamicProperty1(value));
      case 2 -> content.setDynamicProperty2(new DynamicProperty2(value));
      case 3 -> content.setDynamicProperty3(new DynamicProperty3(value));
      case 4 -> content.setDynamicProperty4(new DynamicProperty4(value));
      case 5 -> content.setDynamicProperty5(new DynamicProperty5(value));
      case 6 -> content.setDynamicProperty6(new DynamicProperty6(value));
      case 7 -> content.setDynamicProperty7(new DynamicProperty7(value));
      case 8 -> content.setDynamicProperty8(new DynamicProperty8(value));
      case 9 -> content.setDynamicProperty9(new DynamicProperty9(value));
      case 10 -> content.setDynamicProperty10(new DynamicProperty10(value));
      case 11 -> content.setDynamicProperty11(new DynamicProperty11(value));
      case 12 -> content.setDynamicProperty12(new DynamicProperty12(value));
      case 13 -> content.setDynamicProperty13(new DynamicProperty13(value));
      case 14 -> content.setDynamicProperty14(new DynamicProperty14(value));
      case 15 -> content.setDynamicProperty15(new DynamicProperty15(value));
      case 16 -> content.setDynamicProperty16(new DynamicProperty16(value));
      case 17 -> content.setDynamicProperty17(new DynamicProperty17(value));
      case 18 -> content.setDynamicProperty18(new DynamicProperty18(value));
      case 19 -> content.setDynamicProperty19(new DynamicProperty19(value));
      case 20 -> content.setDynamicProperty20(new DynamicProperty20(value));
      default -> {
        // Dynamic property indexes outside 1..20 are not supported and ignored.
      }
    }
  }

  // ==================== Keyword References ====================

  private void setKeywordReferences(NodeRef ref, Content content) {
    try {
      @SuppressWarnings("unchecked")
      List<NodeRef> kwRefs = (List<NodeRef>) nodeService.getProperty(
        ref,
        DocumentModel.PROP_KEYWORD
      );
      if (kwRefs != null && !kwRefs.isEmpty()) {
        KeywordReferences kwReferences = new KeywordReferences();
        for (NodeRef kwRef : kwRefs) {
          Integer id = keywordIdMap.get(kwRef.getId());
          if (id != null) {
            kwReferences.getIds().add(id);
          }
        }
        if (!kwReferences.getIds().isEmpty()) {
          content.setKeywords(kwReferences);
        }
      }
    } catch (Exception e) {
      logger.debug("Could not export keywords for " + ref);
    }
  }

  // ==================== Version History ====================

  private void setVersionHistory(NodeRef ref, Content content) {
    try {
      org.alfresco.service.cmr.version.VersionHistory vh =
        versionService.getVersionHistory(ref);
      if (vh == null) return;
      Collection<Version> allVersions = vh.getAllVersions();
      if (allVersions == null || allVersions.size() <= 1) return;

      // All versions except the latest (which is the current content)
      // Alfresco returns newest first; reverse view to oldest first
      List<Version> sorted = new ArrayList<>(allVersions).reversed();

      // Skip the last one (it's the current head content)
      if (sorted.size() > 1) {
        LibraryContentVersions versions = new LibraryContentVersions();
        for (int i = 0; i < sorted.size() - 1; i++) {
          Version v = sorted.get(i);
          LibraryContentVersion lcv = new LibraryContentVersion();
          lcv.setVersionLabel(new VersionLabelProperty(v.getVersionLabel()));
          NodeRef frozenRef = v.getFrozenStateNodeRef();
          if (frozenRef != null) {
            ContentData vcd = (ContentData) nodeService.getProperty(
              frozenRef,
              ContentModel.PROP_CONTENT
            );
            if (vcd != null) lcv.setUri(vcd.getContentUrl());
          }
          versions.getVersions().add(lcv);
        }
        content.setVersions(versions);
      }

      // Set the current version label
      Version current = sorted.get(sorted.size() - 1);
      content.setVersionLabel(
        new VersionLabelProperty(current.getVersionLabel())
      );
    } catch (Exception e) {
      logger.debug(
        "Could not export version history for " + ref + ": " + e.getMessage()
      );
    }
  }

  // ==================== Multilingual Documents ====================

  private boolean isMultilingualContainer(NodeRef ref) {
    QName type = nodeService.getType(ref);
    return QName.createQName(
      "http://www.alfresco.org/model/content/1.0",
      "mlContainer"
    ).equals(type);
  }

  private MlContent exportMlContent(NodeRef containerRef) {
    MlContent ml = new MlContent();
    setTitleAndDesc(containerRef, ml);
    setAuditProps(containerRef, ml);

    // Pivot locale
    try {
      Locale pivotLocale =
        multilingualContentService.getPivotTranslation(containerRef) != null
          ? (Locale) nodeService.getProperty(
              multilingualContentService.getPivotTranslation(containerRef),
              ContentModel.PROP_LOCALE
            )
          : null;
      if (pivotLocale != null) ml.setPivotLang(new LocaleProperty(pivotLocale));
    } catch (Exception e) {
      logger.debug("Could not determine pivot locale for " + containerRef);
    }

    // Security ranking and expiration from container level
    try {
      Serializable secRanking = nodeService.getProperty(
        containerRef,
        DocumentModel.PROP_SECURITY_RANKING
      );
      if (secRanking != null) ml.setSecurityRanking(
        new SecurityRankingProperty(secRanking.toString())
      );
      Serializable expDate = nodeService.getProperty(
        containerRef,
        DocumentModel.PROP_EXPIRATION_DATE
      );
      if (expDate instanceof Date d) ml.setExpirationDate(
        new ExpirationDateProperty(d)
      );
    } catch (Exception e) {
      logger.debug("Could not export ML metadata for " + containerRef);
    }

    // Translations (children of the mlContainer)
    try {
      Map<Locale, NodeRef> translations =
        multilingualContentService.getTranslations(containerRef);
      if (translations != null) {
        for (Map.Entry<Locale, NodeRef> entry : translations.entrySet()) {
          ml
            .getTranslations()
            .add(exportTranslation(entry.getValue(), entry.getKey()));
        }
      }
    } catch (Exception e) {
      logger.debug("Could not export translations for " + containerRef);
    }

    // Discussion on the container itself
    Discussions disc = exportDiscussions(containerRef);
    if (disc != null) ml.setDiscussions(disc);

    return ml;
  }

  private LibraryTranslation exportTranslation(NodeRef ref, Locale locale) {
    LibraryTranslation tr = new LibraryTranslation();
    String name = (String) nodeService.getProperty(ref, ContentModel.PROP_NAME);
    tr.setName(new NameProperty(name));
    tr.setLang(new LocaleProperty(locale));
    setAuditProps(ref, tr);

    ContentData cd = (ContentData) nodeService.getProperty(
      ref,
      ContentModel.PROP_CONTENT
    );
    if (cd != null) tr.setUri(cd.getContentUrl());

    String title = (String) nodeService.getProperty(
      ref,
      ContentModel.PROP_TITLE
    );
    if (title != null) tr.setTitle(new TitleProperty(title));

    setTranslationMetadata(ref, tr);

    setTranslationKeywords(ref, tr);

    setTranslationVersions(ref, tr);

    // Discussions on translation
    Discussions disc = exportDiscussions(ref);
    if (disc != null) tr.setDiscussions(disc);

    return tr;
  }

  /** Exports the translation's document metadata (status, issue date, reference, author). */
  private void setTranslationMetadata(NodeRef ref, LibraryTranslation tr) {
    try {
      Serializable status = nodeService.getProperty(
        ref,
        DocumentModel.PROP_STATUS
      );
      if (status != null) tr.setStatus(new StatusProperty(status.toString()));
      Serializable issueDate = nodeService.getProperty(
        ref,
        DocumentModel.PROP_ISSUE_DATE
      );
      if (issueDate instanceof Date d) tr.setIssueDate(
        new IssueDateProperty(d)
      );
      Serializable reference = nodeService.getProperty(
        ref,
        DocumentModel.PROP_REFERENCE
      );
      if (reference != null && !reference.toString().isEmpty()) tr.setReference(
        new ReferenceProperty(reference.toString())
      );
      Serializable author = nodeService.getProperty(
        ref,
        ContentModel.PROP_AUTHOR
      );
      if (author != null && !author.toString().isEmpty()) tr.setAuthor(
        new AuthorProperty(author.toString())
      );
    } catch (Exception e) {
      logger.debug("Could not export translation metadata for " + ref);
    }
  }

  /** Exports the translation's keyword references, mapping repo node refs to export ids. */
  private void setTranslationKeywords(NodeRef ref, LibraryTranslation tr) {
    try {
      @SuppressWarnings("unchecked")
      List<NodeRef> kwRefs = (List<NodeRef>) nodeService.getProperty(
        ref,
        DocumentModel.PROP_KEYWORD
      );
      if (kwRefs != null && !kwRefs.isEmpty()) {
        KeywordReferences kwReferences = new KeywordReferences();
        for (NodeRef kwRef : kwRefs) {
          Integer id = keywordIdMap.get(kwRef.getId());
          if (id != null) kwReferences.getIds().add(id);
        }
        if (!kwReferences.getIds().isEmpty()) tr.setKeywords(kwReferences);
      }
    } catch (Exception e) {
      logger.debug("Could not export translation keywords for " + ref);
    }
  }

  /** Exports the translation's version history (all versions except the current head). */
  private void setTranslationVersions(NodeRef ref, LibraryTranslation tr) {
    try {
      org.alfresco.service.cmr.version.VersionHistory vh =
        versionService.getVersionHistory(ref);
      if (vh == null) return;
      Collection<Version> allVersions = vh.getAllVersions();
      if (allVersions == null || allVersions.size() <= 1) return;
      List<Version> sorted = new ArrayList<>(allVersions).reversed();
      LibraryTranslationVersions versions = new LibraryTranslationVersions();
      for (int i = 0; i < sorted.size() - 1; i++) {
        versions.getVersions().add(buildTranslationVersion(sorted.get(i)));
      }
      tr.setVersions(versions);
      Version current = sorted.get(sorted.size() - 1);
      tr.setVersionLabel(new VersionLabelProperty(current.getVersionLabel()));
    } catch (Exception e) {
      logger.debug("Could not export translation version history for " + ref);
    }
  }

  /** Builds one exported translation version entry (label + frozen content URI). */
  private LibraryTranslationVersion buildTranslationVersion(Version v) {
    LibraryTranslationVersion ltv = new LibraryTranslationVersion();
    ltv.setVersionLabel(new VersionLabelProperty(v.getVersionLabel()));
    NodeRef frozenRef = v.getFrozenStateNodeRef();
    if (frozenRef != null) {
      ContentData vcd = (ContentData) nodeService.getProperty(
        frozenRef,
        ContentModel.PROP_CONTENT
      );
      if (vcd != null) ltv.setUri(vcd.getContentUrl());
    }
    return ltv;
  }

  // ==================== Dossiers ====================

  private Dossier exportDossier(NodeRef ref) {
    Dossier dossier = new Dossier();
    String name = (String) nodeService.getProperty(ref, ContentModel.PROP_NAME);
    dossier.setName(new NameProperty(name));
    setTitleAndDesc(ref, dossier);
    setAuditProps(ref, dossier);
    return dossier;
  }

  // ==================== Discussions ====================

  private Discussions exportDiscussions(NodeRef ref) {
    if (!nodeService.hasAspect(ref, ForumModel.ASPECT_DISCUSSABLE)) return null;
    try {
      List<ChildAssociationRef> discAssocs = nodeService.getChildAssocs(
        ref,
        ForumModel.ASSOC_DISCUSSION,
        ForumModel.ASSOC_DISCUSSION
      );
      if (discAssocs == null || discAssocs.isEmpty()) return null;

      NodeRef forumRef = discAssocs.get(0).getChildRef();
      Discussions disc = new Discussions();
      setAuditProps(forumRef, disc);

      for (ChildAssociationRef topicChild : nodeService.getChildAssocs(
        forumRef
      )) {
        NodeRef topicRef = topicChild.getChildRef();
        if (ForumModel.TYPE_TOPIC.equals(nodeService.getType(topicRef))) {
          disc.getTopics().add(exportTopic(topicRef));
        }
      }
      return disc.getTopics().isEmpty() ? null : disc;
    } catch (Exception e) {
      logger.debug("Could not export discussions for " + ref);
      return null;
    }
  }

  // ==================== Newsgroups ====================

  private void exportNewsgroups(NodeRef ngRef, Newsgroups newsgroups) {
    for (ChildAssociationRef child : nodeService.getChildAssocs(ngRef)) {
      NodeRef childRef = child.getChildRef();
      if (ForumModel.TYPE_FORUM.equals(nodeService.getType(childRef))) {
        newsgroups.getFora().add(exportForum(childRef));
      }
    }
  }

  private Forum exportForum(NodeRef forumRef) {
    Forum forum = new Forum();
    String name = (String) nodeService.getProperty(
      forumRef,
      ContentModel.PROP_NAME
    );
    forum.setName(new NameProperty(name));
    setAuditProps(forumRef, forum);

    for (ChildAssociationRef child : nodeService.getChildAssocs(forumRef)) {
      NodeRef childRef = child.getChildRef();
      QName type = nodeService.getType(childRef);
      if (ForumModel.TYPE_FORUM.equals(type)) {
        forum.getFora().add(exportForum(childRef));
      } else if (ForumModel.TYPE_TOPIC.equals(type)) {
        forum.getTopics().add(exportTopic(childRef));
      }
    }
    return forum;
  }

  private Topic exportTopic(NodeRef topicRef) {
    Topic topic = new Topic();
    String name = (String) nodeService.getProperty(
      topicRef,
      ContentModel.PROP_NAME
    );
    topic.setName(new NameProperty(name));
    setAuditProps(topicRef, topic);

    for (ChildAssociationRef child : nodeService.getChildAssocs(topicRef)) {
      NodeRef childRef = child.getChildRef();
      if (ForumModel.TYPE_POST.equals(nodeService.getType(childRef))) {
        topic.getMessages().add(exportMessage(childRef));
      }
    }
    return topic;
  }

  private Message exportMessage(NodeRef msgRef) {
    Message message = new Message();
    setAuditProps(msgRef, message);
    try {
      ContentReader reader = contentService.getReader(
        msgRef,
        ContentModel.PROP_CONTENT
      );
      if (reader != null && reader.exists()) {
        message.setContent(reader.getContentString());
      }
    } catch (Exception e) {
      logger.debug("Could not read message content for " + msgRef);
    }
    return message;
  }

  // ==================== Information Export ====================

  private void exportInformation(NodeRef infoRef, Information information) {
    for (ChildAssociationRef child : nodeService.getChildAssocs(infoRef)) {
      NodeRef childRef = child.getChildRef();
      QName type = nodeService.getType(childRef);
      if (ContentModel.TYPE_FOLDER.equals(type)) {
        information.getInfSpaces().add(exportInfSpace(childRef));
      } else if (ContentModel.TYPE_CONTENT.equals(type)) {
        information.getInfContents().add(exportInfContent(childRef));
      } else if (
        CircabcModel.TYPE_INFORMATION_NEWS.equals(type) ||
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_INFORMATION_NEWS)
      ) {
        information.getInfNews().add(exportInfNews(childRef));
      }
    }
  }

  private InfSpace exportInfSpace(NodeRef ref) {
    InfSpace infSpace = new InfSpace();
    String name = (String) nodeService.getProperty(ref, ContentModel.PROP_NAME);
    infSpace.setName(new NameProperty(name));
    setAuditProps(ref, infSpace);

    for (ChildAssociationRef child : nodeService.getChildAssocs(ref)) {
      NodeRef childRef = child.getChildRef();
      QName type = nodeService.getType(childRef);
      if (ContentModel.TYPE_FOLDER.equals(type)) {
        infSpace.getInfSpaces().add(exportInfSpace(childRef));
      } else if (ContentModel.TYPE_CONTENT.equals(type)) {
        infSpace.getInfContents().add(exportInfContent(childRef));
      }
    }
    return infSpace;
  }

  private InfContent exportInfContent(NodeRef ref) {
    InfContent infContent = new InfContent();
    String name = (String) nodeService.getProperty(ref, ContentModel.PROP_NAME);
    infContent.setName(new NameProperty(name));
    setAuditProps(ref, infContent);

    ContentData cd = (ContentData) nodeService.getProperty(
      ref,
      ContentModel.PROP_CONTENT
    );
    if (cd != null) infContent.setUri(cd.getContentUrl());

    setInfContentVersions(ref, infContent);

    return infContent;
  }

  /** Exports an information content's version history (all versions except the current head). */
  private void setInfContentVersions(NodeRef ref, InfContent infContent) {
    try {
      org.alfresco.service.cmr.version.VersionHistory vh =
        versionService.getVersionHistory(ref);
      if (vh == null) return;
      Collection<Version> allVersions = vh.getAllVersions();
      if (allVersions == null || allVersions.size() <= 1) return;
      List<Version> sorted = new ArrayList<>(allVersions).reversed();
      InformationContentVersions versions = new InformationContentVersions();
      for (int i = 0; i < sorted.size() - 1; i++) {
        versions.getVersions().add(buildInfContentVersion(sorted.get(i)));
      }
      infContent.setVersions(versions);
      Version current = sorted.get(sorted.size() - 1);
      infContent.setVersionLabel(
        new VersionLabelProperty(current.getVersionLabel())
      );
    } catch (Exception e) {
      logger.debug("Could not export info content version history for " + ref);
    }
  }

  /** Builds one exported information-content version entry (label + frozen content URI). */
  private InformationContentVersion buildInfContentVersion(Version v) {
    InformationContentVersion icv = new InformationContentVersion();
    icv.setVersionLabel(new VersionLabelProperty(v.getVersionLabel()));
    NodeRef frozenRef = v.getFrozenStateNodeRef();
    if (frozenRef != null) {
      ContentData vcd = (ContentData) nodeService.getProperty(
        frozenRef,
        ContentModel.PROP_CONTENT
      );
      if (vcd != null) icv.setUri(vcd.getContentUrl());
    }
    return icv;
  }

  // ==================== Information News ====================

  private InfNews exportInfNews(NodeRef ref) {
    InfNews news = new InfNews();
    String name = (String) nodeService.getProperty(ref, ContentModel.PROP_NAME);
    news.setName(new NameProperty(name));
    setTitleAndDesc(ref, news);
    setAuditProps(ref, news);

    try {
      Serializable nc = nodeService.getProperty(
        ref,
        CircabcModel.PROP_NEWS_CONTENT
      );
      if (nc != null) news.setNewsContent(nc.toString());

      Serializable np = nodeService.getProperty(
        ref,
        CircabcModel.PROP_NEWS_PATTERN
      );
      if (np != null) news.setNewsPattern(np.toString());

      Serializable nl = nodeService.getProperty(
        ref,
        CircabcModel.PROP_NEWS_LAYOUT
      );
      if (nl != null) news.setNewsLayout(nl.toString());

      Serializable ns = nodeService.getProperty(
        ref,
        CircabcModel.PROP_NEWS_SIZE
      );
      if (ns instanceof Integer i) news.setNewsSize(i);

      Serializable nd = nodeService.getProperty(
        ref,
        CircabcModel.PROP_NEWS_DATE
      );
      if (nd instanceof Date d) news.setNewsDate(d);

      Serializable nu = nodeService.getProperty(
        ref,
        CircabcModel.PROP_NEWS_URL
      );
      if (nu != null) news.setNewsUrl(nu.toString());
    } catch (Exception e) {
      logger.debug("Could not export news properties for " + ref);
    }

    // News attachments (child content nodes)
    for (ChildAssociationRef child : nodeService.getChildAssocs(ref)) {
      NodeRef childRef = child.getChildRef();
      if (ContentModel.TYPE_CONTENT.equals(nodeService.getType(childRef))) {
        news.getInfContents().add(exportInfContent(childRef));
      }
    }

    return news;
  }

  // ==================== Events Export ====================

  private void exportEvents(NodeRef igRef, Events events) {
    try {
      NodeRef eventRoot = eventService.getIGsEventRoot(igRef.getId());
      if (eventRoot == null) return;

      boolean mlAware = MLPropertyInterceptor.isMLAware();
      try {
        MLPropertyInterceptor.setMLAware(false);
        for (ChildAssociationRef child : nodeService.getChildAssocs(
          eventRoot
        )) {
          NodeRef childRef = child.getChildRef();
          if (nodeService.hasAspect(childRef, CircabcModel.ASPECT_EVENT)) {
            // Meetings and events are distinguished by their type or properties
            Serializable meetingType = nodeService.getProperty(
              childRef,
              QName.createQName(EVENTS_MODEL_NS, "meetingType")
            );
            if (meetingType != null) {
              events.getMeetings().add(exportMeeting(childRef));
            } else {
              events.getEvents().add(exportEvent(childRef));
            }
          }
        }
      } finally {
        MLPropertyInterceptor.setMLAware(mlAware);
      }
    } catch (Exception e) {
      logger.debug(
        "Could not export events for " + igRef + ": " + e.getMessage()
      );
    }
  }

  /**
   * Parses a value using the supplied parser, returning {@code fallback} when the
   * parser throws (for example when an enum literal is unknown). Extracted so that
   * callers do not need a nested try/catch block.
   *
   * @param parser   supplier that produces the parsed value
   * @param fallback value returned when parsing fails
   * @param <T>      the parsed value type
   * @return the parsed value, or {@code fallback} on failure
   */
  private static <T> T parseOrDefault(
    java.util.function.Supplier<T> parser,
    T fallback
  ) {
    try {
      return parser.get();
    } catch (Exception ex) {
      return fallback;
    }
  }

  private Meeting exportMeeting(NodeRef ref) {
    Meeting meeting = new Meeting();
    populateAppointment(ref, meeting);

    try {
      QName nsEvents = QName.createQName(EVENTS_MODEL_NS, "meetingType");
      Serializable mt = nodeService.getProperty(ref, nsEvents);
      if (mt != null) {
        meeting.setType(
          parseOrDefault(
            () -> MeetingType.fromValue(mt.toString()),
            MeetingType.FACE_TO_FACE
          )
        );
      }

      QName avail = QName.createQName(EVENTS_MODEL_NS, "availability");
      Serializable av = nodeService.getProperty(ref, avail);
      if (av != null) {
        meeting.setAvailability(
          parseOrDefault(
            () -> Availability.fromValue(av.toString()),
            Availability.PRIVATE
          )
        );
      }

      QName org = QName.createQName(EVENTS_MODEL_NS, "organization");
      Serializable orgVal = nodeService.getProperty(ref, org);
      if (orgVal != null) meeting.setOrganization(orgVal.toString());

      QName agenda = QName.createQName(EVENTS_MODEL_NS, "agenda");
      Serializable agendaVal = nodeService.getProperty(ref, agenda);
      if (agendaVal != null) meeting.setAgenda(agendaVal.toString());
    } catch (Exception e) {
      logger.debug("Could not export meeting properties for " + ref);
    }
    return meeting;
  }

  private Event exportEvent(NodeRef ref) {
    Event event = new Event();
    populateAppointment(ref, event);

    try {
      QName eventType = QName.createQName(EVENTS_MODEL_NS, "eventType");
      Serializable et = nodeService.getProperty(ref, eventType);
      if (et != null) {
        event.setType(
          parseOrDefault(
            () -> EventType.fromValue(et.toString()),
            EventType.TASK
          )
        );
      }

      QName priority = QName.createQName(EVENTS_MODEL_NS, "priority");
      Serializable pr = nodeService.getProperty(ref, priority);
      if (pr != null) {
        event.setPriority(
          parseOrDefault(
            () -> EventPriority.fromValue(pr.toString()),
            EventPriority.LOW
          )
        );
      }
    } catch (Exception e) {
      logger.debug("Could not export event properties for " + ref);
    }
    return event;
  }

  private void populateAppointment(NodeRef ref, Appointment appt) {
    String nsUri = EVENTS_MODEL_NS;
    try {
      Serializable title = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "eventTitle")
      );
      if (title != null) appt.setAppointmentTitle(title.toString());

      Serializable lang = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "language")
      );
      if (lang != null) appt.setLanguage(Locale.of(lang.toString()));

      Serializable abs = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "eventAbstract")
      );
      if (abs != null) appt.setAbstract(abs.toString());

      Serializable loc = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "location")
      );
      if (loc != null) appt.setLocation(loc.toString());

      Serializable startDate = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "startDate")
      );
      if (startDate instanceof Date d) appt.setStartDate(d);

      Serializable startTime = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "startTime")
      );
      if (startTime instanceof Date d) appt.setStartTime(d);

      Serializable endTime = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "endTime")
      );
      if (endTime instanceof Date d) appt.setEndTime(d);

      // Contact information
      ContactInformation contact = new ContactInformation();
      Serializable cName = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "contactName")
      );
      contact.setName(cName != null ? cName.toString() : "");
      Serializable cEmail = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "contactEmail")
      );
      contact.setEmail(cEmail != null ? cEmail.toString() : "");
      Serializable cPhone = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "contactPhone")
      );
      contact.setPhone(cPhone != null ? cPhone.toString() : "");
      Serializable cUrl = nodeService.getProperty(
        ref,
        QName.createQName(nsUri, "contactUrl")
      );
      if (cUrl != null) contact.setUrl(cUrl.toString());
      appt.setContact(contact);
    } catch (Exception e) {
      logger.debug(
        "Could not populate appointment for " + ref + ": " + e.getMessage()
      );
    }
  }

  // ==================== Utility Methods ====================

  private void setTitleAndDesc(NodeRef ref, TitledNode node) {
    String title = (String) nodeService.getProperty(
      ref,
      ContentModel.PROP_TITLE
    );
    if (title != null) node.setTitle(new TitleProperty(title));
    String desc = (String) nodeService.getProperty(
      ref,
      ContentModel.PROP_DESCRIPTION
    );
    if (desc != null) node.setDescription(new DescriptionProperty(desc));
  }

  private void setAuditProps(NodeRef ref, Node node) {
    // Record the source NodeRef so old (source) deep links can be resolved to the
    // migrated node on import (persisted via the ci:migrated / ci:originalNodeRef aspect).
    node.setOriginalNodeRef(ref);
    Date created = (Date) nodeService.getProperty(
      ref,
      ContentModel.PROP_CREATED
    );
    if (created != null) node.setCreated(new CreatedProperty(created));
    String creator = (String) nodeService.getProperty(
      ref,
      ContentModel.PROP_CREATOR
    );
    if (creator != null) node.setCreator(new CreatorProperty(creator));
    Date modified = (Date) nodeService.getProperty(
      ref,
      ContentModel.PROP_MODIFIED
    );
    if (modified != null) node.setModified(new ModifiedProperty(modified));
    String modifier = (String) nodeService.getProperty(
      ref,
      ContentModel.PROP_MODIFIER
    );
    if (modifier != null) node.setModifier(new ModifierProperty(modifier));
  }
}
