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
package eu.cec.digit.circabc.repo.statistics.ig;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.repo.log.ActivityCountDAO;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.statistics.ig.IgStatisticsDaoService;
import eu.cec.digit.circabc.service.statistics.ig.IgStatisticsService;
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

/** @author beaurpi */
public class IgStatisticsServiceImpl implements IgStatisticsService {

  /** A logger for the class */
  static final Log logger = LogFactory.getLog(IgStatisticsServiceImpl.class);
  private NodeService nodeService;
  private FileFolderService fileFolderService;
  private VersionService versionService;
  private LogService logService;
  private IgStatisticsDaoService igStatisticsDaoService;
  private CircabcService circabcService;
  private LockService circabcLockService;

  private int numberOfLibraryDocuments = 0;
  private int numberOfLibraryFolders = 0;
  private long sizeOfLibraryDocuments = 0;
  private int numberOfInformationDocuments = 0;
  private int numberOfInformationFolders = 0;
  private long sizeOfInformationDocuments = 0;
  private int numberOfMeetings = 0;
  private int numberOfEvents = 0;
  private int numberOfForums = 0;
  private int numberOfTopics = 0;
  private int numberOfPosts = 0;
  private int numberOfUsers = 0;
  private int maxLevel = 0;
  private int numberOfVersions = 0;
  private long sizeOfVersions = 0;
  private int numberOfCustomizationAndHiddenContent = 0;
  private long sizeOfCustomizationAndHiddenContent = 0;

  public String getIGTitle(NodeRef igRoot) {
    return nodeService.getProperty(igRoot, ContentModel.PROP_TITLE).toString();
  }

  private Serializable getIGCreationDate(NodeRef igRoot) {
    return nodeService.getProperty(igRoot, ContentModel.PROP_CREATED);
  }

  /** @param nodeService the nodeService to set */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setIgStatisticsDaoService(
    IgStatisticsDaoService igStatisticsDaoService
  ) {
    this.igStatisticsDaoService = igStatisticsDaoService;
  }

  public void setCircabcService(CircabcService circabcService) {
    this.circabcService = circabcService;
  }

  public void setCircabcLockService(LockService circabcLockService) {
    this.circabcLockService = circabcLockService;
  }

  private boolean isStatisticsUptoDate(
    IgStatisticsParameter igStatisticsParameter
  ) {
    boolean result = false;
    if (igStatisticsParameter != null) {
      //we will calculate again the statistics and update the table only if the statistics are not up-to-date
      //When date of last calculation of statistics is before the last update date on the IG
      Date lastUpdate =
        logService.getLastUpdateDateOnInterestGroupForStatistics(
          igStatisticsParameter.getIgId()
        );
      //if last upfate is null, we consider that the content of CBC_GROUP_STATISTICS table is up-to-date
      result =
        (lastUpdate == null) ||
        (lastUpdate.before(igStatisticsParameter.getRequestDate()));
    }
    return result;
  }

  /** @see eu.cec.digit.circabc.service.statistics.ig.IgStatisticsService#buildStatsData(NodeRef) */
  @Override
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
        numberOfUsers = circabcService.countMembersInIg(igRoot.getId());
        sizeOfVersions = 0;
        numberOfCustomizationAndHiddenContent = 0;
        sizeOfCustomizationAndHiddenContent = 0;
        maxLevel = 0;

        //calculate the statistics
        final RunAsWork<Object> work = new RunAsWork<Object>() {
          @Override
          public Object doWork() throws Exception {
            String lockName = "IG_SS_" + igRoot.getId();
            boolean isLocked = circabcLockService.tryLock(lockName);
            if (!isLocked) {
              return null;
            }
            try {
              countNodes(igRoot, Place.IG_ROOT, -1);
            } catch (final Exception e) {
              logger.error(
                "Exception while calculating the IG " + "Statistics counts.",
                e
              );
            } finally {
              circabcLockService.unlock(lockName);
            }
            return null;
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
   * @param nodeRef
   * @param place
   */
  private void countNodes(NodeRef nodeRef, Place place, int level) {
    if (nodeRef == null || !nodeService.exists(nodeRef)) {
      return;
    }

    // Count by type and place
    QName type = nodeService.getType(nodeRef);

    if (ContentModel.TYPE_CONTENT.equals(type)) {
      // Consider all CIRCABC custom content definitions
      QName contentPropertyQName = ContentModel.PROP_CONTENT;

      if (CircabcModel.TYPE_CUSTOMIZATION_CONTENT.equals(type)) {
        contentPropertyQName = CircabcModel.PROP_CONTENT;
      } else if (DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(type)) {
        contentPropertyQName = DocumentModel.PROP_CONTENT;
      } else {
        // Count and size versions
        VersionHistory history = null;
        try {
          history = versionService.getVersionHistory(nodeRef);
        } catch (Exception e) {
          logger.error(
            "Exception while retrieving the version " +
            "history during the calculation the IG Statistics " +
            "counts. NodeRef: " +
            nodeRef.toString(),
            e
          );
        }
        if (history != null) {
          Collection<Version> versions = history.getAllVersions();
          numberOfVersions += versions.size();
          for (Version version : versions) {
            NodeRef versionNodeRef = version.getFrozenStateNodeRef();
            sizeOfVersions += getContentSize(
              versionNodeRef,
              ContentModel.PROP_CONTENT
            );
          }
        }
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
      } else if (place.equals(Place.Library)) {
        numberOfLibraryDocuments++;
        sizeOfLibraryDocuments += getContentSize(nodeRef, contentPropertyQName);
      } else if (place.equals(Place.Information)) {
        numberOfInformationDocuments++;
        sizeOfInformationDocuments += getContentSize(
          nodeRef,
          contentPropertyQName
        );
      }
    } else if (ContentModel.TYPE_FOLDER.equals(type)) {
      if (
        place.equals(Place.Library) &&
        nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
      ) {
        numberOfLibraryFolders++;
      } else if (
        place.equals(Place.Information) &&
        nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
      ) {
        numberOfInformationFolders++;
      }
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

    // We are entering Library or Information
    String name = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_NAME
    );
    if (Place.Library.toString().equals(name)) {
      place = Place.Library;
    } else if (Place.Information.toString().equals(name)) {
      place = Place.Information;
    }

    // Recurse without counting content children nor renditions
    if (!ContentModel.TYPE_CONTENT.equals(type)) {
      List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);

      for (ChildAssociationRef childRef : children) {
        NodeRef childNodeRef = childRef.getChildRef();
        if (childNodeRef != null && nodeService.exists(nodeRef)) {
          Integer tmpLevel = level;

          if (
            ContentModel.TYPE_FOLDER.equals(type) ||
            ForumModel.TYPE_FORUM.equals(type)
          ) {
            tmpLevel += 1;
          }

          countNodes(childNodeRef, place, tmpLevel);
        }
      }
    }

    if (level > maxLevel) {
      maxLevel = level;
    }

    // We are exiting Library or Information
    if (
      Place.Library.toString().equals(name) ||
      Place.Information.toString().equals(name)
    ) {
      place = Place.IG_ROOT;
    }
  }

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

  /** * Walk recursively into the library service and build a tree representation of all folders */
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
   * * Walk recursively into the information service and build a tree representation of all folders
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
   * * Walk recursively into the information service and build a tree representation of all folders
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

  private void recursiveWalkAndBuildTree(NodeRef node, Child child) {
    List<FileInfo> lf = fileFolderService.listFolders(node);

    if (lf.size() > 0) {
      for (FileInfo fi : lf) {
        Child cTmp = new Child();

        cTmp.setName(fi.getName());
        cTmp.setNode(fi.getNodeRef());

        child.getChildren().add(cTmp);

        recursiveWalkAndBuildTree(fi.getNodeRef(), cTmp);
      }
    }
  }

  /** @return the fileFolderService */
  public FileFolderService getFileFolderService() {
    return fileFolderService;
  }

  /** @param fileFolderService the fileFolderService to set */
  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  private NodeRef getLibraryNodeRefFromLucene(NodeRef igRoot) {
    return nodeService.getChildByName(
      igRoot,
      ContentModel.ASSOC_CONTAINS,
      "Library"
    );
  }

  private NodeRef getInformationNodeRefFromLucene(NodeRef igRoot) {
    return nodeService.getChildByName(
      igRoot,
      ContentModel.ASSOC_CONTAINS,
      "Information"
    );
  }

  private NodeRef getNewsgroupsNodeRefFromLucene(NodeRef igRoot) {
    return nodeService.getChildByName(
      igRoot,
      ContentModel.ASSOC_CONTAINS,
      "Newsgroups"
    );
  }

  public List<ActivityCountDAO> getListOfActivityCount(NodeRef igRoot) {
    Long igDbNode = Long.valueOf(
      nodeService.getProperty(igRoot, ContentModel.PROP_NODE_DBID).toString()
    );

    return logService.getListOfActivityCountForInterestGroup(igDbNode);
  }

  /** @return the logService */
  public LogService getLogService() {
    return logService;
  }

  /** @param logService the logService to set */
  public void setLogService(LogService logService) {
    this.logService = logService;
  }

  /**
   * Sets the value of the versionService
   *
   * @param versionService the versionService to set.
   */
  public void setVersionService(VersionService versionService) {
    this.versionService = versionService;
  }

  private enum Place {
    IG_ROOT,
    Library,
    Information,
  }
}
