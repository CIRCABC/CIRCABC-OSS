/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.journal.MigrationLog;
import eu.cec.digit.circabc.migration.journal.MigrationLog.LogLevel;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;

/**
 * Base class for evaluate specific elements of a class hierarchie.
 *
 * Added coded evaluator are needed to evaluate all kind of restriction that are not possible with an XML Schema
 *
 * @author Yanick Pignot
 */
public abstract class JXPathValidator implements Validator
{
	private static final String SELECT_ALL_INTEREST_GROUPS = ".//interestGroups";

	private static final Log logger = LogFactory.getLog(JXPathValidator.class);

	private CircabcServiceRegistry registry;
	
	private LogLevel logLevel = LogLevel.WARNING;   


	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.validation.Validator#performValidation(eu.cec.digit.circabc.migration.entities.generated.ImportRoot)
	 */
	public void performValidation(final CircabcServiceRegistry registry, final MigrationTracer<ImportRoot> journal) throws Exception
	{
		if(logger.isDebugEnabled())
		{
			logger.debug(this.getClass().getSimpleName() + " validator starting for import Root");
		}

		this.registry = registry;

		journal.setRunningPhase("Java coded validation: " + this.getClass().getSimpleName());

		final ImportRoot importRoot = journal.getUnmarshalledObject();

		if(importRoot.getCircabc() != null)
		{
			final JXPathContext newContext = JXPathContext.newContext(importRoot.getCircabc());
			validateCircabcImpl(newContext, journal);
		}

		if(importRoot.getPersons() != null)
		{
			validatePersonsImpl(JXPathContext.newContext(importRoot.getPersons()), journal);
		}

		if(logger.isDebugEnabled())
		{
			logger.debug(this.getClass().getSimpleName() + " successfully terminated.");
		}
	}

	protected void validateCircabcImpl(final JXPathContext context, final MigrationTracer<ImportRoot> journal) throws Exception
	{
		// not forced to override it
	}

	protected void validatePersonsImpl(final JXPathContext context, final MigrationTracer<ImportRoot> journal) throws Exception
	{
		// not forced to override it
	}

	@SuppressWarnings("unchecked")
	protected List<InterestGroup> getInterestGroups(final JXPathContext context)
	{
		return (List<InterestGroup>) context.selectNodes(SELECT_ALL_INTEREST_GROUPS);
	}


	protected final void debug(final MigrationTracer<ImportRoot> journal, final String message, final Object ... element)
	{
		if (logLevel.ordinal() >= logLevel.DEBUG.ordinal() )
		{
			log(journal, LogLevel.DEBUG, message, element);
		}
	}

	protected final void warn(final MigrationTracer<ImportRoot> journal, final String message, final Object ... element)
	{
		if (logLevel.ordinal() >= LogLevel.WARNING.ordinal() )
		{
			log(journal, LogLevel.WARNING, message, element);
		}
	}

	protected final void error(final MigrationTracer<ImportRoot> journal, final String message, final Object ... element)
	{
		if (logLevel.ordinal() >= LogLevel.ERROR.ordinal() )
		{
			log(journal, LogLevel.ERROR, message, element);
		}
	}

	private void log(final MigrationTracer<ImportRoot> journal, final LogLevel level, final String message, final Object element)
	{
		journal.addValidationMessage(new MigrationLog(level, message, element));
	}


	public final CircabcServiceRegistry getRegistry()
	{
		return registry;
	}
	
	public final void setRegistry(CircabcServiceRegistry registry)
	{
		this.registry = registry;
	}
}
