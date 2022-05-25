/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import eu.cec.digit.circabc.migration.reader.impl.circa.domain.News;

/**
 * Service layer accessing persistent circa <b>News</b> and <b>NewsLinguistic</b> entities directly
 *
 * @author Yanick Pignot
 */
public interface NewsDao
{
	/**
	 * Get all news of an interest group
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract List<News> getAllNews(final String virtualCirca, final String interestGroup) throws SQLException, IOException;

	/**
	 * Get a specified news by its title
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @param title    			        The unique title of the News
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract News getNewsByTitle(final String virtualCirca, final String interestGroup, final String title) throws SQLException, IOException;
}
