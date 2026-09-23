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
package eu.europa.ec.digit.circabc.rest.service.statistic.ig;

import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Data-holder bean describing a Category in the context of Interest Group (IG) statistics.
 *
 * <p>An instance aggregates the identity of a CIRCABC Category (its technical name, human-readable
 * title and the Alfresco {@link NodeRef} that backs it) together with the collection of Interest
 * Groups it contains and the set of administrators associated with it. It is a plain transfer
 * object with no behaviour of its own: it is populated by the statistics services and consumed when
 * rendering statistic reports.
 *
 * @author beaurpi
 */
public class CategoryDescriptor {

  /** Technical (unique) name of the category. */
  private String name;

  /** Human-readable display title of the category. */
  private String title;

  /** Reference to the Alfresco repository node representing this category. */
  private NodeRef ref;

  /** Interest Groups belonging to this category, each described by an {@link IgDescriptor}. */
  private List<IgDescriptor> listOfIgs;

  /** User names (or identifiers) of the administrators of this category. */
  private Set<String> listOfAdmins;

  /** Default constructor for bean initialization */
  @SuppressWarnings("java:S1186") // Empty method is intentional
  public CategoryDescriptor() {
    // Default constructor required for bean/serialization frameworks
  }

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  /** @return the title */
  public String getTitle() {
    return title;
  }

  /** @param title the title to set */
  public void setTitle(String title) {
    this.title = title;
  }

  /** @return the ref */
  public NodeRef getRef() {
    return ref;
  }

  /** @param ref the ref to set */
  public void setRef(NodeRef ref) {
    this.ref = ref;
  }

  /** @return the listOfIgs */
  public List<IgDescriptor> getListOfIgs() {
    return listOfIgs;
  }

  /** @param listOfIgs the listOfIgs to set */
  public void setListOfIgs(List<IgDescriptor> listOfIgs) {
    this.listOfIgs = listOfIgs;
  }

  /** @return the listOfAdmins */
  public Set<String> getListOfAdmins() {
    return listOfAdmins;
  }

  /** @param listOfAdmins the listOfAdmins to set */
  public void setListOfAdmins(Set<String> listOfAdmins) {
    this.listOfAdmins = listOfAdmins;
  }
}
