/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.report.impl;

import java.io.IOException;
import java.io.Writer;

import eu.cec.digit.circabc.migration.entities.adapter.DateAdapterUtils;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.migration.journal.report.ReportDecoratorBase;


/**
 * A decorator to draw a report in an xml format
 *
 * @author Yanick Pignot
 */
public class XmlReportDecorator extends ReportDecoratorBase
{
	/** */
	private static final long serialVersionUID = 260181441956541451L;

	private static final String REPORT = "report";
	private static final String SECTION = "section";
	private static final String SUBSECTION = "subsection";
	private static final String LINE = "line";

	private final boolean prettyPrint;

	public XmlReportDecorator(final Report report)
    {
       this(report, false);
    }

	public XmlReportDecorator(final Report report, final boolean prettyPrint)
    {
       super(report);
       this.prettyPrint = prettyPrint;
    }

	/* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportLog#writeReport(java.io.Writer)
     */
    @Override
    public void writeReport(final Writer writer) throws IOException
    {
    	writeHeader(writer);

        for(Section section : report.getSections())
        {
        	prettyXML(writer, 1);
            writeSection(writer, section.getTitle());

            for(SubSection subSection : section.getSubSections())
            {
            	prettyXML(writer, 2);
                writeSubSection(writer, subSection.getTitle());

                for(String line : subSection.getLines())
                {
                	prettyXML(writer, 3);
                    writeLine(writer, line);
                }

                prettyXML(writer, 2);
                closeSubSection(writer);
            }

            prettyXML(writer, 1);
            closeSection(writer);
        }

        prettyXML(writer, 0);
        writeFooter(writer);
    }

    private void closeSection(final Writer writer) throws IOException
	{
    	writer.append("</");
		writer.append(SECTION);
		writer.append(">");
	}

	private void closeSubSection(final Writer writer) throws IOException
	{
		writer.append("</");
		writer.append(SUBSECTION);
		writer.append(">");
	}

	private void prettyXML(final Writer writer, final int level) throws IOException
	{
		if(prettyPrint)
		{
			writer.append('\n');
			for(int x = 0; x < level; ++x)
			{
				writer.append('\t')	;
			}
		}
	}

	/* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeFooter(java.io.Writer)
     */
    public void writeFooter(final Writer writer) throws IOException
    {
    	writer.append("</");
		writer.append(REPORT);
		writer.append(">");
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeHeader(java.io.Writer)
     */
    public void writeHeader(final Writer writer) throws IOException
    {
    	writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

    	writer.append("<");
		writer.append(REPORT);
		writer.append(" date=\"");
    	writer.append(DateAdapterUtils.marshalDateTime(report.getCreationTime()));
    	writer.append("\">");
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeLine(java.io.Writer, java.lang.String)
     */
    public void writeLine(final Writer writer, final String line) throws IOException
    {
    	writer.append("<");
		writer.append(LINE);
		writer.append(">");
    	writer.append(line);
    	writer.append("</");
		writer.append(LINE);
		writer.append(">");

    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeSection(java.io.Writer, java.lang.String)
     */
    public void writeSection(final Writer writer, final String sectionTitle) throws IOException
    {
    	writer.append("<");
		writer.append(SECTION);
		writer.append(" title=\"");
    	writer.append(sectionTitle);
    	writer.append("\">");
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.ReportDecorator#writeSubSection(java.io.Writer, java.lang.String)
     */
    public void writeSubSection(final Writer writer, final String subSectionTitle) throws IOException
    {
    	writer.append("<");
		writer.append(SUBSECTION);
		writer.append(" title=\"");
    	writer.append(subSectionTitle);
    	writer.append("\">");
    }

    protected void startWrittingLines(Writer writer){}

    protected void stopWrittingLines(Writer writer){}
}
