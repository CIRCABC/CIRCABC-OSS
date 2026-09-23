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
 * Defines the Alfresco content model constants for the CIRCABC survey
 * specification.
 *
 * <p>This is a non-instantiable utility holder that centralizes the namespace
 * URI, model prefix and {@link QName} type definitions used to represent survey
 * content within the Alfresco repository. The values declared here mirror the
 * corresponding XML content model definition and are referenced throughout the
 * survey-related services and web scripts.
 *
 * @author yanick pignot
 */
public final class SurveyModel {

  /**
   * Private constructor to prevent instantiation of this constants-only
   * utility class.
   */
  private SurveyModel() {}

  /**
   * Circabc Survey namespace
   */
  public static final String CIRCABC_SURVEY_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/survey/1.0";

  /**
   * Circabc Model Prefix
   */
  public static final String CIRCABC_SURVEY_MODEL_PREFIX = "su";

  /**
   * Survey Type name
   */
  public static final QName TYPE_SURVEY_SPACE = QName.createQName(
    CIRCABC_SURVEY_MODEL_1_0_URI,
    "surveys"
  );
}
