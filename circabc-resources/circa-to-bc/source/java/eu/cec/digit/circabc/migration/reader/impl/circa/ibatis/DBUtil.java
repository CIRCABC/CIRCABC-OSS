/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.ibatis;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.MySQLCodec;

/**
 * @author Yanick Pignot
 */
abstract class DBUtil
{
	/**
	 * Return a valid name for an Interest Group or a Virtual Circa usable in the database
	 *
	 * @param name
	 * @return
	 */
	static String getValidName(final String name)
	{
		String cleanName = ESAPI.encoder().encodeForSQL( new MySQLCodec(MySQLCodec.Mode.STANDARD)  , name);
		return cleanName.replaceAll("-", "_");
	}
}
