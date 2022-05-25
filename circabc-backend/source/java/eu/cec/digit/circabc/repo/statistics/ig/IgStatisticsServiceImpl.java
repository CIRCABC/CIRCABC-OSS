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
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.statistics.ig.IgStatisticsService;
import eu.cec.digit.circabc.web.Services;
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

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** @author beaurpi */
public class IgStatisticsServiceImpl implements IgStatisticsService {

    /** A logger for the class */
    static final Log logger = LogFactory.getLog(IgStatisticsServiceImpl.class);
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private VersionService versionService;
    private LogService logService;
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
    private int maxLevel = 0;
    private int numberOfVersions = 0;
    private long sizeOfVersions = 0;
    private int numberOfCustomizationAndHiddenContent = 0;
    private long sizeOfCustomizationAndHiddenContent = 0;

    public Integer getNumberOfUsers(NodeRef igRoot) {

        final FacesContext fc = FacesContext.getCurrentInstance();

        final IGRootProfileManagerService igrootProfileManger =
                Services.getCircabcServiceRegistry(fc).getIGRootProfileManagerService();

        Map<String, Profile> invitedUsersProfiles = igrootProfileManger.getInvitedUsersProfiles(igRoot);

        return invitedUsersProfiles.size();
    }

    public Serializable getIGCreationDate(NodeRef igRoot) {

        return nodeService.getProperty(igRoot, ContentModel.PROP_CREATED);
    }

    /** @return the nodeService */
    public NodeService getNodeService() {
        return nodeService;
    }

    /** @param nodeService the nodeService to set */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /** @see eu.cec.digit.circabc.service.statistics.ig.IgStatisticsService#buildStatsData(NodeRef) */
    @Override
    public synchronized void buildStatsData(final NodeRef igRoot) {

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
        sizeOfVersions = 0;
        numberOfCustomizationAndHiddenContent = 0;
        sizeOfCustomizationAndHiddenContent = 0;
        maxLevel = 0;

        RunAsWork<Object> work =
                new RunAsWork<Object>() {

                    @Override
                    public Object doWork() throws Exception {
                        try {
                            countNodes(igRoot, Place.IG_ROOT, -1);
                        } catch (Exception e) {
                            logger.error("Exception while calculating the IG " + "Statistics counts.", e);
                        }
                        return null;
                    }
                };

        AuthenticationUtil.runAs(work, AuthenticationUtil.SYSTEM_USER_NAME);
    }

    public Integer getNumberOfLibraryDocuments() {
        return numberOfLibraryDocuments;
    }

    /** @return the numberOfVersions */
    public Integer getNumberOfVersions() {
        return numberOfVersions;
    }

    public Integer getNumberOfLibrarySpaces() {
        return numberOfLibraryFolders;
    }

    /** @return the numberOfCustomizationAndHiddenContent */
    public Integer getNumberOfCustomizationAndHiddenContent() {
        return numberOfCustomizationAndHiddenContent;
    }

    /** @return the sizeOfCustomizationAndHiddenContent */
    public Long getSizeOfCustomizationAndHiddenContent() {
        return sizeOfCustomizationAndHiddenContent;
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
                            "Exception while retrieving the version "
                                    + "history during the calculation the IG Statistics "
                                    + "counts. NodeRef: "
                                    + nodeRef.toString(),
                            e);
                }
                if (history != null) {
                    Collection<Version> versions = history.getAllVersions();
                    numberOfVersions += versions.size();
                    for (Version version : versions) {
                        NodeRef versionNodeRef = version.getFrozenStateNodeRef();
                        sizeOfVersions += getContentSize(versionNodeRef, ContentModel.PROP_CONTENT);
                    }
                }
            }

            if (CircabcModel.TYPE_CUSTOMIZATION_CONTENT.equals(type)
                    || DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(type)) {
                numberOfCustomizationAndHiddenContent++;
                sizeOfCustomizationAndHiddenContent += getContentSize(nodeRef, contentPropertyQName);
            } else if (place.equals(Place.Library)) {
                numberOfLibraryDocuments++;
                sizeOfLibraryDocuments += getContentSize(nodeRef, contentPropertyQName);
            } else if (place.equals(Place.Information)) {
                numberOfInformationDocuments++;
                sizeOfInformationDocuments += getContentSize(nodeRef, contentPropertyQName);
            }
        } else if (ContentModel.TYPE_FOLDER.equals(type)) {
            if (place.equals(Place.Library)
                    && nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
                numberOfLibraryFolders++;
            } else if (place.equals(Place.Information)
                    && nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)) {
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
        String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
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

                    if (ContentModel.TYPE_FOLDER.equals(type) || ForumModel.TYPE_FORUM.equals(type)) {
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
        if (Place.Library.toString().equals(name) || Place.Information.toString().equals(name)) {
            place = Place.IG_ROOT;
        }
    }

    private long getContentSize(NodeRef nodeRef, QName contentPropertyQName) {
        ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, contentPropertyQName);
        if (contentData != null) {
            return contentData.getSize();
        }
        return 0;
    }

    public Integer getNumberOfInformationDocuments() {
        return numberOfInformationDocuments;
    }

    public Integer getNumberOfInformationSpaces() {
        return numberOfInformationFolders;
    }

    public String getIGTitle(NodeRef igRoot) {
        return nodeService.getProperty(igRoot, ContentModel.PROP_TITLE).toString();
    }

    public Integer getNumbetOfMeetings() {
        return numberOfMeetings;
    }

    public Integer getNumbetOfEvents() {
        return numberOfEvents;
    }

    public Integer getNumberOfForums() {
        return numberOfForums;
    }

    public Integer getNumberOfTopics() {
        return numberOfTopics;
    }

    public Integer getNumberOfPosts() {
        return numberOfPosts;
    }

    public Long getContentSizeOfLibrary() {
        return sizeOfLibraryDocuments;
    }

    public Long getContentSizeOfInformation() {
        return sizeOfInformationDocuments;
    }

    /** @return the sizeOfVersions */
    public Long getSizeOfVersions() {
        return sizeOfVersions;
    }

    /** * Walk recursively into the library service and build a tree representation of all folders */
    public ServiceTreeRepresentation getLibraryStructure(NodeRef igRoot) {

        ServiceTreeRepresentation libraryTree = new ServiceTreeRepresentation("library");
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

        ServiceTreeRepresentation informationTree = new ServiceTreeRepresentation("library");
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

        ServiceTreeRepresentation newsgroupTree = new ServiceTreeRepresentation("newsgroup");
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
        return nodeService.getChildByName(igRoot, ContentModel.ASSOC_CONTAINS, "Library");
    }

    private NodeRef getInformationNodeRefFromLucene(NodeRef igRoot) {
        return nodeService.getChildByName(igRoot, ContentModel.ASSOC_CONTAINS, "Information");
    }

    private NodeRef getNewsgroupsNodeRefFromLucene(NodeRef igRoot) {
        return nodeService.getChildByName(igRoot, ContentModel.ASSOC_CONTAINS, "Newsgroups");
    }

    public List<ActivityCountDAO> getListOfActivityCount(NodeRef igRoot) {

        Long igDbNode =
                Long.valueOf(
                        this.getNodeService().getProperty(igRoot, ContentModel.PROP_NODE_DBID).toString());

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

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    private enum Place {
        IG_ROOT,
        Library,
        Information
    }
}
