package eu.cec.digit.circabc.service.rendition.job;

import eu.cec.digit.circabc.action.config.RenditionServiceConfig;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionHelper;
import eu.cec.digit.circabc.service.rendition.RenditionDaoService;
import eu.cec.digit.circabc.service.rendition.Request;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition.executer.ReformatRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import java.util.*;

/**
 * Job to render PDFs that have been posted for rendering.
 *
 * @author schwerr
 */
public class RenderJob implements StatefulJob {

    private static final Log logger = LogFactory.getLog(RenderJob.class);
    List<String> documentIds = null;
    private RenditionServiceConfig renditionServiceConfig = null;
    private RenditionService renditionService = null;
    private NodeService nodeService = null;
    private DictionaryService dictionaryService = null;
    private ContentService contentService = null;
    private CheckOutCheckInService checkOutCheckInService = null;
    private LockService lockService = null;
    private RenditionDaoService renditionDaoService = null;
    private eu.cec.digit.circabc.service.lock.LockService circabcLockService;

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        final JobDataMap jobData = context.getJobDetail().getJobDataMap();

        renditionServiceConfig = (RenditionServiceConfig) jobData.get("renditionServiceConfig");

        // Check if rendition system enabled (enable through JMX)
        if (!renditionServiceConfig.isRenderJobEnabled()) {
            logger.info(
                    "Rendition system disabled through JMX - Change at: "
                            + "CIRCABC:Name=Configuration,Type=Rendition Service");
            return;
        }

        final ServiceRegistry serviceRegistry = (ServiceRegistry) jobData.get("serviceRegistry");
        final CircabcServiceRegistry circabcServiceRegistry =
                (CircabcServiceRegistry) jobData.get("circabcServiceRegistry");
        circabcLockService =
                (eu.cec.digit.circabc.service.lock.LockService) jobData.get("circabcLockService");

        renditionDaoService =
                (RenditionDaoService)
                        circabcServiceRegistry.getService(CircabcServiceRegistry.RENDITION_DAO_SERVICE);

        nodeService = serviceRegistry.getNodeService();
        renditionService = serviceRegistry.getRenditionService();
        dictionaryService = serviceRegistry.getDictionaryService();
        contentService = serviceRegistry.getContentService();
        checkOutCheckInService = serviceRegistry.getCheckOutCheckInService();
        lockService = serviceRegistry.getLockService();

        RunAsWork<Object> work =
                new RunAsWork<Object>() {

                    @Override
                    public Object doWork() throws Exception {

                        String lockName = "RenderJob";

                        boolean couldLock = circabcLockService.tryLock(lockName);
                        if (!couldLock) {
                            return null;
                        }

                        try {

                            // Fetch the requests (get them and mark them as fetched)
                            List<Request> requests = fetchRequests();

                            for (Request request : requests) {

                                NodeRef nodeRef = new NodeRef(request.getNodeRefUUID());

                                // Node may have not been indexed yet, or maybe it was
                                // deleted before its turn to be processed, or altered
                                if (!nodeService.exists(nodeRef)) {
                                    String errorMessage = "NodeRef does not exist.";
                                    logger.error(errorMessage);
                                    request.setStartProcessingDate(new Date());
                                    updateForError(request, errorMessage, false);
                                    continue;
                                }

                                // Check if the node is being used
                                if (checkOutCheckInService.isCheckedOut(nodeRef)
                                        || checkOutCheckInService.isWorkingCopy(nodeRef)
                                        || lockService.getLockStatus(nodeRef) == LockStatus.LOCKED
                                        || lockService.getLockStatus(nodeRef) == LockStatus.LOCK_OWNER) {
                                    // Continue to the next node and leave this one for the next round
                                    request.setErrorMessage("Document was locked");
                                    request.setRemainingRenderRetries(0);
                                    request.setSuccess(false);
                                    renditionDaoService.updateRequest(request);
                                    continue;
                                }

                                render(request);
                            } // end for

                        } finally {
                            circabcLockService.unlock(lockName);
                        }

                        return null;
                    }
                };

        AuthenticationUtil.runAs(work, AuthenticationUtil.SYSTEM_USER_NAME);
    }

    /**
     * Get requests and mark as fetched.
     */
    private List<Request> fetchRequests() {

        List<Request> requests = null;

        List<Request> requestsToProcess = new ArrayList<>();

        try {
            requests = renditionDaoService.getTenRequests();

            if (logger.isDebugEnabled()) {
                logger.debug("Fetched " + requests.size() + " request(s).");
            }

            int count = 0;
            documentIds = new ArrayList<>();
            for (Request request : requests) {
                requestsToProcess.add(request);
                documentIds.add(request.getNodeRefUUID());
                count++;
                if (count == 10) {
                    // fetch & process at most 10 requests per run
                    break;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "About to process "
                                + requestsToProcess.size()
                                + " request(s). NodeRefs: "
                                + documentIds);
            }

            renditionDaoService.updateFetchedRequests(requestsToProcess);
        } catch (Exception e) {
            logger.error("Error fetching request(s).", e);
        }

        // always return 10 or less requests
        return requestsToProcess;
    }

    /**
     * Does the actual rendering of each nodeRef
     */
    private boolean render(Request request) {

        long startTime = 0;

        if (logger.isDebugEnabled()) {
            startTime = System.nanoTime();
        }

        NodeRef nodeRef = new NodeRef(request.getNodeRefUUID());

        // Get all content definitions
        Collection<QName> contentQNames =
                dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);

        QName nodeType = nodeService.getType(nodeRef);

        TypeDefinition typeDefinition = dictionaryService.getType(nodeType);

        Map<QName, PropertyDefinition> propertyDefinitions = typeDefinition.getProperties();
        QName sourceContentQName = null;

        // Iterate through all content definitions until we find the actual
        // content property to extract. This step is necessary because
        // CIRCABC uses default and custom content properties, which
        // means that the actual content could be in a different property
        // than cm:content (example: newsgroup attachments)
        for (QName contentQName : contentQNames) {

            // Check if the property exists for the nodeRef's type
            if (propertyDefinitions.containsKey(contentQName)) {

                // Check if the document is already a PDF
                ContentReader reader = contentService.getReader(nodeRef, contentQName);
                if (reader != null && MimetypeMap.MIMETYPE_PDF.equals(reader.getMimetype())) {
                    // If PDF, return
                    String errorMessage = "Already a PDF";
                    logger.error(errorMessage);
                    request.setStartProcessingDate(new Date());
                    updateForError(request, errorMessage, true);
                    return false;
                } else if (reader != null && !MimetypeMap.MIMETYPE_PDF.equals(reader.getMimetype())) {
                    // If not PDF but exists as content, exit and proceed to
                    // calculate its rendition
                    sourceContentQName = contentQName;
                    break;
                }
            }
        }

        if (sourceContentQName == null) {
            // If the loop exited and no content property was found, return
            // with error (unexpected)
            String errorMessage =
                    "Reader is null attempting to calculate " + "the rendition! Is this node a content item?";
            logger.error(errorMessage);
            updateForError(request, errorMessage, false);
            return false;
        }

        String renditionName =
                (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
                        + CircabcRenditionHelper.PDF_PREVIEW_RENDITION_SUFFIX;

        QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, renditionName);

        // Check if there is already a PDF rendition for this document
        // This is necessary because it could be that all cluster nodes have
        // to process the same node, so the first one, is taken
        ChildAssociationRef pdfRendition = renditionService.getRenditionByName(nodeRef, renditionQName);

        if (pdfRendition != null) {
            handleSuccess(request);
            return true;
        }

        RenditionDefinition renditionDefinition =
                renditionService.createRenditionDefinition(renditionQName, ReformatRenderingEngine.NAME);

        renditionDefinition.setParameterValue(
                AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_PDF);

        // In case the content property is not the default one
        renditionDefinition.setParameterValue(
                AbstractRenderingEngine.PARAM_SOURCE_CONTENT_PROPERTY, sourceContentQName);

        if (logger.isDebugEnabled()) {
            logger.debug("Processing nodeRef " + nodeRef.getId());
        }

        // Now execute this rendition definition
        try {
            request.setStartProcessingDate(new Date());
            renditionDaoService.updateRequest(request);
            renditionService.render(nodeRef, renditionDefinition);
            handleSuccess(request);
        } catch (Exception e) {
            String errorMessage = "Exception rendering PDF: ";
            logger.error(errorMessage + e.getMessage());
            updateForError(request, errorMessage + ": " + e.getMessage(), false);
        }

        if (logger.isDebugEnabled()) {
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            logger.debug("NodeRef " + nodeRef.getId() + " rendering time " + totalTime);
        }

        return true;
    }

    /**
     * Handle the update for a successful rendition.
     */
    private void handleSuccess(Request request) {
        request.setEndProcessingDate(new Date());
        request.setErrorMessage(null);
        request.setSuccess(true);
        try {
            renditionDaoService.updateRequest(request);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Request rendered: "
                                + request.getNodeRefUUID()
                                + ", found in fetched: "
                                + documentIds.contains(request.getNodeRefUUID()));
            }
        } catch (Exception e) {
            logger.error("Error updating the request of a successful rendition.", e);
        }
    }

    /**
     * Updates the process when error.
     */
    private void updateForError(Request request, String errorMessage, boolean success) {

        request.setSuccess(success);
        request.setEndProcessingDate(new Date());
        request.setErrorMessage(errorMessage);
        request.decRemainingRenderRetries();

        try {
            renditionDaoService.updateRequest(request);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Request could NOT be rendered: "
                                + request.getNodeRefUUID()
                                + ", found in fetched: "
                                + documentIds.contains(request.getNodeRefUUID()));
            }
        } catch (Exception e) {
            logger.error("Error updating the error.", e);
        }
    }
}
