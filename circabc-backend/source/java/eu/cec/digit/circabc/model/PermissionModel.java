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
package eu.cec.digit.circabc.model;

import org.alfresco.service.namespace.QName;

/**
 * @author yanick pignot
 */
public interface PermissionModel extends CircabcModel {

    /**
     * Qualified name of the property of type ci:profile containing the
     * circaBC permission
     */
    QName CIRCABC_PERMISSION = QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaBCPermission");

    /**
     * Qualified name of the property of type ci:profile containing the
     * category permission
     */
    QName CATEGORY_PERMISSION = QName
            .createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryPermission");

    /**
     * Qualified name of the property of type ci:profile containing the
     * directory permission
     */
    QName DIRECTORY_PERMISSION = QName
            .createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootDirectoryPermission");

    /**
     * Qualified name of the property of type ci:profile containing the
     * library permission
     */
    QName LIBRARY_PERMISSION = QName
            .createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaLibraryPermission");

    /**
     * Qualified name of the property of type ci:profile containing the
     * directory permission
     */
    QName NEWSGROUP_PERMISSION = QName
            .createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaNewsGroupPermission");

    /**
     * Qualified name of the property of type ci:profile containing the
     * survey permission
     */
    QName SURVEY_PERMISSION = QName
            .createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaSurveyPermission");

    /**
     * Qualified name of the property of type ci:profile containing the
     * information permission
     */
    QName INFORMATION_PERMISSION = QName
            .createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circabcInformationPermission");

    /**
     * Qualified name of the property of type ci:profile containing the
     * event permission
     */
    QName EVENT_PERMISSION = QName
            .createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circabcEventPermission");
}
