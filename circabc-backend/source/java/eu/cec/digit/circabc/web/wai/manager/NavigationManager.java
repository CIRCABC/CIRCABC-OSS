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
package eu.cec.digit.circabc.web.wai.manager;

import eu.cec.digit.circabc.repo.app.SecurityService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import eu.cec.digit.circabc.web.wai.bean.navigation.*;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Bean that manage the navigation beans. These beans must be an instance of WaiNavigator
 *
 * @author yanick pignot
 * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator
 */
public class NavigationManager implements Serializable {

    public static final String BEAN_NAME = "WaiNavigationManager";
    private static final long serialVersionUID = -7002395459540564631L;
    private static final Log logger = LogFactory.getLog(NavigationManager.class);

    //private WaiNavigator currentBean;
    private NavigationState state;

    private List<Node> navigationList = null;

    private String renderPropertyNameFromBean;

    private transient SecurityService securityService;

    public static boolean areStatesEquals(final NavigationState state, final Object obj) {
        if (state == null) {
            return false;
        } else {
            return state.equals(obj);
        }
    }

    /**
     * Init the current navigation.
     *
     * @param navigationBeanName the bean name that backs the navigation.
     */
    public void initNavigation(final String navigationBeanName) {
        if (navigationBeanName == null || navigationBeanName.length() < 1) {
            throw new IllegalArgumentException("The navigation bean name is a mandatory parameter.");
        }

        // set the bean that will back the navigation
        final WaiNavigator currentBean = Beans.getWaiBrosableBean(navigationBeanName);
        final Node currentNode = Beans.getWaiNavigator().getCurrentNode();
        if (logger.isDebugEnabled()) {
            logger.debug("currentBean:" + currentBean + "\ncurrentNode:" + currentNode);
        }

        state = new NavigationState(currentBean, currentNode);
        navigationList = null;

        if (currentBean instanceof LibraryBean) {
            renderPropertyNameFromBean = ((LibraryBean) currentBean)
                    .getRenderPropertyNameNavigationPreference();
        } else if (currentBean instanceof NewsGroupBean) {
            renderPropertyNameFromBean = ((NewsGroupBean) currentBean)
                    .getRenderPropertyNameNavigationPreference();
        } else if (currentBean instanceof InformationBean) {
            renderPropertyNameFromBean = ((InformationBean) currentBean)
                    .getRenderPropertyNameNavigationPreference();
        } else {
            renderPropertyNameFromBean = null;
        }

        initNavigationList();

        if (currentBean == null) {
            throw new IllegalArgumentException(
                    "The navigation manager can't handle this navigation. The WaiNavigator "
                            + navigationBeanName + " must be declared as a managed bean.");
        }
    }

    public void setParamsToApply(final Map<String, String> parameters) {
        state.getBean().setParamsToApply(parameters);
    }

    /**
     * @return the current navigation bean
     */
    public WaiNavigator getBean() {
        return getState().getBean();
    }


    public boolean isIconVisible() {
        return getState().getBean().getPageIcon() != null;
    }

    public boolean isNavigationVisible() {
        return getNavigation() != null;
    }

    /**
     * @return the current jsp Page that handle this navigation
     */
    public String getPage() {
        if (logger.isInfoEnabled()) {
            logger.info("jsp page: " + getState().getBean().getRelatedJsp());
        }
        return getState().getBean().getRelatedJsp();
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator#getPageDescription()
     */
    public String getDescription() {
        return getSecurityService().getCleanHTML(getState().getBean().getPageDescription(), true);
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator#getPageIcon()
     */
    public String getIcon() {
        return getState().getBean().getPageIcon();
    }


    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator#getPageIconAltText()
     */
    public String getIconAlt() {
        return getState().getBean().getPageIconAltText();
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator#getPageTitle()
     */
    public String getTitle() {
        return getSecurityService().getCleanHTML(getState().getBean().getPageTitle(), false);
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator#getHttpUrl()
     */
    public String getHttpUrl() {
        return getState().getBean().getHttpUrl();
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator#getPreviousHttpUrl()
     */
    public String getPreviousHttpUrl() {
        return getState().getBean().getPreviousHttpUrl();
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator#getWebdavUrl()
     */
    public String getWebdavUrl() {
        return getState().getBean().getWebdavUrl();
    }


    /**
     * @return
     */
    public List<Node> getNavigation() {
        return navigationList;
    }

    public String getBrowserTitle() {
        return getState().getBean().getBrowserTitle();
    }

    public NavigationState getState() {
        if (state == null) {
            // the user has probably called the wai browse page directly via an url.

            if (Beans.getWaiNavigator().getCurrentNodeType() == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Redirect to the welcome page");
                }
                // the user come from an non circabc node. Or it is its first navigation request.
                initNavigation(WelcomeBean.BEAN_NAME);
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Redirect to the latest requested page:" + Beans.getWaiNavigator()
                            .getCurrentNodeType().getBeanName());
                }

                // return to the last requested page
                initNavigation(Beans.getWaiNavigator().getCurrentNodeType().getBeanName());
            }

        }
        return this.state;
    }

    public void restoreState(final NavigationState state) {
        this.state = state;

        if (!state.getNode().getId().equals(Beans.getWaiNavigator().getCurrentNodeId())) {
            Beans.getWaiNavigator().setCurrentNodeId(state.getNode().getId());

            // add the notification because the space is changed !!!
            UIContextService.getInstance(FacesContext.getCurrentInstance()).spaceChanged();
        }

        getBean().restored();
    }

    @Override
    public String toString() {
        return "The navigation being managed: \n\tPage: " + getPage() + "\n\tBean: " + getState()
                .getBean().getManagedNodeType().getBeanName() + "\n\tNode Type: " + getBean()
                .getManagedNodeType().getComparatorQName();
    }

    private void initNavigationList() {
        //	we don't need navigation list on IGroot or above
        if (Beans.getWaiNavigator().getCurrentIGService() != null) {
            navigationList = new LinkedList<>();

            final NavigableNodeType type2 = getState().getBean().getManagedNodeType();
            final NavigableNodeType type = type2.getRelatedIgService();

            if (type != null) {
                if (NavigableNodeType.DIRECTORY.equals(type)) {
                    navigationList.add(Beans.getWaiNavigator().getCurrentIGService());
                } else if (NavigableNodeType.EVENT.equals(type)) {
                    navigationList.add((Node) Beans.getWaiNavigator().getCurrentIGRoot()
                            .get(InterestGroupNode.EVENT_SERVICE));
                } else {
                    final ManagementService managementService = Services
                            .getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getManagementService();

                    final List<NodeRef> nav = managementService.getAllParents(
                            getState().getNode().getNodeRef(),
                            true,
                            type.getComparatorQName(),
                            true);

                    for (final NodeRef ref : nav) {
                        final Node node = new Node(ref);
                        // don't add the Post name.
                        if (node.getType().equals(ForumModel.TYPE_POST) == false) {
                            ((LinkedList<Node>) navigationList).addLast(node);
                        }
                    }

                    if (NavigableNodeType.NEWSGROUP_POST.equals(type) || NavigableNodeType.LIBRARY_POST
                            .equals(type)) {
                        navigationList.remove(navigationList.size() - 1);
                    }
                }
            }
        } else if (Beans.getWaiNavigator().getCurrentIGRoot() != null) {
            navigationList = new LinkedList<>();
            navigationList.add(null);
        }
    }

    /**
     * @return the renderPropertyNameFromBean
     */
    public String getRenderPropertyNameFromBean() {
        return renderPropertyNameFromBean;
    }

    /**
     * @param renderPropertyNameFromBean the renderPropertyNameFromBean to set
     */
    public void setRenderPropertyNameFromBean(String renderPropertyNameFromBean) {
        this.renderPropertyNameFromBean = renderPropertyNameFromBean;
    }

    public SecurityService getSecurityService() {
        if (securityService == null) {
            securityService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getSecurityService();
        }
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
