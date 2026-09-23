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
 *
 * @author beaurpi, Alain Morlet
 */
package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Represents a node in a hierarchical tree structure used when computing Interest Group (IG)
 * statistics.
 *
 * <p>Each instance holds a display {@code name}, a reference to the underlying Alfresco repository
 * node ({@code node}), and an ordered list of child nodes. A {@code Child} with no children is
 * considered a leaf (see {@link #isLeaf()}), which allows callers to walk the tree and distinguish
 * containers from terminal items while aggregating statistics.
 */
public class Child {

  /** Human-readable name of this node (for example a folder or item name). */
  private String name;

  /** Reference to the Alfresco repository node this element represents. */
  private NodeRef node;

  /** Child nodes contained under this node; never {@code null}. */
  private List<Child> childrenContainer;

  /** Creates an empty node with an initialised, empty list of children. */
  public Child() {
    this.childrenContainer = new ArrayList<>();
  }

  /**
   * Creates a fully populated node.
   *
   * @param name the display name of the node
   * @param node the Alfresco repository node reference this element represents
   * @param childrenContainer the child nodes; if {@code null}, an empty list is used instead
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

  /**
   * Returns the child nodes contained under this node.
   *
   * @return the list of children
   */
  public List<Child> getChildren() {
    return childrenContainer;
  }

  /**
   * Sets the child nodes contained under this node.
   *
   * @param children the children to set
   */
  public void setChildren(List<Child> children) {
    this.childrenContainer = children;
  }

  /**
   * Returns the display name of this node.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the display name of this node.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the Alfresco repository node reference this element represents.
   *
   * @return the node reference
   */
  public NodeRef getNode() {
    return node;
  }

  /**
   * Sets the Alfresco repository node reference this element represents.
   *
   * @param node the node reference to set
   */
  public void setNode(NodeRef node) {
    this.node = node;
  }

  /**
   * Indicates whether this node is a leaf, i.e. it has no children.
   *
   * @return {@code true} if the children list is {@code null} or empty, {@code false} otherwise
   */
  public Boolean isLeaf() {
    return (
      (this.childrenContainer == null) || (this.childrenContainer.isEmpty())
    );
  }
}
