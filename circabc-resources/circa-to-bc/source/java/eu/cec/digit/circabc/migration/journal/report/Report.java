/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.report;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Date;
import java.util.List;


/**
 * Wraper object that define a simple 3 level report. This object can be easyly convert in any format using the Report Decorator
 *
 * @see eu.cec.digit.circabc.migration.journal.report.impl.TextReportDecorator
 *
 * @author Yanick Pignot
 */
public interface Report extends Serializable
{

	/**
	 * Append a new section
	 *
	 * @param title				the title of the section
	 */
	public abstract void appendSection(String title);

	/**
	 * Append a new subsection to the last section
	 *
	 * @param title				the title of the subsection
	 */
	public abstract void appendSubSection(String title);

	/**
	 * Append a new line to the last subsection of the last section
	 *
	 * @param line				the line to append
	 */
	public abstract void appendLine(String line);

	/**
	 * Append new lines to the last subsection of the last section
	 *
	 * @param line				the line to append
	 */
	public abstract void appendLines(List<String> lines);


	/**
	 * When the report has started
	 *
	 * @return				The start time of the report building
	 */
	public abstract Date getCreationTime();

    /**
     * Write the report in the given writer
     *
     * @param writer			The output writter
     * @throws IOException
     */
    public void writeReport(Writer writer) throws IOException;


    /**
     * Get all sections
     *
     * @return				The list of sections
     */
    public List<? extends Section> getSections();


    /**
     * Wrap a section of the logger
     */
    public interface Section
    {
        /**
         * @return the title
         */
        public String getTitle();

        /**
         * @return the subSections
         */
        public List<SubSection> getSubSections();
    }

    /**
     * Wrap a subsection of the logger
     */
    public interface SubSection
    {

        /**
         * @return the title
         */
        public String getTitle();

        /**
         * @return the lines
         */
        public List<String> getLines();

    }

}