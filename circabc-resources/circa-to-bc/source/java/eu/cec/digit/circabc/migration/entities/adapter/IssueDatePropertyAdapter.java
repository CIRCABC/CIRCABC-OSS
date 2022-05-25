/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import java.util.Date;

import eu.cec.digit.circabc.migration.entities.TypedProperty;

/**
 * Adapt the Issue Date property.
 *
 * @author Yanick Pignot
 */

public class IssueDatePropertyAdapter extends PropertyAdapterBase
{

	@Override
	public String marshal(TypedProperty typedProperty)
	{
		final String strValue = DateAdapterUtils.marshalDate((Date)typedProperty.getValue());
		return strValue;

	}

	@Override
	public TypedProperty unmarshal(String propertyStringValue) throws Exception
	{
		final Date date = DateAdapterUtils.unmarshalDate(propertyStringValue);
		return new TypedProperty.IssueDateProperty(date);
	}

}
