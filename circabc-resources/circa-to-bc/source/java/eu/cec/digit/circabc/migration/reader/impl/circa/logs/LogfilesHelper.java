/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.logs;

import java.util.StringTokenizer;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.TitledNode;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.validation.impl.NodeReferencesValidator;

/**
 * @author Yanick Pignot
 *
 */
/*package*/ abstract class LogfilesHelper
{
	private static final String REMOTE_NEWSGROUP_REGEX = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\:.+";

	/*package*/ static final String COMA = ",";
	/*package*/ static final String COLON = ":";

	private LogfilesHelper(){}

	/*package*/ static final StringBuilder computeIGPath(final InterestGroup ig)
	{
		final StringBuilder builder = new StringBuilder(NodeReferencesValidator.CIRCABC_REFERENCE);
		final Category category = ElementsHelper.getElementCategory(ig);

		builder
         	.append(FileClient.PATH_SEPARATOR)
         	.append(category.getName().getValue())
         	.append(FileClient.PATH_SEPARATOR)
         	.append(ig.getName().getValue());

		return builder;
	}

	/*package*/ static final StringBuilder computeIGServicePath(final InterestGroup ig, final Class<? extends TitledNode> serviceClass)
	{
		return computeIGPath(ig)
					.append(FileClient.PATH_SEPARATOR)
					.append(serviceClass.getSimpleName());
	}

	/*package*/ static final String getEndOfLine(final String line, final String separator)
	{
		if(line == null)
		{
			return "";
		}
		else if(separator == null)
		{
			return line;
		}
		else
		{
			final int idx = line.indexOf(separator);

			if(idx < 0)
			{
				return "";
			}
			else
			{
				return line.substring(idx + 1);
			}
		}
	}

	/*package*/  static final String isolatePath(final String line, final boolean isNewsgroup)
	{
		if(line == null)
		{
			return "";
		}
		else
		{
			String newsSeparator = null;
			int startIndex = 0;
			int endIndex = line.length();
			if(isNewsgroup && line.matches(REMOTE_NEWSGROUP_REGEX))
			{
				startIndex = line.indexOf(COLON) + 1;
			}
			else if (isNewsgroup)
			{
				newsSeparator = getNewsgroupSeparator(line);
				endIndex = newsSeparator != null ? line.indexOf(newsSeparator) : endIndex;
			}
			else
			{
				endIndex = line.indexOf(COLON);
			}

			if(endIndex < 0)
			{
				return line;
			}
			else
			{
				String path = line.substring(startIndex, endIndex);

				if(isNewsgroup && COLON.equals(newsSeparator))
				{
					// transform europa/cat/ig/forum.5 to europa.cat.ig.forum:5
					path = path.replace(".", ":");
					path = path.replace("/", ".");
				}

				return path;
			}
		}
	}

	/*package*/  static final String getNewsgroupSeparator(final String logInfo)
	{
		if(logInfo.startsWith("europa/"))
		{
			return COLON;
		}
		else if(logInfo.startsWith("europa."))
		{
			return COMA;
		}
		else
		{
			return null;
		}
	}

	/*package*/ static final String retreiveForumName(final String logInfo)
	{

		try
		{
			final StringTokenizer tokens = new StringTokenizer(logInfo, ".", false);

			tokens.nextToken(); // remove europa
			tokens.nextToken(); // remove catName
			tokens.nextToken(); // remove igName

			return tokens.nextToken(":").substring(1);
		}
		catch(java.util.NoSuchElementException e)
		{
			// probably the newsgroup name itself.
			return logInfo;
		}
	}
}
