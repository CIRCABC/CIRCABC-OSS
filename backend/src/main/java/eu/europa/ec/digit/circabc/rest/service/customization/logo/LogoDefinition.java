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
package eu.europa.ec.digit.circabc.rest.service.customization.logo;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Describes a customization logo within the CIRCABC repository.
 *
 * <p>A logo is backed by a content node in the Alfresco repository and is associated with the node
 * on which it is defined (for example a header, category or interest group). Implementations expose
 * read-only access to the logo's underlying node references and to its descriptive metadata (name,
 * title and description).
 *
 * @author Yanick Pignot
 */
public interface LogoDefinition {
  /**
   * Returns the reference to the content node holding the logo image.
   *
   * @return the {@link NodeRef} of the logo content node
   */
  NodeRef getReference();

  /**
   * Returns the reference to the node on which this logo is defined (the node the logo is attached
   * to, such as a header, category or interest group).
   *
   * @return the {@link NodeRef} of the node where the logo is defined
   */
  NodeRef getDefinedOn();

  /**
   * Returns the human-readable description of the logo.
   *
   * @return the logo description, or {@code null} if none is set
   */
  String getDescription();

  /**
   * Returns the name of the logo.
   *
   * @return the logo name
   */
  String getName();

  /**
   * Returns the title of the logo.
   *
   * @return the logo title
   */
  String getTitle();
}
