package eu.cec.digit.circabc.util.exporter;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.view.ExportPackageHandler;
import org.alfresco.service.cmr.view.ExporterException;

import java.io.OutputStream;


/**
 * Exporter Service
 *
 * @author David Caruana
 */
public interface CircabcExporterService {

    /**
     * Export a view of the Repository using the default xml view schema.
     * <p>
     * All repository information is exported to the single output stream.  This means that any
     * content properties are base64 encoded.
     *
     * @param viewWriter the output stream to export to
     * @param parameters export parameters
     * @param progress   exporter callback for tracking progress of export
     */
    @Auditable(parameters = {"viewWriter", "parameters", "progress"})
    void exportView(OutputStream viewWriter, ExporterCrawlerParameters parameters, Exporter progress)
            throws ExporterException;

    /**
     * Export a view of the Repository using the default xml view schema.
     * <p>
     * This export supports the custom handling of content properties.
     *
     * @param exportHandler the custom export handler for content properties
     * @param parameters    export parameters
     * @param progress      exporter callback for tracking progress of export
     */
    @Auditable(parameters = {"exportHandler", "parameters", "progress"})
    void exportView(ExportPackageHandler exportHandler, ExporterCrawlerParameters parameters,
                    Exporter progress)
            throws ExporterException;


    /**
     * Export a view of the Repository using a custom crawler and exporter.
     *
     * @param exporter   custom exporter
     * @param parameters export parameters
     * @param progress   exporter callback for tracking progress of export
     */
    @Auditable(parameters = {"exporter", "parameters", "progress"})
    void exportView(Exporter exporter, ExporterCrawlerParameters parameters, Exporter progress);

}
