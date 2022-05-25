/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.cec.digit.circabc.migration.entities.TypedPreference;

/**
 * Adapt the Interface Laguage adapt preference.
 *
 * @author Yanick Pignot
 */

public class InterfaceLanguagePreferenceAdapter extends XmlAdapter<String, TypedPreference>
{

	@Override
	public TypedPreference unmarshal(final String prefStringValue) throws Exception
	{
		return new TypedPreference.InterfaceLanguagePreference(prefStringValue);
	}

	@Override
	public String marshal(final TypedPreference typedPreference)
    {
    	if(typedPreference == null || typedPreference.getValue() == null)
    	{
    		return "";
    	}
    	else
    	{
    		return typedPreference.getValue().toString();
    	}
    }

}
