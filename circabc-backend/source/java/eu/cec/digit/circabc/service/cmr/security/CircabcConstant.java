/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.cmr.security;

public class CircabcConstant {

    /**
     * The guest authority
     */
    public static final String GUEST_AUTHORITY = "guest";
    public static final String REGISTERED_AUTHORITY = "EVERYONE";
    /**
     * session key for BulkDownloadServlet to save file path to download
     */
    public static final String BULK_DOWNLOAD_FILE = "bdf";

    private CircabcConstant() {
    }
}
