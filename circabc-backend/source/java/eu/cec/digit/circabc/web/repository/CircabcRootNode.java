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
package eu.cec.digit.circabc.web.repository;

import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrap a The root circabc node for the Client Side.
 *
 * @author Yanick Pignot
 */
public class CircabcRootNode extends NavigableNode {

    private static final long serialVersionUID = 4245045930595760474L;

    /**
     * the list of categoryHeaders
     */
    private List<NavigableNode> categoryHeadersNode = null;
    private NodePropertyResolver resolverRootCategoryHeader = new NodePropertyResolver() {

        private static final long serialVersionUID = 9182503427802227850L;

        public Object get(final Node node) {
            return getManagementService().getRootCategoryHeader();
        }
    };

    public CircabcRootNode(final NodeRef nodeRef) {
        super(nodeRef);

        if (!getNavigableNodeType().equals(NavigableNodeType.CIRCABC_ROOT)) {
            throw new IllegalArgumentException(
                    "NodeRef must be a Circabc Root and have the " + NavigableNodeType.CIRCABC_ROOT
                            .getComparatorQName() + " aspect applied.");
        }

        this.addPropertyResolver("rootCategoryHeader", resolverRootCategoryHeader);

    }

    @Override
    public List<NavigableNode> getNavigableChilds() {
        if (categoryHeadersNode == null) {
            final List<NodeRef> categoryHeadersNodeRef = getManagementService()
                    .getExistingCategoryHeaders();

            categoryHeadersNode = new ArrayList<>(categoryHeadersNodeRef.size());

            for (final NodeRef cat : categoryHeadersNodeRef) {
                categoryHeadersNode.add(new CategoryHeaderNode(cat, this));
            }
        }
        return categoryHeadersNode;
    }

    @Override
    public NavigableNode getNavigableParent() {
        // no node above manageable by circabc
        return null;
    }

    @Override
    public void resetCache() {
        categoryHeadersNode = null;
    }


}
