/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.JXPathValidator;

/**
 * Validate the keywords setted in the given object graph
 *
 * @author yanick pignot
 */
public class KeywordsValidator extends JXPathValidator
{

	private static final String KEYWORDS_IDS = ".//keywords/ids";
	private static final String KEYWORD_DEFINITIONS_ID = ".//keywordDefinitions/definitions/id";

	@Override
	protected void validateCircabcImpl(JXPathContext context, final MigrationTracer<ImportRoot> journal)
	{
		for(final InterestGroup interestGroup: getInterestGroups(context))
		{
			checkKeywords(interestGroup, journal);
		}
	}

	@SuppressWarnings("unchecked")
	private void checkKeywords(final InterestGroup interstGroup, final MigrationTracer<ImportRoot> journal)
	{
		final JXPathContext context = JXPathContext.newContext(interstGroup);

		debug(journal, "Cheking keywords for interest group ", interstGroup);

		final List<Integer> definitionIds = (List<Integer>) context.selectNodes(KEYWORD_DEFINITIONS_ID);
		final List<Integer> assignedKeywordIds = (List<Integer>) context.selectNodes(KEYWORDS_IDS);


		final Set<Integer> assignedAsSet = new HashSet<Integer>(assignedKeywordIds);
		final Set<Integer> definitionAsSet = new HashSet<Integer>(definitionIds);
		
		assignedAsSet.removeAll(definitionAsSet);
		

		for (final Integer id : assignedAsSet)
		{
			error(journal, "All assigned keywords must be defined at the interest group level. The illegal keyword definition id was found with id '" + id + "': ",
						context.selectNodes(".//keywords[contains(ids, '" + id + "')]"));
			
		}
	}

}
