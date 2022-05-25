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
package eu.cec.digit.circabc.service.compress;

// import java.util.zip.ZipEntry;

import de.schlichtherle.util.zip.ZipEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompressedEntryImpl implements CompressedEntry {

    private static final Log logger = LogFactory.getLog(CompressedEntryImpl.class);
    private String fileName;
    private long fileSize;
    private long fileCompressedSize;
    private String comment;
    private boolean isDirectory;
    private long crc;
    private long time;

    public CompressedEntryImpl(final ZipEntry e) {
        fileName = e.getName();
        fileSize = e.getSize();
        fileCompressedSize = e.getCompressedSize();
        isDirectory = e.isDirectory();
        comment = e.getComment();
        crc = e.getCrc();
        time = e.getTime();
        if (logger.isDebugEnabled()) {
            logger.debug("new Compressed Entry:" + this);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getComment() {
        return comment;
    }

    public long getFileCompressedSize() {
        return fileCompressedSize;
    }

    public long getCrc() {
        return crc;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {

        return fileName;
    }
}
