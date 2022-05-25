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
package eu.cec.digit.circabc.model;

import org.alfresco.service.namespace.QName;

public interface ProfileModel extends BaseCircabcModel {

    QName PROP_PROFILE_EXPORTED = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "isExported");
    QName PROP_PROFILE_IMPORTED = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "isImported");
    QName PROP_PROFILE_IMPORTED_REF = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "importedNodeRef");
    QName ASSOC_PROFILE_IMPORTED_TO = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "importedTo");
    QName ASSOC_IG_ROOT_PROFILE = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootProfileAssoc");
    QName PROP_IG_ROOT_PROFILE_NAME = QName
            .createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRootProfileName");

}
