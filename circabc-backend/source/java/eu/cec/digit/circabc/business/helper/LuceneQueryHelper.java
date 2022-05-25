/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.business.helper;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO9075;
import org.alfresco.web.bean.repository.Repository;

import javax.faces.context.FacesContext;
import java.util.Collection;

/**
 * @author beaurpi
 *
 */
public class LuceneQueryHelper {

    public static String getPathFromSpaceRef(NodeRef igRoot) {

        final FacesContext fc = FacesContext.getCurrentInstance();

        final NodeService nodeService = Repository.getServiceRegistry(fc).getNodeService();
        final NamespaceService namespaceService = Repository.getServiceRegistry(fc)
                .getNamespaceService();

        final Path path = nodeService.getPath(igRoot);
        final StringBuilder buf = new StringBuilder(64);
        String elementString;
        Path.Element element;
        ChildAssociationRef elementRef;
        Collection<?> prefixes;
        for (int i = 0; i < path.size(); i++) {
            elementString = "";
            element = path.get(i);
            if (element instanceof Path.ChildAssocElement) {
                elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null) {
                    prefixes = namespaceService.getPrefixes(elementRef.getQName().getNamespaceURI());
                    if (prefixes.size() > 0) {
                        elementString = '/' + (String) prefixes.iterator().next() + ':' + ISO9075
                                .encode(elementRef.getQName().getLocalName());
                    }
                }
            }

            buf.append(elementString);
        }

        buf.append("/");

        return buf.toString();
    }

}
