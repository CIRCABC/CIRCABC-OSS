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

/**
 * Holds shared constants for the CIRCABC Alfresco content model.
 *
 * <p>This is a non-instantiable utility class that centralizes model-level
 * definitions used across the CIRCABC content model, most notably the XML
 * namespace URI under which CIRCABC-specific types, aspects and properties are
 * declared in the Alfresco repository.
 */
public final class BaseCircabcModel {

  /**
   * Private constructor to prevent instantiation
   */
  private BaseCircabcModel() {
    // Utility class should not be instantiated
  }

  /**
   * XML namespace URI for the CIRCABC content model.
   *
   * <p>Used to qualify CIRCABC-specific type, aspect and property names when
   * interacting with the Alfresco repository.
   */
  public static final String CIRCABC_NAMESPACE = "http://www.cc.cec/circabc";
}
