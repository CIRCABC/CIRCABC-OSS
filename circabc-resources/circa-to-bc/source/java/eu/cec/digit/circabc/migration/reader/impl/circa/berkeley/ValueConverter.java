/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.berkeley;

import java.io.Serializable;

import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Interface for a value converter. That helps to convert a data retreived from berkeley db into a specific type
 *
 * @author Yanick Pignot
 */
public interface ValueConverter<T> extends Serializable
{
	/**
	 * The method that is launched to convert a string value
	 *
	 * @param value
	 * @return
	 */
	abstract public T adapt(final String value) throws ExportationException;
}
