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
package eu.europa.ec.digit.circabc.rest.model;

import io.swagger.model.alfresco.DocumentModel;
import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alfresco policy behaviour bound to the {@code urlable} aspect ({@link
 * DocumentModel#ASPECT_URLABLE}).
 *
 * <p>This behaviour keeps the binary content of a URL node synchronized with its {@link
 * DocumentModel#PROP_URL} property. Whenever the URL property changes, the node's content is
 * rewritten with a small HTML document that immediately redirects the browser to the target URL.
 * Conversely, it forbids manual updates of the content of a URL node, since that content is
 * managed exclusively through the URL property.
 *
 * <p>It implements two Alfresco policies:
 *
 * <ul>
 *   <li>{@link ContentServicePolicies.OnContentUpdatePolicy} - rejects direct content updates.
 *   <li>{@link NodeServicePolicies.OnUpdatePropertiesPolicy} - regenerates the redirecting HTML
 *       content when the URL property is modified.
 * </ul>
 *
 * @author David Ferraz
 */
public class UrlModelAspect
  implements
    ContentServicePolicies.OnContentUpdatePolicy,
    NodeServicePolicies.OnUpdatePropertiesPolicy
{

  /**
   * Template for the HTML container of a URL node. It is a minimal HTML document whose
   * {@code meta refresh} tag redirects the browser to the target URL. The single placeholder
   * {@code {0}} is substituted with the actual URL via {@link MessageFormat#format(String,
   * Object...)}.
   */
  public static final String URL_FIXED_CONTENT =
    "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">" +
    "<html> <head> <meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\">" +
    "<meta http-equiv=\"refresh\" content=\"0;url={0}\"></head><body></body></html>";

  /** Alfresco component used to bind this class' methods to content and property policies. */
  @Autowired
  private PolicyComponent policyComponent;

  /**
   * Filter used to temporarily disable this behaviour while it rewrites the node content, so that
   * the content update triggered by the behaviour does not recurse back into it.
   */
  @Autowired
  private BehaviourFilter policyBehaviourFilter;

  /** Service used to obtain a {@link ContentWriter} and rewrite the node's content. */
  @Autowired
  private ContentService contentService;

  /**
   * Registers this class' policy behaviours against the {@code urlable} aspect once the Spring
   * bean has been fully initialized. Binds {@link #onContentUpdate(NodeRef, boolean)} to the
   * {@code onContentUpdate} policy and {@link #onUpdateProperties(NodeRef, Map, Map)} to the
   * {@code onUpdateProperties} policy.
   */
  @PostConstruct
  public void init() {
    // registers the policy behaviours
    policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate"),
      DocumentModel.ASPECT_URLABLE,
      new JavaBehaviour(this, "onContentUpdate")
    );
    policyComponent.bindClassBehaviour(
      QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
      DocumentModel.ASPECT_URLABLE,
      new JavaBehaviour(this, "onUpdateProperties")
    );
  }

  /**
   * {@link ContentServicePolicies.OnContentUpdatePolicy} callback. The content of a URL node is
   * managed automatically from its URL property, so any attempt to update it manually is rejected.
   *
   * @param nodeRef the node whose content was updated
   * @param newContent {@code true} if the content is newly created, {@code false} if it is being
   *     updated
   * @throws AlfrescoRuntimeException always, to prevent manual updates of a URL node's content
   */
  public void onContentUpdate(final NodeRef nodeRef, final boolean newContent) {
    throw new AlfrescoRuntimeException(
      "Impossible to update manually the content of an URL"
    );
  }

  /**
   * {@link NodeServicePolicies.OnUpdatePropertiesPolicy} callback. When the {@link
   * DocumentModel#PROP_URL} property changes, regenerates the node's content with a redirecting
   * HTML document (see {@link #URL_FIXED_CONTENT}) pointing to the new URL. The content is written
   * as {@code text/html} with UTF-8 encoding. This behaviour is temporarily disabled for the node
   * during the rewrite to avoid re-triggering itself.
   *
   * @param nodeRef the node whose properties were updated
   * @param before the property values before the update, or {@code null} if not available
   * @param after the property values after the update, or {@code null} if not available
   */
  public void onUpdateProperties(
    final NodeRef nodeRef,
    final Map<QName, Serializable> before,
    final Map<QName, Serializable> after
  ) {
    if (after != null && after.containsKey(DocumentModel.PROP_URL)) {
      final Serializable newUrl = after.get(DocumentModel.PROP_URL);
      final Serializable oldUrl = (before == null)
        ? null
        : before.get(DocumentModel.PROP_URL);

      if (newUrl != null && !newUrl.equals(oldUrl)) {
        // sets the content to be written
        final ContentWriter writer = contentService.getWriter(
          nodeRef,
          ContentModel.PROP_CONTENT,
          true
        );

        try {
          this.policyBehaviourFilter.disableBehaviour(
            nodeRef,
            DocumentModel.ASPECT_URLABLE
          );

          // sets the mimetype and encoding
          writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
          writer.setEncoding("UTF-8");
          writer.putContent(MessageFormat.format(URL_FIXED_CONTENT, newUrl));
        } finally {
          this.policyBehaviourFilter.enableBehaviour(
            nodeRef,
            DocumentModel.ASPECT_URLABLE
          );
        }
      }
    }
  }
}
