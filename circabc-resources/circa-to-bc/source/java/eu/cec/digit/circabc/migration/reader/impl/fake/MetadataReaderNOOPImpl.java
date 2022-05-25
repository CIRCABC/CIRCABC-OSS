/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.fake;

import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.reader.MetadataReader;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * A simple implementation that does not support MetadataReader.
 *
 * @author Yanick Pignot
 */
public class MetadataReaderNOOPImpl implements MetadataReader
{

	public void setDynamicPropertyDefinition(InterestGroup interestGroup) throws ExportationException
	{

	}

	public void setIconsDefinition(InterestGroup interestGroup) throws ExportationException
	{

	}

	public void setKeywordDefinition(InterestGroup interestGroup) throws ExportationException
	{

	}

	public void setProperties(XMLNode node) throws ExportationException
	{

	}



}