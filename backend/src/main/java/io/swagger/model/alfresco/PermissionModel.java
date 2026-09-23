/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package io.swagger.model.alfresco;

import org.alfresco.service.namespace.QName;

/**
 * Constants holder that defines the Alfresco {@link QName} identifiers for the CIRCABC
 * permission-related properties carried by {@code ci:profile} nodes.
 *
 * <p>Each constant maps a specific CIRCABC service (CircaBC, Category, Directory, Library,
 * Newsgroup, Survey, Information and Event) to the qualified name of the property that stores the
 * profile's permission level for that service. The names are resolved against the CIRCABC content
 * model namespace ({@link CircabcModel#CIRCABC_CONTENT_MODEL_1_0_URI}).
 *
 * <p>This class is not meant to be instantiated; it only exposes {@code public static final}
 * constants.
 *
 * @author yanick pignot
 */
public final class PermissionModel {

  private PermissionModel() {}

  /**
   * Qualified name of the property of type ci:profile containing the
   * circaBC permission
   */
  public static final QName CIRCABC_PERMISSION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaBCPermission"
  );

  /**
   * Qualified name of the property of type ci:profile containing the
   * category permission
   */
  public static final QName CATEGORY_PERMISSION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategoryPermission"
  );

  /**
   * Qualified name of the property of type ci:profile containing the
   * directory permission
   */
  public static final QName DIRECTORY_PERMISSION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootDirectoryPermission"
  );

  /**
   * Qualified name of the property of type ci:profile containing the
   * library permission
   */
  public static final QName LIBRARY_PERMISSION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaLibraryPermission"
  );

  /**
   * Qualified name of the property of type ci:profile containing the
   * newsgroup permission
   */
  public static final QName NEWSGROUP_PERMISSION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaNewsGroupPermission"
  );

  /**
   * Qualified name of the property of type ci:profile containing the
   * survey permission
   */
  public static final QName SURVEY_PERMISSION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaSurveyPermission"
  );

  /**
   * Qualified name of the property of type ci:profile containing the
   * information permission
   */
  public static final QName INFORMATION_PERMISSION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcInformationPermission"
  );

  /**
   * Qualified name of the property of type ci:profile containing the
   * event permission
   */
  public static final QName EVENT_PERMISSION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circabcEventPermission"
  );
}
