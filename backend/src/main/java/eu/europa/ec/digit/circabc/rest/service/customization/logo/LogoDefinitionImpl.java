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

import java.io.Serializable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Default, serializable wrapper describing a single customization logo.
 *
 * <p>This class is the standard implementation of {@link LogoDefinition}. It groups together the
 * {@link NodeRef} of the content node holding the logo image, the {@link NodeRef} of the node the
 * logo is defined on (for example a header, category or interest group) and the logo's descriptive
 * metadata (name, title and description).
 *
 * <p>Read access is exposed publicly through the {@link LogoDefinition} contract, while the setters
 * have package-private visibility so that only classes within this package (typically the logo
 * service/factory) can populate a definition.
 *
 * @author yanick pignot
 */
public class LogoDefinitionImpl implements Serializable, LogoDefinition {

  /** Serialization version identifier for this wrapper. */
  private static final long serialVersionUID = -4254888522583804060L;

  /** Reference to the content node holding the logo image. */
  private NodeRef logo = null;

  /** Reference to the node this logo is defined on (header, category, interest group, ...). */
  private NodeRef definedOn = null;

  /** Human-readable title of the logo. */
  private String title;

  /** Human-readable description of the logo. */
  private String description;

  /** Name of the logo. */
  private String name;

  /**
   * Returns the reference to the content node holding the logo image.
   *
   * @return the {@link NodeRef} of the logo content node, or {@code null} if not set
   */
  public final NodeRef getReference() {
    return logo;
  }

  /**
   * Sets the reference to the content node holding the logo image.
   *
   * @param logo the {@link NodeRef} of the logo content node to set
   */
  /*package*/
  final void setReference(NodeRef logo) {
    this.logo = logo;
  }

  /**
   * Returns the reference to the node on which this logo is defined.
   *
   * @return the {@link NodeRef} of the node where the logo is defined, or {@code null} if not set
   */
  public final NodeRef getDefinedOn() {
    return definedOn;
  }

  /**
   * Sets the reference to the node on which this logo is defined.
   *
   * @param definedOn the {@link NodeRef} of the node where the logo is defined
   */
  /*package*/ void setDefinedOn(NodeRef definedOn) {
    this.definedOn = definedOn;
  }

  /**
   * Returns the human-readable description of the logo.
   *
   * @return the logo description, or {@code null} if none is set
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Sets the human-readable description of the logo.
   *
   * @param logoDescription the logo description to set
   */
  /*package*/
  final void setDescription(String logoDescription) {
    this.description = logoDescription;
  }

  /**
   * Returns the name of the logo.
   *
   * @return the logo name, or {@code null} if none is set
   */
  public final String getName() {
    return name;
  }

  /**
   * Sets the name of the logo.
   *
   * @param logoName the logo name to set
   */
  /*package*/
  final void setName(String logoName) {
    this.name = logoName;
  }

  /**
   * Returns the title of the logo.
   *
   * @return the logo title, or {@code null} if none is set
   */
  public final String getTitle() {
    return title;
  }

  /**
   * Sets the title of the logo.
   *
   * @param logoTitle the logo title to set
   */
  /*package*/
  final void setTitle(String logoTitle) {
    this.title = logoTitle;
  }
}
