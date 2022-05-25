/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.forums;

import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.util.Map;

/**
 * @author beaurpi
 *
 */
public class SwitchForumModerationDialog extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = 7380614912705665301L;

    private static final String SWITCH_MODERATION_STATUS_ENABLED = "enabled";
    private static final String SWITCH_MODERATION_STATUS_DISABLED = "disabled";
    private static final String SWITCH_MODERATION_ACTION_ACCEPT = "accept";
    private static final String SWITCH_MODERATION_ACTION_REFUSE = "refuse";
    private static final String SWITCH_MODERATION_ACTION_REFUSE_MESSAGE = "This post has been automatically refused, because this forum is not anymore moderated";

    private static final String LUCENE_PATH_PREFIX = "(PATH:\"";
    private static final String LUCENE_AND_TYPE_PREFIX = " AND (TYPE:\"";
    private static final String LUCENE_OR_TYPE_PREFIX = " OR TYPE:\"";
    private static final String LUCENE_END_PATH_PREFIX = "\"";
    private static final String LUCENE_STAR_PATH_PREFIX = "/*\")";

    private Boolean statusBoolean;
    private String moderationAction;
    private String moderationStatus;

    private ModerationService moderationService;

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getPageIconAltText()
     */
    @Override
    public String getPageIconAltText() {

        return translate("switch_forum_moderation_dialog_description");
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getBrowserTitle()
     */
    @Override
    public String getBrowserTitle() {

        return translate("switch_forum_moderation_dialog_title");
    }

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        statusBoolean = false;
        if (this.getNodeService()
                .getProperty(this.getActionNode().getNodeRef(), ModerationModel.PROP_IS_MODERATED)
                != null) {
            statusBoolean = Boolean.parseBoolean(this.getNodeService()
                    .getProperty(this.getActionNode().getNodeRef(), ModerationModel.PROP_IS_MODERATED)
                    .toString());
        }

        moderationAction = SwitchForumModerationDialog.SWITCH_MODERATION_ACTION_REFUSE;
        moderationStatus = SWITCH_MODERATION_STATUS_DISABLED;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        if (statusBoolean && moderationStatus.equals(SWITCH_MODERATION_STATUS_DISABLED)) {

            moderationService.stopModeration(this.getActionNode().getNodeRef(), moderationAction);

            //updateChildren();
            //this.getNodeService().setProperty(this.getActionNode().getNodeRef(), ModerationModel.PROP_IS_MODERATED, false);

        } else if (!statusBoolean && moderationStatus.equals(SWITCH_MODERATION_STATUS_ENABLED)) {
            moderationService.applyModeration(this.getActionNode().getNodeRef(), false);
            //updateChildren();
            //this.getNodeService().setProperty(this.getActionNode().getNodeRef(), ModerationModel.PROP_IS_MODERATED, true);
        }

        return outcome;
    }


    public void statusValueChanged(ValueChangeEvent event) {
        System.out.println("old = " + event.getOldValue());
        System.out.println("new = " + event.getNewValue());
    }

    /**
     * @return the statusBoolean
     */
    public Boolean getStatusBoolean() {
        return statusBoolean;
    }

    /**
     * @param statusBoolean the statusBoolean to set
     */
    public void setStatusBoolean(Boolean statusBoolean) {
        this.statusBoolean = statusBoolean;
    }

    /**
     * @return the moderationAction
     */
    public String getModerationAction() {
        return moderationAction;
    }

    /**
     * @param moderationAction the moderationAction to set
     */
    public void setModerationAction(String moderationAction) {
        this.moderationAction = moderationAction;
    }

    /**
     * @return the moderationStatus
     */
    public String getModerationStatus() {
        return moderationStatus;
    }

    /**
     * @param moderationStatus the moderationStatus to set
     */
    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    /**
     * @return the moderationService
     */
    public ModerationService getModerationService() {
        return moderationService;
    }

    /**
     * @param moderationService the moderationService to set
     */
    public void setModerationService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }
}
