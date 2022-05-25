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
package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.user.UserCategoryMembershipRecord;
import eu.cec.digit.circabc.service.user.UserIGMembershipRecord;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.repository.CircabcRootNode;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bean that backs the navigation inside the root node of Circabc
 *
 * @author yanick pignot
 */
public class WelcomeBean extends BaseWaiNavigator {

    public static final String JSP_NAME = "circabc-home.jsp";
    public static final String BEAN_NAME = "WelcomeBean";
    public static final String MSG_PAGE_TITLE = "welcome_title";
    public static final String MSG_PAGE_DESCRIPTION = "welcome_title_desc";
    public static final String MSG_BROWSER_TITLE = "title_welcome";
    private static final String GUEST = "guest";
    /**
     *
     */
    private static final long serialVersionUID = -6967164595499663893L;
    private static String helpLink;
    private static String eLearningLink;

    // membeers for

    private UserService userService;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.CIRCABC_ROOT;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION);
    }

    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE, CircabcConfiguration.getApplicationName());
    }

    public void init(final Map<String, String> parameters) {
        // Ensure that the mlText interceptor is running to avoid a classcast exception
        // Bug fix: DIGIT-CIRCABC-658
        MLPropertyInterceptor.setMLAware(false);
    }

    public List<NavigableNode> getCategoryHeaders() {
        final CircabcRootNode circabcNode = getNavigator().getCircabcHomeNode();

        if (circabcNode != null && circabcNode.getNavigableChilds() != null) {
            return circabcNode.getNavigableChilds();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getBrowserTitle() {
        return translate(MSG_BROWSER_TITLE);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public boolean isRegistered() {
        final String userName = getNavigator().getCurrentAlfrescoUserName();
        return ((userName != null && !userName.equalsIgnoreCase(GUEST)));
    }


    public List<UserIGMembershipRecord> getIgRoles() {

        final String userName = getNavigator().getCurrentAlfrescoUserName();
        if (userName != null && !userName.equalsIgnoreCase(GUEST)) {
            return getUserService().getInterestGroups(userName);

        } else {
            return Collections.emptyList();
        }

    }


    public List<UserCategoryMembershipRecord> getCategoryRoles() {
        final String userName = getNavigator().getCurrentAlfrescoUserName();
        if (userName != null && !userName.equalsIgnoreCase(GUEST)) {
            return getUserService().getCategories(userName);
        } else {
            return Collections.emptyList();
        }
    }

    public String getHelpLink() {

        NodeRef rootRef = Beans.getWaiNavigator().getCircabcHomeNode().getNodeRef();

        if (this.getNodeService().getProperty(rootRef, CircabcModel.PROP_HELP_LINK) != null) {
            WelcomeBean.helpLink = this.getNodeService().getProperty(rootRef, CircabcModel.PROP_HELP_LINK)
                    .toString();
        }

        return WelcomeBean.helpLink;
    }

    /**
     * @return the eLearningLink
     */
    public String geteLearningLink() {

        NodeRef rootRef = Beans.getWaiNavigator().getCircabcHomeNode().getNodeRef();

        if (this.getNodeService().getProperty(rootRef, CircabcModel.PROP_ELEARNING_LINK) != null) {
            WelcomeBean.eLearningLink = this.getNodeService()
                    .getProperty(rootRef, CircabcModel.PROP_ELEARNING_LINK).toString();
        }

        return WelcomeBean.eLearningLink;
    }


}
