/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation.impl;

import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.JXPathValidator;

/**
 * Validate the pivot translation of a multilingual contents
 *
 * @author yanick pignot
 */
public class PivotTranslationValidator extends JXPathValidator
{

	private static final String LANG = "./*/lang/value";
	private static final String PIVOT_LANG = "pivotLang/value";
	private static final String PIVOT_LANGS = ".//*[pivotLang]";

	@SuppressWarnings("unchecked")
	@Override
	protected void validateCircabcImpl(JXPathContext context, final MigrationTracer<ImportRoot> journal)
	{
		JXPathContext containerContext;
		List<Locale> childLocales;
		Locale pivotLocale;

		for(final XMLNode mlContrainer: (List<XMLNode>) context.selectNodes(PIVOT_LANGS))
		{
			containerContext = JXPathContext.newContext(mlContrainer);
			pivotLocale = (Locale) containerContext.selectSingleNode(PIVOT_LANG);
			childLocales = (List<Locale>) containerContext.selectNodes(LANG);

			if(!childLocales.contains(pivotLocale))
			{
				error(journal, "The pivot translation must be an existing translation inside the mlContent. Expected: " + pivotLocale + ". Found: " + childLocales, mlContrainer);
			}
			else
			{
				debug(journal, pivotLocale.getLanguage() +  " pivot translation successfully found for container: ", mlContrainer);
			}
		}

	}
}
