package eu.cec.digit.circabc.service.rendition.job;

import eu.cec.digit.circabc.action.config.RenditionServiceConfig;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionHelper;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionService;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Job to pre-render PDFs for the document preview.
 *
 * @author schwerr
 */
public class PreviewPDFRenditionJob implements StatefulJob {

    private static final Log logger = LogFactory.getLog(PreviewPDFRenditionJob.class);

    private RenditionServiceConfig renditionServiceConfig = null;

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        final JobDataMap jobData = context.getJobDetail().getJobDataMap();

        renditionServiceConfig = (RenditionServiceConfig) jobData.get("renditionServiceConfig");

        // Check if rendition system enabled (enable through JMX)
        if (!renditionServiceConfig.isPreviewJobEnabled()) {
            logger.info(
                    "Rendition system disabled through JMX - Change at: "
                            + "CIRCABC:Name=Configuration,Type=Rendition Service");
            return;
        }

        final ServiceRegistry serviceRegistry = (ServiceRegistry) jobData.get("serviceRegistry");
        final CircabcServiceRegistry circabcServiceRegistry =
                (CircabcServiceRegistry) jobData.get("circabcServiceRegistry");

        final int renderAmount = Integer.valueOf((String) jobData.get("renderAmount"));
        String startYear = (String) jobData.get("startYear");

        final CircabcRenditionService circabcRenditionService =
                (CircabcRenditionService)
                        circabcServiceRegistry.getService(CircabcServiceRegistry.CIRCABC_RENDITION_SERVICE);

        final NodeService nodeService = serviceRegistry.getNodeService();
        final RenditionService renditionService = serviceRegistry.getRenditionService();
        final DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        final ContentService contentService = serviceRegistry.getContentService();
        final TransactionService transactionService = serviceRegistry.getTransactionService();
        final SearchService searchService = serviceRegistry.getSearchService();

        final String query =
                "ASPECT:\""
                        + CircabcModel.ASPECT_LIBRARY
                        + "\" AND ASPECT:\""
                        + DocumentModel.ASPECT_CIRCABC_DOCUMENT
                        + "\" AND @cm\\:created:["
                        + startYear
                        + "\\-01\\-01T00:00:00 TO MAX] "
                        + "-ASPECT:\""
                        + RenditionModel.ASPECT_RENDITIONED
                        + "\"";

        RunAsWork<Object> work =
                new RunAsWork<Object>() {

                    @Override
                    public Object doWork() throws Exception {

                        ResultSet resultSet = null;

                        try {
                            resultSet =
                                    searchService.query(
                                            new StoreRef("workspace", "SpacesStore"),
                                            SearchService.LANGUAGE_LUCENE,
                                            query);

                            // Return if no results were found (everything was rendered)
                            if (resultSet.length() == 0) {
                                return null;
                            }

                            final List<NodeRef> nodeRefs = resultSet.getNodeRefs();

                            final RetryingTransactionHelper txnHelper =
                                    transactionService.getRetryingTransactionHelper();

                            final RetryingTransactionCallback<Object> callback =
                                    new RetryingTransactionCallback<Object>() {
                                        @Override
                                        public Object execute() throws Throwable {

                                            List<NodeRef> renderingNodeRefs = new ArrayList<>();

                                            int toRender = 0;

                                            // Collect nodeRefs that have not been yet rendered
                                            for (NodeRef nodeRef : nodeRefs) {

                                                // Check if already PDF
                                                if (isPDF(nodeRef, nodeService, dictionaryService, contentService)) {
                                                    continue;
                                                }

                                                // Check existing rendition
                                                if (nodeService.hasAspect(nodeRef, RenditionModel.ASPECT_RENDITIONED)) {

                                                    List<ChildAssociationRef> children =
                                                            renditionService.getRenditions(nodeRef);

                                                    boolean hasPreviewRendition = false;

                                                    for (ChildAssociationRef childAssocRef : children) {

                                                        NodeRef childRef = childAssocRef.getChildRef();

                                                        if (((String) nodeService.getProperty(childRef, ContentModel.PROP_NAME))
                                                                .endsWith(CircabcRenditionHelper.PDF_PREVIEW_RENDITION_SUFFIX)) {
                                                            hasPreviewRendition = true;
                                                            break;
                                                        }
                                                    }

                                                    if (!hasPreviewRendition) {
                                                        renderingNodeRefs.add(nodeRef);
                                                        toRender++;
                                                    }
                                                } else {
                                                    renderingNodeRefs.add(nodeRef);
                                                    toRender++;
                                                }

                                                // Collect batches of renderAmount nodes
                                                if (toRender >= renderAmount) {
                                                    break;
                                                }
                                            }

                                            // If 0, there is no more to render
                                            if (toRender == 0) {
                                                return null;
                                            }

                                            logger.info("Rendering " + toRender + " documents.");

                                            // Adds the nodeRefs to render
                                            for (NodeRef nodeRef : renderingNodeRefs) {
                                                circabcRenditionService.addRequest(nodeRef);
                                            }

                                            return null;
                                        }
                                    };

                            return txnHelper.doInTransaction(callback, false, true);
                        } catch (Throwable t) {
                            logger.error("Error while rendering nodes for the preview.", t);
                            return null;
                        } finally {
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        }
                    }
                };

        AuthenticationUtil.runAs(work, AuthenticationUtil.SYSTEM_USER_NAME);
    }

    /**
     * Check if the given nodeRef's content has PDF mimetype.
     */
    private boolean isPDF(
            NodeRef nodeRef,
            NodeService nodeService,
            DictionaryService dictionaryService,
            ContentService contentService) {

        // Get all content definitions
        Collection<QName> contentQNames =
                dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);

        QName nodeType = nodeService.getType(nodeRef);

        TypeDefinition typeDefinition = dictionaryService.getType(nodeType);

        Map<QName, PropertyDefinition> propertyDefinitions = typeDefinition.getProperties();

        // Iterate through all content definitions until we find the actual
        // content property to extract. This step is necessary because CIRCABC
        // uses default and custom content properties, which means that the
        // actual content could be in a different property than cm:content
        // (example: newsgroup attachments)
        for (QName contentQName : contentQNames) {

            // Check if the property exists for the nodeRef's type
            if (propertyDefinitions.containsKey(contentQName)) {

                // Check if the document is already a PDF
                ContentReader reader = contentService.getReader(nodeRef, contentQName);
                if (reader != null && MimetypeMap.MIMETYPE_PDF.equals(reader.getMimetype())) {
                    // If PDF, true
                    return true;
                } else if (reader != null && !MimetypeMap.MIMETYPE_PDF.equals(reader.getMimetype())) {
                    // If not PDF but exists as content, return false
                    return false;
                }
            }
        }

        return false;
    }
}
