package eu.cec.digit.circabc.migration.reader.impl.circa.ldap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import eu.cec.digit.circabc.migration.aida.LdapHelper;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Client used to access to circa ldap
 *
 * @author Yanick Pignot
 */
public class CircaLdapClient
{
	private static final String JAVA_NAMING_LDAP_ATTRIBUTES_BINARY = "java.naming.ldap.attributes.binary";
	public static final String QUERY_INTEREST_GROUP_SETTINGS = "(&(objectClass=interestgroup)(cn={0}))";
	public static final String QUERY_PROFILES = "(objectClass=igclass)";
	public static final String QUERY_SPECIFIC_CATEGORY = "(ou={0})";

	public static final String KEY_REGISTER = "register";
	public static final String KEY_PUBLIC = "public";

	public static final String KEY_CN = "cn";

	public static final String KEY_PERMISSIONS = "grouprole";
	public static final String KEY_EXPORTED = "isexported";
	public static final String KEY_IMPORTED_SOURCE = "sourceclass";
	public static final String KEY_INVITED_PERSONS = "uniqueMember";

	public static final String CLASS_NAMES = "classname";

	private static final int MAX_CNT_RETRY_MILLIS = 1000*30;

	private String contextFactory = null;
	private String providerUrl = null;
	private String authentication = null;
	private String principal = null;
	private String credentials = null;
	private String usersLocation = null;
	private String igLocation = null;
	private String catLocation = null;
	private String rootLocation = null;
	private String systemEncoding = null;

	private Hashtable<String, String> userEnvironment = null;
	private Hashtable<String, String> igEnvironment = null;

	private static final Log logger = LogFactory.getLog(CircaLdapClient.class);
	
	public List<Attributes> queryUserDetails(final List<String> userIds) throws ExportationException
	{
		if(userIds != null && userIds.size() > 0)
		{
			final StringBuffer query = new StringBuffer("(|");

			for(final String userId: userIds)
			{
				query.append("(uid="+ userId + ")");
			}

			query.append(")");


			return queryImpl(getUserEnvironment(), query.toString());
		}
		else
		{
			return null;
		}
	}

	public List<Attributes> queryUser(final String query) throws ExportationException
	{
		return queryImpl(getUserEnvironment(), query);
	}

	public List<Attributes> queryInterestGroup(final String virtualCirca, final String interestGroup, final String query) throws ExportationException
	{
		return queryImpl(getIGEnvironment(virtualCirca, interestGroup), query);
	}

	public List<Attributes> queryCategory(final String virtualCirca, final String query) throws ExportationException
	{
		return queryImpl(getCatEnvironment(virtualCirca), query);
	}

	public List<Attributes> queryCircaRoot(final String query) throws ExportationException
	{
		return queryImpl(getRootEnvironment(), query);
	}

	private List<Attributes> queryImpl(final Hashtable<String, String> environment, final String query) throws ExportationException
	{
		DirContext ctx = null;
		NamingEnumeration results = null;
		final List<Attributes> attributes = new ArrayList<Attributes>();
		try
		{
			for(int x = 0; ;x++)
			{
				try
				{
					ctx = new InitialDirContext(environment);
					break;
				}
				catch(Exception ex)
				{
					if ( logger.isErrorEnabled())
					{
						logger.error("Error when connecting  to  LDAP server" , ex); 
					}
					
					int timeToWait = x*500;

					if(timeToWait >= MAX_CNT_RETRY_MILLIS)
					{
						throw new ExportationException("Impossible to establish a connection to ldap with query " + query + ". The problem persists after " + x + " tries.", ex);
					}


					try
					{
						Thread.sleep(timeToWait);
					}
					catch (InterruptedException e)
					{
						throw new ExportationException("Impossible to retry the ldap conncetion after " + (timeToWait/1000) + " seconds with query " + query, e);
					}
				}
			}

			final SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

			results = ctx.search("", query, controls);

			while(results.hasMoreElements())
			{
				final SearchResult searchResult = (SearchResult) results.nextElement();
				attributes.add(searchResult.getAttributes());
			}

			return attributes;
		}
		catch (NamingException e)
		{
			throw new ExportationException("Problem accessing to the circa ladp with query " + query, e);
		}
		finally
		{
			if (results != null)
			{
				try
				{
					results.close();
				}
				catch (Exception e){}
			}
			if (ctx != null)
			{
				try
				{
					ctx.close();
				}
				catch (Exception e) {}
			}
		}
	}

	public List<String> getCategoryNames() throws Exception
	{
		final List<Attributes> cats = queryCircaRoot("(ou=*)");
		final List<String> categories = new ArrayList<String>(cats.size());
		for(final Attributes attr: cats)
		{
			categories.add(LdapHelper.mandatoryValue(attr, "ou", getSystemEncoding()));
		}

		return categories;
	}

	public List<String> getInterestGroupNames(final String virtualCirca) throws Exception
	{
		final List<Attributes> igs = queryCategory(virtualCirca, "(cn=*)");
		final List<String> interestGroups = new ArrayList<String>(igs.size());
		for(final Attributes attr: igs)
		{
			interestGroups.add(LdapHelper.mandatoryValue(attr, "cn", getSystemEncoding()));
		}

		return interestGroups;
	}

	private Hashtable<String, String> getUserEnvironment()
	{
		if(userEnvironment == null)
		{

			userEnvironment = new Hashtable<String, String>();

			Assert.hasText(contextFactory);
			Assert.hasText(providerUrl);
			Assert.hasText(authentication);
			Assert.hasText(principal);
			Assert.hasText(credentials);
			Assert.hasText(usersLocation);

			// Enable connection pooling
			
			userEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
			userEnvironment.put("com.sun.jndi.ldap.connect.pool", "true");
			userEnvironment.put(Context.SECURITY_AUTHENTICATION, authentication);
			userEnvironment.put(Context.PROVIDER_URL, providerUrl + "/" + usersLocation);
			userEnvironment.put(Context.SECURITY_PRINCIPAL, principal);
			userEnvironment.put(Context.SECURITY_CREDENTIALS, credentials);
			userEnvironment.put(Context.SECURITY_CREDENTIALS, credentials);
			userEnvironment.put(JAVA_NAMING_LDAP_ATTRIBUTES_BINARY, "sn cn mails title o fax description postalAddress telephoneNumber labeledURI userPassword");

			Collections.unmodifiableMap(userEnvironment);
		}



		return userEnvironment;
	}

	private Hashtable<String, String> getIGEnvironment(final String virtualCirca, final String interestGroup)
	{
		if(igEnvironment == null)
		{

			igEnvironment = new Hashtable<String, String>();

			Assert.hasText(contextFactory);
			Assert.hasText(contextFactory);
			Assert.hasText(providerUrl);
			Assert.hasText(authentication);
			Assert.hasText(principal);
			Assert.hasText(credentials);
			Assert.hasText(igLocation);

			igEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
			igEnvironment.put("com.sun.jndi.ldap.connect.pool", "true");
			igEnvironment.put(Context.SECURITY_AUTHENTICATION, authentication);
			igEnvironment.put(Context.SECURITY_PRINCIPAL, principal);
			igEnvironment.put(Context.SECURITY_CREDENTIALS, credentials);
			igEnvironment.put(JAVA_NAMING_LDAP_ATTRIBUTES_BINARY, "classname");


		}

		Assert.hasText(virtualCirca);
		Assert.hasText(interestGroup);

		igEnvironment.put(Context.PROVIDER_URL, providerUrl + "/" + MessageFormat.format(igLocation, virtualCirca, interestGroup));

		return igEnvironment;
	}

	private Hashtable<String, String> getCatEnvironment(final String virtualCirca)
	{
		Hashtable<String, String> igEnvironment = getIGEnvironment("_NULL", "_NULL");

		igEnvironment.put(Context.PROVIDER_URL, providerUrl + "/" + MessageFormat.format(catLocation,  virtualCirca));

		return igEnvironment;
	}

	private Hashtable<String, String> getRootEnvironment()
	{
		Hashtable<String, String> igEnvironment = getIGEnvironment("_NULL", "_NULL");

		igEnvironment.put(Context.PROVIDER_URL, providerUrl + "/" + rootLocation);

		return igEnvironment;
	}


	/**
	 * @param authentication the authentication to set
	 */
	public final void setAuthentication(String authentication)
	{
		this.authentication = authentication;
	}

	/**
	 * @param contextFactory the contextFactory to set
	 */
	public final void setContextFactory(String contextFactory)
	{
		this.contextFactory = contextFactory;
	}

	/**
	 * @param credentials the credentials to set
	 */
	public final void setCredentials(String credentials)
	{
		this.credentials = credentials;
	}

	/**
	 * @param igLocation the igLocation to set
	 */
	public final void setIgLocation(String igLocation)
	{
		this.igLocation = igLocation;
	}

	/**
	 * @param principal the principal to set
	 */
	public final void setPrincipal(String principal)
	{
		this.principal = principal;
	}

	/**
	 * @param providerUrl the providerUrl to set
	 */
	public final void setProviderUrl(String providerUrl)
	{
		this.providerUrl = providerUrl;
	}

	/**
	 * @param usersLocation the usersLocation to set
	 */
	public final void setUsersLocation(String usersLocation)
	{
		this.usersLocation = usersLocation;
	}

	/**
	 * @param catLocation the catLocation to set
	 */
	public final void setCatLocation(String catLocation)
	{
		this.catLocation = catLocation;
	}

	/**
	 * @param rootLocation the rootLocation to set
	 */
	public final void setRootLocation(String rootLocation)
	{
		this.rootLocation = rootLocation;
	}

	/**
	 * @return the systemEncoding
	 */
	public final String getSystemEncoding()
	{
		return systemEncoding;
	}

	/**
	 * @param systemEncoding the systemEncoding to set
	 */
	public final void setSystemEncoding(String systemEncoding)
	{
		this.systemEncoding = systemEncoding;
	}



}
