/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Adapt the information adapt property.
 *
 * @author Yanick Pignot
 */

public class NodeRefAdapter extends XmlAdapter<String, NodeRef>
{
    public String marshal(NodeRef nodeRef)
    {
    	if(nodeRef == null)
    	{
    		return "";
    	}
    	else
    	{
    		return nodeRef.toString();
    	}
    }

	@Override
	public NodeRef unmarshal(String nodeRef) throws Exception
	{
		if(NodeRef.isNodeRef(nodeRef))
		{
			return new NodeRef(nodeRef);
		}
		else
		{
			return null;
		}
	}

}

