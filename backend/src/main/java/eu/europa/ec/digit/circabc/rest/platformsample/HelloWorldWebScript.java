/**
 * Copyright (C) 2017 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.digit.circabc.rest.platformsample;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * A demonstration Java controller for the "Hello World" sample Web Script,
 * shipped as part of the Alfresco SDK platform sample.
 *
 * <p>This is a read-only Alfresco {@link DeclarativeWebScript} whose controller
 * populates the response model consumed by the associated FreeMarker template.
 * It serves as a minimal example of how a Java-backed web script is wired into
 * the Alfresco Web Scripts framework, exposing a single value ({@code fromJava})
 * that the template can render.</p>
 *
 * @author martin.bergljung@alfresco.com
 * @since 2.1.0
 */
public class HelloWorldWebScript extends DeclarativeWebScript {

  /** Logger used to trace invocations of this web script. */
  private static Log logger = LogFactory.getLog(HelloWorldWebScript.class);

  /**
   * Builds the response model for the Hello World web script.
   *
   * <p>Adds a single {@code fromJava} entry to the model map so the FreeMarker
   * template can render a value produced by this Java controller. No request
   * parameters are read and no repository state is modified.</p>
   *
   * @param req the incoming web script request
   * @param status the response status, which may be updated by the controller
   * @param cache the cache directives to apply to the response
   * @return the model map passed to the FreeMarker template for rendering
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>();
    model.put("fromJava", "HelloFromJava");

    logger.debug("Your 'Hello World' Web Script was called!");

    return model;
  }
}
