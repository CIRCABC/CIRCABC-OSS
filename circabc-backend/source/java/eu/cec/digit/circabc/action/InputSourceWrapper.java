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
package eu.cec.digit.circabc.action;

import org.alfresco.service.cmr.repository.ContentReader;
import org.springframework.core.io.InputStreamSource;

import java.io.IOException;

/**
 * @author Ph Dubois
 * @author Roy Wetherall
 * <p>
 * 26-juin-07 - 14:29:46
 */
public class InputSourceWrapper implements InputStreamSource {

    ContentReader cr = null;

    /**
     * constructor.
     *
     * @param cr ContentReader
     */
    public InputSourceWrapper(final ContentReader cr) {
        this.cr = cr;
    }


    /**
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
