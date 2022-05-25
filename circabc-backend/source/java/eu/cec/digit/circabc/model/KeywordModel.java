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

/**
 * It is the model for the keyword specification
 *
 * @author Yanick Pignot
 */
public interface KeywordModel extends BaseCircabcModel {

    /**
     * Circabc Keywords namespace
     */
    String CIRCABC_KEYWORD_MODEL_1_0_URI = CIRCABC_NAMESPACE + "/model/keyword/1.0";

    /**
     * Circabc Keywords prefix
     */
    String CIRCABC_KEYWORD_MODEL_PREFIX = "kw";

    /**
     * Circabc Keywords root container
     */
    QName TYPE_KEYWORD_CONTAINER = QName
            .createQName(CIRCABC_KEYWORD_MODEL_1_0_URI, "keywordContainer");

    /**
     * Circabc Keywords element
     */
    QName TYPE_KEYWORD = QName.createQName(CIRCABC_KEYWORD_MODEL_1_0_URI, "keyword");

    /**
     * Circabc Keywords association beetween the root container and the keyword elements
     */
    QName ASSOC_KEYWORDS = QName.createQName(CIRCABC_KEYWORD_MODEL_1_0_URI, "keywords");

    /**
     * Circabc Keywords association beetween the keyword elements and another keyword elements (Not
     * used yet)
     */
    QName ASSOC_SUB_KEYWORDS = QName.createQName(CIRCABC_KEYWORD_MODEL_1_0_URI, "subkeywords");

    /**
     * Circabc keyword properties that define if the keyword is multilingal or not
     */
    QName PROP_TRANSLATED = QName.createQName(CIRCABC_KEYWORD_MODEL_1_0_URI, "translated");

}
