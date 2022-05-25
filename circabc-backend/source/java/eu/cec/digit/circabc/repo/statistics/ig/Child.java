/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.cec.digit.circabc.repo.statistics.ig;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.List;

/** @author beaurpi */
public class Child {

    private String name;
    private NodeRef node;
    private List<Child> childrenContainer;

    /** Empty constructor */
    public Child() {
        this.childrenContainer = new ArrayList<>();
    }

    /**
     * * complete constructor
     *
     * @param name
     * @param node
     * @param childrenContainer
     */
    public Child(String name, NodeRef node, List<Child> childrenContainer) {
        this.name = name;
        this.node = node;
        if (childrenContainer == null) {
            this.childrenContainer = new ArrayList<>();
        } else {
            this.childrenContainer = childrenContainer;
        }
    }

    /** @return the children */
    public List<Child> getChildren() {
        return childrenContainer;
    }

    /** @param children the children to set */
    public void setChildren(List<Child> children) {
        this.childrenContainer = children;
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @param name the name to set */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the node */
    public NodeRef getNode() {
        return node;
    }

    /** @param node the node to set */
    public void setNode(NodeRef node) {
        this.node = node;
    }

    public Boolean isLeaf() {
        return ((this.childrenContainer == null) || (this.childrenContainer.size() == 0))
                ? true
                : false;
    }
}
