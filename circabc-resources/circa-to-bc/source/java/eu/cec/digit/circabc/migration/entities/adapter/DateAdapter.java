/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * Adpat the xml date and jre date at the unmarshal/marshal time.
 *
 * @author Yanick Pignot
 */
public class DateAdapter extends XmlAdapter<String, Date>
{
	@Override
	public String marshal(final Date jreDate) throws Exception
	{
		return DateAdapterUtils.marshalDate(jreDate);
	}

	@Override
	public Date unmarshal(String xmlDate) throws Exception
	{
		return DateAdapterUtils.unmarshalDate(xmlDate);
	}
}
