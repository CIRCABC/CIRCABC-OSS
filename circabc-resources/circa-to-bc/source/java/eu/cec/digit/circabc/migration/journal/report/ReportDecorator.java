/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.report;

import java.io.IOException;
import java.io.Writer;

/**
 * Extended report to offer extended capabilities of export
 *
 * @author Yanick Pignot
 */
public interface ReportDecorator extends Report
{

	/**
     * Write the header of the report
     *
     * @param writer				The output
     * @throws IOException			For writer errors
     */
    public void writeHeader(final Writer writer) throws IOException;

    /**
     * Write the footer of the report
     *
     * @param writer				The output
     * @throws IOException			For writer errors
     */
    public void writeFooter(final Writer writer) throws IOException;

    /**
     * Write a section title
     *
     * @param writer					The output
     * @param sectionTitle				The title of the section
     * @throws IOException				For writer errors
     */
    public void writeSection(final Writer writer, final String sectionTitle) throws IOException;

    /**
     * Write a subsection title
     *
     * @param writer					The output
     * @param subSectionTitle			The title of the subsection
     * @throws IOException				For writer errors
     */
    public void writeSubSection(final Writer writer, final String subSectionTitle) throws IOException;

    /**
     * Write a terminal line of the report
     *
     * @param writer				The output
     * @param line					A line of the subsection
     * @throws IOException			For writer errors
     */
    public void writeLine(final Writer writer, final String line) throws IOException;


}