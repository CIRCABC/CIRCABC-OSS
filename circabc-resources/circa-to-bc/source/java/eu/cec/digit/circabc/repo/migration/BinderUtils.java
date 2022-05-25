/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.migration.entities.XMLElement;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Library;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Guest;
import eu.cec.digit.circabc.migration.entities.generated.permissions.RegistredUsers;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;

/**
 * @author Yanick Pignot
 *
 */
public abstract class BinderUtils
{

	private static final String GET_METHOD_PREFIX = "get";
	private static final String SET_METHOD_PREFIX = "set";
	private static final String WITH_METHOD_PREFIX = "with";

	private BinderUtils(){}




	/**
	 * Return a category in a category header. Create it if needed.
	 *
	 * @param header
	 * @param categoryName
	 * @return
	 */
	public static Category getOrInitNewCategory(final CategoryHeader header, final String categoryName, final RemoteFileReader fileReader, final Log logger)
	{
		Category category = null;

		for(final Category existingCategory: header.getCategories())
		{
			if(existingCategory.getName().getValue().equals(categoryName))
			{
				category = existingCategory;
				break;
			}
		}

		if(category == null)
		{
			category = new Category().withName(buildNameProperty(categoryName));
			header.withCategories(category);
			ElementsHelper.setParent(header, category);
			fileReader.setNodePath(category);

			if(logger.isDebugEnabled())
			{
				logger.debug("Category successfully added to the header " + header.getName()) ;
			}
		}

		return category;
	}

	/**
	 * Return a category header in circabc. Create it if needed.
	 *
	 * @param circabc
	 * @param headerName
	 * @param fileReader
	 * @param logger
	 * @return
	 */
	public static CategoryHeader getOrInitNewCategoryHeader(final Circabc circabc, final String headerName, final RemoteFileReader fileReader, final Log logger)
	{
		CategoryHeader header = null;

		for(final CategoryHeader existingHeader: circabc.getCategoryHeaders())
		{
			if(existingHeader.getName().getValue().equals(headerName))
			{
				header = existingHeader;
				break;
			}
		}

		if(header == null)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("New category header added " + headerName) ;
			}

			header = new CategoryHeader().withName(buildNameProperty(headerName));
			circabc.withCategoryHeaders(header);
			ElementsHelper.setParent(circabc, header);
			fileReader.setNodePath(header);
		}

		return header;
	}



	/**
	 * Return an Interest Group in a category. Create it if needed.
	 *
	 * @param category
	 * @param igName
	 * @return
	 */
	public static InterestGroup getOrInitNewInterestGroup(final Category category, final String igName, final RemoteFileReader fileReader, final Log logger)
	{
		InterestGroup ig = null;

		for(final InterestGroup existingIg: category.getInterestGroups())
		{
			if(existingIg.getName().getValue().equals(igName))
			{
				ig = existingIg;
				break;
			}
		}

		if(ig == null)
		{
			ig = createInterestGroup(igName);

			ig.getDirectory()
				.withGuest(new Guest())
				.withRegistredUsers(new RegistredUsers());

			ElementsHelper.setParent(category, ig);
			ElementsHelper.setParent(ig, ig.getInformation());
			ElementsHelper.setParent(ig, ig.getLibrary());
			ElementsHelper.setParent(ig, ig.getDirectory());
			ElementsHelper.setParent(ig, ig.getNewsgroups());
			ElementsHelper.setParent(ig, ig.getEvents());

			fileReader.setNodePath(ig);
			fileReader.setNodePath(ig.getInformation());
			fileReader.setNodePath(ig.getLibrary());
			fileReader.setNodePath(ig.getDirectory());
			fileReader.setNodePath(ig.getNewsgroups());
			fileReader.setNodePath(ig.getEvents());

			category.withInterestGroups(ig);

			if(logger.isDebugEnabled())
			{
				logger.debug("New Interest Group added with name " + ig.getName().getValue()) ;
				logger.debug("  ---  With Services:" );
				logger.debug("  -----  Information:  Yes");
				logger.debug("  -----  Library:      Yes");
				logger.debug("  -----  Directory:    Yes");
				logger.debug("  -----  Newsgroup:    Yes");
				logger.debug("  -----  Events:       Yes");
				logger.debug("  -----  Surveys:      No");
			}

		}

		return ig;
	}

	/**
	 * Return an instance of circabc with minimum required element defined in schema.
	 *
	 * @param igName
	 * @return
	 */
	public static InterestGroup createInterestGroup(final String igName)
	{
		final InterestGroup ig = new InterestGroup();
		ig
			.withName(buildNameProperty(igName))
			.withInformation(new Information())
			.withLibrary(new Library())
			.withNewsgroups(new Newsgroups())
			.withEvents(new Events())
			.withDirectory(new Directory());

		return ig;
	}

	/**
	 * Add a child to a parent if it not exists and return it.
	 *
	 * @param parent
	 * @param child
	 * @param logger
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static XMLElement addChild(final XMLElement parent, final XMLElement child, final Log logger) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		return addChild(parent, child, true, logger);
	}

	/**
	 * Add a child to a parent if it not exists and return it.
	 *
	 * @param parent
	 * @param child
	 * @param logger
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static XMLElement addChild(final XMLElement parent, final XMLElement child, final boolean checkExists, final Log logger) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// Add a child to a parent using the method parent.withChild(Child);

		final Collection childSingleton = Collections.singleton(child);
		final Map<String, Method> methods = getSetterAndGetters(parent.getClass(), child);
		final XMLElement existingChild = checkExists ? getChild(methods.get(GET_METHOD_PREFIX), parent, child) : null;
		if(existingChild != null)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug(child.getClass().getSimpleName() + " NOT added as a children of " + parent.getClass().getSimpleName() + " because it exists.") ;
			}

			return existingChild;
		}
		else
		{
			if(methods.containsKey(SET_METHOD_PREFIX))
			{
				methods.get(SET_METHOD_PREFIX).invoke(parent, new Object[] {child});
			}
			else if(methods.containsKey(WITH_METHOD_PREFIX))
			{
				methods.get(WITH_METHOD_PREFIX).invoke(parent, new Object[] {childSingleton});
			}
			else
			{
				throw new IllegalArgumentException("Impossible to add a child from type " + child.getClass().getSimpleName() + " to its parent " + parent.getClass().getSimpleName() + ". No Setter found !!!");
			}

			if(logger.isDebugEnabled())
			{
				logger.debug(child.getClass().getSimpleName() + " successfully added as a children of " + parent.getClass().getSimpleName()) ;
			}

			return child;
		}
	}

	/**
	 * Buil a typed property for a name
	 *
	 * @param name
	 * @return
	 */
	public static NameProperty buildNameProperty(final String name)
	{
		return new NameProperty(name);
	}

	public static void addPersons(final ImportRoot root, final List<Person> newPersons, final Log logger)
	{
		Persons persons = root.getPersons();
		if(persons == null)
		{
			root.setPersons(persons = new Persons());
		}

		for(final Person person: newPersons)
		{
			if(person != null)
			{
				boolean existing = false;
				final List<Person> oldPersons = persons.getPersons();

				for(final Person oldPerson: oldPersons)
				{
					final Serializable oldUserName = oldPerson.getUserId().getValue();
					final Serializable newUserName = person.getUserId().getValue();
					if(oldUserName.equals(newUserName))
					{
						existing = true;

						if(logger.isDebugEnabled())
						{
							logger.debug("Person already added. " + newUserName);
						}

						break;
					}
				}

				if(existing == false)
				{
					persons.withPersons(person);

					if(logger.isDebugEnabled())
					{
						logger.debug("Person successfully added to the tree. " + person.getUserId() + ": " + person.getEmail());
					}
				}
			}

		}

	}


	//-- Private (internal) helpers...

	@SuppressWarnings("unchecked")
	private static XMLElement getChild(final Method getter, final XMLElement parent, final XMLElement child) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		if(getter == null)
		{
			throw new IllegalArgumentException("Impossible to add a child from type " + child.getClass().getSimpleName() + " to its parent " + parent.getClass().getSimpleName() + ". No Getter found !!!");
		}

		final Object result = getter.invoke(parent, new Object[]{});

		if(result == null)
		{
			return null;
		}
		else if(result instanceof Collection == false)
		{
			return (XMLElement) result;
		}
		else
		{
			final Collection<? extends XMLElement> collection = (Collection) result;
			if(collection.size() < 1)
			{
				return null;
			}
			else
			{
				XMLElement elementToReturn = null;
				final String xpath = ElementsHelper.getXPath(child);

				if(xpath == null)
				{
					return null;
				}
				else
				{
					for(final XMLElement elem:  collection)
					{
						if(xpath.equals(ElementsHelper.getXPath(elem)))
						{
							elementToReturn = elem;
							break;
						}
					}
				}
				return elementToReturn;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Map<String, Method> getSetterAndGetters(final Class fromClass, final T assignableFor)
	{
		final Map<String, Method> methods = new HashMap<String, Method>(2);
		final Class collectionClass = Collection.class;
		final Class listClass = List.class;
		final Class childClass = assignableFor.getClass();

		Class[] parameters;
		Class returnType;
		String methodName;
		for(final Method method : fromClass.getMethods())
		{
			methodName = method.getName();
			parameters = method.getParameterTypes();
			returnType = method.getReturnType();

			if(methodName.startsWith(WITH_METHOD_PREFIX)
					&& parameters.length == 1
					&& parameters[0].equals(collectionClass)
					&& isGeneric(method.getGenericParameterTypes()[0], childClass))

			{
				methods.put(WITH_METHOD_PREFIX, method);
			}
			else if(methodName.startsWith(SET_METHOD_PREFIX)
					&& parameters.length == 1
					&& parameters[0].equals(childClass))
			{
				methods.put(SET_METHOD_PREFIX, method);
			}
			else if(methodName.startsWith(GET_METHOD_PREFIX)
					&& parameters.length == 0
					&& (returnType.equals(childClass)
							|| returnType.equals(listClass)
							&& isGeneric(method.getGenericReturnType(), childClass)))
			{
				methods.put(GET_METHOD_PREFIX, method);
			}
		}
		return methods;
	}

	private static boolean isGeneric(final Type paramType, final Class genericType)
	{
		boolean generic = false;

		if(paramType != null && paramType instanceof ParameterizedType)
		{
		    final ParameterizedType type = (ParameterizedType) paramType;
		    final Type[] typeArguments = type.getActualTypeArguments();

		    for(final Type typeArgument : typeArguments)
		    {
		    	final Class typeArgClass = (Class) typeArgument;
		    	if(genericType.equals(typeArgClass))
		    	{
		    		generic = true;
		    		break;
		    	}
		    }
		}

		return generic;
	}
}
