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
package eu.cec.digit.circabc.action.config;

import org.alfresco.enterprise.repo.management.MBeanSupport;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * MBean to manage the clipboard configuration parameters.
 *
 * @author schwerr
 */
@ManagedResource
public class ClipboardConfigMBean extends MBeanSupport {

  private ClipboardConfig clipboardConfig = null;

  /**
   * Gets the value of the clipboardConfig
   *
   * @return the clipboardConfig
   */
  public ClipboardConfig getClipboardConfig() {
    return clipboardConfig;
  }

  /**
   * Sets the value of the clipboardConfig
   *
   * @param clipboardConfig the clipboardConfig to set.
   */
  public void setClipboardConfig(ClipboardConfig clipboardConfig) {
    this.clipboardConfig = clipboardConfig;
  }

  /**
   * Gets the value of the downloadLimitMB
   *
   * @return the downloadLimitMB
   */
  @ManagedAttribute(description = "Get the download limit in megabatyes")
  public Long getDownloadLimitMB() {
    return clipboardConfig.getDownloadLimitMB();
  }

  /**
   * Sets the value of the downloadLimitMB
   *
   * @param downloadLimitMB the downloadLimitMB to set.
   */
  @ManagedAttribute(description = "Set the download limit in megabatyes")
  public void setDownloadLimitMB(Long downloadLimitMB) {
    clipboardConfig.setDownloadLimitMB(downloadLimitMB);
  }
}
