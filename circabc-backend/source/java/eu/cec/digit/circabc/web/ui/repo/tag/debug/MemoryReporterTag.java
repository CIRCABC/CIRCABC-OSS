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
package eu.cec.digit.circabc.web.ui.repo.tag.debug;

import org.alfresco.web.ui.common.tag.debug.BaseDebugTag;

/**
 * Tag implementation used to place the Circabc specific memory usage and cache management.
 *
 * @author Yanick Pignot
 */
public class MemoryReporterTag extends BaseDebugTag {

  /**
   * @see javax.faces.webapp.UIComponentTag#getComponentType()
   */
  public String getComponentType() {
    return "eu.cec.digit.circabc.faces.debug.MemoryReporter";
  }
}
