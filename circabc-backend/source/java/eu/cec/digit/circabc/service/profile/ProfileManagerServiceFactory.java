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
package eu.cec.digit.circabc.service.profile;

import eu.cec.digit.circabc.model.CircabcModel;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.util.Set;

/**
 * @author Clinckart Stephane
 */
public class ProfileManagerServiceFactory implements BeanFactoryAware {

    /**
     * The logger
     */
    private static final Log logger = LogFactory.getLog(ProfileManagerServiceFactory.class);
    private static final String profileManagerServiceFactory = "profileManagerServiceFactory";
    private static final String profileManagerServiceFactorySecure = "ProfileManagerServiceFactory";
    private boolean secure;
    /**
     * IOC Params
     */
    private BeanFactory factory = null;

    private NodeService nodeService;

    /**
     * Getter that help to get the BeanName This should be used only if IOC injection is not possible
     *
     * @return the name of the FactoryBean
     */
    public static String getBeanName(boolean secure) {
        if (secure) {
            return profileManagerServiceFactorySecure;
        } else {
            return profileManagerServiceFactory;
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.factory = beanFactory;
    }

    /**
     * Depending the Aspect applied to the NodeRef, the factory will return the appropriate instance
     * of ProfileManagerService
     */
    public ProfileManagerService getProfileManagerService(NodeRef nodeRef) {
        if (nodeRef == null) {
            throw new NullPointerException("NodeRef is a mandatory parameter");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Try to get ProfileManagerService for node :" + nodeRef);
        }

        try {
            final Set<QName> aspects = nodeService.getAspects(nodeRef);
            return getProfileManagerService(aspects);
        } catch (InvalidNodeRefException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("InvalidNodeRefException raised for Node :" + nodeRef);
            }
        } catch (Throwable ex) {
            if (logger.isErrorEnabled()) {
                logger.error(ex.getClass() + " raised for NodeRef :" + nodeRef);
            }
        }

        if (logger.isWarnEnabled()) {
            logger.warn("No ProfileManager found for Node :" + nodeRef);
        }

        return null;
    }

    @Deprecated // should be moved in the web client part
    public ProfileManagerService getProfileManagerService(Node node) {
        if (logger.isTraceEnabled()) {
            logger.trace("Try to get ProfileManagerService for node :" + node);
        }
        try {
            return getProfileManagerService(node.getAspects());
        } catch (InvalidNodeRefException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("InvalidNodeRefException raised for Node :" + node);
            }
        } catch (Throwable ex) {
            if (logger.isErrorEnabled()) {
                logger.error(ex.getClass() + " raised for NodeRef :" + node);
            }
        }

        if (logger.isWarnEnabled()) {
            logger.warn("No ProfileManager found for Node :" + node);
        }

        return null;
    }

    /**
     * Depending the Aspect applied to the NodeRef, the factory will return the appropriate instance
     * of ProfileManagerService
     */
    private ProfileManagerService getProfileManagerService(final Set<QName> aspects) {

        if (aspects.contains(CircabcModel.ASPECT_LIBRARY)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Node has Library Root Aspect");
            }

            return getLibraryProfileManagerService();
        }

        if (aspects.contains(CircabcModel.ASPECT_NEWSGROUP)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Node has News Group Aspect");
            }

            return getNewsGroupProfileManagerService();
        }

        if (aspects.contains(CircabcModel.ASPECT_INFORMATION)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Node has Information Aspect");
            }

            return getInformationProfileManagerService();
        }

        if (aspects.contains(CircabcModel.ASPECT_EVENT)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Node has Event Aspect");
            }

            return getEventProfileManagerService();
        }

        if (aspects.contains(CircabcModel.ASPECT_SURVEY)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Node has Survey Aspect");
            }

            return getSurveyProfileManagerService();
        }

        if (aspects.contains(CircabcModel.ASPECT_IGROOT)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Node has  IGRoot Aspect");
            }

            return getIGRootProfileManagerService();
        }

        if (aspects.contains(CircabcModel.ASPECT_CATEGORY)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Node has Category Aspect");
            }

            return getCategoryProfileManagerService();
        }

        if (aspects.contains(CircabcModel.ASPECT_CIRCABC_ROOT)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Node has CircaBC Aspect");
            }

            return getCircabcRootProfileManagerService();
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Node has no Circabc Aspect");
        }

        return null;
    }

    public CircabcRootProfileManagerService getCircabcRootProfileManagerService() {
        if (secure) {
            return (CircabcRootProfileManagerService)
                    this.factory.getBean(CircabcRootProfileManagerService.PROXIED_SERVICE_NAME);
        } else {
            return (CircabcRootProfileManagerService)
                    this.factory.getBean(CircabcRootProfileManagerService.SERVICE_NAME);
        }
    }

    public CategoryProfileManagerService getCategoryProfileManagerService() {
        // This is an hack to autorize the AlfrescoAdmin that has no permission
        // By this way AlfrescoAdmin can access to categoryProfileManagerService (not secured)
        // boolean isAdmin = authorityService.hasAdminAuthority();

        if (secure) {
            return (CategoryProfileManagerService)
                    this.factory.getBean(CategoryProfileManagerService.PROXIED_SERVICE_NAME);
        } else {
            return (CategoryProfileManagerService)
                    this.factory.getBean(CategoryProfileManagerService.SERVICE_NAME);
        }
    }

    public IGRootProfileManagerService getIGRootProfileManagerService() {
        if (secure) {
            return (IGRootProfileManagerService)
                    this.factory.getBean(IGRootProfileManagerService.PROXIED_SERVICE_NAME);
        } else {
            return (IGRootProfileManagerService)
                    this.factory.getBean(IGRootProfileManagerService.SERVICE_NAME);
        }
    }

    public LibraryProfileManagerService getLibraryProfileManagerService() {
        if (secure) {
            return (LibraryProfileManagerService)
                    this.factory.getBean(LibraryProfileManagerService.PROXIED_SERVICE_NAME);
        } else {
            return (LibraryProfileManagerService)
                    this.factory.getBean(LibraryProfileManagerService.SERVICE_NAME);
        }
    }

    public NewsGroupProfileManagerService getNewsGroupProfileManagerService() {
        if (secure) {
            return (NewsGroupProfileManagerService)
                    this.factory.getBean(NewsGroupProfileManagerService.PROXIED_SERVICE_NAME);
        } else {
            return (NewsGroupProfileManagerService)
                    this.factory.getBean(NewsGroupProfileManagerService.SERVICE_NAME);
        }
    }

    public SurveyProfileManagerService getSurveyProfileManagerService() {
        if (secure) {
            return (SurveyProfileManagerService)
                    this.factory.getBean(SurveyProfileManagerService.PROXIED_SERVICE_NAME);
        } else {
            return (SurveyProfileManagerService)
                    this.factory.getBean(SurveyProfileManagerService.SERVICE_NAME);
        }
    }

    public InformationProfileManagerService getInformationProfileManagerService() {
        if (secure) {
            return (InformationProfileManagerService)
                    this.factory.getBean(InformationProfileManagerService.PROXIED_SERVICE_NAME);
        } else {
            return (InformationProfileManagerService)
                    this.factory.getBean(InformationProfileManagerService.SERVICE_NAME);
        }
    }

    /**
     * IOC
     */

    public EventProfileManagerService getEventProfileManagerService() {
        if (secure) {
            return (EventProfileManagerService)
                    this.factory.getBean(EventProfileManagerService.PROXIED_SERVICE_NAME);
        } else {
            return (EventProfileManagerService)
                    this.factory.getBean(EventProfileManagerService.SERVICE_NAME);
        }
    }

    /**
     * Setter method for nodeService
     *
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Setter method for secure This help to chose if ProfileManeger has to bo proxied or not.
     *
     * @param secure the use proxy or not
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
