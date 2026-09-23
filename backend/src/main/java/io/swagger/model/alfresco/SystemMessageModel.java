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
 * Content model constants for the CIRCABC "system message" feature.
 *
 * <p>This is a non-instantiable utility holder that centralizes the Alfresco
 * {@link QName} definitions (namespace URI, prefix, content type and properties)
 * describing the banner/notice message that can be displayed system-wide in
 * CIRCABC. These constants are used across the REST layer and services to read
 * and update the system message node in the Alfresco repository.
 */
public final class SystemMessageModel {

  /** Prevents instantiation of this constants-only utility class. */
  private SystemMessageModel() {}

  /** Namespace URI of the CIRCABC system message content model (version 1.0). */
  public static final String CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/systemmessage/1.0";

  /** Short namespace prefix associated with the system message model. */
  public static final String CIRCABC_SYSTEMMESSAGE_MODEL_PREFIX = "sm";

  /** Content type representing a system message node in the repository. */
  public static final QName TYPE_SYSTEMMESSAGE = QName.createQName(
    CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI,
    "systemMessage"
  );

  /**
   * Boolean property indicating whether the system message is currently enabled
   * and should be displayed to users.
   */
  public static final QName PROP_IS_SYSTEMMESSAGE_ENABLED = QName.createQName(
    CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI,
    "isSytemMessageEnabled"
  );

  /** Text property holding the content of the system message to display. */
  public static final QName PROP_SYSTEMMESSAGE_TEXT = QName.createQName(
    CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI,
    "systemMessageText"
  );
}
