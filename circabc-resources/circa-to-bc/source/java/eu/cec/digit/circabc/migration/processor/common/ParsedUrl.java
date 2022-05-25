/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.common;

/**
 * @author Yanick Pignot
 */
public class ParsedUrl
{
	private static final String PROTOCOLE_END = "://";
	private final String protocole;
	private final String username;
	private final String password;
	private final String host;
	private final int port;
	private final String target;

	public ParsedUrl(final String url, final int defaultPort)
	{
		final int protocoleIdx = url.indexOf(PROTOCOLE_END);
		protocole = url.substring(0, protocoleIdx + PROTOCOLE_END.length());

		final String cnxString = url.substring(protocoleIdx + "://".length());
		final String autentication;
		final String hostAnPort;

		final int hostPos = cnxString.indexOf('@');
		if(hostPos > 0)
		{
			autentication = cnxString.substring(0, hostPos);
			hostAnPort = cnxString.substring(hostPos + 1);
		}
		else
		{
			autentication = null;
			hostAnPort = cnxString.substring(hostPos + 1);
		}

		if(autentication != null)
		{
			int passwPos = autentication.indexOf(':');
			if(passwPos >= 0)
			{
				password = autentication.substring(passwPos + 1, hostPos);
				username = autentication.substring(0, passwPos);
			}
			else
			{
				username = autentication.substring(0, hostPos);
				password = null;
			}
		}
		else
		{
			username = null;
			password = null;
		}

		final int portPos = hostAnPort.indexOf(':');
		final int endOfPort = hostAnPort.indexOf('/');
		if(portPos >= 0)
		{
			port = Integer.parseInt(hostAnPort.substring(portPos + 1, endOfPort));
			host = hostAnPort.substring(0, portPos);
		}
		else
		{
			port = defaultPort;
			host = hostAnPort.substring(0, endOfPort);
		}
		target = hostAnPort.substring(endOfPort);
	}


	public final String getHost()
	{
		return host;
	}
	public final String getPassword()
	{
		return password;
	}
	public final int getPort()
	{
		return port;
	}
	public final String getProtocole()
	{
		return protocole;
	}
	public final String getTarget()
	{
		return target;
	}
	public final String getUsername()
	{
		return username;
	}

}
