/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.jcr.PathNotFoundException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.Statistic;
import eu.cec.digit.circabc.migration.entities.generated.Statistics;
import eu.cec.digit.circabc.migration.entities.generated.Version;
import eu.cec.digit.circabc.migration.entities.generated.VersionHistory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.ContentNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.TitledNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.service.struct.ManagementService;

/**
 * Util class that perfrom some usefull operation on XML elements and are in charge to propxy the access to
 * package visibility filds.
 *
 * In XMLElement and XMLNode, the mutators visibility is set as package to not interfer with marshaller and
 * JXPath queries.
 *
 * @author Yanick Pignot
 */
public abstract class ElementsHelper
{
	private static final String SELECT_ALL_CHILDS = "./*";

	private static final String SELECT_OTHER_CHILDS = "./categoryHeaders/*|./*/versions/*|./mlContents/*|./infMLContents/*|./mlContents/*/versions/*|./infMLContents/versions/*";

	private ElementsHelper(){};


	/**
	 * @param element	the elment to access
	 * @return			the parent of the given element
	 */
	public static final XMLElement getParent(final XMLElement element)
	{
		return element.getParent();
	}

	/**
	 * @param element	the elment to access
	 * @return			the parent noderef of the given element
	 */
	public static final NodeRef getParentNodeRef(final XMLNode element)
	{
		final XMLElement parent = element.getParent();

		if(parent != null && parent instanceof Node)
		{
			return getNodeRef((Node) parent);
		}
		else
		{
			return null;
		}
	}


	/**
	 * Set the parent of an element.
	 *
	 * @param parent	the parent element
	 * @param element 	the child element to modify
	 *
	 * @throws IllegalAccessError if property is already setted
	 */
	public static final  void setParent(final XMLElement parent, final XMLElement element)
	{
		ParameterCheck.mandatory("The parent element", parent);
		ParameterCheck.mandatory("The child element", element);

		if(getParent(element) == null)
		{
			element.setParent(parent);
		}
		else
		{
			throw new IllegalAccessError("The parent of an element is immutable once setted. Setted value: "  + element.getParent() + ". Candidate value: " + parent);
		}
	}

	/**
	 * @param element	the elment to access
	 * @return			the xpath of the given element
	 */
	public static final String getXPath(final XMLElement element)
	{
		return element.getXPath();
	}


	/**
	 * Set the xpath of an element.
	 *
	 * @param element 	the element to modify
	 * @param xpath		the xpath to set
	 * @throws IllegalAccessError if property is already setted
	 */
	public static final  void setXPath(final XMLElement element, final String xpath)
	{
		ParameterCheck.mandatoryString("xpath", xpath);

		if(getXPath(element) == null)
		{
			element.setXPath(xpath);
		}
		else
		{
			throw new IllegalAccessError("The xpath of an element is immutable once setted. Setted value: "  + element.getXPath() + ". Candidate value: " + xpath);
		}
	}

	/**
	 * @param node		The node to modify
	 * @param path		The path of the node for export purposes
	 */
	public static void setExportationPath(final XMLNode node, final String path)
	{
		ParameterCheck.mandatoryString("export path", path);

		if(node.getExportPath() == null)
		{
			node.setExportPath(path);
		}
		else
		{
			throw new IllegalAccessError("The importation path of a node is immutable once setted. Setted value: "  + node.getExportPath() + ". Candidate value: " + path);
		}
	}

	/**
	 * Get the exportation path of a node
	 *
	 * @param node		The node to query
	 * @return			The path of the node
	 */
	public static String getExportationPath(final XMLNode node)
	{
		return node.getExportPath();
	}

	/**
	 * get the qualified name of a node
	 *
	 * @param node			the node to access
	 * @return				the qualified name
	 */
	public static final String getQualifiedName(final XMLNode node)
	{
		return node.getQualifiedName();
	}


	/**
	 * Set the qualified name of an node.
	 *
	 * @param node					the node to modify
	 * @param qualifiedName			the qualifiedName to set
	 * @throws IllegalAccessError if property is already setted
	 */
	public static final void setQualifiedName(final XMLNode node, final String qualifiedName)
	{
		ParameterCheck.mandatoryString("qualifiedName", qualifiedName);

		if(getQualifiedName(node) == null)
		{
			node.setQualifiedName(qualifiedName);
		}
		else
		{
			throw new IllegalAccessError("The qualified name of an element is immutable once setted. Setted value: "  + node.getQualifiedName() + ". Candidate value: " + qualifiedName);
		}
	}

	/**
	 * get the noderef of a node
	 *
	 * @param node			the node to access
	 * @return				the noderef of a node, null should means that the node is not created yet
	 */
	public static final NodeRef getNodeRef(final Node node)
	{
		return node.getNodeReference();
	}

	/**
	 * return if the node is already created in circabc.
	 *
	 * @param node			the node to access
	 * @return				true is the node is created
	 */
	public static final boolean isNodeCreated(final Node node)
	{
		return  null != node.getNodeReference();
	}

	/**
	 *	Get the properties of a node
	 *
	 * @param node			the node to access
	 * @return				the property map, never null.
	 */
	public static final PropertyMap getProperties(final XMLNode node)
	{
		return node.getProperties();
	}


	/**
	 * Add a list of properties to the existing ones of a node
	 *
	 * @param node					the node to modify
	 * @param properties			the properties to add
	 */
	public static final void addProperties(final XMLNode node, final PropertyMap properties)
	{
		ParameterCheck.mandatory("properties", properties);
		node.addProperties(properties);
	}


	/**
	 * Add a single property entry to the existing ones of a node
	 *
	 * @param node					the node to modify
	 * @param key					the key of the property to set
	 * @param value					the value of the property to set
	 */
	public static final void addProperty(final XMLNode node, final QName key, final Serializable value)
	{
		ParameterCheck.mandatory("key", key);
		node.addProperty(key, value);
	}


	/**
	 * Set the noderef of a node
	 *
	 * @param node					the node to modify
	 * @param ref					the node reference to set
	 * @throws IllegalAccessError if property is already setted
	 */
	public static final void setNodeRef(final Node node, final NodeRef ref)
	{
		ParameterCheck.mandatory("ref", ref);

		if(getNodeRef(node) == null)
		{
			node.setNodeReference(ref);
		}
		else
		{
			throw new IllegalAccessError("The noderef of an element is immutable once setted. Setted value: "  + node.getNodeReference() + ". Candidate value: " + ref);
		}
	}

	/**
	 * The only one method that allows to set a noderef as null ...
	 *
	 * @param node
	 */
	public static final void resetNodeRef(final Node node)
	{
		node.setNodeReference(null);
	}

	/**
	 * Return the direct node parent of any element.
	 *
	 * @param element		The child
	 * @return				The parent node of the element
	 */
	public static final Node getNodeParent( final XMLElement element)
	{
		XMLElement parent = element;

		do
		{
			parent = getParent(parent);
		}
		while(!(parent instanceof Node) && !(parent instanceof Circabc) && parent != null);

		return (Node)  parent;
	}

	/**
	 * Return the topic parent of any element.
	 *
	 * @param element			The element
	 * @return					The parent node of element
	 */
	public static final Topic getElementTopic(final XMLElement element)
	{
		XMLElement topic = element;

		while(topic != null && !(topic instanceof Topic))
		{
			topic = getNodeParent( topic);
		}

		return (Topic) topic;
	}

	/**
	 * Return the interest group service of the given element (Library, Directory, Newsgroup ...).
	 *
	 * @param element			The element
	 * @return					The parent node of element
	 */
	public static final TitledNode getElementIgService(final XMLElement element)
	{
		XMLElement recurse = element;
		XMLElement service = null;
		while(recurse != null && !(recurse instanceof InterestGroup))
		{
			service = recurse;
			recurse = getParent(service);
		}

		return (TitledNode) service;
	}


	/**
	 * Return the interest group parent of any element.
	 *
	 * @param element			The element
	 * @return					The parent node of element
	 */
	public static final InterestGroup getElementInterestGroup(final XMLElement element)
	{
		XMLElement interestGroup = element;

		while(interestGroup != null && !(interestGroup instanceof InterestGroup))
		{
			interestGroup = getNodeParent( interestGroup);
		}

		return (InterestGroup) interestGroup;
	}

	/**
	 * Return the category parent of any element.
	 *
	 * @param element			The element
	 * @return					The parent node of element
	 */
	public static final Category getElementCategory(final XMLElement element)
	{
		XMLElement category = element;

		while(category != null && !(category instanceof Category))
		{
			category = getNodeParent( category);
		}

		return (Category) category;
	}

	/**
	 * Get a Circabc Like path reference of a given Simple path as string.
	 *
	 * @see eu.cec.digit.circabc.repo.struct.SimplePath
	 * @param managementService		the management service reference needed for perform the operation
	 * @param nodeReference			the path as string to check the existence
	 * @return						The Simple path reference of an existing node or null if doesn't exist
	 */
	public static final SimplePath getRepositorySimplePath(final ManagementService managementService, final String nodeReference)
	{
		try
		{
			final SimplePath path = managementService.getNodePath(nodeReference);
			return path;
		}
		catch (final PathNotFoundException e)
		{
			return null;
		}
	}

	/**
	 * Check if a node path reference is existing in circabc
	 *
	 * @see eu.cec.digit.circabc.repo.struct.SimplePath
	 * @param managementService		the management service reference needed for perform the operation
	 * @param nodeReference			the path as string to check the existence
	 * @return						if the node reference path is existing in Circabc
	 */
	public static final boolean isPathExistingInRepository(final ManagementService managementService, final String nodeReference)
	{
		return null != getRepositorySimplePath(managementService, nodeReference);
	}

	public static final String getQualifiedPath(final XMLNode node)
	{
		final StringBuffer buff = new StringBuffer("");

		XMLElement recursNode = node;

		while(recursNode != null)
		{
			if(recursNode instanceof Person)
			{
				buff
					.insert(0, '/')
					.insert(1, ((Person)recursNode).getUserId().getValue());
			}
			if(recursNode instanceof XMLNode)
			{
				buff
					.insert(0, '/')
					.insert(1, getQualifiedName((XMLNode)recursNode));
			}

			recursNode = getParent(recursNode);
		}

		return buff.toString();
	}


	/**
	 * Get the XML Element referenced by the xmlReference parameter
	 *
	 * @param circabc					The root object
	 * @param xmlReference				The reference
	 * @return							the target element
	 */
	@SuppressWarnings("unchecked")
	public static final XMLElement getReferencedElement(final Circabc circabc, final String xmlReference)
	{
		final StringTokenizer tokens = new StringTokenizer(xmlReference, "/", false);

		String token = tokens.nextToken();
		if(!token.equalsIgnoreCase(getQualifiedName(circabc)))
		{
			return null;
		}

		JXPathContext context = null;
		List<Object> childs;
		XMLElement element = circabc;

		while(tokens.hasMoreElements())
		{
			context = JXPathContext.newContext(element);
			token = tokens.nextToken();
			childs = context.selectNodes(SELECT_ALL_CHILDS);

			// the reference can either use the qualified name (Circabc/My Header/My catgeroy/My Interest Group/...)
			// either an indexed xptah (Circabc/CategoryHeader[2]/Category[1]/InterestGroup[6]/...)

			int idx = getIndex(token);

			boolean found = false;

			if(idx < 0)
			{
				for(final Object child : childs)
				{
					// only nodes can be named
					if(isChildQualifiedNameMatches(child, token))
					{
						element = (XMLElement) child;
						found = true;
						break;
					}
				}

				if(!found)
				{
					// if not direct child, search in the ctegory header/categories, the mlContent/translations, the content/versions
					for(final Object otherChild : context.selectNodes(SELECT_OTHER_CHILDS))
					{
						if(isChildQualifiedNameMatches(otherChild, token))
						{
							element = (XMLElement) otherChild;
							found = true;
							break;
						}
					}
				}
			}
			else
			{
				for(final Object child : childs)
				{
					if(isChildNameMatches(child, token))
					{
						element = (XMLElement) child;
						found = true;
						break;
					}
				}
			}

			if(!found)
			{
				element = null;
				break;
			}
		}

		return element;
	}

	/**
	 * Return the referenced element noderef. It can be either found in the repository either in the XML document
	 *
	 * @param managementService				The management service to serach in the repository
	 * @param circabc						The circabc root to search in the binded object graph
	 * @param reference						The reference to search
	 * @return								The noderef of the reference, null if not found.
	 */
	public static NodeRef getTargetRef(final ManagementService managementService, final Circabc circabc, final String reference)
	{
		final SimplePath repoPath = getRepositorySimplePath(managementService, reference);

		if(repoPath != null)
		{
			return repoPath.getNodeRef();
		}
		else
		{
			final XMLElement targetNode = getReferencedElement(circabc, reference);

			if(targetNode == null || !(targetNode instanceof Node))
			{
				return null;
			}
			else
			{
				return getNodeRef((Node) targetNode);
			}
		}

	}

	/**
	 * Secure method that return the oldest version of a content. If not versionnable, the oldest version is naturally the current one.
	 *
	 * @param lastVersion					The last version (current)
	 * @param versions						All other version
	 * @return								The oldest version
	 */
	public static ContentNode getOldestVersion(final ContentNode lastVersion, final List<? extends ContentNode> versions)
	{
		if(versions == null || versions.size() < 1)
		{
			return lastVersion;
		}
		else
		{
			return versions.get(versions.size() - 1);
		}
	}

	public static Pair<Integer, Integer> getLastVersion(final ImportRoot root)
	{
		final VersionHistory versionHistory = root.getVersionHistory();
		final List<Version> versions;

		Integer minor = 0;
		Integer major = 0;

		if(versionHistory != null && (versions = versionHistory.getVersions()).size() > 0)
		{
			for(final Version version: versions)
			{
				switch (version.getMajor().compareTo(major))
				{
					case -1:
						break;
					case 0:
						if(version.getMinor().compareTo(minor) > 0)
						{
							minor = version.getMinor();
						}
						break;
					case 1:
						major = version.getMajor();
						minor = version.getMinor();
						break;

				}
			}
		}

		return new Pair<Integer, Integer>(major, minor);
	}

	public static void addStatistics(final ImportRoot root, final String key, final String value)
	{
		addStatistics(root, key, value, null);
	}

	public static void addStatistics(final ImportRoot root, final String key, final String value, Pair<Integer, Integer> version)
	{
		Statistics statistics = root.getStatistics();
		if(statistics == null)
		{
			root.withStatistics((statistics = new Statistics()));
		}

		final String keyPrefix;
		if(version == null)
		{
			keyPrefix = "";
		}
		else
		{
			keyPrefix = "v" + version.getFirst() + "." + version.getSecond() + " ";
		}

		statistics.withStatistics(new Statistic(value, keyPrefix + key));

	}

	public static Pair<Integer, Integer> addVersion(final ImportRoot root, boolean major, final String versionNote)
	{
		final Pair<Integer, Integer> lastVersion = getLastVersion(root);

		VersionHistory versionHistory = root.getVersionHistory();
		if(versionHistory == null)
		{
			versionHistory = new VersionHistory();
			root.withVersionHistory(versionHistory);
		}

		final Version version = new Version()
			.withDate(new Date())
			.withUser(AuthenticationUtil.getFullyAuthenticatedUser())
			.withDescription(versionNote)
			.withMajor(major ? lastVersion.getFirst() + 1 : lastVersion.getFirst())
			.withMinor(major ? 0 : lastVersion.getSecond() + 1);

		root.getVersionHistory().withVersions(version);

		return new Pair<Integer, Integer>(version.getMajor(), version.getMinor());
	}

	private static int getIndex(String reference)
	{
		int pos1 = reference.indexOf('[');
		int pos2 = reference.lastIndexOf(']');

		if(pos1 > 0 && pos2 > 0)
		{
			String index = reference.substring(pos1+1, pos2);
			try
			{
				return Integer.parseInt(index);
			}
			catch(NumberFormatException ex)
			{
				return -1;
			}
		}
		else
		{
			return -1;
		}
	}

	private static boolean isChildNameMatches(final Object child, final String token)
	{
		return child instanceof XMLElement && getXPath((XMLElement) child).endsWith("/" + token);
	}


	private static boolean isChildQualifiedNameMatches(final Object child, final String token)
	{
		return child instanceof XMLNode && getQualifiedName((XMLNode) child).equalsIgnoreCase(token);
	}
}
