/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.listener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.Unmarshaller.Listener;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLElement;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.LogFile;
import eu.cec.digit.circabc.migration.entities.generated.nodes.NamedNode;

/**
 * Listener that enhance the elements at the marshaler time
 *
 * @author Yanick Pignot
 */
public class StructureListener extends Listener
{

    final Map<String,Integer> maxPath = new HashMap<String,Integer>(1024);

    @Override
    public void afterUnmarshal(Object target, Object parent)
    {
    	super.afterUnmarshal(target, parent);

    	setNodeQualifiedName(target);
    	setNodeQualifiedName(parent);
    }

    @Override
    public void beforeUnmarshal(Object target, Object parent)
    {
    	if(parent == null || parent instanceof ImportRoot || parent instanceof LogFile)
        {
            // nothing to do, it is ImportRoot element
        }
    	else if(target instanceof XMLElement && parent instanceof XMLElement)
    	{
    		setElementPath(target, parent);
        	ElementsHelper.setParent((XMLElement) parent, (XMLElement) target);
    	}
        else
        {
        	// not managed the JaxbElement instances
        }

        super.beforeUnmarshal(target, parent);
    }

    /**
     * Set the qualified node to an element
     * <pre>
     * 		For named nodes (node with name property), its name: My interest group or document.txt, ...
     * 		For others, its type: circabc or library or message, ...
     * </pre>
     *
     * @param element
     */
    private void setNodeQualifiedName(Object element)
    {
    	if(element instanceof XMLNode)
    	{
    		final XMLNode elemenetNode = (XMLNode) element;

    		if(ElementsHelper.getQualifiedName(elemenetNode) == null)
    		{
    			final String elementName = computeElementName((XMLNode) elemenetNode);
    			ElementsHelper.setQualifiedName(elemenetNode, elementName);
    		}
    	}
    }

    /**
     * Set the xpath for each element to locate it in debug mode or error report
     * <pre>
     * 		ie: circabc/categoryHeader[4]/category[2]/interestgroup[5]/library[1]/content[8]
     * </pre>
     *
     * @param target
     * @param parent
     */
    private void setElementPath(Object target, Object parent)
    {
    	final XMLElement parentElement = (XMLElement) parent;
        if(ElementsHelper.getXPath(parentElement) == null)
        {
        	ElementsHelper.setXPath(parentElement, "/" + computeElementPath(parent));
        }

        if(target instanceof XMLElement)
        {
            final XMLElement targetElement = (XMLElement) target;
            final String parentPath = ElementsHelper.getXPath(parentElement);
            final String targetElementPath = computeElementPath(targetElement);

            String candidatePath = null;
            final String key = parentPath + targetElementPath;
			if (maxPath.containsKey(key))
            {
            	 Integer maxIndex = maxPath.get(key);
            	 maxIndex++;
            	 candidatePath = computeFullXPath(parentPath, targetElementPath, maxIndex);
            	 maxPath.put(key,maxIndex);
            }
            else
            {
            	Integer maxIndex = 1;
           	 	candidatePath = computeFullXPath(parentPath, targetElementPath, maxIndex);
           	 	maxPath.put(key,maxIndex);
            }
			
            ElementsHelper.setXPath(targetElement, candidatePath);
        }
    }

    private String computeFullXPath(final String parentPath, final String childName, int index)
    {
    	StringBuilder buff = new StringBuilder(parentPath);
    	buff
    		.append('/' )
    		.append(childName)
    		.append('[')
    		.append(index)
    		.append( ']');

    	return buff.toString();
    }

    private String computeElementPath(Object obj)
    {
        return obj.getClass().getSimpleName();
    }

    private String computeElementName(XMLNode node)
    {
        if(node instanceof NamedNode)
        {
        	final NamedNode namedNode = (NamedNode)node;
        	final Serializable name = namedNode.getName().getValue();
        	return (String) name;
        }
        else
        {
        	return computeElementPath(node);
        }
    }
}
