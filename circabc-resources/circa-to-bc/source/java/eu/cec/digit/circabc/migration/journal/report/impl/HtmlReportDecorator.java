/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.report.impl;

import java.io.IOException;
import java.io.Writer;

import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.migration.journal.report.ReportDecoratorBase;


/**
 * A simple decorator to draw a report in an HTML format
 *
 * @author Yanick Pignot
 */
public class HtmlReportDecorator extends ReportDecoratorBase
{
	/** */
	private static final long serialVersionUID = -6511234555338645423L;

	public HtmlReportDecorator(Report report)
    {
       super(report);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeFooter(java.io.Writer)
     */
    public void writeFooter(Writer writer) throws IOException
    {
    	writer.append("<br /><h1><u><i>End of report.</i></u></h1><br/>");
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeHeader(java.io.Writer)
     */
    public void writeHeader(Writer writer) throws IOException
    {
    	writer.append("<h1><u><i>Report of the import on ");
    	writer.append(report.getCreationTime().toString());
    	writer.append("</i></u></h1><br/><br/>");
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeLine(java.io.Writer, java.lang.String)
     */
    public void writeLine(Writer writer, String line) throws IOException
    {
    	writer.append("<u>");
    	writer.append(line);
    	writer.append("</u>");
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeSection(java.io.Writer, java.lang.String)
     */
    public void writeSection(Writer writer, String sectionTitle) throws IOException
    {
    	writer.append("<b>");
    	writer.append(sectionTitle);
    	writer.append("</b><br />");
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeSubSection(java.io.Writer, java.lang.String)
     */
    public void writeSubSection(Writer writer, String subSectionTitle) throws IOException
    {
    	writer.append("&nbsp&nbsp");
    	writer.append(subSectionTitle);
    	writer.append("<br />");
    }

    protected void startWrittingLines(Writer writer) throws IOException
    {
    	writer.append("<li>");
    }

    protected void stopWrittingLines(Writer writer) throws IOException
    {
    	writer.append("</li>");
    }
}
