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
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

@SuppressWarnings("unchecked")
public class ExternalLinkNode extends NavigableNode {

    private static final long serialVersionUID = 4487878958844970845L;
    private CategoryHeaderNode categoryHeaderNode = null;

    public ExternalLinkNode(NodeRef nodeRef) {
        this(nodeRef, null);
    }

    public ExternalLinkNode(final NodeRef nodeRef, final CategoryHeaderNode categoryHeaderNode) {
        super(nodeRef);
        this.categoryHeaderNode = categoryHeaderNode;
    }

    @Override
    public NavigableNode getNavigableParent() {
        if (categoryHeaderNode == null) {
            final List<NodeRef> cats = (List<NodeRef>) getProperties()
                    .get(ContentModel.PROP_CATEGORIES.toString());
            if (cats == null || cats.size() < 1) {
                throw new IllegalStateException(
                        "All the Categories MUST be linked to a Category Header." + this.getName());
            } else {
                // get first categoryHeader
                this.categoryHeaderNode = new CategoryHeaderNode(cats.get(0), null);
            }
        }

        return categoryHeaderNode;
    }

    @Override
    public List<NavigableNode> getNavigableChilds() {
        return null;
    }

    @Override
    public void resetCache() {
        categoryHeaderNode = null;
    }
}
