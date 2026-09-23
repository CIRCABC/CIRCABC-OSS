package eu.europa.ec.digit.circabc.rest.service.migration;

import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.*;
import eu.cec.digit.circabc.migration.entities.generated.permissions.*;
import eu.cec.digit.circabc.migration.entities.generated.properties.ContactInformation;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyImpl;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyType;
import eu.europa.ec.digit.circabc.rest.service.event.EventImpl;
import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.event.MeetingImpl;
import eu.europa.ec.digit.circabc.rest.service.keyword.Keyword;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordImpl;
import eu.europa.ec.digit.circabc.rest.service.keyword.KeywordsService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CategoriesApi;
import io.swagger.api.GroupsApi;
import io.swagger.api.ProfilesApi;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.I18nProperty;
import io.swagger.model.InterestGroupPostModel;
import io.swagger.model.MembershipPostDefinition;
import io.swagger.model.Profile;
import io.swagger.model.User;
import io.swagger.model.UserProfile;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.DossierModel;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * Default implementation of {@link IgImportService} that rebuilds a complete
 * Interest Group (IG) from a parsed {@link ImportRoot} JAXB export into the
 * Alfresco repository.
 *
 * <p>The import walks the exported CIRCABC hierarchy
 * (headers &rarr; categories &rarr; interest groups) and, for each IG, recreates:
 * <ul>
 *   <li>referenced {@code Person}s (users) so memberships can be assigned;</li>
 *   <li>the IG node itself via the Categories API, plus IG-level properties
 *       (contact information, allowApply flag, pending membership applications);</li>
 *   <li>access profiles and their members, and the built-in guest / EVERYONE
 *       profile permissions;</li>
 *   <li>keyword and dynamic-property definitions (created first so per-document
 *       references can be resolved);</li>
 *   <li>Library, Information and Newsgroup content (folders, documents,
 *       multilingual documents, dossiers, forums/topics/posts) including
 *       version history, discussions and audit metadata;</li>
 *   <li>Events (meetings and events).</li>
 * </ul>
 *
 * <p>Content binaries are streamed from the source system over HTTP. Downloads
 * are parallelised through a fixed thread pool and node creation is performed in
 * batched retrying transactions. During content import Alfresco behaviours and
 * rules are disabled so that original audit metadata and version labels are
 * preserved rather than being overwritten by policy handlers.
 *
 * <p>Collaborators (services and APIs) are injected via Spring {@code @Autowired}
 * / {@code @Qualifier} bean wiring.
 */
public class IgImportServiceImpl implements IgImportService {

  /** Logger for import progress, warnings and per-item failures. */
  private static final Log logger = LogFactory.getLog(
    IgImportServiceImpl.class
  );
  /** Size of the fixed thread pool used to download content binaries in parallel. */
  private static final int DOWNLOAD_THREADS = 10;
  /** Number of content items created per retrying transaction / download batch. */
  private static final int BATCH_SIZE = 500;

  // Profile permission map keys.
  private static final String PERM_KEY_INFORMATION = "information";
  private static final String PERM_KEY_LIBRARY = "library";
  private static final String PERM_KEY_MEMBERS = "members";
  private static final String PERM_KEY_EVENTS = "events";
  private static final String PERM_KEY_NEWSGROUPS = "newsgroups";
  private static final String PERM_KEY_VISIBILITY = "visibility";
  // Default "no access" permission values per module.
  private static final String PERM_INF_NO_ACCESS = "InfNoAccess";
  private static final String PERM_LIB_NO_ACCESS = "LibNoAccess";
  private static final String PERM_DIR_NO_ACCESS = "DirNoAccess";
  private static final String PERM_EVE_NO_ACCESS = "EveNoAccess";
  private static final String PERM_NWS_NO_ACCESS = "NwsNoAccess";
  private static final String PERM_VISIBILITY = "Visibility";
  private static final String PERM_NO_VISIBILITY = "NoVisibility";

  @Autowired
  private CategoriesApi categoriesApi;

  @Autowired
  private GroupsApi groupsApi;

  @Autowired
  private ProfilesApi profilesApi;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private UserService userService;

  @Autowired
  @Qualifier("nodeService")
  private NodeService nodeService;

  @Autowired
  @Qualifier("ContentService")
  private ContentService contentService;

  @Autowired
  @Qualifier("personService")
  private PersonService personService;

  @Autowired
  private TransactionService transactionService;

  @Autowired
  @Qualifier("policyBehaviourFilter")
  private BehaviourFilter policyBehaviourFilter;

  @Autowired
  private RuleService ruleService;

  @Autowired
  private KeywordsService keywordsService;

  @Autowired
  private DynamicPropertyService dynamicPropertyService;

  @Autowired
  @Qualifier("MultilingualContentService")
  private MultilingualContentService multilingualContentService;

  @Autowired
  private EventService eventService;

  @Autowired
  private VersionService versionService;

  /** Username used for HTTP Basic authentication when downloading source content. */
  @Value("${import.username:admin}")
  private String importUsername;

  /** Password used for HTTP Basic authentication when downloading source content. */
  @Value("${import.password:}")
  private String importPassword;

  /** Lazily created HTTP client shared by all downloads of a single import run. */
  private HttpClient httpClient;
  /** Lazily created thread pool executing content downloads in parallel. */
  private ExecutorService downloadExecutor;

  private String getImportPassword() {
    // Spring @Value may mangle passwords containing $ (interpreted as nested placeholder).
    // Fall back to reading the env var directly.
    if (importPassword != null && !importPassword.isEmpty()) {
      return importPassword;
    }
    String envVal = System.getenv("IMPORT_PASSWORD");
    return envVal != null ? envVal : "";
  }

  private void initHttpClient() {
    if (httpClient == null) {
      httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    }
    if (downloadExecutor == null) {
      downloadExecutor = Executors.newFixedThreadPool(DOWNLOAD_THREADS);
    }
  }

  private void shutdownHttpClient() {
    if (downloadExecutor != null) {
      downloadExecutor.shutdown();
      try {
        downloadExecutor.awaitTermination(5, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      downloadExecutor = null;
    }
    httpClient = null;
  }

  private String buildAuthHeader() {
    String password = getImportPassword();
    if (
      importUsername != null && !importUsername.isEmpty() && !password.isEmpty()
    ) {
      String creds = importUsername + ":" + password;
      return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes());
    }
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation first ensures all referenced persons exist (in a
   * dedicated transaction), then iterates the exported
   * header/category/interest-group hierarchy and imports each Interest Group in
   * turn via {@link #importSingleIg(String, InterestGroup, ImportResult)}. The
   * shared HTTP client and download thread pool are initialised before the
   * import and always shut down afterwards.
   *
   * @param categoryId the identifier of the target category into which the
   *     Interest Group(s) are created
   * @param importRoot the parsed export payload; if it carries no
   *     {@code Circabc} root an empty result is returned
   * @return an {@link ImportResult} accumulating created-node counts and any
   *     errors encountered
   */
  @Override
  public ImportResult importIg(String categoryId, ImportRoot importRoot) {
    ImportResult result = new ImportResult();
    Circabc circabc = importRoot.getCircabc();
    if (circabc == null) return result;

    initHttpClient();
    try {
      // Ensure persons exist
      runInNewTransaction(() -> {
        importPersons(importRoot.getPersons(), result);
        return null;
      });

      for (CategoryHeader header : circabc.getCategoryHeaders()) {
        for (Category category : header.getCategories()) {
          for (InterestGroup ig : category.getInterestGroups()) {
            importSingleIg(categoryId, ig, result);
          }
        }
      }
    } finally {
      shutdownHttpClient();
    }
    return result;
  }

  private void runInNewTransaction(Callable<Void> work) {
    transactionService
      .getRetryingTransactionHelper()
      .doInTransaction(
        () -> {
          work.call();
          return null;
        },
        false,
        true
      );
  }

  private void importSingleIg(
    String categoryId,
    InterestGroup ig,
    ImportResult result
  ) {
    String igName =
      ig.getName() != null ? ig.getName().getValue().toString() : "imported-ig";

    // Create IG (own transaction)
    io.swagger.model.InterestGroup createdIg;
    try {
      createdIg = runInNewTx(() -> {
        InterestGroupPostModel igPost = new InterestGroupPostModel();
        igPost.setName(igName);
        if (ig.getTitle() != null) {
          I18nProperty t = new I18nProperty();
          t.put("en", ig.getTitle().getValue().toString());
          igPost.setTitle(t);
        }
        if (ig.getDescription() != null) {
          I18nProperty d = new I18nProperty();
          d.put("en", ig.getDescription().getValue().toString());
          igPost.setDescription(d);
        }
        igPost.setLeaders(Collections.emptyList());
        igPost.setNotify(false);
        logger.info("IgImportService: creating IG name=" + igName);
        return categoriesApi.categoriesIdGroupsPost(categoryId, igPost);
      });
      logger.info("IgImportService: IG created id=" + createdIg.getId());
      result.addNodes(1);
    } catch (Exception e) {
      result.addError("IG creation failed: " + e.getMessage());
      logger.error("IgImportService: IG creation FAILED", e);
      return;
    }

    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      createdIg.getId()
    );

    // IG-level properties carried by the interestGroup element: contact
    // information and any pending membership applications. (allowApply is not
    // exposed by the generated migration model, so it cannot be read here.)
    runInNewTransaction(() -> {
      importIgProperties(igRef, ig, result);
      return null;
    });

    // Profiles and members (own transaction)
    runInNewTransaction(() -> {
      importProfilesAndMembers(igRef, ig.getDirectory(), result);
      return null;
    });

    // Sync IG
    runInNewTransaction(() -> {
      circabcService.resyncInterestGroup(igRef);
      return null;
    });

    // Update guest/EVERYONE permissions (after resync so service nodes exist)
    runInNewTransaction(() -> {
      updateBuiltinProfiles(igRef, ig.getDirectory(), result);
      return null;
    });

    // Keyword & dynamic-property DEFINITIONS (must run before content so that
    // per-document keyword references can be resolved to created keyword nodes).
    final Map<Integer, NodeRef> keywordMap = new HashMap<>();
    runInNewTransaction(() -> {
      importDefinitions(igRef, ig, result, keywordMap);
      return null;
    });

    // Library - batched with parallel downloads
    if (
      ig.getLibrary() != null &&
      createdIg.getLibraryId() != null &&
      !createdIg.getLibraryId().isEmpty()
    ) {
      NodeRef libRef = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        createdIg.getLibraryId()
      );
      importLibraryBatched(libRef, ig.getLibrary(), result, keywordMap);
    }

    // Information - batched
    if (
      ig.getInformation() != null &&
      createdIg.getInformationId() != null &&
      !createdIg.getInformationId().isEmpty()
    ) {
      NodeRef infoRef = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        createdIg.getInformationId()
      );
      importInformationBatched(infoRef, ig.getInformation(), result);
    }

    // Newsgroups - batched
    if (
      ig.getNewsgroups() != null &&
      createdIg.getNewsgroupId() != null &&
      !createdIg.getNewsgroupId().isEmpty()
    ) {
      NodeRef ngRef = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        createdIg.getNewsgroupId()
      );
      importNewsgroupsBatched(ngRef, ig.getNewsgroups(), result);
    }

    // Events (meetings + events)
    if (ig.getEvents() != null) {
      final String igId = createdIg.getId();
      runInNewTransaction(() -> {
        importEvents(igId, ig.getEvents(), result);
        return null;
      });
    }

    // Restore the IG node's own audit metadata (created/creator/modified/
    // modifier) from the export. The IG node is created via the Categories API
    // (categoriesIdGroupsPost) and then mutated by all the steps above — contact
    // information, profiles/members, resync, keyword/dynamic-property definitions
    // and the Library/Information/Newsgroup/Events content — each of which
    // auto-stamps cm:modified/cm:modifier to "now"/importing-user. Setting the
    // audit properties LAST, with the auditable behaviour disabled, preserves the
    // original values from the XML, mirroring how every other imported node type
    // is handled (setAuditProperties / setAuditPropertiesFromNode). InterestGroup
    // extends Node, so the shared helper reads created/creator/modified/modifier
    // directly.
    runInNewTransaction(() -> {
      setAuditPropertiesFromNode(igRef, ig);
      return null;
    });
  }

  private <T> T runInNewTx(Callable<T> work) {
    return transactionService
      .getRetryingTransactionHelper()
      .doInTransaction(work::call, false, true);
  }

  // === Profiles and Members ===

  /** Sets the IG contact information from localised values, else the single contact string. */
  private void importContactInfo(
    NodeRef igRef,
    InterestGroup ig,
    ImportResult result
  ) {
    // Contact information: prefer the localised I18NContactInfo values, else the
    // single contactInfo string.
    MLText contact = buildMlTitle(ig.getI18NContactInfos());
    if (
      contact.isEmpty() &&
      ig.getContactInfo() != null &&
      ig.getContactInfo().getValue() != null
    ) {
      contact.put(
        Locale.ENGLISH,
        String.valueOf(ig.getContactInfo().getValue()).trim()
      );
    }
    if (!contact.isEmpty()) {
      try {
        nodeService.setProperty(
          igRef,
          CircabcModel.PROP_CONTACT_INFORMATION,
          contact
        );
      } catch (Exception e) {
        result.addError("Contact info: " + e.getMessage());
        logger.warn("Failed to set IG contact information", e);
      }
    }
  }

  /** Applies the allowApply flag and updates the IG application group accordingly. */
  private void importAllowApply(
    NodeRef igRef,
    InterestGroup ig,
    ImportResult result
  ) {
    // Whether registered users may apply for membership (allowApply). Mirrors
    // the make/update-IG endpoint: set the property and update the application
    // group accordingly.
    if (ig.isAllowApply() != null) {
      try {
        nodeService.setProperty(
          igRef,
          CircabcModel.PROP_CAN_REGISTERED_APPLY,
          ig.isAllowApply()
        );
        circabcService.updateInterestGroupApplication(igRef, ig.isAllowApply());
      } catch (Exception e) {
        result.addError("allowApply: " + e.getMessage());
        logger.warn("Failed to set allowApply", e);
      }
    }
  }

  /** Imports the pending membership applications onto the IG node. */
  private void importApplications(
    NodeRef igRef,
    InterestGroup ig,
    ImportResult result
  ) {
    // Pending membership applications.
    if (
      ig.getApplications() == null ||
      ig.getApplications().getApplications() == null ||
      ig.getApplications().getApplications().isEmpty()
    ) {
      return;
    }
    try {
      Map<String, io.swagger.model.alfresco.Applicant> applicants =
        buildApplicants(ig);
      if (!applicants.isEmpty()) {
        nodeService.setProperty(
          igRef,
          CircabcModel.PROP_APPLICANTS,
          (Serializable) applicants
        );
        result.addNodes(applicants.size());
        logger.info(
          "IgImportService: imported " +
            applicants.size() +
            " membership application(s)"
        );
      }
    } catch (Exception e) {
      result.addError("Membership applications: " + e.getMessage());
      logger.warn("Failed to import membership applications", e);
    }
  }

  /** Builds the applicant map from the IG's application elements (skips blank users). */
  private Map<String, io.swagger.model.alfresco.Applicant> buildApplicants(
    InterestGroup ig
  ) {
    Map<String, io.swagger.model.alfresco.Applicant> applicants =
      new HashMap<>();
    for (eu.cec.digit.circabc.migration.entities.generated.permissions.Application app : ig
      .getApplications()
      .getApplications()) {
      if (app.getUser() == null || app.getUser().isEmpty()) {
        continue;
      }
      Date when = app.getDate() != null ? app.getDate() : new Date();
      applicants.put(
        app.getUser(),
        new io.swagger.model.alfresco.Applicant(
          app.getUser(),
          when,
          app.getMessage()
        )
      );
    }
    return applicants;
  }

  /**
   * Import IG-level properties carried by the {@code interestGroup} element:
   * the contact information ({@code contactInfo} / {@code I18NContactInfo}) and
   * any pending membership applications (the {@code applications} element).
   *
   * <p>Membership applications are stored on the IG node as the
   * {@code circabc:applicants} property map (userId &rarr; Applicant), exactly
   * like a live "apply for membership" request — but here we preserve the
   * original application date and message from the export rather than stamping
   * "now" and notifying administrators.
   */
  private void importIgProperties(
    NodeRef igRef,
    InterestGroup ig,
    ImportResult result
  ) {
    importContactInfo(igRef, ig, result);
    importAllowApply(igRef, ig, result);
    importApplications(igRef, ig, result);
  }

  private void importProfilesAndMembers(
    NodeRef igRef,
    Directory directory,
    ImportResult result
  ) {
    if (directory == null) return;
    Map<String, Profile> existingByName = getExistingProfilesByName(igRef);
    for (AccessProfile ap : directory.getAccessProfiles()) {
      importAccessProfile(igRef, ap, existingByName, result);
    }
  }

  /** Loads the IG's existing profiles keyed by name (to avoid duplicates). */
  private Map<String, Profile> getExistingProfilesByName(NodeRef igRef) {
    List<Profile> existingProfiles = profilesApi.groupsIdProfilesGet(
      igRef.getId(),
      null,
      false
    );
    Map<String, Profile> existingByName = new HashMap<>();
    for (Profile ep : existingProfiles) {
      existingByName.put(ep.getName(), ep);
    }
    return existingByName;
  }

  /**
   * Imports a single access profile (update if it exists, else create) in its own
   * retrying transaction, then adds its members. A per-profile transaction lets the
   * helper retry optimistic-lock conflicts and isolates failures.
   */
  private void importAccessProfile(
    NodeRef igRef,
    AccessProfile ap,
    Map<String, Profile> existingByName,
    ImportResult result
  ) {
    final String xmlName = ap.getName();
    final List<String> users = ap.getUsers();
    final Profile[] target = new Profile[1];
    try {
      runInNewTransaction(() -> {
        Profile existing = existingByName.get(xmlName);
        if (existing != null) {
          target[0] = updateExistingProfile(existing, ap);
        } else {
          target[0] = createProfileFromXml(igRef, ap);
          result.addNodes(1);
        }
        return null;
      });
    } catch (Exception e) {
      result.addError("Profile/" + xmlName + ": " + e.getMessage());
      logger.error("Failed to create/update profile " + xmlName, e);
      return;
    }
    if (users != null && !users.isEmpty() && target[0] != null) {
      addProfileMembers(igRef, target[0], users, xmlName, result);
    }
  }

  /** Adds the given users to a profile as members, in a single transaction. */
  private void addProfileMembers(
    NodeRef igRef,
    Profile targetProfile,
    List<String> users,
    String xmlName,
    ImportResult result
  ) {
    try {
      runInNewTransaction(() -> {
        MembershipPostDefinition m = new MembershipPostDefinition();
        m.setAdminNotifications(false);
        m.setUserNotifications(false);
        List<UserProfile> members = new ArrayList<>();
        for (String userId : users) {
          UserProfile up = new UserProfile();
          User u = new User();
          u.setUserId(userId);
          up.setUser(u);
          Profile p = new Profile();
          p.setName(targetProfile.getName());
          p.setGroupName(targetProfile.getGroupName());
          up.setProfile(p);
          members.add(up);
        }
        m.setMemberships(members);
        groupsApi.groupsIdMembersPost(igRef, m);
        result.addNodes(members.size());
        return null;
      });
    } catch (Exception e) {
      result.addError("Members/" + xmlName + ": " + e.getMessage());
      logger.warn("Failed to add members to " + xmlName, e);
    }
  }

  private void updateBuiltinProfiles(
    NodeRef igRef,
    Directory directory,
    ImportResult result
  ) {
    if (directory == null) return;
    List<Profile> existingProfiles = profilesApi.groupsIdProfilesGet(
      igRef.getId(),
      null,
      false
    );
    Map<String, Profile> existingByName = new HashMap<>();
    for (Profile ep : existingProfiles) {
      existingByName.put(ep.getName(), ep);
    }

    if (directory.getGuest() != null) {
      Profile guestProfile = existingByName.get("guest");
      if (guestProfile != null) {
        try {
          updateGlobalProfilePermissions(guestProfile, directory.getGuest());
          NodeRef guestRef = new NodeRef(
            StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
            guestProfile.getId()
          );
          profilesApi.profilesIdPut(guestRef, guestProfile);
        } catch (Exception e) {
          result.addError("guest permissions: " + e.getMessage());
          logger.warn("Failed to update guest permissions", e);
        }
      }
    }

    if (directory.getRegistredUsers() != null) {
      Profile everyoneProfile = existingByName.get("EVERYONE");
      if (everyoneProfile != null) {
        try {
          updateGlobalProfilePermissions(
            everyoneProfile,
            directory.getRegistredUsers()
          );
          NodeRef everyoneRef = new NodeRef(
            StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
            everyoneProfile.getId()
          );
          profilesApi.profilesIdPut(everyoneRef, everyoneProfile);
        } catch (Exception e) {
          result.addError("EVERYONE permissions: " + e.getMessage());
          logger.warn("Failed to update EVERYONE permissions", e);
        }
      }
    }
  }

  private Map<String, String> buildAccessProfilePermissions(AccessProfile ap) {
    Map<String, String> perms = new HashMap<>();
    perms.put(
      PERM_KEY_INFORMATION,
      ap.getInformationPermission() != null
        ? ap.getInformationPermission().value()
        : PERM_INF_NO_ACCESS
    );
    perms.put(
      PERM_KEY_LIBRARY,
      ap.getLibraryPermission() != null
        ? ap.getLibraryPermission().value()
        : PERM_LIB_NO_ACCESS
    );
    perms.put(
      PERM_KEY_MEMBERS,
      ap.getDirectoryPermission() != null
        ? ap.getDirectoryPermission().value()
        : PERM_DIR_NO_ACCESS
    );
    perms.put(
      PERM_KEY_EVENTS,
      ap.getEventPermission() != null
        ? ap.getEventPermission().value()
        : PERM_EVE_NO_ACCESS
    );
    perms.put(
      PERM_KEY_NEWSGROUPS,
      ap.getNewsgroupPermission() != null
        ? ap.getNewsgroupPermission().value()
        : PERM_NWS_NO_ACCESS
    );
    perms.put(PERM_KEY_VISIBILITY, PERM_VISIBILITY);
    return perms;
  }

  private void updateGlobalProfilePermissions(
    Profile profile,
    eu.cec.digit.circabc.migration.entities.generated.permissions.GlobalAccessProfile gap
  ) {
    Map<String, String> perms = new HashMap<>();
    perms.put(
      PERM_KEY_INFORMATION,
      gap.getInformationPermission() != null
        ? gap.getInformationPermission().value()
        : PERM_INF_NO_ACCESS
    );
    perms.put(
      PERM_KEY_LIBRARY,
      gap.getLibraryPermission() != null
        ? gap.getLibraryPermission().value()
        : PERM_LIB_NO_ACCESS
    );
    perms.put(
      PERM_KEY_MEMBERS,
      gap.getDirectoryPermission() != null
        ? gap.getDirectoryPermission().value()
        : PERM_DIR_NO_ACCESS
    );
    perms.put(
      PERM_KEY_EVENTS,
      gap.getEventPermission() != null
        ? gap.getEventPermission().value()
        : PERM_EVE_NO_ACCESS
    );
    perms.put(
      PERM_KEY_NEWSGROUPS,
      gap.getNewsgroupPermission() != null
        ? gap.getNewsgroupPermission().value()
        : PERM_NWS_NO_ACCESS
    );
    // Respect the exported visibility flag: guest visibility="false" must not
    // grant Visibility (IG hidden from guests), registredUsers visibility="true"
    // grants it (IG discoverable/joinable by registered users).
    perms.put(
      PERM_KEY_VISIBILITY,
      gap.isVisibility() ? PERM_VISIBILITY : PERM_NO_VISIBILITY
    );
    profile.setPermissions(perms);
  }

  private Profile updateExistingProfile(Profile existing, AccessProfile ap) {
    Map<String, String> perms = buildAccessProfilePermissions(ap);
    existing.setPermissions(perms);
    I18nProperty titleProp = new I18nProperty();
    String titleValue = getProfileTitle(ap);
    titleProp.put("en", titleValue);
    if (ap.getI18NTitles() != null) {
      for (I18NProperty i18n : ap.getI18NTitles()) {
        if (i18n.getLang() != null && i18n.getValue() != null) {
          titleProp.put(i18n.getLang().getLanguage(), i18n.getValue().trim());
        }
      }
    }
    existing.setTitle(titleProp);
    NodeRef profileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      existing.getId()
    );
    return profilesApi.profilesIdPut(profileRef, existing);
  }

  private Profile createProfileFromXml(NodeRef igRef, AccessProfile ap) {
    Profile profile = new Profile();
    I18nProperty titleProp = new I18nProperty();
    String titleValue = getProfileTitle(ap);
    titleProp.put("en", titleValue);
    if (ap.getI18NTitles() != null) {
      for (I18NProperty i18n : ap.getI18NTitles()) {
        if (i18n.getLang() != null && i18n.getValue() != null) {
          titleProp.put(i18n.getLang().getLanguage(), i18n.getValue().trim());
        }
      }
    }
    profile.setTitle(titleProp);
    Map<String, String> perms = buildAccessProfilePermissions(ap);
    profile.setPermissions(perms);
    return profilesApi.groupsIdProfilesPost(igRef, profile);
  }

  private String getProfileTitle(AccessProfile ap) {
    if (ap.getI18NTitles() != null) {
      for (I18NProperty i18n : ap.getI18NTitles()) {
        if (
          i18n.getLang() != null &&
          "en".equals(i18n.getLang().getLanguage()) &&
          i18n.getValue() != null
        ) return i18n.getValue().trim();
      }
      for (I18NProperty i18n : ap.getI18NTitles()) {
        if (
          i18n.getValue() != null && !i18n.getValue().trim().isEmpty()
        ) return i18n.getValue().trim();
      }
    }
    return ap.getName();
  }

  // === Batched Library Import with Parallel Downloads ===

  private static class ContentTask {

    final NodeRef parentRef;
    final String name;
    final String title;
    final String description;
    final String uri;
    final Date created;
    final String creator;
    final Date modified;
    final String modifier;
    Future<Path> downloadFuture;
    // CircaBC document properties (library content only)
    List<TypedProperty> dynProps;
    List<Integer> keywordIds;
    // CircaBC document metadata (status, securityRanking, issueDate, expirationDate, reference, author)
    Map<QName, Serializable> bcProps;
    // Attached discussion forum (fm:discussable), if any
    Discussions discussions;
    // Multilingual support: locale of this translation and the created node ref
    Locale locale;
    NodeRef createdRef;
    // Version history: prior versions + this node's head label. Typed as the
    // shared ContentNode base so library content, translations and information
    // content (which use distinct JAXB version subtypes) can all be replayed.
    List<? extends ContentNode> versions;
    String versionLabel;
    // Original (source) NodeRef recorded on export; applied on import via ci:migrated.
    String originalNodeRef;

    @SuppressWarnings("java:S107")
    // Parameters mirror the content node's audit fields; a parameter object would add
    // indirection without value for this private inner-class constructor.
    ContentTask(
      NodeRef parentRef,
      String name,
      String title,
      String description,
      String uri,
      Date created,
      String creator,
      Date modified,
      String modifier
    ) {
      this.parentRef = parentRef;
      this.name = name;
      this.title = title;
      this.description = description;
      this.uri = uri;
      this.created = created;
      this.creator = creator;
      this.modified = modified;
      this.modifier = modifier;
    }
  }

  /**
   * A multilingual document group: a set of translation {@link ContentTask}s that
   * must be linked under a single {@code cm:mlContainer} after the nodes are created.
   */
  private static class MlGroup {

    Locale pivotLang;
    MLText title;
    Discussions discussions;
    final List<ContentTask> translations = new ArrayList<>();
  }

  private void importLibraryBatched(
    NodeRef parentRef,
    Library library,
    ImportResult result,
    Map<Integer, NodeRef> keywordMap
  ) {
    // Disable behaviours and rules for the entire library import so that:
    // 1) Adding children to folders doesn't update parent's cm:modified
    // 2) Content writes don't override audit metadata
    // 3) Inbound rules (async aspect additions) don't overwrite cm:modified after commit
    List<MlGroup> mlGroups = new ArrayList<>();
    policyBehaviourFilter.disableBehaviour();
    ruleService.disableRules();
    try {
      // Collect all work items (folders first, then content with parallel downloads)
      List<ContentTask> allTasks = new ArrayList<>();
      collectLibraryTasks(
        parentRef,
        library.getSpaces(),
        library.getContents(),
        library.getMlContents(),
        library.getDossiers(),
        allTasks,
        mlGroups
      );

      logger.info(
        "IgImportService: Library has " +
          allTasks.size() +
          " content items to import"
      );

      // Process in batches — download per batch to avoid future timeout
      String authHeader = buildAuthHeader();

      AtomicInteger nodesCreated = new AtomicInteger(0);
      AtomicInteger contentWritten = new AtomicInteger(0);
      for (int i = 0; i < allTasks.size(); i += BATCH_SIZE) {
        int end = Math.min(i + BATCH_SIZE, allTasks.size());
        List<ContentTask> batch = allTasks.subList(i, end);

        // Submit downloads for this batch only
        submitDownloads(batch, authHeader);

        // Create nodes and write content
        try {
          runInNewTransaction(() -> {
            policyBehaviourFilter.disableBehaviour();
            for (ContentTask task : batch) {
              createLibraryContentNode(
                task,
                keywordMap,
                authHeader,
                contentWritten
              );
              nodesCreated.incrementAndGet();
            }
            return null;
          });
        } catch (Exception e) {
          result.addError("Library batch at " + i + ": " + e.getMessage());
          logger.error("Library batch failed at offset " + i, e);
        }
        if (i % 2000 == 0 && i > 0) {
          logger.info(
            "IgImportService: Library progress " +
              i +
              "/" +
              allTasks.size() +
              " contentWritten=" +
              contentWritten.get()
          );
        }
      }
      result.addNodes(nodesCreated.get());
      logger.info(
        "IgImportService: Library done. " +
          nodesCreated.get() +
          " nodes, " +
          contentWritten.get() +
          " with content"
      );
    } finally {
      ruleService.enableRules();
      policyBehaviourFilter.enableBehaviour();
    }
    // Link multilingual documents under mlContainers (behaviours enabled so the
    // cm:mlDocument/cm:mlContainer model policies run correctly).
    if (!mlGroups.isEmpty()) {
      linkMlContainers(mlGroups, result);
    }
  }

  /**
   * Creates one library content node: writes its content, applies CircaBC document
   * properties, attaches its discussion, and rebuilds its version history (unless it
   * is a multilingual translation, which is versioned after the mlContainer is linked).
   */
  private void createLibraryContentNode(
    ContentTask task,
    Map<Integer, NodeRef> keywordMap,
    String authHeader,
    AtomicInteger contentWritten
  ) {
    NodeRef ref = createContentWithProps(
      task.parentRef,
      task.name,
      task.title,
      task.description
    );
    task.createdRef = ref;
    if (task.downloadFuture != null) {
      writeDownloadedContent(ref, task);
      contentWritten.incrementAndGet();
    }
    // Apply CircaBC document properties (dynamic properties + keyword references)
    applyContentProperties(ref, task, keywordMap);
    // Attached discussion forum, if any
    if (task.discussions != null) {
      createDiscussion(ref, task.discussions);
    }
    // Reconstruct the full version history (preserving XML labels) when
    // the document has prior versions; otherwise create a single 1.0
    // baseline. Either way the versions API (getCurrentVersion) works —
    // cd:circadocument mandates cm:versionable.
    //
    // EXCEPTION: multilingual translations (task.locale != null) are
    // versioned later, after the mlContainer is linked. Making them
    // versionable now would let makeTranslation/addTranslation
    // auto-create a spurious extra version. CircaBC's own make-multilingual
    // endpoint removes cm:versionable before makeTranslation for the same
    // reason (DIGIT-CIRCABC-2290). See linkMlContainers.
    if (task.locale == null) {
      if (task.versions != null && !task.versions.isEmpty()) {
        importLibraryVersionHistory(ref, task, authHeader);
      } else {
        ensureInitialVersion(ref);
      }
    }
    // Set audit LAST so cm:modified/modifier aren't overwritten by later writes
    setAuditProperties(ref, task);
  }

  /** Returns {@code p.getValue().toString()}, or {@code dflt} when {@code p} is null. */
  private static String strProp(TypedProperty p, String dflt) {
    return p != null ? p.getValue().toString() : dflt;
  }

  /** Returns the property value cast to {@link Date}, or {@code null} when {@code p} is null. */
  private static Date dateProp(TypedProperty p) {
    return p != null ? (Date) p.getValue() : null;
  }

  /** Builds the audit property map, including only the values that are non-null. */
  private Map<QName, Serializable> buildAuditProps(
    Date created,
    String creator,
    Date modified,
    String modifier
  ) {
    Map<QName, Serializable> auditProps = new HashMap<>();
    if (created != null) auditProps.put(ContentModel.PROP_CREATED, created);
    if (creator != null) auditProps.put(ContentModel.PROP_CREATOR, creator);
    if (modified != null) auditProps.put(ContentModel.PROP_MODIFIED, modified);
    if (modifier != null) auditProps.put(ContentModel.PROP_MODIFIER, modifier);
    return auditProps;
  }

  /** Builds an {@link MLText} title from the given I18N properties (blank entries skipped). */
  private MLText buildMlTitle(List<I18NProperty> titles) {
    MLText mlTitle = new MLText();
    if (titles != null) {
      for (I18NProperty v : titles) {
        if (v.getLang() != null && v.getValue() != null) {
          mlTitle.put(v.getLang(), v.getValue().trim());
        }
      }
    }
    return mlTitle;
  }

  /**
   * Creates a folder and stamps its audit metadata with the auditable behaviour
   * disabled, all within the caller's transaction.
   */
  private NodeRef createFolderWithAudit(
    NodeRef parentRef,
    String name,
    String title,
    String desc,
    Map<QName, Serializable> auditProps,
    Node sourceNode
  ) {
    // Disable auditable behaviour globally BEFORE node creation.
    // BehaviourFilter state is transaction-scoped (TransactionalResourceHelper),
    // so this only affects this transaction and is cleaned up on commit.
    policyBehaviourFilter.disableBehaviour();
    NodeRef ref = createFolderWithProps(parentRef, name, title, desc);
    // Apply the migrated aspect BEFORE the audit properties so that any
    // cm:modified/cm:modifier stamping caused by addAspect is overwritten by the
    // original exported audit values written last.
    applyMigratedAspect(ref, sourceNode);
    // Set all auditable properties in ONE call so they go through
    // AbstractNodeDAOImpl.setNodePropertiesImpl atomically.
    if (!auditProps.isEmpty()) {
      nodeService.addProperties(ref, auditProps);
    }
    return ref;
  }

  /** Collects one {@link Space}: creates its folder in a mini-tx, then recurses into its children. */
  private void collectSpaceTask(
    NodeRef parentRef,
    Space space,
    List<ContentTask> tasks,
    List<MlGroup> mlGroups
  ) {
    String name = strProp(space.getName(), "folder");
    String title = strProp(space.getTitle(), null);
    String desc = strProp(space.getDescription(), null);
    Date created = dateProp(space.getCreated());
    String creator = strProp(space.getCreator(), null);
    Date modified = dateProp(space.getModified());
    String modifier = strProp(space.getModifier(), null);

    NodeRef folderRef;
    try {
      folderRef = runInNewTx(() ->
        createFolderWithAudit(
          parentRef,
          name,
          title,
          desc,
          buildAuditProps(created, creator, modified, modifier),
          space
        )
      );
    } catch (Exception e) {
      logger.warn("Failed to create folder " + name, e);
      return;
    }
    // Recurse into subfolder
    collectLibraryTasks(
      folderRef,
      space.getSpaces(),
      space.getContents(),
      space.getMlContents(),
      space.getDossiers(),
      tasks,
      mlGroups
    );
  }

  /** Builds a {@link ContentTask} from a {@link Content} element. */
  private ContentTask buildContentTask(NodeRef parentRef, Content content) {
    String name = strProp(content.getName(), "file");
    String title = strProp(content.getTitle(), null);
    String desc = strProp(content.getDescription(), null);
    Date created = dateProp(content.getCreated());
    String creator = strProp(content.getCreator(), null);
    Date modified = dateProp(content.getModified());
    String modifier = strProp(content.getModifier(), null);
    ContentTask task = new ContentTask(
      parentRef,
      name,
      title,
      desc,
      content.getUri(),
      created,
      creator,
      modified,
      modifier
    );
    task.dynProps = collectContentDynProps(content);
    task.keywordIds = (content.getKeywords() != null)
      ? content.getKeywords().getIds()
      : null;
    task.bcProps = buildBcMetadata(
      content.getStatus(),
      content.getSecurityRanking(),
      content.getIssueDate(),
      content.getExpirationDate(),
      content.getReference(),
      content.getAuthor()
    );
    task.discussions = content.getDiscussions();
    task.versions = (content.getVersions() != null)
      ? content.getVersions().getVersions()
      : null;
    task.versionLabel = (content.getVersionLabel() != null)
      ? content.getVersionLabel().getValue().toString()
      : null;
    task.originalNodeRef =
      content.getOriginalNodeRef() != null
        ? content.getOriginalNodeRef().toString()
        : null;
    return task;
  }

  /** Collects one multilingual document: queues each translation and records the group. */
  private void collectMlContentTask(
    NodeRef parentRef,
    MlContent ml,
    List<ContentTask> tasks,
    List<MlGroup> mlGroups
  ) {
    MlGroup group = new MlGroup();
    group.pivotLang = toLocale(ml.getPivotLang());
    group.title = buildMlTitle(ml.getI18NTitles());
    // The multilingual document itself may carry a discussion (distinct from
    // per-translation discussions per NodesSchema libraryMLContent) — attach
    // it to the ML container node after linking.
    group.discussions = ml.getDiscussions();
    // securityRanking and expirationDate are properties of the multilingual
    // document as a whole (the MlContent), not of individual translations —
    // the JAXB LibraryTranslation type has no such getters. Read them once
    // from the parent and apply to each translation.
    TypedProperty mlSecurityRanking = ml.getSecurityRanking();
    TypedProperty mlExpirationDate = ml.getExpirationDate();
    if (ml.getTranslations() != null) {
      for (LibraryTranslation tr : ml.getTranslations()) {
        ContentTask task = buildTranslationTask(
          parentRef,
          tr,
          mlSecurityRanking,
          mlExpirationDate
        );
        tasks.add(task);
        group.translations.add(task);
      }
    }
    mlGroups.add(group);
  }

  /** Builds a translation {@link ContentTask}, applying the ML-level security/expiration. */
  private ContentTask buildTranslationTask(
    NodeRef parentRef,
    LibraryTranslation tr,
    TypedProperty mlSecurityRanking,
    TypedProperty mlExpirationDate
  ) {
    String name = strProp(tr.getName(), "file");
    String trTitle = strProp(tr.getTitle(), null);
    Date created = dateProp(tr.getCreated());
    String creator = strProp(tr.getCreator(), null);
    Date modified = dateProp(tr.getModified());
    String modifier = strProp(tr.getModifier(), null);
    ContentTask task = new ContentTask(
      parentRef,
      name,
      trTitle,
      null,
      tr.getUri(),
      created,
      creator,
      modified,
      modifier
    );
    task.locale = toLocale(tr.getLang());
    task.dynProps = collectTranslationDynProps(tr);
    task.keywordIds = (tr.getKeywords() != null)
      ? tr.getKeywords().getIds()
      : null;
    task.bcProps = buildBcMetadata(
      tr.getStatus(),
      mlSecurityRanking,
      tr.getIssueDate(),
      mlExpirationDate,
      tr.getReference(),
      tr.getAuthor()
    );
    task.discussions = tr.getDiscussions();
    task.versions = (tr.getVersions() != null)
      ? tr.getVersions().getVersions()
      : null;
    task.versionLabel = (tr.getVersionLabel() != null)
      ? tr.getVersionLabel().getValue().toString()
      : null;
    task.originalNodeRef =
      tr.getOriginalNodeRef() != null
        ? tr.getOriginalNodeRef().toString()
        : null;
    return task;
  }

  /** Creates a do:dossier node with title and audit metadata in its own transaction. */
  private void createDossier(NodeRef parentRef, Dossier dossier) {
    String name = strProp(dossier.getName(), "dossier");
    MLText title = buildMlTitle(dossier.getI18NTitles());
    if (title.isEmpty() && dossier.getTitle() != null) {
      title.put(Locale.ENGLISH, dossier.getTitle().getValue().toString());
    }
    Date created = dateProp(dossier.getCreated());
    String creator = strProp(dossier.getCreator(), null);
    Date modified = dateProp(dossier.getModified());
    String modifier = strProp(dossier.getModifier(), null);
    final MLText fTitle = title;
    try {
      runInNewTx(() ->
        createDossierNode(
          parentRef,
          name,
          fTitle,
          new AuditMetadata(created, creator, modified, modifier),
          dossier
        )
      );
    } catch (Exception e) {
      logger.warn("Failed to create dossier " + name, e);
    }
  }

  /** Audit timestamps and author names captured from the export source. */
  private record AuditMetadata(
    Date created,
    String creator,
    Date modified,
    String modifier
  ) {}

  /** Creates the dossier node, sets its title and audit metadata (behaviour disabled). */
  private NodeRef createDossierNode(
    NodeRef parentRef,
    String name,
    MLText title,
    AuditMetadata audit,
    Node sourceNode
  ) {
    policyBehaviourFilter.disableBehaviour();
    NodeRef ref = createNode(parentRef, name, DossierModel.TYPE_DOSSIER_SPACE);
    if (!title.isEmpty()) {
      if (!nodeService.hasAspect(ref, ContentModel.ASPECT_TITLED)) {
        nodeService.addAspect(ref, ContentModel.ASPECT_TITLED, null);
      }
      nodeService.setProperty(ref, ContentModel.PROP_TITLE, title);
    }
    // Apply the migrated aspect BEFORE the audit properties so the exported
    // created/creator/modified/modifier values are the last write and preserved.
    applyMigratedAspect(ref, sourceNode);
    Map<QName, Serializable> auditProps = buildAuditProps(
      audit.created(),
      audit.creator(),
      audit.modified(),
      audit.modifier()
    );
    if (!auditProps.isEmpty()) {
      nodeService.addProperties(ref, auditProps);
    }
    return ref;
  }

  private void collectLibraryTasks(
    NodeRef parentRef,
    List<Space> spaces,
    List<Content> contents,
    List<MlContent> mlContents,
    List<Dossier> dossiers,
    List<ContentTask> tasks,
    List<MlGroup> mlGroups
  ) {
    // Folders must be created first so children can reference them
    if (spaces != null) {
      for (Space space : spaces) {
        collectSpaceTask(parentRef, space, tasks, mlGroups);
      }
    }
    // Content goes into the batch queue
    if (contents != null) {
      for (Content content : contents) {
        tasks.add(buildContentTask(parentRef, content));
      }
    }
    // Multilingual documents: queue each translation as a normal content task and
    // record the group so the mlContainer can be built after node creation.
    if (mlContents != null) {
      for (MlContent ml : mlContents) {
        collectMlContentTask(parentRef, ml, tasks, mlGroups);
      }
    }
    // Dossiers: CircaBC do:dossier nodes (a cm:folder subtype). Create them with
    // title and audit metadata. (URL/link children are out of scope here.)
    if (dossiers != null) {
      for (Dossier dossier : dossiers) {
        createDossier(parentRef, dossier);
      }
    }
  }

  /** Convert a JAXB LocaleProperty to a {@link Locale}, tolerating either a Locale or String value. */
  private Locale toLocale(TypedProperty.LocaleProperty lp) {
    if (lp == null) {
      return null;
    }
    Object v = lp.getValue();
    if (v == null) {
      return null;
    }
    if (v instanceof Locale loc) {
      return loc;
    }
    String s = v.toString().trim();
    return s.isEmpty() ? null : Locale.of(s);
  }

  /** Returns a mutable list of the given properties, skipping nulls (order preserved). */
  private List<TypedProperty> nonNullList(TypedProperty... props) {
    List<TypedProperty> list = new ArrayList<>();
    for (TypedProperty p : props) {
      if (p != null) {
        list.add(p);
      }
    }
    return list;
  }

  /** Collect the non-null dynamic property values of a translation node. */
  private List<TypedProperty> collectTranslationDynProps(LibraryTranslation t) {
    return nonNullList(
      t.getDynamicProperty1(),
      t.getDynamicProperty2(),
      t.getDynamicProperty3(),
      t.getDynamicProperty4(),
      t.getDynamicProperty5(),
      t.getDynamicProperty6(),
      t.getDynamicProperty7(),
      t.getDynamicProperty8(),
      t.getDynamicProperty9(),
      t.getDynamicProperty10(),
      t.getDynamicProperty11(),
      t.getDynamicProperty12(),
      t.getDynamicProperty13(),
      t.getDynamicProperty14(),
      t.getDynamicProperty15(),
      t.getDynamicProperty16(),
      t.getDynamicProperty17(),
      t.getDynamicProperty18(),
      t.getDynamicProperty19(),
      t.getDynamicProperty20()
    );
  }

  /** Selects the translations that were actually created, de-duplicated by locale. */
  private List<ContentTask> collectCreatedTranslations(MlGroup group) {
    List<ContentTask> created = new ArrayList<>();
    Set<Locale> seen = new HashSet<>();
    for (ContentTask t : group.translations) {
      if (
        t.createdRef != null &&
        t.locale != null &&
        nodeService.exists(t.createdRef) &&
        seen.add(t.locale)
      ) {
        created.add(t);
      }
    }
    return created;
  }

  /**
   * Removes cm:versionable from the given translation nodes so that
   * makeTranslation/addTranslation do not auto-create a spurious extra version.
   */
  private void removeVersionableAspect(List<ContentTask> created) {
    for (ContentTask t : created) {
      if (
        nodeService.hasAspect(t.createdRef, ContentModel.ASPECT_VERSIONABLE)
      ) {
        nodeService.removeAspect(t.createdRef, ContentModel.ASPECT_VERSIONABLE);
      }
    }
  }

  /** Chooses the pivot translation (matching pivotLang, else the first created). */
  private ContentTask choosePivot(MlGroup group, List<ContentTask> created) {
    if (group.pivotLang != null) {
      for (ContentTask t : created) {
        if (group.pivotLang.equals(t.locale)) {
          return t;
        }
      }
    }
    return created.get(0);
  }

  /** Attaches every non-pivot translation to the pivot's container. */
  private void linkTranslations(List<ContentTask> created, ContentTask pivot) {
    for (ContentTask t : created) {
      if (t == pivot) {
        continue;
      }
      if (!multilingualContentService.isTranslation(t.createdRef)) {
        multilingualContentService.addTranslation(
          t.createdRef,
          pivot.createdRef,
          t.locale
        );
      }
    }
  }

  /** Sets the container title from the ML titles and attaches its discussion, if any. */
  private void setContainerTitleAndDiscussion(
    ContentTask pivot,
    MlGroup group
  ) {
    NodeRef container = multilingualContentService.getTranslationContainer(
      pivot.createdRef
    );
    if (container != null) {
      if (group.title != null && !group.title.isEmpty()) {
        nodeService.setProperty(
          container,
          ContentModel.PROP_TITLE,
          group.title
        );
      }
      if (group.discussions != null) {
        createDiscussion(container, group.discussions);
      }
    }
  }

  /** (Re)builds each translation's version history, or a single 1.0 baseline. */
  private void rebuildTranslationVersions(MlGroup group, String authHeader) {
    for (ContentTask t : group.translations) {
      if (t.createdRef == null || !nodeService.exists(t.createdRef)) {
        continue;
      }
      if (t.versions != null && !t.versions.isEmpty()) {
        importLibraryVersionHistory(t.createdRef, t, authHeader);
      } else {
        ensureInitialVersion(t.createdRef);
      }
    }
  }

  /** Links one multilingual group's translations under a container within a transaction. */
  private void linkMlGroupInTx(
    MlGroup group,
    String authHeader,
    ImportResult result
  ) {
    // The importer creates content with behaviours disabled, so nodes do
    // not carry every mandatory aspect that cd:circadocument declares
    // (bproperties/cproperties/versionable). makeTranslation/addTranslation
    // mark those nodes dirty; with behaviours enabled the IntegrityChecker
    // would then reject the commit. Disable behaviours here too — the ML
    // container and associations are created programmatically by the
    // service and do not rely on policy behaviours.
    policyBehaviourFilter.disableBehaviour();
    // Only translations that were actually created and carry a locale
    List<ContentTask> created = collectCreatedTranslations(group);
    if (created.isEmpty()) {
      return;
    }
    // Ensure the translation nodes are NOT versionable while they are linked,
    // mirroring CircaBC's make-multilingual endpoint for DIGIT-CIRCABC-2290.
    // The version history is rebuilt after linking, below.
    removeVersionableAspect(created);
    // Choose the pivot translation (match pivotLang, else first)
    ContentTask pivot = choosePivot(group, created);
    if (!multilingualContentService.isTranslation(pivot.createdRef)) {
      multilingualContentService.makeTranslation(
        pivot.createdRef,
        pivot.locale
      );
    }
    linkTranslations(created, pivot);
    // Set the container title from the mlContent I18N titles, and attach
    // the multilingual-document-level discussion (if any) to the container.
    setContainerTitleAndDiscussion(pivot, group);
    // Linking is complete and the nodes were not versionable while
    // makeTranslation/addTranslation ran, so no spurious version was
    // created. Now (re)build each translation's version history.
    rebuildTranslationVersions(group, authHeader);
    result.addNodes(1);
  }

  /**
   * Link the created translation nodes of each multilingual document under a single
   * {@code cm:mlContainer}. The pivot-language translation is registered first
   * (creating the container), then the remaining translations are attached.
   */
  private void linkMlContainers(List<MlGroup> mlGroups, ImportResult result) {
    ruleService.disableRules();
    String authHeader = buildAuthHeader();
    try {
      for (MlGroup group : mlGroups) {
        try {
          runInNewTransaction(() -> {
            linkMlGroupInTx(group, authHeader, result);
            return null;
          });
          logger.info(
            "IgImportService: linked ML container (" +
              group.translations.size() +
              " translations, pivot=" +
              group.pivotLang +
              ")"
          );
        } catch (Exception e) {
          result.addError("MlContent link: " + e.getMessage());
          logger.warn("Failed to link multilingual container", e);
        }
      }
    } finally {
      ruleService.enableRules();
    }
  }

  // === Keyword & Dynamic Property Definitions ===

  /**
   * Import the interest-group-level keyword and dynamic-property definitions.
   * The created keyword nodes are registered in {@code keywordMap} (XML id ->
   * repository NodeRef) so that per-document keyword references can be resolved
   * later when the library content is imported.
   */
  private void importDefinitions(
    NodeRef igRef,
    InterestGroup ig,
    ImportResult result,
    Map<Integer, NodeRef> keywordMap
  ) {
    // --- Dynamic property definitions ---
    if (
      ig.getDynamicPropertyDefinitions() != null &&
      ig.getDynamicPropertyDefinitions().getDefinitions() != null
    ) {
      for (DynamicPropertyDefinition def : ig
        .getDynamicPropertyDefinitions()
        .getDefinitions()) {
        importDynamicPropertyDefinition(igRef, def, result);
      }
    }

    // --- Keyword definitions ---
    if (
      ig.getKeywordDefinitions() != null &&
      ig.getKeywordDefinitions().getDefinitions() != null
    ) {
      for (KeywordDefinition def : ig
        .getKeywordDefinitions()
        .getDefinitions()) {
        importKeywordDefinition(igRef, def, keywordMap, result);
      }
    }
  }

  /** Imports one dynamic property definition onto the IG. */
  private void importDynamicPropertyDefinition(
    NodeRef igRef,
    DynamicPropertyDefinition def,
    ImportResult result
  ) {
    try {
      MLText label = buildDynPropLabel(def);
      DynamicPropertyType type = resolveDynamicPropertyType(def);
      String validValues = null;
      if (
        def.getSelectionCases() != null && !def.getSelectionCases().isEmpty()
      ) {
        validValues = String.join(
          DynamicPropertyService.MULTI_VALUES_SEPARATOR_STRING,
          def.getSelectionCases()
        );
      }
      Long index = def.getId() != null ? def.getId().longValue() : null;
      DynamicProperty dp = new DynamicPropertyImpl(
        index,
        label,
        type,
        validValues
      );
      dynamicPropertyService.addDynamicProperty(igRef, dp);
      result.addNodes(1);
      logger.info(
        "IgImportService: created dynamic property def id=" +
          index +
          " label=" +
          label.getDefaultValue()
      );
    } catch (Exception e) {
      result.addError("DynamicProperty def: " + e.getMessage());
      logger.warn("Failed to import dynamic property definition", e);
    }
  }

  /**
   * Builds the dynamic property label from localised values (trimmed), falling back
   * to the plain value only when no I18N values are present.
   */
  private MLText buildDynPropLabel(DynamicPropertyDefinition def) {
    MLText label = new MLText();
    if (def.getI18NValues() != null && !def.getI18NValues().isEmpty()) {
      for (I18NProperty v : def.getI18NValues()) {
        if (v.getLang() != null && v.getValue() != null) {
          label.put(v.getLang(), v.getValue().trim());
        }
      }
    } else if (def.getValue() != null) {
      label.put(Locale.ENGLISH, def.getValue());
    }
    return label;
  }

  /** Imports one keyword definition and records its id mapping. */
  private void importKeywordDefinition(
    NodeRef igRef,
    KeywordDefinition def,
    Map<Integer, NodeRef> keywordMap,
    ImportResult result
  ) {
    try {
      MLText values = buildMlTitle(def.getI18NValues());
      Keyword kw;
      if (!values.isEmpty()) {
        kw = new KeywordImpl(values);
      } else if (def.getValue() != null && !def.getValue().trim().isEmpty()) {
        kw = new KeywordImpl(def.getValue().trim());
      } else {
        return;
      }
      Keyword created = keywordsService.createKeyword(igRef, kw);
      if (def.getId() != null && created != null && created.getId() != null) {
        keywordMap.put(def.getId(), created.getId());
      }
      result.addNodes(1);
      logger.info(
        "IgImportService: created keyword def id=" +
          def.getId() +
          " values=" +
          values
      );
    } catch (Exception e) {
      result.addError("Keyword def: " + e.getMessage());
      logger.warn("Failed to import keyword definition", e);
    }
  }

  /** Collect the non-null dynamic property values (dynamicProperty1..20) of a content node. */
  private List<TypedProperty> collectContentDynProps(Content c) {
    return nonNullList(
      c.getDynamicProperty1(),
      c.getDynamicProperty2(),
      c.getDynamicProperty3(),
      c.getDynamicProperty4(),
      c.getDynamicProperty5(),
      c.getDynamicProperty6(),
      c.getDynamicProperty7(),
      c.getDynamicProperty8(),
      c.getDynamicProperty9(),
      c.getDynamicProperty10(),
      c.getDynamicProperty11(),
      c.getDynamicProperty12(),
      c.getDynamicProperty13(),
      c.getDynamicProperty14(),
      c.getDynamicProperty15(),
      c.getDynamicProperty16(),
      c.getDynamicProperty17(),
      c.getDynamicProperty18(),
      c.getDynamicProperty19(),
      c.getDynamicProperty20()
    );
  }

  /**
   * Build the CircaBC document metadata property map (status, security ranking,
   * issue/expiration dates, reference and author) from the JAXB typed properties.
   * Empty string values (e.g. an empty reference) are skipped.
   */
  private Map<QName, Serializable> buildBcMetadata(
    TypedProperty status,
    TypedProperty securityRanking,
    TypedProperty issueDate,
    TypedProperty expirationDate,
    TypedProperty reference,
    TypedProperty author
  ) {
    Map<QName, Serializable> m = new HashMap<>();
    putIfValue(m, DocumentModel.PROP_STATUS, status);
    putIfValue(m, DocumentModel.PROP_SECURITY_RANKING, securityRanking);
    putIfValue(m, DocumentModel.PROP_ISSUE_DATE, issueDate);
    putIfValue(m, DocumentModel.PROP_EXPIRATION_DATE, expirationDate);
    putIfValue(m, DocumentModel.PROP_REFERENCE, reference);
    putIfValue(m, ContentModel.PROP_AUTHOR, author);
    return m;
  }

  private void putIfValue(
    Map<QName, Serializable> m,
    QName qname,
    TypedProperty tp
  ) {
    if (tp == null) {
      return;
    }
    Serializable v = tp.getValue();
    if (v == null) {
      return;
    }
    if (v instanceof String s && s.trim().isEmpty()) {
      return;
    }
    m.put(qname, v);
  }

  /**
   * Apply dynamic property values and keyword references to a freshly created
   * content node. Requires the {@code cd:cproperties} aspect (added on demand).
   */
  private void applyContentProperties(
    NodeRef ref,
    ContentTask task,
    Map<Integer, NodeRef> keywordMap
  ) {
    boolean hasDyn = task.dynProps != null && !task.dynProps.isEmpty();
    boolean hasKw = task.keywordIds != null && !task.keywordIds.isEmpty();
    boolean hasMeta = task.bcProps != null && !task.bcProps.isEmpty();
    if (!hasDyn && !hasKw && !hasMeta) {
      return;
    }
    try {
      if (!nodeService.hasAspect(ref, DocumentModel.ASPECT_CPROPERTIES)) {
        nodeService.addAspect(ref, DocumentModel.ASPECT_CPROPERTIES, null);
      }
      // Document metadata (status/securityRanking/issueDate/expirationDate/reference/author).
      // status/issue_date/reference live on cd:cproperties; security_ranking/expiration_date
      // on cd:bproperties (both added in addCircabcAspects); author lives on cm:author.
      if (hasMeta) {
        applyBcMetadata(ref, task);
      }
      if (hasDyn) {
        applyDynProps(ref, task);
      }
      if (hasKw) {
        applyKeywords(ref, task, keywordMap);
      }
    } catch (Exception e) {
      logger.warn(
        "Failed to apply dynprops/keywords for " +
          task.name +
          ": " +
          e.getMessage()
      );
    }
  }

  /** Adds the cm:author aspect when needed and applies the CircaBC metadata properties. */
  private void applyBcMetadata(NodeRef ref, ContentTask task) {
    if (
      task.bcProps.containsKey(ContentModel.PROP_AUTHOR) &&
      !nodeService.hasAspect(ref, ContentModel.ASPECT_AUTHOR)
    ) {
      nodeService.addAspect(ref, ContentModel.ASPECT_AUTHOR, null);
    }
    nodeService.addProperties(ref, task.bcProps);
  }

  /** Sets each non-null dynamic property (by its own QName identifier) on the node. */
  private void applyDynProps(NodeRef ref, ContentTask task) {
    for (TypedProperty tp : task.dynProps) {
      if (tp != null && tp.getIdentifier() != null && tp.getValue() != null) {
        nodeService.setProperty(ref, tp.getIdentifier(), tp.getValue());
      }
    }
  }

  /** Resolves the task's keyword ids to node refs and sets them as the keyword property. */
  private void applyKeywords(
    NodeRef ref,
    ContentTask task,
    Map<Integer, NodeRef> keywordMap
  ) {
    List<NodeRef> kwRefs = new ArrayList<>();
    for (Integer id : task.keywordIds) {
      NodeRef kwRef = keywordMap.get(id);
      if (kwRef != null) {
        kwRefs.add(kwRef);
      }
    }
    if (!kwRefs.isEmpty()) {
      nodeService.setProperty(
        ref,
        DocumentModel.PROP_KEYWORD,
        (Serializable) kwRefs
      );
    }
  }

  /**
   * Ensure the node is versionable and has an initial version (1.0). Uses
   * ensureVersioningEnabled which adds cm:versionable and creates the version
   * head if missing — required because cd:circadocument mandates cm:versionable
   * and the content/versions API dereferences getCurrentVersion().
   */
  private void ensureInitialVersion(NodeRef ref) {
    try {
      Map<QName, Serializable> vProps = new HashMap<>();
      vProps.put(ContentModel.PROP_AUTO_VERSION, Boolean.TRUE);
      vProps.put(ContentModel.PROP_AUTO_VERSION_PROPS, Boolean.FALSE);
      vProps.put(ContentModel.PROP_INITIAL_VERSION, Boolean.TRUE);
      versionService.ensureVersioningEnabled(ref, vProps);
    } catch (Exception e) {
      logger.warn(
        "Failed to enable versioning for " + ref + ": " + e.getMessage()
      );
    }
  }

  /**
   * Reconstruct the full version history of a library document, preserving the
   * exact version labels from the export. Alfresco assigns labels via a serial
   * policy, so we replay each snapshot oldest-to-newest choosing MAJOR/MINOR so
   * the policy reproduces the source labels, then force the stored label to the
   * XML value as a safeguard. The node ends in its current (head) state.
   */
  private void importLibraryVersionHistory(
    NodeRef ref,
    ContentTask task,
    String authHeader
  ) {
    try {
      if (!nodeService.hasAspect(ref, ContentModel.ASPECT_VERSIONABLE)) {
        Map<QName, Serializable> vAspect = new HashMap<>();
        vAspect.put(ContentModel.PROP_AUTO_VERSION, Boolean.TRUE);
        vAspect.put(ContentModel.PROP_AUTO_VERSION_PROPS, Boolean.FALSE);
        vAspect.put(ContentModel.PROP_INITIAL_VERSION, Boolean.FALSE);
        nodeService.addAspect(ref, ContentModel.ASPECT_VERSIONABLE, vAspect);
      }
      // Prior versions, oldest first.
      List<ContentNode> ordered = new ArrayList<>(task.versions);
      ordered.sort((a, b) ->
        compareVersionLabels(versionLabelOf(a), versionLabelOf(b))
      );
      String prevLabel = null;
      for (ContentNode v : ordered) {
        String label = versionLabelOf(v);
        if (v.getUri() != null && !v.getUri().isEmpty()) {
          writeContentFromUri(ref, v.getUri(), task.name, authHeader);
        }
        setAuditPropertiesFromNode(ref, v);
        createLabeledVersion(ref, prevLabel, label, null);
        prevLabel = label;
      }
      // Finally the current (head) content + label.
      if (task.uri != null && !task.uri.isEmpty()) {
        writeContentFromUri(ref, task.uri, task.name, authHeader);
      }
      String headLabel = (task.versionLabel != null)
        ? task.versionLabel
        : nextMinor(prevLabel);
      createLabeledVersion(ref, prevLabel, headLabel, null);
    } catch (Exception e) {
      logger.warn(
        "Version history import failed for " + ref + ": " + e.getMessage()
      );
      ensureInitialVersion(ref);
    }
  }

  /** Create a version whose type makes the serial policy yield {@code label}, then force the label. */
  private void createLabeledVersion(
    NodeRef ref,
    String prevLabel,
    String label,
    String note
  ) {
    VersionType type;
    if (prevLabel == null) {
      type = VersionType.MAJOR; // first version -> 1.0
    } else {
      int[] pv = parseVersionLabel(prevLabel);
      int[] cv = parseVersionLabel(label);
      type = (cv[0] > pv[0]) ? VersionType.MAJOR : VersionType.MINOR;
    }
    Map<String, Serializable> vp = new HashMap<>();
    vp.put(VersionBaseModel.PROP_VERSION_TYPE, type);
    if (note != null) {
      vp.put(Version.PROP_DESCRIPTION, note);
    }
    Version created = versionService.createVersion(ref, vp);
    forceVersionLabel(created, label);
  }

  /** Best-effort override of the stored version label to the exact XML value. */
  private void forceVersionLabel(Version version, String label) {
    if (version == null || label == null) {
      return;
    }
    try {
      NodeRef vnode = version.getFrozenStateNodeRef();
      if (vnode != null && nodeService.exists(vnode)) {
        nodeService.setProperty(
          vnode,
          VersionModel.PROP_QNAME_VERSION_LABEL,
          label
        );
        nodeService.setProperty(vnode, ContentModel.PROP_VERSION_LABEL, label);
      }
    } catch (Exception e) {
      logger.warn(
        "Could not force version label " + label + ": " + e.getMessage()
      );
    }
  }

  private void writeContentFromUri(
    NodeRef ref,
    String uri,
    String name,
    String authHeader
  ) {
    try {
      Path tmp = downloadToTemp(uri, authHeader);
      if (tmp != null) {
        ContentWriter w = contentService.getWriter(
          ref,
          ContentModel.PROP_CONTENT,
          true
        );
        w.setMimetype(guessMimeType(name));
        w.putContent(tmp.toFile());
        Files.deleteIfExists(tmp);
      }
    } catch (Exception e) {
      logger.warn("Failed to download version content: " + uri, e);
    }
  }

  private String versionLabelOf(ContentNode v) {
    return (
        v.getVersionLabel() != null && v.getVersionLabel().getValue() != null
      )
      ? v.getVersionLabel().getValue().toString()
      : "1.0";
  }

  private int[] parseVersionLabel(String label) {
    try {
      String[] p = label.trim().split("\\.");
      int major = Integer.parseInt(p[0]);
      int minor = p.length > 1 ? Integer.parseInt(p[1]) : 0;
      return new int[] { major, minor };
    } catch (Exception e) {
      return new int[] { 1, 0 };
    }
  }

  private int compareVersionLabels(String a, String b) {
    int[] pa = parseVersionLabel(a);
    int[] pb = parseVersionLabel(b);
    return (pa[0] != pb[0])
      ? Integer.compare(pa[0], pb[0])
      : Integer.compare(pa[1], pb[1]);
  }

  private String nextMinor(String prevLabel) {
    if (prevLabel == null) {
      return "1.0";
    }
    int[] p = parseVersionLabel(prevLabel);
    return p[0] + "." + (p[1] + 1);
  }

  // === Parallel Download ===

  /**
   * Parse a download URI, percent-encoding characters that are illegal in a URI
   * path but appear literally in some exported filenames (e.g. square brackets
   * in "background_document_en[1].pdf", spaces, braces). Only applied as a
   * fallback when strict parsing fails, so already-encoded URIs are untouched.
   */
  private URI parseUriLenient(String uri) {
    try {
      return URI.create(uri);
    } catch (IllegalArgumentException e) {
      String fixed = uri
        .replace(" ", "%20")
        .replace("[", "%5B")
        .replace("]", "%5D")
        .replace("{", "%7B")
        .replace("}", "%7D")
        .replace("|", "%7C")
        .replace("^", "%5E")
        .replace("`", "%60")
        .replace("\"", "%22")
        .replace("<", "%3C")
        .replace(">", "%3E");
      return URI.create(fixed);
    }
  }

  private Path downloadToTemp(String uri, String authHeader) {
    try {
      HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
        .uri(parseUriLenient(uri))
        .timeout(Duration.ofSeconds(60))
        .GET();
      if (authHeader != null) {
        reqBuilder.header("Authorization", authHeader);
      }
      HttpResponse<Path> resp = httpClient.send(
        reqBuilder.build(),
        HttpResponse.BodyHandlers.ofFile(
          TempFileProvider.createTempFile("import-", ".tmp").toPath()
        )
      );
      if (resp.statusCode() == 200) {
        return resp.body();
      } else {
        Files.deleteIfExists(resp.body());
        logger.warn(
          "Download failed HTTP " + resp.statusCode() + " for " + uri
        );
        return null;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.warn("Download interrupted for " + uri + ": " + e.getMessage());
      return null;
    } catch (Exception e) {
      logger.warn("Download error for " + uri + ": " + e.getMessage());
      return null;
    }
  }

  private void writeDownloadedContent(NodeRef ref, ContentTask task) {
    try {
      Path tempFile = task.downloadFuture.get(120, TimeUnit.SECONDS);
      if (tempFile != null && Files.exists(tempFile)) {
        try (InputStream is = Files.newInputStream(tempFile)) {
          ContentWriter writer = contentService.getWriter(
            ref,
            ContentModel.PROP_CONTENT,
            true
          );
          writer.setMimetype(guessMimeType(task.name));
          writer.putContent(is);
        } finally {
          Files.deleteIfExists(tempFile);
        }
      } else {
        logger.error(
          "IgImportService: download returned null for " +
            task.name +
            " uri=" +
            task.uri
        );
      }
    } catch (TimeoutException e) {
      logger.error(
        "IgImportService: Download TIMED OUT for " +
          task.name +
          " uri=" +
          task.uri
      );
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error(
        "IgImportService: Interrupted while writing content for " +
          task.name +
          ": " +
          e.getMessage()
      );
    } catch (Exception e) {
      logger.error(
        "IgImportService: Error writing content for " +
          task.name +
          ": " +
          e.getMessage()
      );
    }
  }

  // === Information Import (batched) ===

  private void importInformationBatched(
    NodeRef parentRef,
    Information info,
    ImportResult result
  ) {
    // Folders first
    importInfSpaces(parentRef, info, result);
    // Content with parallel downloads — per batch
    importInfContents(parentRef, info, result);
    // News items
    importInfNews(parentRef, info, result);
  }

  /** Creates the information sub-folders (and their trees) in one transaction. */
  private void importInfSpaces(
    NodeRef parentRef,
    Information info,
    ImportResult result
  ) {
    if (info.getInfSpaces() == null) return;
    try {
      runInNewTransaction(() -> {
        policyBehaviourFilter.disableBehaviour();
        String authHeader = buildAuthHeader();
        for (InfSpace s : info.getInfSpaces()) {
          createInfSpaceTree(parentRef, s, result, authHeader);
        }
        return null;
      });
    } catch (Exception e) {
      result.addError("Info folders: " + e.getMessage());
    }
  }

  /** Imports information content documents in batches with parallel downloads. */
  private void importInfContents(
    NodeRef parentRef,
    Information info,
    ImportResult result
  ) {
    if (
      info.getInfContents() == null || info.getInfContents().isEmpty()
    ) return;
    List<ContentTask> tasks = new ArrayList<>();
    String authHeader = buildAuthHeader();
    for (InfContent ic : info.getInfContents()) {
      tasks.add(buildInfContentTask(parentRef, ic));
    }
    AtomicInteger count = new AtomicInteger(0);
    for (int i = 0; i < tasks.size(); i += BATCH_SIZE) {
      int end = Math.min(i + BATCH_SIZE, tasks.size());
      List<ContentTask> batch = tasks.subList(i, end);
      submitDownloads(batch, authHeader);
      try {
        runInNewTransaction(() -> {
          policyBehaviourFilter.disableBehaviour();
          for (ContentTask task : batch) {
            createInfContentNode(task, authHeader);
            count.incrementAndGet();
          }
          return null;
        });
      } catch (Exception e) {
        result.addError("Info batch at " + i + ": " + e.getMessage());
      }
    }
    result.addNodes(count.get());
  }

  /** Builds a {@link ContentTask} for an information content document. */
  private ContentTask buildInfContentTask(NodeRef parentRef, InfContent ic) {
    String name = strProp(ic.getName(), "file");
    Date created = dateProp(ic.getCreated());
    String creator = strProp(ic.getCreator(), null);
    Date modified = dateProp(ic.getModified());
    String modifier = strProp(ic.getModifier(), null);
    ContentTask ct = new ContentTask(
      parentRef,
      name,
      null,
      null,
      ic.getUri(),
      created,
      creator,
      modified,
      modifier
    );
    ct.versions = (ic.getVersions() != null)
      ? ic.getVersions().getVersions()
      : null;
    ct.versionLabel = (ic.getVersionLabel() != null)
      ? ic.getVersionLabel().getValue().toString()
      : null;
    ct.originalNodeRef =
      ic.getOriginalNodeRef() != null
        ? ic.getOriginalNodeRef().toString()
        : null;
    return ct;
  }

  /** Submits async downloads for every task in the batch that has a URI. */
  private void submitDownloads(List<ContentTask> batch, String authHeader) {
    for (ContentTask t : batch) {
      if (t.uri != null && !t.uri.isEmpty()) {
        t.downloadFuture = downloadExecutor.submit(() ->
          downloadToTemp(t.uri, authHeader)
        );
      }
    }
  }

  /** Creates one information content node, writes its content and versions, sets audit. */
  private void createInfContentNode(ContentTask task, String authHeader) {
    NodeRef ref = createContentWithProps(task.parentRef, task.name, null, null);
    if (task.downloadFuture != null) writeDownloadedContent(ref, task);
    // Version the info document so it round-trips like the library:
    // replay history when present, else a single 1.0 baseline.
    if (task.versions != null && !task.versions.isEmpty()) {
      importLibraryVersionHistory(ref, task, authHeader);
    } else {
      ensureInitialVersion(ref);
    }
    // Set audit AFTER content write
    setAuditProperties(ref, task);
  }

  /** Imports information news items (and their attachments) in one transaction. */
  private void importInfNews(
    NodeRef parentRef,
    Information info,
    ImportResult result
  ) {
    if (info.getInfNews() == null || info.getInfNews().isEmpty()) return;
    try {
      runInNewTransaction(() -> {
        policyBehaviourFilter.disableBehaviour();
        for (InfNews newsItem : info.getInfNews()) {
          importNewsItem(parentRef, newsItem, result);
        }
        return null;
      });
    } catch (Exception e) {
      result.addError("Info news: " + e.getMessage());
      logger.warn("Info news import failed", e);
    }
  }

  /** Creates a single news node with its title, aspect properties, audit and attachments. */
  private void importNewsItem(
    NodeRef parentRef,
    InfNews newsItem,
    ImportResult result
  ) {
    String newsName = "news_" + UUID.randomUUID();
    NodeRef newsRef = createNode(
      parentRef,
      newsName,
      CircabcModel.TYPE_INFORMATION_NEWS
    );
    setNewsTitle(newsRef, newsItem);
    nodeService.addAspect(
      newsRef,
      CircabcModel.ASPECT_INFORMATION_NEWS,
      buildNewsProps(newsItem)
    );
    // Set audit properties (created/modified/creator/modifier)
    setAuditPropertiesFromNode(newsRef, newsItem);
    // Import news attachments (infContents) as children
    if (newsItem.getInfContents() != null) {
      String authHeader = buildAuthHeader();
      for (InfContent att : newsItem.getInfContents()) {
        importNewsAttachment(newsRef, att, authHeader);
      }
    }
    result.addNodes(1);
  }

  /** Sets the news node title from its i18n titles (untrimmed), falling back to the plain title. */
  private void setNewsTitle(NodeRef newsRef, InfNews newsItem) {
    if (
      newsItem.getI18NTitles() != null && !newsItem.getI18NTitles().isEmpty()
    ) {
      MLText mlTitle = new MLText();
      for (I18NProperty i18n : newsItem.getI18NTitles()) {
        if (i18n.getLang() != null && i18n.getValue() != null) {
          mlTitle.put(i18n.getLang(), i18n.getValue());
        }
      }
      if (!mlTitle.isEmpty()) {
        nodeService.setProperty(newsRef, ContentModel.PROP_TITLE, mlTitle);
      }
    } else if (newsItem.getTitle() != null) {
      nodeService.setProperty(
        newsRef,
        ContentModel.PROP_TITLE,
        newsItem.getTitle().getValue().toString()
      );
    }
  }

  /** Collects the non-null news aspect properties into a map. */
  private Map<QName, Serializable> buildNewsProps(InfNews newsItem) {
    Map<QName, Serializable> newsProps = new HashMap<>();
    if (newsItem.getNewsContent() != null) {
      newsProps.put(CircabcModel.PROP_NEWS_CONTENT, newsItem.getNewsContent());
    }
    if (newsItem.getNewsPattern() != null) {
      newsProps.put(CircabcModel.PROP_NEWS_PATTERN, newsItem.getNewsPattern());
    }
    if (newsItem.getNewsLayout() != null) {
      newsProps.put(CircabcModel.PROP_NEWS_LAYOUT, newsItem.getNewsLayout());
    }
    if (newsItem.getNewsSize() != null) {
      newsProps.put(CircabcModel.PROP_NEWS_SIZE, newsItem.getNewsSize());
    }
    if (newsItem.getNewsDate() != null) {
      newsProps.put(CircabcModel.PROP_NEWS_DATE, newsItem.getNewsDate());
    }
    if (newsItem.getNewsUrl() != null) {
      newsProps.put(CircabcModel.PROP_NEWS_URL, newsItem.getNewsUrl());
    }
    return newsProps;
  }

  /** Creates a news attachment node, downloads its content, versions it and sets audit. */
  private void importNewsAttachment(
    NodeRef newsRef,
    InfContent att,
    String authHeader
  ) {
    String attName = strProp(att.getName(), "attachment");
    NodeRef attRef = createContentWithProps(newsRef, attName, null, null);
    if (att.getUri() != null && !att.getUri().isEmpty()) {
      try {
        Path tempFile = downloadToTemp(att.getUri(), authHeader);
        if (tempFile != null) {
          ContentWriter w = contentService.getWriter(
            attRef,
            ContentModel.PROP_CONTENT,
            true
          );
          w.setMimetype(guessMimeType(attName));
          w.putContent(tempFile.toFile());
          Files.deleteIfExists(tempFile);
        }
      } catch (Exception dlEx) {
        logger.warn(
          "Failed to download news attachment: " + att.getUri(),
          dlEx
        );
      }
    }
    // Version the attachment so it round-trips like other content:
    // replay history when present, else a single 1.0 baseline.
    ContentTask attTask = new ContentTask(
      newsRef,
      attName,
      null,
      null,
      att.getUri(),
      null,
      null,
      null,
      null
    );
    attTask.versions = (att.getVersions() != null)
      ? att.getVersions().getVersions()
      : null;
    attTask.versionLabel = (att.getVersionLabel() != null)
      ? att.getVersionLabel().getValue().toString()
      : null;
    if (attTask.versions != null && !attTask.versions.isEmpty()) {
      importLibraryVersionHistory(attRef, attTask, authHeader);
    } else {
      ensureInitialVersion(attRef);
    }
    setAuditPropertiesFromNode(attRef, att);
  }

  /**
   * Recursively create an information folder tree: the folder itself with its
   * audit metadata, its infContent children (downloaded synchronously in this
   * transaction), and any nested sub-spaces. Content directly under an infSpace
   * was previously skipped (only the folder was created).
   */
  private void createInfSpaceTree(
    NodeRef parentRef,
    InfSpace s,
    ImportResult result,
    String authHeader
  ) {
    String name =
      s.getName() != null ? s.getName().getValue().toString() : "folder";
    NodeRef ref = createFolderWithProps(parentRef, name, null, null);
    // Apply the migrated aspect BEFORE writing the audit properties so the
    // exported created/creator/modified/modifier values are preserved.
    applyMigratedAspect(ref, s);
    Map<QName, Serializable> auditProps = new HashMap<>();
    if (s.getCreated() != null) auditProps.put(
      ContentModel.PROP_CREATED,
      s.getCreated().getValue()
    );
    if (s.getCreator() != null) auditProps.put(
      ContentModel.PROP_CREATOR,
      s.getCreator().getValue().toString()
    );
    if (s.getModified() != null) auditProps.put(
      ContentModel.PROP_MODIFIED,
      s.getModified().getValue()
    );
    if (s.getModifier() != null) auditProps.put(
      ContentModel.PROP_MODIFIER,
      s.getModifier().getValue().toString()
    );
    if (!auditProps.isEmpty()) nodeService.addProperties(ref, auditProps);
    result.addNodes(1);
    if (s.getInfContents() != null) {
      for (InfContent ic : s.getInfContents()) {
        createInfContentNode(ref, ic, result, authHeader);
      }
    }
    if (s.getInfSpaces() != null) {
      for (InfSpace sub : s.getInfSpaces()) {
        createInfSpaceTree(ref, sub, result, authHeader);
      }
    }
  }

  /** Create one information content node (synchronous download + audit). */
  private void createInfContentNode(
    NodeRef parentRef,
    InfContent ic,
    ImportResult result,
    String authHeader
  ) {
    String name =
      ic.getName() != null ? ic.getName().getValue().toString() : "file";
    NodeRef ref = createContentWithProps(parentRef, name, null, null);
    // Reconstruct version history when present (preserving XML labels), like the
    // library import; otherwise write head content and create a single baseline.
    ContentTask task = new ContentTask(
      parentRef,
      name,
      null,
      null,
      ic.getUri(),
      null,
      null,
      null,
      null
    );
    task.versions = (ic.getVersions() != null)
      ? ic.getVersions().getVersions()
      : null;
    task.versionLabel = (ic.getVersionLabel() != null)
      ? ic.getVersionLabel().getValue().toString()
      : null;
    if (task.versions != null && !task.versions.isEmpty()) {
      importLibraryVersionHistory(ref, task, authHeader);
    } else {
      if (ic.getUri() != null && !ic.getUri().isEmpty()) {
        writeContentFromUri(ref, ic.getUri(), name, authHeader);
      }
      ensureInitialVersion(ref);
    }
    setAuditPropertiesFromNode(ref, ic);
    result.addNodes(1);
  }

  // === Newsgroups Import (batched) ===

  private void importNewsgroupsBatched(
    NodeRef parentRef,
    Newsgroups newsgroups,
    ImportResult result
  ) {
    AtomicInteger count = new AtomicInteger(0);
    for (Forum forum : newsgroups.getFora()) {
      try {
        runInNewTransaction(() -> {
          policyBehaviourFilter.disableBehaviour();
          createForumTree(
            parentRef,
            forum,
            count,
            CircabcModel.ASPECT_NEWSGROUP
          );
          return null;
        });
      } catch (Exception e) {
        result.addError("Forum: " + e.getMessage());
        logger.warn("Forum import failed", e);
      }
    }
    result.addNodes(count.get());
  }

  /** Recursively create a forum, its nested sub-fora, and its topics/messages. */
  private void createForumTree(
    NodeRef parentRef,
    Forum forum,
    AtomicInteger count,
    QName serviceAspect
  ) {
    String fname =
      forum.getName() != null ? forum.getName().getValue().toString() : "forum";
    NodeRef forumRef = createNode(parentRef, fname, ForumModel.TYPE_FORUM);
    nodeService.addAspect(forumRef, serviceAspect, null);
    setAuditPropertiesFromNode(forumRef, forum);
    count.incrementAndGet();
    // Nested sub-fora
    if (forum.getFora() != null) {
      for (Forum sub : forum.getFora()) {
        createForumTree(forumRef, sub, count, serviceAspect);
      }
    }
    // Topics
    if (forum.getTopics() != null) {
      for (Topic topic : forum.getTopics()) {
        createTopicTree(forumRef, topic, count, serviceAspect);
      }
    }
  }

  /** Create a topic and its messages under the given parent (forum). */
  private void createTopicTree(
    NodeRef parentRef,
    Topic topic,
    AtomicInteger count,
    QName serviceAspect
  ) {
    String tname =
      topic.getName() != null ? topic.getName().getValue().toString() : "topic";
    NodeRef topicRef = createNode(parentRef, tname, ForumModel.TYPE_TOPIC);
    // getTopicReplies requires the topic to carry the owning service aspect
    // (circaNewsGroup or circaLibrary); without it the replies API returns
    // total=null and the response template fails.
    nodeService.addAspect(topicRef, serviceAspect, null);
    setAuditPropertiesFromNode(topicRef, topic);
    count.incrementAndGet();
    if (topic.getMessages() != null) {
      for (Message msg : topic.getMessages()) {
        createMessageTree(topicRef, msg, count, serviceAspect);
      }
    }
  }

  /** Create a post and its (threaded) replies under the given parent (topic). */
  private void createMessageTree(
    NodeRef topicRef,
    Message msg,
    AtomicInteger count,
    QName serviceAspect
  ) {
    NodeRef msgRef = createNode(
      topicRef,
      UUID.randomUUID().toString(),
      ForumModel.TYPE_POST
    );
    nodeService.addAspect(msgRef, serviceAspect, null);
    setAuditPropertiesFromNode(msgRef, msg);
    if (msg.getContent() != null && !msg.getContent().isEmpty()) {
      ContentWriter w = contentService.getWriter(
        msgRef,
        ContentModel.PROP_CONTENT,
        true
      );
      w.setMimetype("text/plain");
      w.putContent(msg.getContent());
    }
    count.incrementAndGet();
    // Threaded replies are stored as posts under the same topic
    if (msg.getReplies() != null) {
      for (Message reply : msg.getReplies()) {
        createMessageTree(topicRef, reply, count, serviceAspect);
      }
    }
  }

  /**
   * Create a document discussion: add the fm:discussable aspect to the content
   * node and build the discussion forum (with topics and posts) under it.
   */
  private void createDiscussion(NodeRef contentRef, Discussions disc) {
    if (
      disc == null || disc.getTopics() == null || disc.getTopics().isEmpty()
    ) {
      return;
    }
    try {
      if (!nodeService.hasAspect(contentRef, ForumModel.ASPECT_DISCUSSABLE)) {
        nodeService.addAspect(contentRef, ForumModel.ASPECT_DISCUSSABLE, null);
      }
      Map<QName, Serializable> forumProps = new HashMap<>();
      forumProps.put(ContentModel.PROP_NAME, "Discussion");
      NodeRef forumRef = nodeService
        .createNode(
          contentRef,
          ForumModel.ASSOC_DISCUSSION,
          ForumModel.ASSOC_DISCUSSION,
          ForumModel.TYPE_FORUM,
          forumProps
        )
        .getChildRef();
      nodeService.addAspect(forumRef, CircabcModel.ASPECT_LIBRARY, null);
      setAuditPropertiesFromNode(forumRef, disc);
      AtomicInteger ignore = new AtomicInteger(0);
      for (Topic topic : disc.getTopics()) {
        createTopicTree(forumRef, topic, ignore, CircabcModel.ASPECT_LIBRARY);
      }
    } catch (Exception e) {
      logger.warn(
        "Failed to create discussion for node " +
          contentRef +
          ": " +
          e.getMessage()
      );
    }
  }

  // === Events Import (meetings + events) ===

  private void importEvents(String igId, Events events, ImportResult result) {
    if (events == null) {
      return;
    }
    NodeRef eventRoot;
    try {
      eventRoot = eventService.getIGsEventRoot(igId);
    } catch (Exception e) {
      result.addError("Events root: " + e.getMessage());
      logger.warn("Could not resolve events root for IG " + igId, e);
      return;
    }
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    try {
      MLPropertyInterceptor.setMLAware(false);
      // Meetings
      if (events.getMeetings() != null) {
        for (Meeting m : events.getMeetings()) {
          importMeeting(eventRoot, m, result);
        }
      }
      // Events
      if (events.getEvents() != null) {
        for (Event ev : events.getEvents()) {
          importEvent(eventRoot, ev, result);
        }
      }
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }

  /** Imports a single meeting appointment (isolated so one failure doesn't abort the rest). */
  private void importMeeting(
    NodeRef eventRoot,
    Meeting m,
    ImportResult result
  ) {
    try {
      io.swagger.model.Meeting sm = new MeetingImpl();
      mapCommonAppointment(m, sm);
      sm.setAvailability(
        m.getAvailability() != null
          ? io.swagger.model.MeetingAvailability.valueOf(
              m.getAvailability().value()
            )
          : io.swagger.model.MeetingAvailability.Private
      );
      sm.setOrganization(m.getOrganization());
      sm.setAgenda(m.getAgenda());
      if (m.getType() != null) {
        sm.setMeetingTypeString(m.getType().value());
      }
      // librarySection references a source path and is not resolvable here.
      eventService.createMeeting(eventRoot, sm);
      result.addNodes(1);
    } catch (Exception e) {
      result.addError("Meeting: " + e.getMessage());
      logger.warn("Failed to import meeting", e);
    }
  }

  /** Imports a single event appointment (isolated so one failure doesn't abort the rest). */
  private void importEvent(NodeRef eventRoot, Event ev, ImportResult result) {
    try {
      io.swagger.model.Event se = new EventImpl();
      mapCommonAppointment(ev, se);
      se.setEventType(
        ev.getType() != null
          ? io.swagger.model.EventType.valueOf(ev.getType().value())
          : io.swagger.model.EventType.Task
      );
      se.setPriority(
        ev.getPriority() != null
          ? io.swagger.model.EventPriority.valueOf(ev.getPriority().value())
          : io.swagger.model.EventPriority.Low
      );
      eventService.createEvent(eventRoot, se);
      result.addNodes(1);
    } catch (Exception e) {
      result.addError("Event: " + e.getMessage());
      logger.warn("Failed to import event", e);
    }
  }

  /** Map the shared appointment fields from the JAXB entity onto the swagger model. */
  private void mapCommonAppointment(
    Appointment appt,
    io.swagger.model.Appointment out
  ) {
    out.setTitle(appt.getAppointmentTitle());
    out.setLanguage(
      appt.getLanguage() != null ? appt.getLanguage().getLanguage() : "en"
    );
    out.setEventAbstract(appt.getAbstract());
    out.setLocation(appt.getLocation());
    if (appt.getTimeZoneId() != null) {
      out.setTimeZoneId(appt.getTimeZoneId().value());
    }
    if (appt.getStartDate() != null) {
      out.setStartDateAsDate(appt.getStartDate());
      out.setDateAsDate(appt.getStartDate());
    }
    if (appt.getStartTime() != null) {
      out.setStartTimeAsDate(appt.getStartTime());
    }
    if (appt.getEndTime() != null) {
      out.setEndTimeAsDate(appt.getEndTime());
    }
    out.setEnableNotification(false);
    out.setUseBCC(false);
    // Occurrence rate: honour the recurrence choice from the export
    // (singleDate | timesOccurence | everyTimesOccurence).
    out.setOccurenceRate(mapOccurenceRate(appt));
    // All appointments in the export use an open audience.
    out.setAudienceStatus(io.swagger.model.AudienceStatus.Open);
    ContactInformation c = appt.getContact();
    if (c != null) {
      out.setName(c.getName());
      out.setEmail(c.getEmail());
      out.setPhone(c.getPhone());
      out.setUrl(c.getUrl());
    }
  }

  /**
   * Map the recurrence choice of an exported appointment onto the swagger
   * {@link io.swagger.model.OccurenceRate}. The export schema
   * (appointmentProperties) allows exactly one of:
   * <ul>
   *   <li>{@code singleDate} - a one-time appointment (OnlyOnce);</li>
   *   <li>{@code timesOccurence} - repeat a fixed number of times at a given
   *       rate (Daily, Weekly, Yearly, ...);</li>
   *   <li>{@code everyTimesOccurence} - repeat every N days/weeks/months for a
   *       fixed number of times.</li>
   * </ul>
   * Falls back to a single (OnlyOnce) occurrence when no recurrence
   * information is present. This lets recurring appointments be re-created with
   * all their occurrences instead of collapsing to a single one.
   */
  private io.swagger.model.OccurenceRate mapOccurenceRate(Appointment appt) {
    eu.cec.digit.circabc.migration.entities.generated.properties.TimesOccurence times =
      appt.getTimesOccurence();
    if (
      times != null && times.getType() != null && times.getForTimes() != null
    ) {
      return new io.swagger.model.OccurenceRate(
        io.swagger.model.MainOccurence.Times,
        io.swagger.model.TimesOccurence.valueOf(times.getType().value()),
        times.getForTimes()
      );
    }

    eu.cec.digit.circabc.migration.entities.generated.properties.EveryTimesOccurence every =
      appt.getEveryTimesOccurence();
    if (
      every != null &&
      every.getType() != null &&
      every.getEvery() != null &&
      every.getForTimes() != null
    ) {
      return new io.swagger.model.OccurenceRate(
        io.swagger.model.MainOccurence.EveryTimes,
        io.swagger.model.EveryTimesOccurence.valueOf(every.getType().value()),
        every.getEvery(),
        every.getForTimes()
      );
    }

    // singleDate (or nothing) -> one-time appointment
    return new io.swagger.model.OccurenceRate(
      io.swagger.model.MainOccurence.OnlyOnce
    );
  }

  // === Persons Import ===

  private DynamicPropertyType resolveDynamicPropertyType(
    DynamicPropertyDefinition def
  ) {
    try {
      return def.getType() != null
        ? DynamicPropertyType.valueOf(def.getType().value())
        : DynamicPropertyType.TEXT_FIELD;
    } catch (IllegalArgumentException ex) {
      return DynamicPropertyType.TEXT_FIELD;
    }
  }

  private void importPersons(Persons persons, ImportResult result) {
    if (persons == null || persons.getPersons() == null) return;
    for (Person person : persons.getPersons()) {
      importPerson(person, result);
    }
  }

  private void importPerson(Person person, ImportResult result) {
    String userId =
      person.getUserId() != null
        ? person.getUserId().getValue().toString()
        : null;
    if (userId == null) return;
    if (personService.personExists(userId)) {
      if (!circabcService.isUserExists(userId)) circabcService.addUser(userId);
      return;
    }
    try {
      CircabcUserDataBean user = new CircabcUserDataBean();
      user.setUserName(userId);
      if (person.getFirstName() != null) {
        user.setFirstName(person.getFirstName().getValue().toString());
      }
      if (person.getLastName() != null) {
        user.setLastName(person.getLastName().getValue().toString());
      }
      if (person.getEmail() != null) {
        user.setEmail(person.getEmail().getValue().toString());
      }
      // Required CircaBC user fields with safe defaults for imported users
      user.setCompanyId("");
      user.setURL("");
      user.setVisibility(Boolean.FALSE);
      user.setGlobalNotification(Boolean.TRUE);
      // createUser creates both the cm:person node AND the usr:user
      // authentication entry (with a random password), so the account
      // is fully functional and setAuthenticationEnabled() won't throw
      // "User not found" on first EU Login.
      userService.createUser(user, true);
      circabcService.addUser(userId);
      result.addNodes(1);
    } catch (Exception e) {
      result.addError("Person/" + userId + ": " + e.getMessage());
      logger.warn("Failed to create person " + userId, e);
    }
  }

  // === Node Creation Helpers (properties set at creation time) ===

  private NodeRef createFolderWithProps(
    NodeRef parent,
    String name,
    String title,
    String desc
  ) {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, name);
    NodeRef ref = nodeService
      .createNode(
        parent,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          QName.createValidLocalName(name)
        ),
        ContentModel.TYPE_FOLDER,
        props
      )
      .getChildRef();
    addCircabcAspects(ref, title, desc);
    return ref;
  }

  private NodeRef createContentWithProps(
    NodeRef parent,
    String name,
    String title,
    String desc
  ) {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, name);
    NodeRef ref = nodeService
      .createNode(
        parent,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          QName.createValidLocalName(name)
        ),
        ContentModel.TYPE_CONTENT,
        props
      )
      .getChildRef();
    addCircabcAspects(ref, title, desc);
    return ref;
  }

  private void addCircabcAspects(NodeRef ref, String title, String desc) {
    QName circaLibrary = QName.createQName(
      "http://www.cc.cec/circabc/model/content/1.0",
      "circaLibrary"
    );
    if (!nodeService.hasAspect(ref, circaLibrary)) {
      nodeService.addAspect(ref, circaLibrary, null);
    }
    QName circaDocument = QName.createQName(
      "http://www.cc.cec/circabc/model/document/1.0",
      "circadocument"
    );
    if (!nodeService.hasAspect(ref, circaDocument)) {
      nodeService.addAspect(ref, circaDocument, null);
    }
    // cd:circadocument declares cd:bproperties and cd:cproperties as mandatory
    // aspects. Add them so the mandatory-aspect integrity contract is satisfied
    // whenever behaviours/integrity are enforced (e.g. when a node is later made
    // a multilingual translation). Both have only optional properties.
    if (!nodeService.hasAspect(ref, DocumentModel.ASPECT_BPROPERTIES)) {
      nodeService.addAspect(ref, DocumentModel.ASPECT_BPROPERTIES, null);
    }
    if (!nodeService.hasAspect(ref, DocumentModel.ASPECT_CPROPERTIES)) {
      nodeService.addAspect(ref, DocumentModel.ASPECT_CPROPERTIES, null);
    }
    QName circaContentNotify = QName.createQName(
      "http://www.cc.cec/circabc/model/content/1.0",
      "circaContentNotify"
    );
    if (!nodeService.hasAspect(ref, circaContentNotify)) {
      nodeService.addAspect(ref, circaContentNotify, null);
    }
    if (!nodeService.hasAspect(ref, ContentModel.ASPECT_TITLED)) {
      Map<QName, Serializable> titledProps = new HashMap<>();
      if (title != null) titledProps.put(ContentModel.PROP_TITLE, title);
      if (desc != null) titledProps.put(ContentModel.PROP_DESCRIPTION, desc);
      nodeService.addAspect(
        ref,
        ContentModel.ASPECT_TITLED,
        titledProps.isEmpty() ? null : titledProps
      );
    }
    if (!nodeService.hasAspect(ref, ContentModel.ASPECT_OWNABLE)) {
      nodeService.addAspect(ref, ContentModel.ASPECT_OWNABLE, null);
    }
  }

  private void setAuditProperties(NodeRef ref, ContentTask task) {
    // Set all auditable properties in ONE call so they go through
    // AbstractNodeDAOImpl.setNodePropertiesImpl atomically. Individual setProperty
    // calls cause multiple updateNodeImpl passes where cached old values can overwrite.
    policyBehaviourFilter.disableBehaviour(ref, ContentModel.ASPECT_AUDITABLE);
    // Apply the migrated aspect BEFORE writing the audit properties so that any
    // cm:modified/cm:modifier stamping caused by addAspect is overwritten by the
    // original exported audit values written last.
    if (task.originalNodeRef != null) {
      Map<QName, Serializable> migratedProps = HashMap.newHashMap(1);
      migratedProps.put(
        CircabcModel.PROP_ORIGINAL_NODE_REF,
        task.originalNodeRef
      );
      nodeService.addAspect(ref, CircabcModel.ASPECT_MIGRATED, migratedProps);
    }
    Map<QName, Serializable> auditProps = new HashMap<>();
    if (task.created != null) auditProps.put(
      ContentModel.PROP_CREATED,
      task.created
    );
    if (task.creator != null) auditProps.put(
      ContentModel.PROP_CREATOR,
      task.creator
    );
    if (task.modified != null) auditProps.put(
      ContentModel.PROP_MODIFIED,
      task.modified
    );
    if (task.modifier != null) auditProps.put(
      ContentModel.PROP_MODIFIER,
      task.modifier
    );
    if (!auditProps.isEmpty()) {
      nodeService.addProperties(ref, auditProps);
    }
  }

  private void setAuditPropertiesFromNode(NodeRef ref, Node xmlNode) {
    policyBehaviourFilter.disableBehaviour(ref, ContentModel.ASPECT_AUDITABLE);
    // Apply the migrated aspect BEFORE writing the audit properties so audit
    // (created/creator/modified/modifier) is the last write and is preserved.
    applyMigratedAspect(ref, xmlNode);
    Map<QName, Serializable> auditProps = new HashMap<>();
    if (xmlNode.getCreated() != null) auditProps.put(
      ContentModel.PROP_CREATED,
      xmlNode.getCreated().getValue()
    );
    if (xmlNode.getCreator() != null) auditProps.put(
      ContentModel.PROP_CREATOR,
      xmlNode.getCreator().getValue().toString()
    );
    if (xmlNode.getModified() != null) auditProps.put(
      ContentModel.PROP_MODIFIED,
      xmlNode.getModified().getValue()
    );
    if (xmlNode.getModifier() != null) auditProps.put(
      ContentModel.PROP_MODIFIER,
      xmlNode.getModifier().getValue().toString()
    );
    if (!auditProps.isEmpty()) {
      nodeService.addProperties(ref, auditProps);
    }
  }

  /**
   * If the exported node carries an original (source) NodeRef, store it on the
   * created node via the {@code ci:migrated} aspect ({@code ci:originalNodeRef}
   * property) so the origin of the migrated node is preserved and old (source)
   * deep links can be resolved to it. No-op when the export carries no ref.
   */
  private void applyMigratedAspect(NodeRef ref, Node xmlNode) {
    if (
      ref == null || xmlNode == null || xmlNode.getOriginalNodeRef() == null
    ) {
      return;
    }
    Map<QName, Serializable> migratedProps = HashMap.newHashMap(1);
    migratedProps.put(
      CircabcModel.PROP_ORIGINAL_NODE_REF,
      xmlNode.getOriginalNodeRef().toString()
    );
    nodeService.addAspect(ref, CircabcModel.ASPECT_MIGRATED, migratedProps);
  }

  private NodeRef createNode(NodeRef parent, String name, QName type) {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, name);
    return nodeService
      .createNode(
        parent,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          QName.createValidLocalName(name)
        ),
        type,
        props
      )
      .getChildRef();
  }

  // === Mime Type Guessing ===

  private static final Map<String, String> MIME_BY_EXTENSION =
    createMimeByExtension();

  private static Map<String, String> createMimeByExtension() {
    Map<String, String> m = new java.util.LinkedHashMap<>();
    m.put(".pdf", "application/pdf");
    m.put(".doc", "application/msword");
    m.put(
      ".docx",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    m.put(".xls", "application/vnd.ms-excel");
    m.put(
      ".xlsx",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    m.put(".ppt", "application/vnd.ms-powerpoint");
    m.put(
      ".pptx",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );
    m.put(".txt", "text/plain");
    m.put(".html", "text/html");
    m.put(".htm", "text/html");
    m.put(".xml", "application/xml");
    m.put(".zip", "application/zip");
    m.put(".png", "image/png");
    m.put(".jpg", "image/jpeg");
    m.put(".jpeg", "image/jpeg");
    m.put(".gif", "image/gif");
    m.put(".csv", "text/csv");
    return m;
  }

  private String guessMimeType(String fileName) {
    if (fileName == null) return "application/octet-stream";
    String lower = fileName.toLowerCase();
    for (Map.Entry<String, String> entry : MIME_BY_EXTENSION.entrySet()) {
      if (lower.endsWith(entry.getKey())) {
        return entry.getValue();
      }
    }
    return "application/octet-stream";
  }
}
