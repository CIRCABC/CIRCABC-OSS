/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.report.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.cec.digit.circabc.migration.journal.report.Report;


/**
 * Wraper object that define a simple 3 level report.
 *
 * @author Yanick Pignot
 */
public class ReportLog implements Report
{
	/** */
	private static final long serialVersionUID = 4455407912028651317L;

	private final List<SectionImpl> sections;
	private final Date date;

	public ReportLog()
    {
    	this.sections = new ArrayList<SectionImpl>();
    	date = new Date();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.Report#appendSection(java.lang.String)
     */
    public void appendSection(String title)
    {
    	this.sections.add(new SectionImpl(title));
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.Report#appendSubSection(java.lang.String)
     */
    public void appendSubSection(String title)
    {
    	getLastSection().appendSubSections(new SubSectionImpl(title));
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.Report#appendLine(java.lang.String)
     */
    public void appendLine(String line)
    {
    	((SubSectionImpl)getLastSection().getLastSubSection()).appendLine(line);
    }


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.Report#appendLines(java.util.List)
     */
    public void appendLines(List<String> lines)
    {
    	final SubSectionImpl subsection = (SubSectionImpl) getLastSection().getLastSubSection();

    	for(final String line : lines)
    	{
    		subsection.appendLine(line);
    	}
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.Report#writeReport(java.io.Writer)
     */
    public void writeReport(Writer writer) throws IOException
    {
    	writer.append(sections.toString());
    }


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.Report#getSections()
     */
    public List<? extends Section> getSections()
    {
    	return sections;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.migration.report.Report#getCreationTime()
     */
    public Date getCreationTime()
    {
    	return date;
    }

    private SectionImpl getLastSection()
    {
    	if(sections.size() > 0)
    	{
    		return sections.get(sections.size() - 1);
    	}
    	else
    	{
    		sections.add(new SectionImpl(""));

    		return getLastSection();
    	}
    }


    /**
     * Wrap a section of the logger
     */
    private class SectionImpl implements Section
    {
        private final String title;
        private final List<SubSection> subSections;

        private SectionImpl(String title)
        {
            this.title = title;
            subSections = new ArrayList<SubSection>();
        }

        /**
         * @return the title
         */
        public final String getTitle()
        {
            return title;
        }

        /**
         * @return the subSections
         */
        public final List<SubSection> getSubSections()
        {
            return subSections;
        }

        @Override
        public String toString()
        {
        	return "Section (" + title + "). Subsections: " + subSections;
        }

        /**
         * Append a new sub Section
         *
         * @param subSection		the subscetion to append
         */
        private void appendSubSections(SubSection subSection)
        {
            subSections.add(subSection);
        }

        /**
         * @return 			the last subsection of the list
         */
        private SubSection getLastSubSection()
        {
        	if(subSections.size() > 0)
        	{
        		return subSections.get(subSections.size() - 1);
        	}
        	else
        	{
        		subSections.add(new SubSectionImpl(""));

        		return getLastSubSection();
        	}
        }
    }

    /**
     * Wrap a subsection of the logger
     */
    private class SubSectionImpl implements SubSection
    {
        private final String title;
        private final List<String> lines;

        private SubSectionImpl(String title)
        {
            this.title = title;
            lines = new ArrayList<String>();
        }

        /**
         * Append a new line
         *
         * @param line		the line to append
         */
        private void appendLine(String line)
        {
            lines.add(line);
        }

        /**
         * @return the title
         */
        public final String getTitle()
        {
            return title;
        }

        /**
         * @return the lines
         */
        public final List<String> getLines()
        {
            return lines;
        }

        @Override
        public String toString()
        {
        	return "Subsection (" + title + "). Lines: " + lines;
        }
    }
}
