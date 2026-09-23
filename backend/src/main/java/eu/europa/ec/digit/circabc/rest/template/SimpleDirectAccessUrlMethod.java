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
 * FreeMarker template method that builds a simple direct-access (browse) URL for a repository node.
 *
 * <p>It is registered as a template processor extension and, when invoked from a FreeMarker
 * template, returns a relative browse URL of the form {@code /w/browse/<nodeId>} for the supplied
 * {@link NodeRef}.
 *
 * <p>Unlike {@code DirectAccessUrlMethod}, this implementation is deliberately independent of the
 * JSF {@code FacesContext}, so it can be used safely from contexts where no faces context is
 * available, such as scheduled (cron) jobs and background processing.
 *
 * @author Pierre Beauregard
 */
public class SimpleDirectAccessUrlMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * Builds the direct-access browse URL for the given node.
   *
   * @param nodeRef the reference of the node to build the URL for
   * @return a relative browse URL of the form {@code /w/browse/<nodeId>}
   */
  @Override
  public String getResult(NodeRef nodeRef) {
    return ("/w/browse/" + nodeRef.getId());
  }
}
