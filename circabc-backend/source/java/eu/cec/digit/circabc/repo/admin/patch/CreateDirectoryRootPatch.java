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
package eu.cec.digit.circabc.repo.admin.patch;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;

/**
 * Applicatif patch that add the directory root container under each Interest group.
 *
 * @author yanick pignot
 */
public class CreateDirectoryRootPatch extends AbstractPatch {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(CreateDirectoryRootPatch.class);

    private int updatedCount = 0;
    private int totalIgCount = 0;

    private ManagementService managementService;

    @Override
    protected String applyInternal() throws Exception {
        updatedCount = 0;
        totalIgCount = 0;

        final NodeRef circabc = managementService.getCircabcNodeRef();

        if (circabc != null) {
            addDirectoryNode(circabc);
        }
        // else it s a new installation and no script to launch.

        return "Directory containers successfully created under Interest Goups"
                + "\n\tInterest Group found:   "
                + totalIgCount
                + "\n\tInterest Group updated: "
                + updatedCount;
    }

    /**
     * @return the managementService
     */
    protected final ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    private void addDirectoryNode(NodeRef ref) {
        final Set<QName> aspects = nodeService.getAspects(ref);

        if (aspects.contains(CircabcModel.ASPECT_CATEGORY)
                || aspects.contains(CircabcModel.ASPECT_CIRCABC_ROOT)) {
            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(ref);
            for (final ChildAssociationRef assoc : childs) {
                addDirectoryNode(assoc.getChildRef());
            }
        } else if (aspects.contains(CircabcModel.ASPECT_IGROOT)) {
            totalIgCount++;

            final List<ChildAssociationRef> associations =
                    nodeService.getChildAssocs(
                            ref, CircabcModel.ASSOC_IG_DIRECTORY_CONTAINER, CircabcModel.TYPE_DIRECTORY_SERVICE);

            NodeRef directory = null;

            if (associations.size() < 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "The Interest Group "
                                    + nodeService.getProperty(ref, ContentModel.PROP_NAME)
                                    + " has not a directory container setted! Let's to create it....");
                }

                directory = managementService.createDirectory(ref);

                updatedCount++;
            } else {
                directory = associations.get(0).getChildRef();

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "The Interest Group "
                                    + nodeService.getProperty(ref, ContentModel.PROP_NAME)
                                    + " has already a directory container setted.");
                }
            }

            if (directory == null) {
                logger.warn(
                        "Something goes wrong. Directory home not created under "
                                + nodeService.getProperty(ref, ContentModel.PROP_NAME)
                                + "("
                                + ref
                                + ")");
            }
            // sometimes the "add circabc management aspect" rule doesn't work successfully ... force it
            // if needed.
            else if (!nodeService.hasAspect(directory, CircabcModel.ASPECT_CIRCABC_MANAGEMENT)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "The directory root of the Interest Group "
                                    + nodeService.getProperty(ref, ContentModel.PROP_NAME)
                                    + " has no the 'CircabcManagement' aspect setted. Set it .....");
                }

                nodeService.addAspect(directory, CircabcModel.ASPECT_CIRCABC_MANAGEMENT, null);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "The directory root of the Interest Group "
                                    + nodeService.getProperty(ref, ContentModel.PROP_NAME)
                                    + " has successfully the 'CircabcManagement' aspect setted.");
                }
            }
        }
    }
}
