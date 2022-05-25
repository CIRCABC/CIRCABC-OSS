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
package eu.cec.digit.circabc.web.wai.dialog.permissions;

import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Map;

import static eu.cec.digit.circabc.web.PermissionUtils.*;

/**
 * Bean that backs edit user permissions dialog (for any kind of node).
 *
 * @author Yanick Pignot
 */
public class RemovePermissionDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "RemovePermissionDialog";
    protected final static String MSG_REMOVE_PAGE_TITLE_USER = "remove_space_permission_page_title_user";
    protected final static String MSG_REMOVE_PAGE_TITLE_PROF = "remove_space_permission_page_title_access_profile";
    protected final static String MSG_REMOVE_PAGE_SUBTITLE_USER = "remove_space_permission_page_subtitle_user";
    protected final static String MSG_REMOVE_PAGE_SUBTITLE_PROF = "remove_space_permission_page_subtitle_access_profile";
    protected final static String MSG_REMOVE_PAGE_CONTENT_USER = "remove_space_permission_page_content_user";
    protected final static String MSG_REMOVE_PAGE_CONTENT_PROF = "remove_space_permission_page_content_access_profile";
    private static final long serialVersionUID = 777770868117140631L;
    private static final Log logger = LogFactory.getLog(RemovePermissionDialog.class);
    private static final String ERROR_DELETE = "error_remove_user";

    private String authority = null;
    private String authType = null;
    private String displayName = null;

    private SimpleCache<Serializable, Object> cache = null;

    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            authority = parameters.get(KEY_AUTHORITY);
            authType = parameters.get(KEY_AUTHORITY_TYPE);
            displayName = parameters.get(KEY_DISPLAY_NAME);

            if (getActionNode() == null) {
                throw new IllegalArgumentException("The node id is a mandatory parameter");
            }

            if (authority == null || authType == null || displayName == null) {
                throw new IllegalArgumentException(
                        "authority, authType, and displayName are mandatory parameters");
            }
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        try {

            if (authority != null) {
                // clear permissions for the specified Authority
                this.getPermissionService().clearPermission(getActionNode().getNodeRef(), authority);
            }

            PermissionUtils.resetCache(cache, logger, PermissionUtils.CIRCABC_CACHE_NAME);

            if (logger.isDebugEnabled()) {
                logger.debug("Permissions successfully removed to " + displayName + " in the space "
                        + getActionNode().getPath());
            }

        } catch (Exception e) {
            outcome = null;

            Utils.addErrorMessage(translate(ERROR_DELETE, e.getMessage()), e);
        }

        return outcome;
    }

    @Override
    public String getContainerTitle() {
        if (AUTH_TYPE_VALUE_GROUP.equals(authType)) {
            return translate(MSG_REMOVE_PAGE_TITLE_PROF, displayName);
        } else {
            return translate(MSG_REMOVE_PAGE_TITLE_PROF, displayName);
        }
    }

    @Override
    public String getContainerDescription() {
        if (AUTH_TYPE_VALUE_GROUP.equals(authType)) {
            return translate(MSG_REMOVE_PAGE_SUBTITLE_PROF);
        } else {
            return translate(MSG_REMOVE_PAGE_SUBTITLE_USER);
        }
    }

    /**
     * Get the remove user/access profile translated content
     **/
    public String getConfirmationMessage() {
        if (AUTH_TYPE_VALUE_GROUP.equals(authType)) {
            return translate(MSG_REMOVE_PAGE_CONTENT_PROF, displayName);
        } else {
            return translate(MSG_REMOVE_PAGE_CONTENT_USER, displayName);
        }
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("no");
    }

    @Override
    public String getFinishButtonLabel() {
        return translate("yes");
    }

    public String getBrowserTitle() {
        return translate("remove_space_permission_page_content_user_browser_title");
    }

    public String getPageIconAltText() {
        return translate("remove_space_permission_page_content_user_icon_tooltip");
    }

    /**
     * @param cache the cache to set
     */
    public void setCache(SimpleCache<Serializable, Object> cache) {
        this.cache = cache;
    }
}
