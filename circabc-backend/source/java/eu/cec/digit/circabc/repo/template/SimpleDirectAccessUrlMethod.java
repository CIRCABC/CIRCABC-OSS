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
package eu.cec.digit.circabc.repo.template;

import eu.cec.digit.circabc.web.servlet.ExternalAccessServlet;
import freemarker.template.TemplateMethodModelEx;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Freemarker method to return the direct access url of a node. THis to render url without
 * facescontext (DirectAccessUrlMethod does not work in cron job as it is tied to facescontext)
 *
 * @author Pierre Beauregard
 */
public class SimpleDirectAccessUrlMethod extends NodeRefBaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    @Override
    public String getResult(NodeRef nodeRef) {
        return ExternalAccessServlet.getServerContext() + "/w/browse/" + nodeRef.getId();
    }
}
