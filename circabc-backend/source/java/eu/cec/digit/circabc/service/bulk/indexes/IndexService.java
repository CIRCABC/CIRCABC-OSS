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
package eu.cec.digit.circabc.service.bulk.indexes;

import eu.cec.digit.circabc.service.bulk.indexes.message.ValidationMessage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IndexService {

    String INDEX_FILE = "index.txt";

    IndexHeaders getIndexHeaders();

    void getIndexRecords(
            final File indexFile,
            final List<IndexRecord> indexRecords,
            final List<ValidationMessage> messages)
            throws IOException;

    void generateIndexRecords(final File indexFile, final List<IndexRecord> indexRecords)
            throws IOException;

    interface Headers {

        String NAME = "NAME";
        String TITLE = "TITLE";
        String DESCRIPTION = "DESCRIPTION";
        String AUTHOR = "AUTHOR";
        String KEYWORDS = "KEYWORDS";
        String STATUS = "STATUS";
        String ISSUE_DATE = "ISSUE DATE";
        String REFERENCE = "REFERENCE";
        String EXPIRATION_DATE = "EXPIRDATE";
        String SECURITY_RANKING = "SECRANK";
        String ATTRIPREFIX = "ATTRI";
        String ATTRI1 = "ATTRI1";
        String ATTRI2 = "ATTRI2";
        String ATTRI3 = "ATTRI3";
        String ATTRI4 = "ATTRI4";
        String ATTRI5 = "ATTRI5";
        String ATTRI6 = "ATTRI6";
        String ATTRI7 = "ATTRI7";
        String ATTRI8 = "ATTRI8";
        String ATTRI9 = "ATTRI9";
        String ATTRI10 = "ATTRI10";
        String ATTRI11 = "ATTRI11";
        String ATTRI12 = "ATTRI12";
        String ATTRI13 = "ATTRI13";
        String ATTRI14 = "ATTRI14";
        String ATTRI15 = "ATTRI15";
        String ATTRI16 = "ATTRI16";
        String ATTRI17 = "ATTRI17";
        String ATTRI18 = "ATTRI18";
        String ATTRI19 = "ATTRI19";
        String ATTRI20 = "ATTRI20";
        String TYPE_DOCUMENT = "TYPE";
        String TRANSLATOR = "TRANSLATOR";
        String DOC_LANG = "LANG";
        String NO_CONTENT = "NOCONTENT";
        String ORI_LANG = "ORILANG";
        String REL_TRANS = "RELTRANS";
        String OVERWRITE = "OVERWRITE";
    }
}
