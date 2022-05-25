/**
 * ***************************************************************************** Copyright 2006
 * European Community
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
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.rendition;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Calculates the renditions of documents as necessary.
 *
 * @author schwerr
 */
public class CircabcRenditionHelper {

    public static final String PDF_RENDITION_DEFINITION = "PDFRenditionDefinition";
    public static final QName RENDITION_NAME =
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, PDF_RENDITION_DEFINITION);

    public static final String PDF_PREVIEW_RENDITION_SUFFIX = "_PDFRendition";

    private static final Log logger = LogFactory.getLog(CircabcRenditionHelper.class);

    private RenditionService renditionService = null;
    private NodeService nodeService = null;
    private DictionaryService dictionaryService = null;
    private ContentService contentService = null;

    private CircabcRenditionService circabcRenditionService = null;

    /**
     * Does the PDF rendition managing for the view.
     */
    public NodeRef getRenditionNodeRef(NodeRef nodeRef) {

        try {

            NodeRef renditionNodeRef = checkAndGetIfPDFRenditionExists(nodeRef);

            if (renditionNodeRef != null) {
                return renditionNodeRef;
            }

            // Post this document to render
            circabcRenditionService.addRequest(nodeRef);

            return null;
        } catch (Exception e) {
            logger.warn(
                    "Node could not be converted. Can these types be "
                            + "converted? "
                            + e.getLocalizedMessage());
            return null;
        }
    }

    public NodeRef checkAndGetIfPDFRenditionExists(NodeRef nodeRef) {

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
                    // If PDF, return its URL
                    return nodeRef;
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
            logger.error(
                    "Reader is null attempting to calculate the "
                            + "rendition! Is this node a content item?");
            return null;
        }

        String renditionName =
                (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
                        + PDF_PREVIEW_RENDITION_SUFFIX;

        QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, renditionName);

        // Check if there is already a PDF rendition for this document
        ChildAssociationRef pdfRendition = renditionService.getRenditionByName(nodeRef, renditionQName);

        if (pdfRendition != null) {
            return pdfRendition.getChildRef();
        }

        return null;
    }

    /**
     * Sets the value of the renditionService
     *
     * @param renditionService the renditionService to set.
     */
    public void setRenditionService(RenditionService renditionService) {
        this.renditionService = renditionService;
    }

    /**
     * Sets the value of the nodeService
     *
     * @param nodeService the nodeService to set.
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Sets the value of the dictionaryService
     *
     * @param dictionaryService the dictionaryService to set.
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Sets the value of the contentService
     *
     * @param contentService the contentService to set.
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * Sets the value of the circabcRenditionService
     *
     * @param circabcRenditionService the circabcRenditionService to set.
     */
    public void setCircabcRenditionService(CircabcRenditionService circabcRenditionService) {
        this.circabcRenditionService = circabcRenditionService;
    }
}
