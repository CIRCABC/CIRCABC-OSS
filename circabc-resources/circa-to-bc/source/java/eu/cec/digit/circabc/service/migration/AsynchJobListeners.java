/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;

import java.io.Serializable;

import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;

/**
 * Listener that help to perform some tasks after and before a migration run.
 *
 * @author Yanick Pignot
 */
public interface AsynchJobListeners
{
	/**
	 * Any job that can be launched before an import / export run
	 *
	 * @author Yanick Pignot
	 */
	public interface BeforeRunJob
	{
		/**
		 * What to do before import / export
		 */
		public abstract void start(final CircabcServiceRegistry registry);
	}

	/**
	 * Any job that can be launched after an import / export run
	 *
	 * @author Yanick Pignot
	 */
	public interface AfterRunJob<T extends Serializable>
	{
		/**
		 * What to do if import / export is a succes
		 */
		public abstract void success(final CircabcServiceRegistry registry, final MigrationTracer<T> tracer);

		/**
		 * What to do if import / export fails
		 *
		 * @param exception
		 */
		public abstract void fail(final CircabcServiceRegistry registry, final Exception exception);
	}
}
