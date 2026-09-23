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

import org.alfresco.service.namespace.QName;

/**
 * Content-model constants for CIRCABC profiles.
 *
 * <p>This utility class centralizes the fully qualified {@link QName} identifiers of the
 * properties and associations that describe an Interest Group profile within the CIRCABC
 * Alfresco content model. All names are resolved against
 * {@link CircabcModel#CIRCABC_CONTENT_MODEL_1_0_URI} so that other components can reference
 * profile-related properties and associations without hard-coding namespace URIs or local names.
 *
 * <p>The class is {@code final} and its constructor is private; it is not meant to be
 * instantiated and only exposes {@code public static final} constants.
 */
public final class ProfileModel {

  /**
   * Private constructor to prevent instantiation of this constants-only utility class.
   */
  private ProfileModel() {}

  /** Boolean property flagging whether a profile has been exported ({@code isExported}). */
  public static final QName PROP_PROFILE_EXPORTED = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "isExported"
  );
  /** Boolean property flagging whether a profile has been imported ({@code isImported}). */
  public static final QName PROP_PROFILE_IMPORTED = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "isImported"
  );
  /** Property holding the node reference of the source node from which the profile was imported ({@code importedNodeRef}). */
  public static final QName PROP_PROFILE_IMPORTED_REF = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "importedNodeRef"
  );
  /** Association linking an imported profile to the target it was imported to ({@code importedTo}). */
  public static final QName ASSOC_PROFILE_IMPORTED_TO = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "importedTo"
  );
  /** Association between an Interest Group root and its profile ({@code circaIGRootProfileAssoc}). */
  public static final QName ASSOC_IG_ROOT_PROFILE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootProfileAssoc"
  );
  /** Property holding the name of an Interest Group root profile ({@code circaIGRootProfileName}). */
  public static final QName PROP_IG_ROOT_PROFILE_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootProfileName"
  );
}
