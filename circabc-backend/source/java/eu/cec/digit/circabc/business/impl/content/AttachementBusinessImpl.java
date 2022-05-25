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

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.BusinessValidationError;
import eu.cec.digit.circabc.business.api.content.Attachement;
import eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv;
import eu.cec.digit.circabc.business.helper.ContentManager;
import eu.cec.digit.circabc.business.helper.TemporaryFileManager;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.newsgroup.AttachementService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Business service to upload a document.
 *
 * @author Yanick Pignot
 */
public class AttachementBusinessImpl implements AttachementBusinessSrv {

    private static final String ERROR_NOT_ATTACH = "business_validation_attachement_not_attach";

    private final Log logger = LogFactory.getLog(AttachementBusinessImpl.class);

    private AttachementService attachementService;
    private NodeService nodeService;

    private ValidationManager validationManager;
    private ContentManager contentManager;
    private TemporaryFileManager temporaryFileManager;

    //--------------
    //-- public methods


    public NodeRef addTempAttachement(final String name, final File file) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateTitle(name, stack);
        stack.finish(logger);

        final NodeRef referer = temporaryFileManager.getTempRoot();
        nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);

        return contentManager.createContent(referer, name, DocumentModel.ASSOC_HIDDEN_REFERENCES,
                DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT, file, false);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv#addAttachement(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.io.File)
     */
    public NodeRef addAttachement(final NodeRef referer, final String name, final File file) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateNodeRef(referer, stack);
        validationManager.validateTitle(name, stack);
        stack.finish(logger);

        nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);

        return contentManager.createContent(referer, name, DocumentModel.ASSOC_HIDDEN_REFERENCES,
                DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT, file, false);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv#addAttachement(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.io.InputStream)
     */
    @Override
    public NodeRef addAttachement(NodeRef referer, String name, InputStream inputStream) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateNodeRef(referer, stack);
        validationManager.validateTitle(name, stack);
        stack.finish(logger);

        nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);

        return contentManager.createContent(referer, name, DocumentModel.ASSOC_HIDDEN_REFERENCES,
                DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT, inputStream, false);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv#addAttachement(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef addAttachement(final NodeRef referer, final NodeRef refered) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateNodeRef(referer, stack);
        validationManager.validateNodeRef(refered, stack);
        validationManager.validateCanRead(refered, stack);
        stack.finish(logger);

        if (temporaryFileManager.isTempFile(refered)) {
            nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);

            ChildAssociationRef assoc = nodeService
                    .moveNode(refered, referer, DocumentModel.ASSOC_HIDDEN_REFERENCES,
                            DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT);

            return assoc.getChildRef();
        } else {
            return attachementService.attach(referer, refered);
        }
    }


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv#getAttachement(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<Attachement> getAttachements(final NodeRef referer) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateNodeRef(referer, stack);
        stack.finish(logger);

        final List<Attachement> attachements = new ArrayList<>();

        for (final NodeRef refered : attachementService.getAttachements(referer)) {
            attachements.add(new AttachementImpl(referer, refered, nodeService));
        }

        return attachements;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.content.AttachementBusinessSrv#removeAttachement(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeAttachement(final NodeRef referer, final NodeRef refered) {
        final BusinessStackError stack = new BusinessStackError();
        validationManager.validateNodeRef(referer, stack);
        validationManager.validateNodeRef(refered, stack);
        validationManager.validateCanDelete(refered, stack);
        stack.finish(logger);

        if (attachementService.getAttachements(referer).contains(refered)) {
            if (attachementService.isHiddenAttachement(refered)) {
                nodeService.deleteNode(refered);
            } else {
                nodeService.removeAssociation(referer, refered, DocumentModel.ASSOC_EXTERNAL_REFERENCES);
            }
        } else {
            throw new BusinessValidationError(ERROR_NOT_ATTACH);
        }
    }

    //--------------
    //-- private helpers

    //--------------
    //-- IOC

    public final void setAttachementService(AttachementService attachementService) {
        this.attachementService = attachementService;
    }

    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public final void setValidationManager(ValidationManager validationManager) {
        this.validationManager = validationManager;
    }

    public final void setContentManager(ContentManager contentManager) {
        this.contentManager = contentManager;
    }

    /**
     * @param temporaryFileManager the temporaryFileManager to set
     */
    public final void setTemporaryFileManager(TemporaryFileManager temporaryFileManager) {
        this.temporaryFileManager = temporaryFileManager;
    }

}
