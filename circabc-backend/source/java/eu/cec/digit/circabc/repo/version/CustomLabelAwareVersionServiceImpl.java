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
package eu.cec.digit.circabc.repo.version;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionHelper;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.version.Version2ServiceImpl;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.ReservedVersionNameException;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VersionNumber;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Override the default version label calculation behaviour.
 *
 * @author Yanick Pignot
 */
public class CustomLabelAwareVersionServiceImpl extends Version2ServiceImpl {

    public static final String PROP_CUSTOM_VERSION_LABEL = "customVersionLabel";
    private static final String LABEL_REGEX = "[0-9]+[\\.[0-9]+]+";
    private RenditionService renditionService = null;

    public static boolean isValidVersionLabel(final String value) {
        return value != null && value.matches(LABEL_REGEX);
    }

    @Override
    protected String invokeCalculateVersionLabel(
            QName classRef,
            Version preceedingVersion,
            int versionNumber,
            Map<String, Serializable> versionProperties) {

        final String customLabel = (String) versionProperties.get(PROP_CUSTOM_VERSION_LABEL);
        versionProperties.remove(PROP_CUSTOM_VERSION_LABEL);

        if (customLabel == null || customLabel.trim().length() < 1) {
            return super.invokeCalculateVersionLabel(
                    classRef, preceedingVersion, versionNumber, versionProperties);
        } else if (isValidVersionLabel(customLabel) == false) {
            throw new IllegalArgumentException(
                    "Invalid value for version label: " + customLabel + ". It must matches " + LABEL_REGEX);
        } else if (preceedingVersion != null
                && isNewLabelValid(preceedingVersion.getVersionLabel(), customLabel) == false) {
            throw new IllegalArgumentException(
                    "Invalid value for version label: "
                            + customLabel
                            + ". It must be greather than the previous one: "
                            + preceedingVersion.getVersionLabel());
        } else {
            return customLabel;
        }
    }

    private boolean isNewLabelValid(final String preceedinLabel, final String customLabel) {

        final VersionNumber oldLabel = new VersionNumber(preceedinLabel);
        final VersionNumber newLabel = new VersionNumber(customLabel);

        return oldLabel.compareTo(newLabel) < 1;
    }

    /**
     * Removes the publish information when versioning the document. This has to be done because each
     * time we create a new version of a document, only the current could have been published. New
     * versions have to be republished since they might represent a new document.
     *
     * @see org.alfresco.repo.version.Version2ServiceImpl#createVersion(org.alfresco.service.cmr.repository.NodeRef,
     * java.util.Map)
     */
    @Override
    public Version createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties)
            throws ReservedVersionNameException, AspectMissingException {

        Version version = super.createVersion(nodeRef, versionProperties);

        NodeRef versionedNodeRef = version.getVersionedNodeRef();

        // Remove aspect and associated properties
        if (nodeService.hasAspect(versionedNodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED)) {
            nodeService.removeProperty(versionedNodeRef, CircabcModel.PROP_REPOSITORIES_INFO);
            nodeService.removeAspect(versionedNodeRef, CircabcModel.ASPECT_EXTERNALLY_PUBLISHED);
        }

        // Check if there is a preview rendition, and delete it to renew it
        // the next time
        if (nodeService.hasAspect(versionedNodeRef, RenditionModel.ASPECT_RENDITIONED)) {

            List<ChildAssociationRef> children = renditionService.getRenditions(versionedNodeRef);

            boolean allDeleted = true;

            for (ChildAssociationRef childAssocRef : children) {

                NodeRef childRef = childAssocRef.getChildRef();

                if (((String) nodeService.getProperty(childRef, ContentModel.PROP_NAME))
                        .endsWith(CircabcRenditionHelper.PDF_PREVIEW_RENDITION_SUFFIX)) {
                    nodeService.deleteNode(childRef);
                } else {
                    allDeleted = false;
                }
            }

            if (allDeleted) {
                nodeService.removeAspect(versionedNodeRef, RenditionModel.ASPECT_RENDITIONED);
            }
        }

        return version;
    }

    /**
     * Sets the value of the renditionService
     *
     * @param renditionService the renditionService to set.
     */
    public void setRenditionService(RenditionService renditionService) {
        this.renditionService = renditionService;
    }
}
