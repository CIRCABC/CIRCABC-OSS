/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.aida.circa;

import eu.cec.digit.circabc.service.migration.AidaMigrationService;
import eu.cec.digit.circabc.service.migration.AidaMigrationService.UserConverter;

/**
 * @author Yanick Pignot
 *
 */
public abstract class BaseConverter implements UserConverter
{
	private AidaMigrationService aidaMigrationService;


	public void init()
	{
		getAidaMigrationService().registerConverter(this);
	}

	/**
	 * @return the aidaMigrationService
	 */
	protected final AidaMigrationService getAidaMigrationService()
	{
		return aidaMigrationService;
	}

	/**
	 * @param aidaMigrationService the aidaMigrationService to set
	 */
	public final void setAidaMigrationService(final AidaMigrationService aidaMigrationService)
	{
		this.aidaMigrationService = aidaMigrationService;
	}


}
