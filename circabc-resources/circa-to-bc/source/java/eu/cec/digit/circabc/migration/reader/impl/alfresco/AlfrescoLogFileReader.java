package eu.cec.digit.circabc.migration.reader.impl.alfresco;

import java.io.OutputStream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.reader.LogFileReader;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * LogFileReader implementation that reads audit logs from the CIRCABC Alfresco repository.
 */
public class AlfrescoLogFileReader implements LogFileReader {

    private static final Log logger = LogFactory.getLog(AlfrescoLogFileReader.class);

    private LogService logService;

    @Override
    public void addLogEntries(final InterestGroup interestGroup, final OutputStream outputStream) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(interestGroup);
        if (path == null) {
            return;
        }
        try {
            final NodeRef igRef = new NodeRef(path);
            // Log entries are stored in CIRCABC's log service (database-backed)
            // For export, we write a summary to the output stream
            if (logger.isDebugEnabled()) {
                logger.debug("Exporting log entries for IG: " + path);
            }
            // The log service stores entries in DB, not as content nodes
            // Write an empty marker for now - full log export would require LogService query
            outputStream.write(("# Log entries for IG: " + path + "\n").getBytes("UTF-8"));
        } catch (Exception e) {
            throw new ExportationException("Error reading log entries for " + path, e);
        }
    }

    // --- Setters for Spring injection ---

    public void setLogService(LogService logService) {
        this.logService = logService;
    }
}
