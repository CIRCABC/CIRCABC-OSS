/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.util.VersionNumber;

import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Concreate file reader for circa information service
 *
 * @author Yanick Pignot
 */
public class CircaInfFileReaderImpl extends CircaLibFileReaderImpl
{
	private static final Locale NA_LOCALE = new Locale("infoService", "N/A");

	@Override
	public List<Locale> getContentTranslations(String path) throws ExportationException
	{
		return Collections.singletonList(NA_LOCALE);
	}


	@Override
	public Map<VersionNumber, String> getContentVersions(String path, Locale locale) throws ExportationException
	{
		return Collections.singletonMap(new VersionNumber("1.0"), path);
	}


	@Override
	public boolean isDocument(String path) throws ExportationException
	{
		return getFileClient().isFile(path);
	}


	@Override
	public boolean isSpace(String path) throws ExportationException
	{
		return getFileClient().isFile(path) == false ;
	}


	@Override
	public List<String> listChidrenPath(String parentPath) throws ExportationException
	{
		return getFileClient().list(parentPath, false);
	}

}