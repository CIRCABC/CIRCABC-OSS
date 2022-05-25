/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.logs;

import static eu.cec.digit.circabc.migration.reader.impl.circa.logs.LogfilesHelper.COLON;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;

/**
 * Base interface that helps to parse a Circa log file description (info column) to a circabc like info column
 *
 * @author Yanick Pignot
 */
public interface CircaInfoParser
{
	/**
	 * Get an interest group instance, a circa log line action and a circa info logline and compute a circabc like info column for business auditing
	 *
	 * @param interestGroup
	 * @param circaAction
	 * @param circaDescription
	 * @return
	 */
	public abstract String parseInfo(final InterestGroup interestGroup, final String circaAction, final String circaDescription);


	/**
	 * Return the end part of the info line, after the first coma or colon
	 *
	 * @author Yanick Pignot
	 */
	public class NewsgroupInfoParser implements CircaInfoParser
	{
		public String parseInfo(final InterestGroup interestGroup, final String circaAction, final String circaDescription)
		{
			if(circaDescription == null)
			{
				return "";
			}
			final String separator = LogfilesHelper.getNewsgroupSeparator(circaDescription);

			return LogfilesHelper.getEndOfLine(circaDescription, separator);
		}
	}

	/**
	 * Return the end part of the info line, after the first colon
	 *
	 * @author Yanick Pignot
	 */
	public class AfterColonInfoParser implements CircaInfoParser
	{
		public String parseInfo(final InterestGroup interestGroup, final String circaAction, final String circaDescription)
		{
			return LogfilesHelper.getEndOfLine(circaDescription, COLON);
		}
	}

	/**
	 * Return always nothing (an empty String)
	 *
	 * @author Yanick Pignot
	 */
	public class NoInfoParser implements CircaInfoParser
	{
		public String parseInfo(final InterestGroup interestGroup, final String circaAction, final String circaDescription)
		{
			return "";
		}
	}

	/**
	 * Return always circaDescription passed in parameter
	 *
	 * @author Yanick Pignot
	 */
	public class SimpleInfoParser implements CircaInfoParser
	{
		public String parseInfo(final InterestGroup interestGroup, final String circaAction, final String circaDescription)
		{
			return circaDescription;
		}
	}



}
