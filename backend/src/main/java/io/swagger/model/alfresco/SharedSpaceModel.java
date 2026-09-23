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
package io.swagger.model.alfresco;

import static io.swagger.model.alfresco.BaseCircabcModel.CIRCABC_NAMESPACE;

import org.alfresco.service.namespace.QName;

/**
 * Content-model constants for the CIRCABC "Shared Space" feature.
 *
 * <p>This is a utility holder that centralises the Alfresco {@link QName}
 * identifiers (namespace URI, prefix, types, associations and properties)
 * that make up the shared space model. Shared spaces allow content to be
 * shared across Interest Groups; the constants defined here are used
 * throughout the code base to reference the corresponding model elements
 * when reading or writing nodes in the Alfresco repository.</p>
 *
 * <p>The class is {@code final} and exposes only {@code public static final}
 * constants, so it is not meant to be instantiated.</p>
 *
 * @author Stephane Clinckart
 * @author Slobodan Filipovic
 */
public final class SharedSpaceModel {

  /**
   * Private constructor to prevent instantiation of this constants-only
   * utility class.
   */
  private SharedSpaceModel() {}

  /**
   * Circabc Shared Space namespace
   */
  public static final String CIRCABC_SHARED_SPACE_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/sharespace/1.0";

  /**
   * Circabc Shared Space prefix
   */
  public static final String CIRCABC_SHARED_SPACE_MODEL_PREFIX = "ss";

  /**
   * Association type linking a node to its shared space container.
   *
   * <p>Note: this association is qualified with the CIRCABC content model
   * namespace ({@link CircabcModel#CIRCABC_CONTENT_MODEL_1_0_URI}) rather
   * than the shared space namespace.</p>
   */
  public static final QName ASSOC_SHARE_SPACE_CONTAINER = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "shareSpaceContainer"
  );

  /**
   * Type of the shared space container node that holds the invited
   * interest groups.
   */
  public static final QName TYPE_CONTAINER = QName.createQName(
    CIRCABC_SHARED_SPACE_MODEL_1_0_URI,
    "Container"
  );

  /**
   * Association linking a shared space to an invited Interest Group.
   */
  public static final QName ASSOC_ITEREST_GROUP = QName.createQName(
    CIRCABC_SHARED_SPACE_MODEL_1_0_URI,
    "InterestGroupAss"
  );

  /**
   * Type representing an Interest Group that has been invited to a
   * shared space.
   */
  public static final QName TYPE_INVITED_INTEREST_GROUP = QName.createQName(
    CIRCABC_SHARED_SPACE_MODEL_1_0_URI,
    "invitedInterestGroup"
  );
  /**
   * Property holding the {@code NodeRef} of the invited Interest Group.
   */
  public static final QName PROP_INTEREST_GROUP_NODE_REF = QName.createQName(
    CIRCABC_SHARED_SPACE_MODEL_1_0_URI,
    "ignoderef"
  );

  /**
   * Property holding the permission granted to the invited Interest Group
   * on the shared space.
   */
  public static final QName PROP_PERMISSION = QName.createQName(
    CIRCABC_SHARED_SPACE_MODEL_1_0_URI,
    "permission"
  );
}
