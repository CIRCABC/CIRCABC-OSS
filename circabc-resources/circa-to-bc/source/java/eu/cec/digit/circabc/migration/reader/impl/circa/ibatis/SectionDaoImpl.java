/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.ibatis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import eu.cec.digit.circabc.migration.reader.impl.circa.dao.SectionDao;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Section;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.SectionLinguistic;


/**
 * Implementation of the circa section dao using ibatis sql mapper.
 *
 * @author Yanick Pignot
 */
public class SectionDaoImpl implements SectionDao
{
	private static final String QUERY_ALL_SECTIONS = "getAllSections";
	private static final String QUERY_SPECIFIC_SECTION = "getSection";
	private static final String QUERY_SPECIFIC_SECTION_LINGUISTIC = "getSectionLinguistic";

	private static final String PARAM_IDENTIFIER = "identifier";

	public List<Section> getAllSections(final String virtualCirca, final String interestGroup) throws SQLException, IOException
	{
		final List<Section> sections = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_ALL_SECTIONS) ;

		for(final Section section: sections)
		{
			fillLinguistic(virtualCirca, interestGroup, section);
		}

		return sections;
	}

	public Section getSectionsByIdentifier(final String virtualCirca, final String interestGroup, final String identifier) throws SQLException, IOException
	{
		final Section section = IbatisDaoManager.querySingle(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_SPECIFIC_SECTION, Collections.singletonMap(PARAM_IDENTIFIER, identifier));
		fillLinguistic(virtualCirca, interestGroup, section);
		return section;
	}

	private void fillLinguistic(final String virtualCirca, final String interestGroup, final Section section) throws SQLException, IOException
	{
		if(section != null)
		{
			final Map<String, String> param = Collections.singletonMap(PARAM_IDENTIFIER, section.getIdentifier());
			final List<SectionLinguistic> linguistic = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_SPECIFIC_SECTION_LINGUISTIC, param);
			section.setSectionLinguistics(linguistic);
		}
	}

}
