/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adpat the xml number (BigInteger) and integer  at the unmarshal/marshal time.
 *
 * @author Yanick Pignot
 */

public class IntegerAdapter extends XmlAdapter<String, Integer>
{
    public Integer unmarshal(String value)
    {
        return Integer.valueOf(value);
    }

    public String marshal(Integer value)
    {
        if (value == null)
        {
            return null;
        }
        return value.toString();
    }

}
