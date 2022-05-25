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
package eu.cec.digit.circabc.repo.newsgroup;

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.newsgroup.AttachementService;
import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.alfresco.model.ContentModel.PROP_NAME;

/**
 * Implementation of newsgroup moderation service.
 *
 * @author Yanick Pignot
 */
public class AttachementServiceImpl implements AttachementService {

    private static final Log logger = LogFactory.getLog(AttachementServiceImpl.class);

    private NodeService nodeService;
    private ContentService contentService;

    public NodeRef attach(final NodeRef referer, final NodeRef refered)
            throws DuplicateChildNodeNameException {
        ParameterCheck.mandatory("The referer", referer);
        ParameterCheck.mandatory("The refered", refered);

        if (getAttachements(referer).contains(refered)) {
            return refered;
      /*      throw new DuplicateChildNodeNameException(referer, DocumentModel.ASSOC_EXTERNAL_REFERENCES,
      (String) nodeService.getProperty(referer, ContentModel.PROP_NAME), null);*/
        }

        nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);
        nodeService.createAssociation(referer, refered, DocumentModel.ASSOC_EXTERNAL_REFERENCES);

        if (logger.isDebugEnabled()) {
            logger.debug("Node " + refered + " attached to " + referer);
        }
        return refered;
    }

    public NodeRef attach(
            final NodeRef referer, final File referedFile, final String encoding, final String mimetype) {
        ParameterCheck.mandatory("The refered file", referedFile);

        return attachImpl(referer, referedFile, null, referedFile.getName(), encoding, mimetype);
    }

    public NodeRef attach(
            final NodeRef referer,
            final InputStream referedIs,
            final String name,
            final String encoding,
            final String mimetype) {
        ParameterCheck.mandatory("The refered input stream", referedIs);

        return attachImpl(referer, null, referedIs, name, encoding, mimetype);
    }

    public List<NodeRef> getAttachements(NodeRef referer) {
        ParameterCheck.mandatory("The referer", referer);

        final List<NodeRef> childs = new ArrayList<>();

        if (nodeService.hasAspect(referer, DocumentModel.ASPECT_ATTACHABLE)) {
            final List<AssociationRef> attachementsAssoc =
                    nodeService.getTargetAssocs(referer, DocumentModel.ASSOC_EXTERNAL_REFERENCES);
            for (final AssociationRef assoc : attachementsAssoc) {
                childs.add(assoc.getTargetRef());
            }

            final List<ChildAssociationRef> attachementChilds =
                    nodeService.getChildAssocs(
                            referer, DocumentModel.ASSOC_HIDDEN_REFERENCES, RegexQNamePattern.MATCH_ALL);
            for (final ChildAssociationRef assoc : attachementChilds) {
                childs.add(assoc.getChildRef());
            }
        }

        return childs;
    }

    public boolean isHiddenAttachement(NodeRef refered) {
        ParameterCheck.mandatory("The refered", refered);

        final QName type = nodeService.getType(refered);

        return DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(type);
    }

    private NodeRef createHiddenNode(final NodeRef parent, final String name) {
        final ChildAssociationRef assoc =
                nodeService.createNode(
                        parent,
                        DocumentModel.ASSOC_HIDDEN_REFERENCES,
                        createNameQName(name),
                        DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT,
                        Collections.<QName, Serializable>singletonMap(PROP_NAME, name));

        return assoc.getChildRef();
    }

    private QName createNameQName(final String name) {
        return QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name));
    }

    private NodeRef attachImpl(
            final NodeRef referer,
            final File referedFile,
            final InputStream referedIs,
            final String candidateName,
            final String encoding,
            final String mimetype) {
        ParameterCheck.mandatory("The referer", referer);
        ParameterCheck.mandatoryString("The name", candidateName);
        ParameterCheck.mandatoryString("The encoding", encoding);
        ParameterCheck.mandatoryString("The charset", mimetype);

        nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);

        String uniqueName = computeUniqueName(referer, candidateName);

        final NodeRef referedRef = createHiddenNode(referer, uniqueName);

        QName propContent = UtilsCircabc.getPropContent(nodeService.getType(referedRef));

        final ContentWriter writer = contentService.getWriter(referedRef, propContent, true);

        writer.setMimetype(mimetype);
        writer.setEncoding(encoding);
        if (referedFile != null) {
            writer.putContent(referedFile);
        } else {
            writer.putContent(referedIs);
        }

        return referedRef;
    }

    /**
     * @param referer
     * @param candidateName
     * @return
     */
    private String computeUniqueName(final NodeRef referer, String candidateName) {
        String uniqueName = null;

        for (int tries = 0; uniqueName == null; ++tries) {
            if (nodeService.getChildByName(referer, DocumentModel.ASSOC_HIDDEN_REFERENCES, candidateName)
                    != null) {
                final int idx = candidateName.lastIndexOf('.');

                if (idx < 0) {
                    uniqueName = candidateName + '(' + tries + ')';
                } else {
                    uniqueName =
                            candidateName.substring(0, idx) + '(' + tries + ')' + candidateName.substring(idx);
                }
            } else if (uniqueName == null) {
                uniqueName = candidateName;
            }
        }

        if (logger.isDebugEnabled() && !uniqueName.endsWith(candidateName)) {
            logger.debug(
                    "Another file found with name "
                            + candidateName
                            + ". The file will be created with name "
                            + uniqueName);
        }
        return uniqueName;
    }

    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public final void setContentService(final ContentService contentService) {
        this.contentService = contentService;
    }
}
