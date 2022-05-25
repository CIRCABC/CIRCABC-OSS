/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.report;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;


/**
 *  Base decorator to draw a report
 *
 * @author Yanick Pignot
 */
public abstract class ReportDecoratorBase implements ReportDecorator
{

    protected final Report report;

    public ReportDecoratorBase(Report report)
    {
        this.report = report;
    }


    public void appendSection(String title)
    {
        this.report.appendSection(title);
    }

    public void appendSubSection(String title)
    {
        this.report.appendSubSection(title);
    }

    public void appendLine(String line)
    {
        this.report.appendLine(line);
    }

    public void appendLines(List<String> lines)
    {
        this.report.appendLines(lines);
    }

    public List<? extends Section> getSections()
    {
    	return report.getSections();
    }

    public Date getCreationTime()
    {
    	return report.getCreationTime();
    }

    public void writeReport(Writer writer) throws IOException
    {
    	writeHeader(writer);

        for(Section section : report.getSections())
        {
            writeSection(writer, section.getTitle());

            for(SubSection subSection : section.getSubSections())
            {
                writeSubSection(writer, subSection.getTitle());

                startWrittingLines(writer);

                for(String line : subSection.getLines())
                {
                    writeLine(writer, line);
                }

                stopWrittingLines(writer);
            }
        }
        writeFooter(writer);
    }

    protected abstract void startWrittingLines(Writer writer) throws IOException;

    protected abstract void stopWrittingLines(Writer writer) throws IOException;
}
