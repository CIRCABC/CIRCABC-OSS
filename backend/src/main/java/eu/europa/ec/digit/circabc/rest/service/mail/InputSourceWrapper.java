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
package eu.europa.ec.digit.circabc.rest.service.mail;

import java.io.IOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.springframework.core.io.InputStreamSource;

/**
 * Adapts an Alfresco {@link ContentReader} to the Spring {@link InputStreamSource}
 * contract so that repository content can be consumed by APIs (such as mail
 * attachments) that expect a Spring input stream source.
 *
 * <p>The wrapper defers reading until {@link #getInputStream()} is invoked and
 * transparently re-obtains a fresh reader if the underlying one has already been
 * closed, since an Alfresco {@code ContentReader} is single-use.
 *
 * @author Ph Dubois
 * @author Roy Wetherall
 * <p>
 * 26-juin-07 - 14:29:46
 */
public class InputSourceWrapper implements InputStreamSource {

  /** The Alfresco content reader providing access to the underlying repository content. */
  ContentReader cr = null;

  /**
   * Creates a wrapper around the given Alfresco content reader.
   *
   * @param cr the {@link ContentReader} whose content will be exposed as an input stream
   */
  public InputSourceWrapper(final ContentReader cr) {
    this.cr = cr;
  }

  /**
   * Returns an input stream for the wrapped content.
   *
   * <p>If the underlying reader has already been closed (content readers are
   * single-use in Alfresco), a fresh reader is obtained before the stream is opened.
   *
   * @return an {@link java.io.InputStream} over the content, or {@code null} if the
   *         content does not exist
   * @throws IOException if the content stream cannot be opened
   * @see org.springframework.core.io.InputStreamSource#getInputStream()
   */
  public java.io.InputStream getInputStream() throws IOException {
    if (cr.exists()) {
      if (cr.isClosed()) {
        cr = cr.getReader();
      }
      return cr.getContentInputStream();
    }
    return null;
  }
}
