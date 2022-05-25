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

import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Section;


/**
 * Service layer accessing persistent circa <b>Section</b> and <b>SectionLinguistic</b> entities directly
 *
 * @author Yanick Pignot
 */
public interface SectionDao
{
	/**
	 * Get all sections of an interest group
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract List<Section> getAllSections(final String virtualCirca, final String interestGroup) throws SQLException, IOException;

	/**
	 * Get a specified section by its identifier
	 *
	 * @param virtualCirca				The virtual circa name
	 * @param interestGroup				The interest group name
	 * @param iddentifier			    The unique identifier of the Section
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract Section getSectionsByIdentifier(final String virtualCirca, final String interestGroup, final String identifier) throws SQLException, IOException;
}
