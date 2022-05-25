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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * It is the model of the new Aspect that defines the Circabc Document.
 *
 * @author patrice.coppens@trasys.lu
 *         <p>
 *         25-juin-07 - 08:22:52
 */
public interface DocumentModel extends BaseCircabcModel {

        /**
         * Circabc Document Model Prefix
         */
        String CIRCABC_DOCUMENT_MODEL_PREFIX = "cd";

        /**
         * Circabc user model namespace.
         */
        String CIRCABC_DOCUMENT_MODEL_1_0_URI = CIRCABC_NAMESPACE + "/model/document/1.0";

        /**
         * Circabc document Aspect name {cd:circadocument}
         */
        QName ASPECT_CIRCABC_DOCUMENT = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
                        "circadocument");

        /**
         * BProperties Aspect name {cd:bproperties}
         */
        QName ASPECT_BPROPERTIES = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, "bproperties");

        /**
         * CProperties Aspect name {cd:cproperties}
         */
        QName ASPECT_CPROPERTIES = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, "cproperties");

        /**
         * URL aspect (cd:urlable)
         */
        QName ASPECT_URLABLE = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, "urlable");

        /**
         * Attachable aspect, allow a node to have some attachment (cd:attachable)
         */
        QName ASPECT_ATTACHABLE = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, "attachable");

        /**
         * External node preferences association (cd:externalReferences)
         */
        QName ASSOC_EXTERNAL_REFERENCES = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
                        "externalReferences");

        /**
         * Hidden node prefereces association (cd:hiddenReferences)
         */
        QName ASSOC_HIDDEN_REFERENCES = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
                        "hiddenReferences");

        /**
         * Hidden attachement contens type (cd:hiddenContent)
         */
        QName TYPE_HIDDEN_ATTACHEMENT_CONTENT = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
                        "hiddenContent");

        QName PROP_CONTENT = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, "content");

        /**
         * QName for extra properties [status] that belong to Circabc Document Ref:
         * CProperties@status, Type selection{draft, final, release}, Searchable.
         */
        QName PROP_STATUS = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "status");

        /**
         * QName for extra properties [Issue date] that belong to Circabc Document Ref:
         * CProperties@issue date, Type date time, Searchable.
         */
        QName PROP_ISSUE_DATE = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "issue_date");

        /**
         * QName for extra properties [reference] that belong to Circabc Document Ref:
         * CProperties@reference, Type text, Searchable.
         */
        QName PROP_REFERENCE = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "reference");

        /**
         * QName for extra properties [DynAttr1] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_1 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr1");

        /**
         * QName for extra properties [DynAttr2] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_2 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr2");

        /**
         * QName for extra properties [DynAttr3] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_3 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr3");

        /**
         * QName for extra properties [DynAttr4] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_4 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr4");

        /**
         * QName for extra properties [DynAttr5] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_5 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr5");

        /**
         * QName for extra properties [DynAttr6] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_6 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr6");

        /**
         * QName for extra properties [DynAttr7] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_7 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr7");

        /**
         * QName for extra properties [DynAttr8] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_8 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr8");

        /**
         * QName for extra properties [DynAttr9] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_9 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr9");

        /**
         * QName for extra properties [DynAttr10] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_10 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr10");

        /**
         * QName for extra properties [DynAttr11] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_11 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr11");

        /**
         * QName for extra properties [DynAttr12] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_12 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr12");

        /**
         * QName for extra properties [DynAttr13] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_13 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr13");

        /**
         * QName for extra properties [DynAttr14] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_14 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr14");

        /**
         * QName for extra properties [DynAttr15] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_15 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr15");

        /**
         * QName for extra properties [DynAttr16] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_16 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr16");

        /**
         * QName for extra properties [DynAttr17] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_17 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr17");

        /**
         * QName for extra properties [DynAttr18] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_18 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr18");

        /**
         * QName for extra properties [DynAttr9] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_19 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr19");

        /**
         * QName for extra properties [DynAttr20] that belong to Circabc Document Ref:
         * CProperties@Dynamic attributes, Type text, Searchable.
         */
        QName PROP_DYN_ATTR_20 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr20");

        /**
         * All available dynamic properties name
         */
        List<QName> ALL_DYN_PROPS = Collections.unmodifiableList(Arrays.asList(PROP_DYN_ATTR_1, PROP_DYN_ATTR_2,
                        PROP_DYN_ATTR_3, PROP_DYN_ATTR_4, PROP_DYN_ATTR_5, PROP_DYN_ATTR_6, PROP_DYN_ATTR_7,
                        PROP_DYN_ATTR_8, PROP_DYN_ATTR_9, PROP_DYN_ATTR_10, PROP_DYN_ATTR_11, PROP_DYN_ATTR_12,
                        PROP_DYN_ATTR_13, PROP_DYN_ATTR_14, PROP_DYN_ATTR_15, PROP_DYN_ATTR_16, PROP_DYN_ATTR_17,
                        PROP_DYN_ATTR_18, PROP_DYN_ATTR_19, PROP_DYN_ATTR_20));

        /**
         * QName for extra properties [keyword] that belong to Circabc Document Ref:
         * CProperties@keyword, Type noderef, Translatable, Searchable.
         */
        QName PROP_KEYWORD = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "keyword");

        /**
         * QName for extra properties [security_ranking] that belong to Circabc Document
         * Ref: BProperties@security_ranking, Type selection{PUBLIC(default), INTERNAL,
         * LIMITED}, Searchable.
         */
        QName PROP_SECURITY_RANKING = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "security_ranking");

        /**
         * QName for extra properties [expiration_date] that belong to Circabc Document
         * Ref: BProperties@expiration_date, Type date-time, Searchable.
         */
        QName PROP_EXPIRATION_DATE = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "expiration_date");

        /**
         * QName for mandatory properties [URL] that is applied with the URL able
         * aspect. BProperties@URL, Type text, Searchable.
         */
        QName PROP_URL = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "url");

        String STATUS_VALUE_DRAFT = "DRAFT";
        String STATUS_VALUE_FINAL = "FINAL";
        String STATUS_VALUE_RELEASE = "RELEASE";

        String SECURITY_RANKINGS_NORMAL = "NORMAL";
        String SECURITY_RANKINGS_SENSITIVE = "SENSITIVE";
        String SECURITY_RANKINGS_SPECIAL_HANDLING = "SPECIAL_HANDLING";
        String SECURITY_RANKINGS_PUBLIC = "PUBLIC";
        String SECURITY_RANKINGS_INTERNAL = "INTERNAL";
        String SECURITY_RANKINGS_LIMITED = "LIMITED";

        /**
         * security ranking values list.
         */
        List<String> SECURITY_RANKINGS = Collections.unmodifiableList(Arrays.asList(SECURITY_RANKINGS_NORMAL,
                        SECURITY_RANKINGS_SENSITIVE, SECURITY_RANKINGS_SPECIAL_HANDLING, SECURITY_RANKINGS_PUBLIC,
                        SECURITY_RANKINGS_INTERNAL, SECURITY_RANKINGS_LIMITED));
        /**
         * status values list.
         */
        List<String> STATUS_VALUES = Collections
                        .unmodifiableList(Arrays.asList(STATUS_VALUE_DRAFT, STATUS_VALUE_FINAL, STATUS_VALUE_RELEASE));

        /**
         * Encryptable
         */
        QName ASPECT_ENCRYPTABLE = QName.createQName(DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, "encryptable");
        QName PROP_ENCRYPTEDTEXT1 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "encryptedText");
        QName PROP_ENCRYPTEDTEXT2 = QName.createQName(CIRCABC_DOCUMENT_MODEL_1_0_URI, "encryptedText2");

}
