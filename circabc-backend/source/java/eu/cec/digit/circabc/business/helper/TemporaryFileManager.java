/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.business.helper;

import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yanick Pignot
 */
public class TemporaryFileManager {

    private static final Log logger = LogFactory.getLog(TemporaryFileManager.class);

    private NodeService nodeService;
    private ManagementService managementService;
    private FileFolderService fileFolderService;

    private ContentManager contentManager;

    private String tempRootFolderName = "temp";
    private NodeRef tempRootRef;


    /* The time that a file can stay in the tempdirectory before cleanup.
     * Since they are not many temp files created, this can be huge.
     * We have to ensure that the user not longer need to access to it
     **/
    private long noAccessTime = 1000L * 60L * 60L % 24L; // 24 hours


    public NodeRef createTempFile(final File file, final String name) {
        final NodeRef parent = getTempRoot(true);
        return contentManager
                .createContent(parent, name, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT, file,
                        true);
    }


    public void removeTempFile(final NodeRef nodeRef) {
        try {
            if (isTempFile(nodeRef)) {
                // ensure that the node will not be moved in the archive store
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                nodeService.deleteNode(nodeRef);
            }
        } catch (Throwable t) {
            if (logger.isDebugEnabled()) {
                logger.debug("Impossible to delete temp node " + nodeRef
                        + ". Certainly due to a cluster concurent access.", t);
            }
        }
    }

    public void removeTempFiles() {
        final NodeRef tempRoot = getTempRoot(false);

        if (tempRoot != null) {
            final List<NodeRef> contents = new ArrayList<>();

            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(tempRoot);

            for (final ChildAssociationRef assoc : childs) {
                contents.add(assoc.getChildRef());
            }

            final Date now = new Date(System.currentTimeMillis() - noAccessTime);

            Date modified;
            for (final NodeRef ref : contents) {
                modified = (Date) nodeService.getProperty(ref, ContentModel.PROP_MODIFIED);

                if (modified == null) {
                    modified = (Date) nodeService.getProperty(ref, ContentModel.PROP_CREATED);
                }

                if (modified == null || modified.before(now)) {
                    removeTempFile(ref);
                }
            }
        }
    }

    public boolean isTempFile(final NodeRef nodeRef) {
        if (nodeRef == null || nodeService.exists(nodeRef) == false) {
            return false;
        } else {
            final NodeRef tempRoot = getTempRoot(false);
            return tempRoot != null && nodeService.getPrimaryParent(nodeRef).getParentRef()
                    .equals(tempRoot);
        }
    }

    public NodeRef getTempRoot() {
        return getTempRoot(true);
    }

    private NodeRef getTempRoot(boolean createIfMissing) {
        if (tempRootRef == null) {
            final NodeRef cbcDDFolder = managementService.getCircabcDictionaryNodeRef();

            tempRootRef = nodeService
                    .getChildByName(cbcDDFolder, ContentModel.ASSOC_CONTAINS, tempRootFolderName);

            if (tempRootRef == null && createIfMissing) {
                tempRootRef = fileFolderService
                        .create(cbcDDFolder, tempRootFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
            }
        }

        return tempRootRef;
    }

    // create a job that clean all files that are not modified since 24 hours


    public void setTempRootFolderName(final String name) {
        this.tempRootFolderName = name;
    }

    public void setTimeBeforeDeletion(long millis) {
        this.noAccessTime = millis;
    }

    //-----
    //Ioc

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public final void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }


    /**
     * @param contentManager the contentManager to set
     */
    public final void setContentManager(ContentManager contentManager) {
        this.contentManager = contentManager;
    }
}
