/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader;

import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Base interface for a metadata reader
 *
 * @author Yanick Pignot
 */
public interface MetadataReader
{

	/**
	 * Export the properties to any kind of node.
	 *
	 * @param node
	 * @throws ExportationException
	 */
	public abstract void setProperties(final XMLNode node) throws ExportationException;

	/**
	 * Export the keywords of an interest group
	 *
	 * @param interestGroup
	 * @throws ExportationException
	 */
	public abstract void setKeywordDefinition(final InterestGroup interestGroup) throws ExportationException;

	/**
	 * Export the dynamic attributes of an interest group
	 *
	 * @param interestGroup
	 * @throws ExportationException
	 */
	public abstract void setDynamicPropertyDefinition(final InterestGroup interestGroup) throws ExportationException;

	/**
	 * Export the icons of the library
	 *
	 * @param interestGroup
	 * @throws ExportationException
	 */
	public abstract void setIconsDefinition(final InterestGroup interestGroup) throws ExportationException;
	
}