/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Adpat the xml locale as string and jre locale at the unmarshal/marshal time.
 *
 * @author Yanick Pignot
 */
public class LocaleAdapter extends XmlAdapter<String, Locale>
{

	@Override
	public String marshal(final Locale locale) throws Exception
	{
		return locale.getLanguage();
	}

	@Override
	public Locale unmarshal(final String xmlLocale) throws Exception
	{
		return I18NUtil.parseLocale(xmlLocale);
	}

}
