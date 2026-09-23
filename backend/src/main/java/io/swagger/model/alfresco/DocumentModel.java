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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.alfresco.service.namespace.QName;

/**
 * Content-model definitions for the Circabc Document aspects.
 *
 * <p>This is a non-instantiable utility class that centralises the Alfresco
 * {@link QName} constants (namespaces, aspects, associations, types and
 * properties) that make up the Circabc Document content model, together with
 * the string constants for their controlled/enumerated values (document
 * status and security ranking) and helper lists of those values.
 *
 * <p>These constants are referenced throughout the REST layer and services to
 * read and write the extra metadata carried by Circabc documents (status,
 * issue date, reference, dynamic attributes, keywords, security ranking,
 * expiration date, URL and encryptable content).
 *
 * @author patrice.coppens@trasys.lu
 *         <p>
 *         25-juin-07 - 08:22:52
 */
public final class DocumentModel {

  /**
   * Private constructor to prevent instantiation of this constants-only
   * utility class.
   */
  private DocumentModel() {}

  /**
   * Circabc Document Model Prefix
   */
  public static final String CIRCABC_DOCUMENT_MODEL_PREFIX = "cd";

  /**
   * Circabc user model namespace.
   */
  public static final String CIRCABC_DOCUMENT_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/document/1.0";

  /**
   * Circabc document Aspect name {cd:circadocument}
   */
  public static final QName ASPECT_CIRCABC_DOCUMENT = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "circadocument"
  );

  /**
   * BProperties Aspect name {cd:bproperties}
   */
  public static final QName ASPECT_BPROPERTIES = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "bproperties"
  );

  /**
   * CProperties Aspect name {cd:cproperties}
   */
  public static final QName ASPECT_CPROPERTIES = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "cproperties"
  );

  /**
   * URL aspect (cd:urlable)
   */
  public static final QName ASPECT_URLABLE = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "urlable"
  );

  /**
   * Attachable aspect, allow a node to have some attachment (cd:attachable)
   */
  public static final QName ASPECT_ATTACHABLE = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "attachable"
  );

  /**
   * External node preferences association (cd:externalReferences)
   */
  public static final QName ASSOC_EXTERNAL_REFERENCES = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "externalReferences"
  );

  /**
   * Hidden node prefereces association (cd:hiddenReferences)
   */
  public static final QName ASSOC_HIDDEN_REFERENCES = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "hiddenReferences"
  );

  /**
   * Hidden attachement contens type (cd:hiddenContent)
   */
  public static final QName TYPE_HIDDEN_ATTACHEMENT_CONTENT = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "hiddenContent"
  );

  /**
   * QName for the document content property (cd:content).
   */
  public static final QName PROP_CONTENT = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "content"
  );

  /**
   * QName for extra properties [status] that belong to Circabc Document Ref:
   * CProperties@status, Type selection{draft, final, release}, Searchable.
   */
  public static final QName PROP_STATUS = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "status"
  );

  /**
   * QName for extra properties [Issue date] that belong to Circabc Document Ref:
   * CProperties@issue date, Type date time, Searchable.
   */
  public static final QName PROP_ISSUE_DATE = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "issue_date"
  );

  /**
   * QName for extra properties [reference] that belong to Circabc Document Ref:
   * CProperties@reference, Type text, Searchable.
   */
  public static final QName PROP_REFERENCE = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "reference"
  );

  /**
   * QName for extra properties [DynAttr1] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_1 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr1"
  );

  /**
   * QName for extra properties [DynAttr2] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_2 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr2"
  );

  /**
   * QName for extra properties [DynAttr3] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_3 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr3"
  );

  /**
   * QName for extra properties [DynAttr4] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_4 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr4"
  );

  /**
   * QName for extra properties [DynAttr5] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_5 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr5"
  );

  /**
   * QName for extra properties [DynAttr6] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_6 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr6"
  );

  /**
   * QName for extra properties [DynAttr7] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_7 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr7"
  );

  /**
   * QName for extra properties [DynAttr8] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_8 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr8"
  );

  /**
   * QName for extra properties [DynAttr9] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_9 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr9"
  );

  /**
   * QName for extra properties [DynAttr10] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_10 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr10"
  );

  /**
   * QName for extra properties [DynAttr11] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_11 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr11"
  );

  /**
   * QName for extra properties [DynAttr12] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_12 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr12"
  );

  /**
   * QName for extra properties [DynAttr13] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_13 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr13"
  );

  /**
   * QName for extra properties [DynAttr14] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_14 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr14"
  );

  /**
   * QName for extra properties [DynAttr15] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_15 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr15"
  );

  /**
   * QName for extra properties [DynAttr16] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_16 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr16"
  );

  /**
   * QName for extra properties [DynAttr17] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_17 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr17"
  );

  /**
   * QName for extra properties [DynAttr18] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_18 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr18"
  );

  /**
   * QName for extra properties [DynAttr9] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_19 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr19"
  );

  /**
   * QName for extra properties [DynAttr20] that belong to Circabc Document Ref:
   * CProperties@Dynamic attributes, Type text, Searchable.
   */
  public static final QName PROP_DYN_ATTR_20 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "dynAttr20"
  );

  /**
   * All available dynamic properties name
   */
  public static final List<QName> ALL_DYN_PROPS = Collections.unmodifiableList(
    Arrays.asList(
      PROP_DYN_ATTR_1,
      PROP_DYN_ATTR_2,
      PROP_DYN_ATTR_3,
      PROP_DYN_ATTR_4,
      PROP_DYN_ATTR_5,
      PROP_DYN_ATTR_6,
      PROP_DYN_ATTR_7,
      PROP_DYN_ATTR_8,
      PROP_DYN_ATTR_9,
      PROP_DYN_ATTR_10,
      PROP_DYN_ATTR_11,
      PROP_DYN_ATTR_12,
      PROP_DYN_ATTR_13,
      PROP_DYN_ATTR_14,
      PROP_DYN_ATTR_15,
      PROP_DYN_ATTR_16,
      PROP_DYN_ATTR_17,
      PROP_DYN_ATTR_18,
      PROP_DYN_ATTR_19,
      PROP_DYN_ATTR_20
    )
  );

  /**
   * QName for extra properties [keyword] that belong to Circabc Document Ref:
   * CProperties@keyword, Type noderef, Translatable, Searchable.
   */
  public static final QName PROP_KEYWORD = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "keyword"
  );

  /**
   * QName for extra properties [security_ranking] that belong to Circabc Document
   * Ref: BProperties@security_ranking, Type selection{PUBLIC(default), INTERNAL,
   * LIMITED}, Searchable.
   */
  public static final QName PROP_SECURITY_RANKING = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "security_ranking"
  );

  /**
   * QName for extra properties [expiration_date] that belong to Circabc Document
   * Ref: BProperties@expiration_date, Type date-time, Searchable.
   */
  public static final QName PROP_EXPIRATION_DATE = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "expiration_date"
  );

  /**
   * QName for mandatory properties [URL] that is applied with the URL able
   * aspect. BProperties@URL, Type text, Searchable.
   */
  public static final QName PROP_URL = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "url"
  );

  /** Document status value: draft. */
  public static final String STATUS_VALUE_DRAFT = "DRAFT";
  /** Document status value: final. */
  public static final String STATUS_VALUE_FINAL = "FINAL";
  /** Document status value: released. */
  public static final String STATUS_VALUE_RELEASE = "RELEASE";

  /** Security ranking value: normal. */
  public static final String SECURITY_RANKINGS_NORMAL = "NORMAL";
  /** Security ranking value: sensitive. */
  public static final String SECURITY_RANKINGS_SENSITIVE = "SENSITIVE";
  /** Security ranking value: special handling. */
  public static final String SECURITY_RANKINGS_SPECIAL_HANDLING =
    "SPECIAL_HANDLING";
  /** Security ranking value: public. */
  public static final String SECURITY_RANKINGS_PUBLIC = "PUBLIC";
  /** Security ranking value: internal. */
  public static final String SECURITY_RANKINGS_INTERNAL = "INTERNAL";
  /** Security ranking value: limited. */
  public static final String SECURITY_RANKINGS_LIMITED = "LIMITED";

  /**
   * security ranking values list.
   */
  public static final List<String> SECURITY_RANKINGS =
    Collections.unmodifiableList(
      Arrays.asList(
        SECURITY_RANKINGS_NORMAL,
        SECURITY_RANKINGS_SENSITIVE,
        SECURITY_RANKINGS_SPECIAL_HANDLING,
        SECURITY_RANKINGS_PUBLIC,
        SECURITY_RANKINGS_INTERNAL,
        SECURITY_RANKINGS_LIMITED
      )
    );
  /**
   * status values list.
   */
  public static final List<String> STATUS_VALUES = Collections.unmodifiableList(
    Arrays.asList(STATUS_VALUE_DRAFT, STATUS_VALUE_FINAL, STATUS_VALUE_RELEASE)
  );

  /**
   * Encryptable
   */
  public static final QName ASPECT_ENCRYPTABLE = QName.createQName(
    DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "encryptable"
  );

  /**
   * QName for the first encrypted text property (cd:encryptedText) carried by
   * the encryptable aspect.
   */
  public static final QName PROP_ENCRYPTEDTEXT1 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "encryptedText"
  );

  /**
   * QName for the second encrypted text property (cd:encryptedText2) carried by
   * the encryptable aspect.
   */
  public static final QName PROP_ENCRYPTEDTEXT2 = QName.createQName(
    CIRCABC_DOCUMENT_MODEL_1_0_URI,
    "encryptedText2"
  );
}
