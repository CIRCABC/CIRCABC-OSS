/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.migration.entities.TypedProperty;

/**
 * Adapt the Locale property.
 *
 * @author Yanick Pignot
 */

public class LocalePropertyAdapter extends PropertyAdapterBase
{

	@Override
	public TypedProperty unmarshal(String propertyStringValue) throws Exception
	{
		final Locale locale = I18NUtil.parseLocale(propertyStringValue);
		return new TypedProperty.LocaleProperty(locale);
	}

}
