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

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrap a Category for the Client Side.
 *
 * @author Yanick Pignot
 */
public class CategoryNode extends NavigableNode {

    private static final long serialVersionUID = 4245045930595760474L;

    /**
     * The interest groups included in the category
     */
    private List<NavigableNode> interestGroupsNodes = null;

    /**
     * The corresponding category Header Node
     */
    private CategoryHeaderNode categoryHeaderNode = null;

    public CategoryNode(final NodeRef nodeRef, final CategoryHeaderNode categoryHeaderNode) {
        super(nodeRef);

        this.categoryHeaderNode = categoryHeaderNode;

        if (!getNavigableNodeType().equals(NavigableNodeType.CATEGORY)) {
            throw new IllegalArgumentException(
                    "NodeRef must be a Circabc Category and have the " + CircabcModel.ASPECT_CATEGORY
                            + " aspect applied.");
        }
    }

    public CategoryNode(final String categoryName) {
        this(categoryName, null);
    }

    public CategoryNode(final NodeRef nodeRef) {
        this(nodeRef, null);
    }

    public CategoryNode(final String categoryName, final CategoryHeaderNode categoryHeaderNode) {
        this(
                Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getManagementService()
                        .getCategory(categoryName), categoryHeaderNode);
    }

    @Override
    public List<NavigableNode> getNavigableChilds() {
        if (interestGroupsNodes == null) {
            final List<NodeRef> interestGroupsNodeRef = getManagementService()
                    .getInterestGroups(this.getNodeRef());
            interestGroupsNodes = new ArrayList<>(interestGroupsNodeRef.size());
            for (final NodeRef ref : interestGroupsNodeRef) {
                interestGroupsNodes.add(new InterestGroupNode(ref, this));
            }
        }

        return interestGroupsNodes;
    }

    @SuppressWarnings("unchecked")
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
    public void resetCache() {
        interestGroupsNodes = null;
        categoryHeaderNode = null;
    }

}
