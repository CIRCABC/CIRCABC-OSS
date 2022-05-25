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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;

public class IndexHeadersImpl implements IndexHeaders {

    private static Log logger = LogFactory.getLog(IndexHeadersImpl.class);
    private final List<IndexHeader> headerList = new LinkedList<>();

    public IndexHeadersImpl() {
    }

    public IndexHeadersImpl(final String[] headers) {
        boolean found = false;
        for (final String header : headers) {
            for (final IndexHeader indexHeader : headerList) {
                if (indexHeader.getHeaderName().equals(header)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                addHeader(new IndexHeaderImpl(header, null));
            }
        }
    }

    public void addHeader(final IndexHeader header) {
        if (logger.isDebugEnabled()) {
            logger.debug("addHeader:" + header.getHeaderName());
        }
        ((LinkedList<IndexHeader>) this.headerList).addLast(header);
    }

    public List<IndexHeader> getHeaders() {

        return this.headerList;
    }

    public IndexHeader getHeader(final String headerName) {
        for (final IndexHeader indexHeader : headerList) {
            if (indexHeader.getHeaderName().equals(headerName)) {
                return indexHeader;
            }
        }
        return null;
    }
}
