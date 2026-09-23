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

/**
 * Represents a single node in the service tree used when reporting Interest Group (IG) statistics.
 *
 * <p>This is a lightweight data holder (bean) that pairs a service {@code name} with a single {@link
 * Child} node, allowing a hierarchical service structure to be modelled and serialised for
 * statistics responses.
 */
public class ServiceTreeRepresentation {

  /** The display name of this service tree node. */
  private String name;

  /** The child node attached to this service tree node, forming the tree hierarchy. */
  private Child child;

  /** Creates an empty {@code ServiceTreeRepresentation} with no name or child set. */
  public ServiceTreeRepresentation() {}

  /**
   * Creates a {@code ServiceTreeRepresentation} with the given name.
   *
   * @param name the name of this service tree node
   */
  public ServiceTreeRepresentation(String name) {
    this.name = name;
  }

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  /** @return the children */
  public Child getChild() {
    return child;
  }

  /** @param child the child to set */
  public void setChild(Child child) {
    this.child = child;
  }
}
