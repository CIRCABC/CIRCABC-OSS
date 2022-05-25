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
package eu.cec.digit.circabc.repo.external.repositories;

import eu.cec.digit.ecas.client.proxy.ProxyTicketHelper;
import eu.cec.digit.ecas.client.proxy.ProxyTicketHelperIntf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * A more intelligent implementation of the proxy ticket resolver that returns the ECAS proxy that
 * has to be validated for the production environment.
 *
 * @author schwerr
 */
public class ProductionProxyTicketResolver implements ProxyTicketResolver, ServletContextAware {

    private static final Log logger = LogFactory.getLog(ProductionProxyTicketResolver.class);

    private ServletContext servletContext = null;

    private String targetService = null;

    /**
     * @see eu.cec.digit.circabc.repo.external.repositories.ProxyTicketResolver#getProxyTicket()
     */
    @Override
    public String getProxyTicket() {

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            logger.error(
                    "Request context attributes "
                            + "not found. Did you add <listener><listener-class>"
                            + "org.springframework.web.context.request."
                            + "RequestContextListener</listener-class></listener> "
                            + "to web.xml?");
            return null;
        }

        return getProxyTicketIntern(
                servletContext, ((ServletRequestAttributes) attributes).getRequest());
    }

    /**
     * Retrieves the actual proxy ticket.
     */
    private String getProxyTicketIntern(ServletContext context, HttpServletRequest request) {

        try {
            ProxyTicketHelperIntf proxyTicketHelper = ProxyTicketHelper.getInstance(context);

            return proxyTicketHelper.getProxyTicket(request, targetService);
        } catch (Exception e) {
            logger.error("Exception resolving ECAS proxy ticket.", e);
            return null;
        }
    }

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Sets the value of the targetService
     *
     * @param targetService the targetService to set.
     */
    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }
}
