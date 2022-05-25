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
package eu.cec.digit.circabc.web.wai.dialog.content.edit;

import eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Bean responsible for the edit document online dialog
 *
 * @author Yanick Pignot
 */
public class EditOnlineDocumentDialog extends CreateContentBaseDialog {

    public static final String BEAN_NAME = "EditOnlineDocumentDialog";
    public static final String MSG_ERROR_UPDATE = "error_update";
    private static final String DIALOG_NAME = "editDocumentInlineWai";
    private static final long serialVersionUID = -194979676833262094L;
    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(EditOnlineDocumentDialog.class);
    private CociContentBusinessSrv cociContentBusinessSrv = null;
    private NodeRef workingCopyNodeRef = null;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setDefaultMode(calculateDefaultMode());
        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id is a mandatory parameter");
        }

        NodeRef documentNodeRef = getActionNode().getNodeRef();

        // Get it, if this is a already a working copy. If not, create one.
        if (getNodeService().hasAspect(documentNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
            workingCopyNodeRef = documentNodeRef;
        } else {
            workingCopyNodeRef = cociContentBusinessSrv.checkOut(documentNodeRef);
        }
    }

    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        final String selectedContent = getSelectedContent();
        if (workingCopyNodeRef != null && selectedContent != null) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Trying to update content node Id: " + workingCopyNodeRef.getId());
                }

                // get an updating writer that we can use to modify the content on the current node
                ContentWriter writer = getContentService()
                        .getWriter(workingCopyNodeRef, ContentModel.PROP_CONTENT, true);
                writer.putContent(selectedContent);

                attachFiles(workingCopyNodeRef);

                // Checkin when saving edition
                cociContentBusinessSrv
                        .checkIn(workingCopyNodeRef, true, translate("edit_inline_action_title"), false);
            } catch (Throwable err) {
                Utils.addErrorMessage(translate(MSG_ERROR_UPDATE) + err.getMessage());
                outcome = null;
            }
        } else {
            logger.warn("WARNING: editInlineOK called without a current Document!");
        }

        return outcome;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.dialog.content.edit.CreateContentBaseDialog#cancel()
     */
    @Override
    public String cancel() {

        // Cancel checkout when cancelling edition
        cociContentBusinessSrv.cancelCheckOut(workingCopyNodeRef);

        return super.cancel();
    }

    @Override
    protected String getDialogName() {
        return DIALOG_NAME;
    }

    @Override
    public boolean isAttachementAllowed() {
        return true;
    }

    @Override
    protected NodeRef getEditablePost() {
        // Work on the working copy
        return workingCopyNodeRef;
    }


    private String calculateDefaultMode() {
        // retrieve the content reader for this node
        final ContentReader reader = getContentService()
                .getReader(getActionNode().getNodeRef(), ContentModel.PROP_CONTENT);

        final String mimetype = reader.getMimetype();

        // calculate which editor screen to display
        if (MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(mimetype) ||
                MimetypeMap.MIMETYPE_XML.equals(mimetype) ||
                MimetypeMap.MIMETYPE_TEXT_CSS.equals(mimetype) ||
                MimetypeMap.MIMETYPE_JAVASCRIPT.equals(mimetype)) {
            return MODE_TEXT;
        } else {
            return MODE_HTML;
        }
    }

    public String getBrowserTitle() {
        return translate("edit_inline_action_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("edit_inline_action_dialog_icon_tooltip");
    }

    /**
     * @param cociContentBusinessSrv the cociContentBusinessSrv to set
     */
    public void setCociContentBusinessSrv(
            CociContentBusinessSrv cociContentBusinessSrv) {
        this.cociContentBusinessSrv = cociContentBusinessSrv;
    }
}
