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
package eu.cec.digit.circabc.web.wai.wizard.users;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.ProfileUtils;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;

/**
 * @author Yanick Pignot
 * @creator Clinckart Stephane
 */
public class ChangeCircabcAccessRightWizard extends InviteCircabcUsersWizard {

    /**
     * Index of the ROLES search filter index
     */
    public static final int IGROOT_INVITED_USERS_IDX = 0;
    /**
     * Index of the PROFILES search filter index
     */
    public static final int PROFILES_FILTER_IDX = 1;
    protected static final String MSG_PROFILES = "profile";
    protected static final String MSG_USERS = "user";
    private static final long serialVersionUID = 3196287985384763157L;
    private static final Log logger = LogFactory.getLog(ChangeCircabcAccessRightWizard.class);
    private static final String PERMISSION_REGEX = "<PERMISSION>";
    private static final String PERMISSION_REGEX_TINYMCE = "[PERMISSION]";
    private static final String KEY_PERMISSION = "permission";


    @Override
    public SelectItem[] getFilters() {
        final ResourceBundle bundle = Application.getBundle(FacesContext
                .getCurrentInstance());

        return new SelectItem[]
                {
                        new SelectItem("" + IGROOT_INVITED_USERS_IDX, bundle.getString(MSG_USERS))
                        // TODO allow the invitation of profile at the Library Space level.
                        , new SelectItem("" + PROFILES_FILTER_IDX, bundle.getString(MSG_PROFILES))
                };
    }

    public List<SelectItem> getPermissions() {
        return PermissionUtils.getPermissions(getActionNode(), logger);
    }


    @Override
    public SelectItem[] pickerCallback(final int filterIndex, final String contains) {
        List<SortableSelectItem> result = null;

        final List<Map> authorities = PermissionUtils
                .getInterestGroupAuthorities(getActionNode().getNodeRef());
        final List<String> alreadyInvitedProfiles = buildInvitedList(authorities);

        if (filterIndex == PROFILES_FILTER_IDX) {
            result = ProfileUtils
                    .buildAllProfileItems(getActionNode(), contains, alreadyInvitedProfiles, logger);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "The Profile search is performed successfully and return " + result + ". Filter Index: "
                                + filterIndex + ". Expression: " + contains + ".");
            }

        } else if (filterIndex == IGROOT_INVITED_USERS_IDX) {
            result = PermissionUtils
                    .buildInvitedUserItems(getActionNode(), contains, false, alreadyInvitedProfiles, logger);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "The User invited in the current IG search is performed successfully and return "
                                + result + ". Filter Index: " + filterIndex + ". Expression: " + contains + ".");
            }

        } else {
            logger.error("The picker is called with an invalid index parameter " + filterIndex
                    + ". This last is not taken in account yet.");

            result = Collections.emptyList();
        }
        return result.toArray(new SelectItem[result.size()]);
    }

    private List<String> buildInvitedList(final List<Map> cachedPersonNodes) {
        final List<String> invitedList = new ArrayList<>(cachedPersonNodes.size());

        for (final Map userMap : cachedPersonNodes) {
            if (userMap.get("authType").equals("group")) {
                invitedList.add((String) userMap.get("fullName"));
            } else {
                invitedList.add((String) userMap.get("userName"));
            }
        }

        return invitedList;
    }

    @Override
    public List<SortableSelectItem> getProfiles() {
        final Set<LibraryPermissions> permissions = LibraryPermissions.getPermissions();

        final List<SortableSelectItem> permAsList = new ArrayList<>(permissions.size());

        String permAsString;
        for (final LibraryPermissions perm : permissions) {
            permAsString = perm.toString();
            permAsList.add(new SortableSelectItem(permAsString, permAsString, permAsString));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("The permissions found are " + permAsList);
        }

        return permAsList;
    }

    @Override
    protected MailTemplate getMailTemplateDefinition() {
        return MailTemplate.CHANGE_PERMISSION;

    }

    @Override
    protected Map<String, Object> getDisplayModelToAdd() {
        return Collections.<String, Object>singletonMap(KEY_PERMISSION, PERMISSION_REGEX);
    }

    @Override
    protected Map<String, String> buildCustomParams(NodeRef person,
                                                    String profile, NodeRef newNodeRef) {

        final Map<QName, Serializable> personProperties = getNodeService().getProperties(person);
        final Map<String, String> params = new HashMap<>(3);

        final String login = PermissionUtils.computeUserLogin(personProperties);

        params.put(FIRSTNAME_REGEX_TINYMCE, (String) personProperties.get(ContentModel.PROP_FIRSTNAME));
        params.put(LASTNAME_REGEX_TINYMCE, (String) personProperties.get(ContentModel.PROP_LASTNAME));
        params
                .put(ECASUNAME_REGEX_TINYMCE, (String) personProperties.get(UserModel.PROP_ECAS_USER_NAME));
        params.put(USERNAME_REGEX_TINYMCE, login);
        params.put(PERMISSION_REGEX_TINYMCE, profile);
        CircabcConfiguration.addApplicationNameToParams(params);

        Node createdNode = new Node(newNodeRef);
        params.put("[NODE_BEST_TITLE]", getBestTitle(createdNode));
        params.put("[NODE_REF]",
                WebClientHelper.getGeneratedWaiFullUrl(createdNode, ExtendedURLMode.HTTP_WAI_BROWSE));

        return params;
    }

    @Override
    public String getBuildTextMessage() {

        String result = "";

        final NodeRef person = getTemplatePerson();
        final MailWrapper mail = getMailPreferencesService()
                .getDefaultMailTemplate(getActionNode().getNodeRef(), getMailTemplateDefinition());
        final Map<String, Object> model = getMailPreferencesService()
                .buildDefaultModel(getActionNode().getNodeRef(), person, null);

        model.putAll(getDisplayModelToAdd());
        model.put("permission", "<PERMISSION>");

        String htmlBody = mail.getBody(model);
        htmlBody = replaceTinyMceTags(htmlBody);
        result = cleanBody(htmlBody);
        setBody(result);
        setSubject(I18NUtil.getMessage("change_permission_group_user_template_mail_subject"));

        return "true";
    }

    protected String replaceTinyMceTags(String htmBody) {
        String body = htmBody.replace(FIRSTNAME_REGEX, FIRSTNAME_REGEX_TINYMCE);
        body = body.replace(LASTNAME_REGEX, LASTNAME_REGEX_TINYMCE);
        if (CircabcConfig.ENT) {
            body = body.replace(ECASUNAME_REGEX, ECASUNAME_REGEX_TINYMCE);
        } else {
            body = body.replace(USERNAME_REGEX, USERNAME_REGEX_TINYMCE);
        }

        body = body.replace(PERMISSION_REGEX, PERMISSION_REGEX_TINYMCE);
        return body;
    }

}
