package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.EventModel;
import io.swagger.model.db.ActivityCountDAO;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link IgStatisticsService} that computes and exposes
 * usage statistics for a CIRCABC Interest Group (IG).
 *
 * <p>The service performs two distinct kinds of work:</p>
 * <ul>
 *   <li><b>Aggregated counts and sizes</b> ({@link #buildStatsData(NodeRef)}): a single
 *       recursive traversal of the IG tree tallies the number and total content size of
 *       library documents/folders, information documents/folders, meetings, events, forums,
 *       topics, posts, versions and customization/hidden content, together with the maximum
 *       folder nesting depth. Results are persisted in the statistics table through
 *       {@link IgStatisticsDaoService} and are only recomputed when they are stale relative to
 *       the last update recorded on the IG.</li>
 *   <li><b>Structure representations</b> ({@link #getLibraryStructure(NodeRef)},
 *       {@link #getInformationStructure(NodeRef)}, {@link #getNewsgroupsStructure(NodeRef)}):
 *       build a folder tree for the corresponding IG service.</li>
 * </ul>
 *
 * <p>The recursive counting pass is executed as the system user and guarded by a distributed
 * lock (via {@link LockService}) so that only one computation runs per IG at a time. The mutable
 * counter fields hold intermediate results for the duration of a single {@code buildStatsData}
 * invocation and are reset at the start of each recomputation.</p>
 */
public class IgStatisticsServiceImpl implements IgStatisticsService {

  /** Logger used to report failures occurring during statistics computation. */
  static final Log logger = LogFactory.getLog(IgStatisticsServiceImpl.class);

  @Autowired
  private NodeService nodeService;

  @Autowired
  private VersionService versionService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private IgStatisticsDaoService igStatisticsDaoService;

  @Autowired
  private LogService logService;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private LockService circabcLockService;

  /** Running count of documents found in the IG Library service during a computation. */
  private int numberOfLibraryDocuments = 0;
  /** Running count of folders found in the IG Library service during a computation. */
  private int numberOfLibraryFolders = 0;
  /** Cumulative content size (in bytes) of Library documents during a computation. */
  private long sizeOfLibraryDocuments = 0;
  /** Running count of documents found in the IG Information service during a computation. */
  private int numberOfInformationDocuments = 0;
  /** Running count of folders found in the IG Information service during a computation. */
  private int numberOfInformationFolders = 0;
  /** Cumulative content size (in bytes) of Information documents during a computation. */
  private long sizeOfInformationDocuments = 0;
  /** Running count of meeting (event definition) nodes during a computation. */
  private int numberOfMeetings = 0;
  /** Running count of event nodes during a computation. */
  private int numberOfEvents = 0;
  /** Running count of forum nodes during a computation. */
  private int numberOfForums = 0;
  /** Running count of topic nodes during a computation. */
  private int numberOfTopics = 0;
  /** Running count of post nodes during a computation. */
  private int numberOfPosts = 0;
  /** Maximum folder/forum nesting depth encountered during a computation. */
  private int maxLevel = 0;
  /** Cumulative number of document versions encountered during a computation. */
  private int numberOfVersions = 0;
  /** Cumulative content size (in bytes) of all document versions during a computation. */
  private long sizeOfVersions = 0;
  /** Running count of customization and hidden-attachment content nodes during a computation. */
  private int numberOfCustomizationAndHiddenContent = 0;
  /** Cumulative content size (in bytes) of customization and hidden content during a computation. */
  private long sizeOfCustomizationAndHiddenContent = 0;

  /**
   * Returns the human-readable title of the given Interest Group.
   *
   * @param igRoot the node reference of the IG root node
   * @return the value of the {@code cm:title} property of the IG root, as a string
   */
  public String getIGTitle(NodeRef igRoot) {
    return nodeService.getProperty(igRoot, ContentModel.PROP_TITLE).toString();
  }

  /**
   * Returns the creation date of the given Interest Group.
   *
   * @param igRoot the node reference of the IG root node
   * @return the {@code cm:created} property of the IG root node
   */
  private Serializable getIGCreationDate(NodeRef igRoot) {
    return nodeService.getProperty(igRoot, ContentModel.PROP_CREATED);
  }

  /**
   * Determines whether the persisted statistics for an IG are still up to date and therefore
   * do not need to be recomputed.
   *
   * <p>Statistics are considered up to date when no update has been logged on the IG, or when
   * the last logged update predates the request date stored on the given parameter.</p>
   *
   * @param igStatisticsParameter the currently persisted statistics parameter for the IG, or
   *                              {@code null} if none exist yet
   * @return {@code true} if the statistics are up to date and can be reused; {@code false} if
   *         they are stale (or if {@code igStatisticsParameter} is {@code null})
   */
  private boolean isStatisticsUptoDate(
    IgStatisticsParameter igStatisticsParameter
  ) {
    boolean result = false;
    if (igStatisticsParameter != null) {
      //we will calculate again the statistics and update the table only if the statistics are not up-to-date
      //When date of last calculation of statistics is before the last update date on the IG
      Date lastUpdate = logService.getLastUpdateOnInterestGroup(
        igStatisticsParameter.getIgId()
      );
      //if last upfate is null, we consider that the content of CBC_GROUP_STATISTICS table is up-to-date
      result =
        (lastUpdate == null) ||
        (lastUpdate.before(igStatisticsParameter.getRequestDate()));
    }
    return result;
  }

  /**
   * Builds (and persists) the aggregated statistics for the given Interest Group.
   *
   * <p>If statistics already exist and are up to date, they are returned unchanged. Otherwise a
   * recursive traversal of the IG tree is performed (as the system user, under a per-IG lock) to
   * recompute all counts and sizes, and the result is either inserted or updated in the
   * statistics table.</p>
   *
   * @param igRoot the node reference of the IG root node
   * @return the up-to-date {@link IgStatisticsParameter} for the IG, or {@code null} if the IG
   *         root node has no database id
   */
  public IgStatisticsParameter buildStatsData(final NodeRef igRoot) {
    IgStatisticsParameter igStatisticsParameter = null;
    Long igDbId = (Long) nodeService.getProperty(
      igRoot,
      ContentModel.PROP_NODE_DBID
    );
    //igdbId should normally never be null but it is best to check
    if (igDbId != null) {
      igStatisticsParameter = igStatisticsDaoService.getIgStatisticsById(
        igDbId
      );
      if (!isStatisticsUptoDate(igStatisticsParameter)) {
        //statistics are not up-to-date and we need to calculate them again and update or insert the result in the database
        //initialize fields
        numberOfLibraryDocuments = 0;
        numberOfLibraryFolders = 0;
        sizeOfLibraryDocuments = 0;
        numberOfInformationDocuments = 0;
        numberOfInformationFolders = 0;
        sizeOfInformationDocuments = 0;
        numberOfMeetings = 0;
        numberOfEvents = 0;
        numberOfForums = 0;
        numberOfTopics = 0;
        numberOfPosts = 0;
        numberOfVersions = 0;
        int numberOfUsers = circabcService.countMembersInIg(igRoot.getId());
        sizeOfVersions = 0;
        numberOfCustomizationAndHiddenContent = 0;
        sizeOfCustomizationAndHiddenContent = 0;
        maxLevel = 0;

        //calculate the statistics
        final RunAsWork<Boolean> work = new RunAsWork<Boolean>() {
          @Override
          public Boolean doWork() throws Exception {
            String lockName = "IG_SS_" + igRoot.getId();
            boolean isLocked = circabcLockService.tryLock(lockName);
            if (!isLocked) {
              return false;
            }
            try {
              countNodes(igRoot, Place.IG_ROOT, -1);
              return true;
            } catch (final Exception e) {
              logger.error(
                "Exception while calculating the IG " + "Statistics counts.",
                e
              );
              return false;
            } finally {
              circabcLockService.unlock(lockName);
            }
          }
        };

        AuthenticationUtil.runAs(work, AuthenticationUtil.SYSTEM_USER_NAME);

        //check if we need to update an existing record or insert a new one if no statistics already exist for this IG
        boolean update = igStatisticsParameter != null;

        //insert new record or update exisitng one in IGStatistics table
        igStatisticsParameter = new IgStatisticsParameter();
        igStatisticsParameter.setIgId(igDbId);
        igStatisticsParameter.setCreationDate((Date) getIGCreationDate(igRoot));
        igStatisticsParameter.setLibraryDocumentCount(numberOfLibraryDocuments);
        igStatisticsParameter.setEventCount(numberOfEvents);
        igStatisticsParameter.setLibraryFolderCount(numberOfLibraryFolders);
        igStatisticsParameter.setForumCount(numberOfForums);
        igStatisticsParameter.setInformationDocumentCount(
          numberOfInformationDocuments
        );
        igStatisticsParameter.setInformationFolderCount(
          numberOfInformationFolders
        );
        igStatisticsParameter.setInformationSize(sizeOfInformationDocuments);
        igStatisticsParameter.setLibrarySize(sizeOfLibraryDocuments);
        igStatisticsParameter.setMeetingCount(numberOfMeetings);
        igStatisticsParameter.setNbUsers(numberOfUsers);
        igStatisticsParameter.setPostCount(numberOfPosts);
        igStatisticsParameter.setRequestDate(new Date());
        igStatisticsParameter.setTopicCount(numberOfTopics);
        igStatisticsParameter.setTotalSize(
          sizeOfInformationDocuments +
            sizeOfVersions +
            sizeOfCustomizationAndHiddenContent
        );
        igStatisticsParameter.setVersionCount(numberOfVersions);
        igStatisticsParameter.setVersionSize(sizeOfVersions);
        igStatisticsParameter.setMaxLevel(maxLevel);
        igStatisticsParameter.setCustomizationAndHiddenContentCount(
          numberOfCustomizationAndHiddenContent
        );
        igStatisticsParameter.setCustomizationAndHiddenContentSize(
          sizeOfCustomizationAndHiddenContent
        );

        if (update) {
          //update
          igStatisticsDaoService.updateIGStatistics(igStatisticsParameter);
        } else {
          igStatisticsDaoService.insertIGStatistics(igStatisticsParameter);
        }
      }
    }
    return igStatisticsParameter;
  }

  /**
   * Does one recursive pass to count all relevant elements at once.
   *
   * <p>Counts the current node according to its type and location, updates the current
   * {@link Place} context, and recurses into the children of non-content nodes.</p>
   *
   * @param nodeRef the node currently being visited (ignored if {@code null} or non-existent)
   * @param place   the service context ({@link Place}) the node belongs to
   * @param level   the current nesting depth, used to track {@link #maxLevel}
   */
  private void countNodes(NodeRef nodeRef, Place place, int level) {
    if (nodeRef == null || !nodeService.exists(nodeRef)) return;
    QName type = nodeService.getType(nodeRef);
    countByType(nodeRef, type, place);
    place = updatePlace(nodeRef, place);
    if (!ContentModel.TYPE_CONTENT.equals(type)) {
      recurseChildren(nodeRef, place, level);
    }
  }

  /**
   * Increments the appropriate counter(s) for a single node based on its content model type.
   *
   * @param nodeRef the node being counted
   * @param type    the content model type ({@link QName}) of the node
   * @param place   the service context ({@link Place}) the node belongs to
   */
  private void countByType(NodeRef nodeRef, QName type, Place place) {
    if (ContentModel.TYPE_CONTENT.equals(type)) {
      countContentNode(nodeRef, type, place);
    } else if (ContentModel.TYPE_FOLDER.equals(type)) {
      countFolderNode(nodeRef, place);
    } else if (EventModel.TYPE_EVENT_MEETING_DEFINITION.equals(type)) {
      numberOfMeetings++;
    } else if (EventModel.TYPE_EVENT.equals(type)) {
      numberOfEvents++;
    } else if (ForumModel.TYPE_FORUM.equals(type)) {
      numberOfForums++;
    } else if (ForumModel.TYPE_TOPIC.equals(type)) {
      numberOfTopics++;
    } else if (ForumModel.TYPE_POST.equals(type)) {
      numberOfPosts++;
    }
  }

  /**
   * Counts a content (document) node and adds its content size to the appropriate total.
   *
   * <p>Customization and hidden-attachment content is tallied separately; regular documents are
   * attributed to the Library or Information totals depending on {@code place}, and their version
   * history is also counted via {@link #countVersions(NodeRef)}.</p>
   *
   * @param nodeRef the content node being counted
   * @param type    the content model type ({@link QName}) of the node
   * @param place   the service context ({@link Place}) the node belongs to
   */
  private void countContentNode(NodeRef nodeRef, QName type, Place place) {
    QName contentPropertyQName = ContentModel.PROP_CONTENT;
    if (CircabcModel.TYPE_CUSTOMIZATION_CONTENT.equals(type)) {
      contentPropertyQName = CircabcModel.PROP_CONTENT;
    } else if (DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(type)) {
      contentPropertyQName = DocumentModel.PROP_CONTENT;
    } else {
      countVersions(nodeRef);
    }
    if (
      CircabcModel.TYPE_CUSTOMIZATION_CONTENT.equals(type) ||
      DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(type)
    ) {
      numberOfCustomizationAndHiddenContent++;
      sizeOfCustomizationAndHiddenContent += getContentSize(
        nodeRef,
        contentPropertyQName
      );
    } else if (place == Place.Library) {
      numberOfLibraryDocuments++;
      sizeOfLibraryDocuments += getContentSize(nodeRef, contentPropertyQName);
    } else if (place == Place.Information) {
      numberOfInformationDocuments++;
      sizeOfInformationDocuments += getContentSize(
        nodeRef,
        contentPropertyQName
      );
    }
  }

  /**
   * Adds the number and cumulative content size of all versions of the given node to the
   * version totals. Any failure while retrieving the version history is logged and ignored.
   *
   * @param nodeRef the node whose version history is inspected
   */
  private void countVersions(NodeRef nodeRef) {
    try {
      VersionHistory history = versionService.getVersionHistory(nodeRef);
      if (history != null) {
        Collection<Version> versions = history.getAllVersions();
        numberOfVersions += versions.size();
        for (Version version : versions) {
          sizeOfVersions += getContentSize(
            version.getFrozenStateNodeRef(),
            ContentModel.PROP_CONTENT
          );
        }
      }
    } catch (Exception e) {
      logger.error(
        "Exception while retrieving the version history during the calculation the IG Statistics counts. NodeRef: " +
          nodeRef,
        e
      );
    }
  }

  /**
   * Increments the Library or Information folder counter for the given folder node, provided the
   * folder carries the corresponding CIRCABC aspect for its {@link Place}.
   *
   * @param nodeRef the folder node being counted
   * @param place   the service context ({@link Place}) the folder belongs to
   */
  private void countFolderNode(NodeRef nodeRef, Place place) {
    if (
      place == Place.Library &&
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ) {
      numberOfLibraryFolders++;
    } else if (
      place == Place.Information &&
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ) {
      numberOfInformationFolders++;
    }
  }

  /**
   * Derives the {@link Place} context that applies to a node's subtree, switching to
   * {@link Place#Library} or {@link Place#Information} when the node is the corresponding
   * service root folder (matched by name).
   *
   * @param nodeRef the node whose name is inspected
   * @param place   the current place context
   * @return the (possibly updated) place context to use for the node and its children
   */
  private Place updatePlace(NodeRef nodeRef, Place place) {
    String name = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_NAME
    );
    if (Place.Library.toString().equals(name)) return Place.Library;
    if (Place.Information.toString().equals(name)) return Place.Information;
    return place;
  }

  /**
   * Recursively visits the children of a node, counting each child and increasing the depth for
   * folder and forum children. Updates {@link #maxLevel} with the deepest level reached.
   *
   * @param nodeRef the node whose children are traversed
   * @param place   the service context ({@link Place}) the children belong to
   * @param level   the nesting depth of {@code nodeRef}
   */
  private void recurseChildren(NodeRef nodeRef, Place place, int level) {
    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
    for (ChildAssociationRef childRef : children) {
      NodeRef childNodeRef = childRef.getChildRef();
      if (childNodeRef != null && nodeService.exists(childNodeRef)) {
        QName childType = nodeService.getType(childNodeRef);
        int nextLevel = level;
        if (
          ContentModel.TYPE_FOLDER.equals(childType) ||
          ForumModel.TYPE_FORUM.equals(childType)
        ) {
          nextLevel++;
        }
        countNodes(childNodeRef, place, nextLevel);
      }
    }
    if (level > maxLevel) {
      maxLevel = level;
    }
  }

  /**
   * Returns the size (in bytes) of the content stored under the given property of a node.
   *
   * @param nodeRef             the node holding the content
   * @param contentPropertyQName the property ({@link QName}) that carries the {@link ContentData}
   * @return the content size in bytes, or {@code 0} if the property has no content
   */
  private long getContentSize(NodeRef nodeRef, QName contentPropertyQName) {
    ContentData contentData = (ContentData) nodeService.getProperty(
      nodeRef,
      contentPropertyQName
    );
    if (contentData != null) {
      return contentData.getSize();
    }
    return 0;
  }

  /**
   * Walk recursively into the library service and build a tree representation of all folders.
   *
   * @param igRoot the node reference of the IG root node
   * @return a {@link ServiceTreeRepresentation} rooted at the IG Library folder
   */
  public ServiceTreeRepresentation getLibraryStructure(NodeRef igRoot) {
    ServiceTreeRepresentation libraryTree = new ServiceTreeRepresentation(
      "library"
    );
    Child libraryRoot = new Child();
    libraryRoot.setName("Library");

    libraryRoot.setNode(getLibraryNodeRefFromLucene(igRoot));

    libraryTree.setChild(libraryRoot);

    recursiveWalkAndBuildTree(libraryRoot.getNode(), libraryRoot);

    return libraryTree;
  }

  /**
   * Walk recursively into the information service and build a tree representation of all folders.
   *
   * @param igRoot the node reference of the IG root node
   * @return a {@link ServiceTreeRepresentation} rooted at the IG Information folder
   */
  public ServiceTreeRepresentation getInformationStructure(NodeRef igRoot) {
    ServiceTreeRepresentation informationTree = new ServiceTreeRepresentation(
      "library"
    );
    Child informationRoot = new Child();
    informationRoot.setName("Information");

    informationRoot.setNode(getInformationNodeRefFromLucene(igRoot));

    informationTree.setChild(informationRoot);

    recursiveWalkAndBuildTree(informationRoot.getNode(), informationRoot);

    return informationTree;
  }

  /**
   * Walk recursively into the newsgroup service and build a tree representation of all folders.
   *
   * @param igRoot the node reference of the IG root node
   * @return a {@link ServiceTreeRepresentation} rooted at the IG Newsgroup folder
   */
  public ServiceTreeRepresentation getNewsgroupsStructure(NodeRef igRoot) {
    ServiceTreeRepresentation newsgroupTree = new ServiceTreeRepresentation(
      "newsgroup"
    );
    Child newsgroupRoot = new Child();
    newsgroupRoot.setName("Newsgroup");

    newsgroupRoot.setNode(getNewsgroupsNodeRefFromLucene(igRoot));

    newsgroupTree.setChild(newsgroupRoot);

    recursiveWalkAndBuildTree(newsgroupRoot.getNode(), newsgroupRoot);

    return newsgroupTree;
  }

  /**
   * Recursively populates the given {@link Child} node with the folder hierarchy found under the
   * supplied node, using {@link FileFolderService#listFolders(NodeRef)}.
   *
   * @param node  the repository node whose sub-folders are listed
   * @param child the tree node to populate with children mirroring the repository folders
   */
  private void recursiveWalkAndBuildTree(NodeRef node, Child child) {
    List<FileInfo> lf = fileFolderService.listFolders(node);

    if (!lf.isEmpty()) {
      for (FileInfo fi : lf) {
        Child cTmp = new Child();

        cTmp.setName(fi.getName());
        cTmp.setNode(fi.getNodeRef());

        child.getChildren().add(cTmp);

        recursiveWalkAndBuildTree(fi.getNodeRef(), cTmp);
      }
    }
  }

  /**
   * Resolves the {@code Library} child folder of the IG root by name.
   *
   * @param igRoot the node reference of the IG root node
   * @return the node reference of the Library folder, or {@code null} if not found
   */
  private NodeRef getLibraryNodeRefFromLucene(NodeRef igRoot) {
    return nodeService.getChildByName(
      igRoot,
      ContentModel.ASSOC_CONTAINS,
      "Library"
    );
  }

  /**
   * Resolves the {@code Information} child folder of the IG root by name.
   *
   * @param igRoot the node reference of the IG root node
   * @return the node reference of the Information folder, or {@code null} if not found
   */
  private NodeRef getInformationNodeRefFromLucene(NodeRef igRoot) {
    return nodeService.getChildByName(
      igRoot,
      ContentModel.ASSOC_CONTAINS,
      "Information"
    );
  }

  /**
   * Resolves the {@code Newsgroups} child folder of the IG root by name.
   *
   * @param igRoot the node reference of the IG root node
   * @return the node reference of the Newsgroups folder, or {@code null} if not found
   */
  private NodeRef getNewsgroupsNodeRefFromLucene(NodeRef igRoot) {
    return nodeService.getChildByName(
      igRoot,
      ContentModel.ASSOC_CONTAINS,
      "Newsgroups"
    );
  }

  /**
   * Returns the per-day activity counts logged for the given Interest Group.
   *
   * @param igRoot the node reference of the IG root node
   * @return the list of {@link ActivityCountDAO} entries recorded for the IG
   */
  public List<ActivityCountDAO> getListOfActivityCount(NodeRef igRoot) {
    Long igDbNode = Long.valueOf(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID).toString()
    );

    return logService.getListOfActivityCountForInterestGroup(igDbNode);
  }

  /**
   * Identifies the IG service context of a node during the counting traversal, so that documents
   * and folders can be attributed to the correct Library or Information totals.
   */
  @SuppressWarnings("java:S115")
  private enum Place {
    /** The IG root, before any specific service folder has been entered. */
    IG_ROOT,
    /** The Library service subtree. */
    Library,
    /** The Information service subtree. */
    Information,
  }
}
