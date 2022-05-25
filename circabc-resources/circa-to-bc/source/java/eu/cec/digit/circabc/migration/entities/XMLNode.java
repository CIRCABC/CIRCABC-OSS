/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

import eu.cec.digit.circabc.migration.walker.XMLNodesVisitor;

/**
 * A specialization of the xml element for each node type element.
 *
 * @author Yanick Pignot
 */
public abstract class XMLNode extends XMLElement
{
	/**
	 *  The properties of the node defined in the in the xml
	 */
	private PropertyMap properties = new PropertyMap();

	/**
	 * a name to identify a node
	 */
	private String qualifiedName;

	/**
	 *	the path of this elemenent for export facilities
	 **/
	private String exportPath;

	/**
	 * Apply polymorphic acceptation of a visitor using reflexion to avoid to
	 * children
	 *
	 * @param walker
	 */
	public void accept(XMLNodesVisitor visitor) throws Exception
	{
		Method walkMethod = null;

		Class clazz = this.getClass();
		while(clazz != null && !clazz.equals(Object.class))
		{
			try
            {
            	 walkMethod = XMLNodesVisitor.class.getDeclaredMethod("visit", new Class[] {clazz});
            	 break;
            }
            catch(NoSuchMethodException ex)
            {
            	clazz = clazz.getSuperclass();
            }
		}

        if(walkMethod == null)
        {
        	throw new IllegalStateException("Impossible to visit class " + clazz + ". Visitor (" + visitor.getClass() + ") doesn't have method for it.");
        }
        else
		{
        	walkMethod.invoke(visitor, new Object[] {this});
		}
	}

	/**
	 * @return The properties of the node defined in the in the xml
	 */
	/* package*/ final PropertyMap getProperties()
	{
		return properties;
	}

	/**
	 * @return the qualifiedName of the node
	 */
	/* package*/ final String getQualifiedName()
	{
		return qualifiedName;
	}

	/**
	 * @param qualifiedName the qualifiedName to set
	 */
	/* package*/ final void setQualifiedName(String qualifiedName)
	{
		this.qualifiedName = qualifiedName;
	}

	/**
	 * @param properties the properties to set
	 */
	/* package */ final void addProperties(PropertyMap properties)
	{
		this.properties.putAll(properties);
	}

	/**
	 * @param key	the key of the property
	 * @param value	the property value
	 */
	/* package */ final void addProperty(QName key, Serializable value)
	{
		this.properties.put(key, value);
	}

	/**
	 * @return the exportaion path
	 */
	/* package*/  final String getExportPath()
	{
		return exportPath;
	}

	/**
	 * @param exportPath the exportation path to set
	 */
	/* package*/  final void setExportPath(String exportPath)
	{
		this.exportPath = exportPath;
	}

}
