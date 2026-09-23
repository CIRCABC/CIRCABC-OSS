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
 * Content-model constants for the CIRCABC "keyword" specification.
 *
 * <p>This utility class defines the Alfresco {@link QName} identifiers and namespace
 * details for the keyword content model: the namespace URI and prefix, the keyword
 * container and keyword element types, the parent/child associations between them, and
 * the property flagging whether a keyword is multilingual. These constants are used
 * throughout the code base to reference keyword types, associations and properties
 * against the Alfresco repository without hard-coding namespace strings.
 *
 * <p>The class is {@code final} and exposes only static constants; it is not meant to be
 * instantiated.
 *
 * @author Yanick Pignot
 */
public final class KeywordModel {

  /**
   * Private constructor to prevent instantiation of this constants-only utility class.
   */
  private KeywordModel() {}

  /**
   * Circabc Keywords namespace
   */
  public static final String CIRCABC_KEYWORD_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/keyword/1.0";

  /**
   * Circabc Keywords prefix
   */
  public static final String CIRCABC_KEYWORD_MODEL_PREFIX = "kw";

  /**
   * Circabc Keywords root container
   */
  public static final QName TYPE_KEYWORD_CONTAINER = QName.createQName(
    CIRCABC_KEYWORD_MODEL_1_0_URI,
    "keywordContainer"
  );

  /**
   * Circabc Keywords element
   */
  public static final QName TYPE_KEYWORD = QName.createQName(
    CIRCABC_KEYWORD_MODEL_1_0_URI,
    "keyword"
  );

  /**
   * Circabc Keywords association beetween the root container and the keyword elements
   */
  public static final QName ASSOC_KEYWORDS = QName.createQName(
    CIRCABC_KEYWORD_MODEL_1_0_URI,
    "keywords"
  );

  /**
   * Circabc Keywords association beetween the keyword elements and another keyword elements (Not
   * used yet)
   */
  public static final QName ASSOC_SUB_KEYWORDS = QName.createQName(
    CIRCABC_KEYWORD_MODEL_1_0_URI,
    "subkeywords"
  );

  /**
   * Circabc keyword properties that define if the keyword is multilingal or not
   */
  public static final QName PROP_TRANSLATED = QName.createQName(
    CIRCABC_KEYWORD_MODEL_1_0_URI,
    "translated"
  );
}
