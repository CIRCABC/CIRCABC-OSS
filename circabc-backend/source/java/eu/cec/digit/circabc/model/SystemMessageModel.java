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

public interface SystemMessageModel extends BaseCircabcModel {

    String CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI = CIRCABC_NAMESPACE + "/model/systemmessage/1.0";
    String CIRCABC_SYSTEMMESSAGE_MODEL_PREFIX = "sm";

    QName TYPE_SYSTEMMESSAGE = QName
            .createQName(CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI, "systemMessage");

    QName PROP_IS_SYSTEMMESSAGE_ENABLED = QName
            .createQName(CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI, "isSytemMessageEnabled");
    QName PROP_SYSTEMMESSAGE_TEXT = QName
            .createQName(CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI, "systemMessageText");
}
