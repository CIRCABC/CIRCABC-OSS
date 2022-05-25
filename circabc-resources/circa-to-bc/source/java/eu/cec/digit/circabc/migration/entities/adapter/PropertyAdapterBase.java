/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.cec.digit.circabc.migration.entities.TypedProperty;

/**
 * Base adpter for any typed property.
 *
 * @author Yanick Pignot
 */

public abstract class PropertyAdapterBase extends XmlAdapter<String, TypedProperty>
{
    public String marshal(TypedProperty typedProperty)
    {
    	if(typedProperty == null || typedProperty.getValue() == null)
    	{
    		return "";
    	}
    	else
    	{
    		return typedProperty.getValue().toString();
    	}
    }

}
