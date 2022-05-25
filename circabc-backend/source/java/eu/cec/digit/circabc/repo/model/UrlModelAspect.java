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
package eu.cec.digit.circabc.repo.model;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import static eu.cec.digit.circabc.model.DocumentModel.ASPECT_URLABLE;
import static eu.cec.digit.circabc.model.DocumentModel.PROP_URL;

/**
 * URL model aspect behaviour. You should synchronize the content of the URL with its url property.
 *
 * @author David Ferraz
 */
public class UrlModelAspect
        implements ContentServicePolicies.OnContentUpdatePolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy {

    /**
     * content of the HTML container of an URL
     */
    public static final String URL_FIXED_CONTENT =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"
                    + "<html> <head> <meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\">"
                    + "<meta http-equiv=\"refresh\" content=\"0;url={0}\"></head><body></body></html>";

    private static final Log logger = LogFactory.getLog(UrlModelAspect.class);

    /**
     * The policy component
     */
    private PolicyComponent policyComponent;

    private BehaviourFilter policyBehaviourFilter;
    private ContentService contentService;

    /**
     * Spring initialise method used to register the policy behaviours
     */
    public void init() {
        // registers the policy behaviours
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate"),
                ASPECT_URLABLE,
                new JavaBehaviour(this, "onContentUpdate"));
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                ASPECT_URLABLE,
                new JavaBehaviour(this, "onUpdateProperties"));
    }

    /**
     * Set the policy component
     *
     * @param policyComponent the policy component
     */
    public void setPolicyComponent(final PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void onContentUpdate(final NodeRef nodeRef, final boolean newContent) {
        throw new AlfrescoRuntimeException("Impossible to update manually the content of an URL");
    }

    public void onUpdateProperties(
            final NodeRef nodeRef,
            final Map<QName, Serializable> before,
            final Map<QName, Serializable> after) {
        if (after != null && after.containsKey(PROP_URL)) {
            final Serializable newUrl = after.get(PROP_URL);
            final Serializable oldUrl = (before == null) ? null : before.get(PROP_URL);

            if (newUrl != null && !newUrl.equals(oldUrl)) {
                // sets the content to be written
                final ContentWriter writer =
                        contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

                try {
                    this.policyBehaviourFilter.disableBehaviour(nodeRef, ASPECT_URLABLE);

                    // sets the mimetype and encoding
                    writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
                    writer.setEncoding("UTF-8");
                    writer.putContent(MessageFormat.format(URL_FIXED_CONTENT, new Object[]{newUrl}));

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Updating URL description file"
                                        + "\n Old URL : "
                                        + oldUrl.toString()
                                        + "\n New URL: "
                                        + newUrl.toString());
                    }
                } finally {
                    this.policyBehaviourFilter.enableBehaviour(nodeRef, ASPECT_URLABLE);
                }
            }
        }
    }

    /**
     * @param contentService the contentService to set
     */
    public final void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @param policyBehaviourFilter the policyBehaviourFilter to set
     */
    public final void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }
}
