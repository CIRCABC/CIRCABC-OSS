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
 * Constants that describe the CIRCABC "dossier" Alfresco content model.
 *
 * <p>This is a non-instantiable utility holder that centralises the namespace
 * URI, the namespace prefix and the {@link QName} identifiers defined by the
 * dossier model. These constants are used across the REST layer to reference
 * dossier types and aspects when reading from or writing to the Alfresco
 * repository, ensuring model identifiers are declared in a single place.
 *
 * @author Slobodan Filipovic
 */
public final class DossierModel {

  /**
   * Prevents instantiation; this class only exposes static model constants.
   */
  private DossierModel() {}

  /**
   * Namespace URI of the CIRCABC dossier content model (version 1.0). Built by
   * appending the model path to the shared CIRCABC namespace root.
   */
  public static final String CIRCABC_DOSSIER_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/dossier/1.0";

  /**
   * Short prefix bound to the dossier model namespace ({@code "do"}), used when
   * referencing dossier types and aspects with prefixed names.
   */
  public static final String CIRCABC_DOSSIER_MODEL_PREFIX = "do";

  /**
   * Qualified name of the dossier space content type ({@code do:dossier})
   * within the dossier model namespace.
   */
  public static final QName TYPE_DOSSIER_SPACE = QName.createQName(
    CIRCABC_DOSSIER_MODEL_1_0_URI,
    "dossier"
  );
}
