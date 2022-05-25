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
package eu.cec.digit.circabc.business.impl.content;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv;
import eu.cec.digit.circabc.business.helper.MetadataManager;
import eu.cec.digit.circabc.business.helper.PermissionManager;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import eu.cec.digit.circabc.business.impl.AssertUtils;
import eu.cec.digit.circabc.business.impl.ValidationUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Business service implementation to check in, check out, update, lock a document.
 *
 * @author Yanick Pignot
 */
public class CociContentBusinessImpl implements CociContentBusinessSrv {

    private final Log logger = LogFactory.getLog(CociContentBusinessImpl.class);

    private BehaviourFilter policyBehaviourFilter;
    private NodeService nodeService;
    private ContentService contentService;
    private WorkflowService workflowService;
    private CheckOutCheckInService checkOutCheckInService;
    private LockService lockService;

    private MetadataManager metadataManager;
    private ValidationManager validationManager;
    private PermissionManager permissionManager;

    //--------------
    //-- public methods

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#checkOut(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef checkOut(final NodeRef nodeRef) {
        ValidationUtils.assertDocument(nodeRef, getValidationManager(), true, true, logger);

        final NodeRef workingCopyRef = getCheckOutCheckInService().checkout(nodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Node successfully locked and checked out. Created working copy: " + workingCopyRef);
        }

        return workingCopyRef;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#checkOutForWorkflow(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef checkOutForWorkflow(final NodeRef nodeRef, final String workflowTaskId) {
        AssertUtils.notEmpty(workflowTaskId);

        final NodeRef workingCopyRef = checkOut(nodeRef);

        final WorkflowTask task = getWorkflowService().getTaskById(workflowTaskId);
        if (task != null) {
            final NodeRef workflowPackage = (NodeRef) task.properties.get(WorkflowModel.ASSOC_PACKAGE);
            if (workflowPackage != null) {
                final String wokingCopyName = (String) getNodeService()
                        .getProperty(workingCopyRef, ContentModel.PROP_NAME);
                final QName validQname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                        QName.createValidLocalName(wokingCopyName));

                getNodeService()
                        .addChild(workflowPackage, workingCopyRef, ContentModel.ASSOC_CONTAINS, validQname);

                if (logger.isDebugEnabled()) {
                    logger.debug("Added working copy to workflow package: " + workflowPackage);
                }
            } else {
                logger.error("No workflow package found for the task " + workflowTaskId
                        + ". Impossible to link the working copy to the workflow.");
            }
        } else {
            throw new CheckOutCheckInServiceException("Invalid workflow task ID: " + workflowTaskId);
        }

        return workingCopyRef;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#cancelCheckOut(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef cancelCheckOut(final NodeRef workingCopy) {
        ValidationUtils.assertWorkingCopyDocument(workingCopy, getValidationManager(), logger);

        final NodeRef originalRef = getCheckOutCheckInService().cancelCheckout(workingCopy);

        if (logger.isDebugEnabled()) {
            logger.debug("Node " + originalRef + " not longer locked. The working copy is deleted.");
        }

        return originalRef;
    }


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#checkIn(org.alfresco.service.cmr.repository.NodeRef, boolean, java.lang.String, boolean)
     */
    public NodeRef checkIn(final NodeRef workingCopy, final boolean minor, final String versionNote,
                           boolean keepCheckOut) {
        ValidationUtils.assertWorkingCopyDocument(workingCopy, getValidationManager(), logger);

        // add version history text to props
        final Map<String, Serializable> props = new HashMap<>(1, 1.0f);
        props.put(Version.PROP_DESCRIPTION, versionNote == null ? "" : versionNote);

        // set the flag for minor or major change
        if (minor) {
            props.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        } else {
            props.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        }

        final ContentData contentData = (ContentData) getNodeService()
                .getProperty(workingCopy, ContentModel.PROP_CONTENT);
        final String contentUrl = (contentData == null ? null : contentData.getContentUrl());

        // perform the checkin
        final NodeRef original = getCheckOutCheckInService()
                .checkin(workingCopy, props, contentUrl, keepCheckOut);

        if (logger.isDebugEnabled()) {
            logger.debug("Document checked in with properties: "
                    + "\n\t Version Desc: " + props.get(Version.PROP_DESCRIPTION)
                    + "\n\t Version type: " + props.get(VersionModel.PROP_VERSION_TYPE)
            );

            if (keepCheckOut) {
                logger.debug("The original document is still locked");
            } else {
                logger.debug("The original document unlocked");
            }
        }

        return original;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#checkIn(org.alfresco.service.cmr.repository.NodeRef, boolean, java.lang.String, boolean, java.io.File)
     */
    public NodeRef checkIn(final NodeRef workingCopy, final boolean minor, final String versionNote,
                           boolean keepCheckOut, final File file, String uploadedfilename) {
        ValidationUtils.assertWorkingCopyDocument(workingCopy, getValidationManager(), logger);

        // update
        update(workingCopy, file, uploadedfilename);

        // checkin
        return checkIn(workingCopy, minor, versionNote, keepCheckOut);

    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#checkIn(org.alfresco.service.cmr.repository.NodeRef, boolean, java.lang.String, boolean, java.io.File, boolean)
     */
    public NodeRef checkIn(final NodeRef workingCopy, final boolean minor, final String versionNote,
                           boolean keepCheckOut, final File file, boolean disableNotification, String uploadedfilename) {
        boolean wasEnable = false;
        try {
            if (disableNotification) {
                wasEnable = !policyBehaviourFilter.isEnabled();
                policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }

            return checkIn(workingCopy, minor, versionNote, keepCheckOut, file, uploadedfilename);

        } finally {
            if (wasEnable) {
                policyBehaviourFilter.enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#lock(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void lock(final NodeRef nodeRef) {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#unlock(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void unlock(final NodeRef nodeRef) {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#update(org.alfresco.service.cmr.repository.NodeRef, java.io.File, boolean)
     */
    public void update(final NodeRef document, final File file, final boolean disableNotification,
                       final String uploadedfilename) {
        boolean wasEnable = false;
        try {
            if (disableNotification) {
                wasEnable = !policyBehaviourFilter.isEnabled();
                policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }

            update(document, file, uploadedfilename);
        } finally {
            if (wasEnable) {
                policyBehaviourFilter.enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#update(org.alfresco.service.cmr.repository.NodeRef, java.io.File)
     */
    public void update(final NodeRef document, final File file, final String uploadedfilename) {
        ValidationUtils.assertDocument(document, getValidationManager(), false, true, logger);
        AssertUtils.canAccess(file);

        final ContentWriter writer = getContentService()
                .getWriter(document, ContentModel.PROP_CONTENT, true);

        NodeRef parentRef = getNodeService().getPrimaryParent(document).getParentRef();

        NodeRef existingNodeRef = getNodeService().getChildByName(parentRef,
                ContentModel.ASSOC_CONTAINS, uploadedfilename);

        String newUploadedfilename = uploadedfilename;

        if (existingNodeRef != null) {
            newUploadedfilename = metadataManager.getValidUniqueName(parentRef, uploadedfilename);
        }

        getNodeService().setProperty(document, ContentModel.PROP_NAME, newUploadedfilename);

        // also update the mime type in case a different type of file is uploaded
        final String mimeType = getMetadataManager().guessMimetype(uploadedfilename);
        writer.setMimetype(mimeType);
        writer.putContent(file);

        if (logger.isDebugEnabled()) {
            logger.debug("Content updated with uploaded file " + file.getName());
        }

    }

    public void update(final NodeRef document, final InputStream inputStream, String mimeType) {

        final ContentWriter writer = getContentService()
                .getWriter(document, ContentModel.PROP_CONTENT, true);

        // also update the mime type in case a different type of file is uploaded
        if (mimeType != null) {
            writer.setMimetype(mimeType);
        }
        writer.putContent(inputStream);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#getWorkingCopy(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getWorkingCopy(final NodeRef lockedRef) {
        ValidationUtils.assertLockedDocument(lockedRef, getValidationManager(), logger);

        final NodeRef workingCopy = getCheckOutCheckInService().getWorkingCopy(lockedRef);

        if (getPermissionManager().canRead(workingCopy)) {
            return workingCopy;
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv#getWorkingCopyOf(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getWorkingCopyOf(final NodeRef workingCopyRef) {
        ValidationUtils.assertWorkingCopyDocument(workingCopyRef, getValidationManager(), logger);

        final NodeRef lockedRef = checkOutCheckInService.getCheckedOut(workingCopyRef);

        if (getPermissionManager().canRead(lockedRef)) {
            return lockedRef;
        } else {
            return null;
        }
    }

    //--------------
    //-- private helpers

    //--------------
    //-- IOC


    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the contentService
     */
    protected final ContentService getContentService() {
        return contentService;
    }

    /**
     * @param contentService the contentService to set
     */
    public final void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @return the policyBehaviourFilter
     */
    protected final BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }

    /**
     * @param policyBehaviourFilter the policyBehaviourFilter to set
     */
    public final void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * @return the checkOutCheckInService
     */
    protected final CheckOutCheckInService getCheckOutCheckInService() {
        return checkOutCheckInService;
    }

    /**
     * @param checkOutCheckInService the checkOutCheckInService to set
     */
    public final void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    /**
     * @return the workflowService
     */
    protected final WorkflowService getWorkflowService() {
        return workflowService;
    }

    /**
     * @param workflowService the workflowService to set
     */
    public final void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * @return the lockService
     */
    protected final LockService getLockService() {
        return lockService;
    }

    /**
     * @param lockService the lockService to set
     */
    public final void setLockService(LockService lockService) {
        this.lockService = lockService;
    }


    /**
     * @return the validationManager
     */
    protected final ValidationManager getValidationManager() {
        return validationManager;
    }

    /**
     * @param validationManager the validationManager to set
     */
    public final void setValidationManager(ValidationManager validationManager) {
        this.validationManager = validationManager;
    }

    /**
     * @return the metadataManager
     */
    protected final MetadataManager getMetadataManager() {
        return metadataManager;
    }

    /**
     * @param metadataManager the metadataManager to set
     */
    public final void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    /**
     * @return the permissionManager
     */
    protected final PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * @param permissionManager the permissionManager to set
     */
    public final void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

}
