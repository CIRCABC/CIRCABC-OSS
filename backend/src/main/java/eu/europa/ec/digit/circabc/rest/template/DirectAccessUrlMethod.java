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
package eu.europa.ec.digit.circabc.rest.template;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * FreeMarker template method extension that builds the direct access (browse) URL for a given
 * repository node.
 *
 * <p>Registered as a FreeMarker method, it is invoked from {@code .ftl} templates with a single
 * argument that resolves to a {@link NodeRef} (either a wrapped {@code NodeRef} or a
 * {@code TemplateNode}). The argument extraction and null handling are performed by the superclass
 * {@link NodeRefBaseTemplateProcessorExtension}; this class only defines how the resulting URL
 * string is composed for a resolved node.
 *
 * @author Yanick Pignot
 */
public class DirectAccessUrlMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * Builds the direct access URL used to browse the given node.
   *
   * <p>The URL is composed by appending the node's identifier to the fixed {@code /w/browse/} path
   * prefix.
   *
   * @param nodeRef the reference to the repository node for which the URL is built; must not be
   *     {@code null}
   * @return the direct browse URL for the node, in the form {@code /w/browse/<nodeId>}
   */
  @Override
  public String getResult(NodeRef nodeRef) {
    return "/w/browse/" + nodeRef.getId();
  }
}
