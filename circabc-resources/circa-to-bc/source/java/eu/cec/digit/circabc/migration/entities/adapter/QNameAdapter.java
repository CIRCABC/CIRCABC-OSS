/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.alfresco.service.namespace.QName;

/**
 * Adpat the xml qname and alfresco QName  at the unmarshal/marshal time.
 *
 * @author Yanick Pignot
 */
public class QNameAdapter extends XmlAdapter<String, QName>
{

	@Override
	public String marshal(final QName alfQName) throws Exception
	{
		return alfQName.toString();
	}

	@Override
	public QName unmarshal(final String xmlQname) throws Exception
	{
		return QName.createQName(xmlQname);
	}

}
