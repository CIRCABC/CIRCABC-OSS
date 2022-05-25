/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.common;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParser;
import it.sauronsoftware.ftp4j.listparsers.UnixListParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.alfresco.util.Pair;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Custom spring like resource to manage ftp contents with caching the connection.
 *
 * <pre><b>The access protocol is ftp.</b></pre>
 *
 * @author Yanick Pignot
 */
public class CachedFTPResource implements Resource
{
	private static final Object MUTEX = new Object();

	private static final String REGEX_FILENAME_REPLACEMENT = "\\ |\\[|\\]";

    public static final int FTP_DEFAULT_PORT = 21;
    private final ParsedUrl parsedUrl;
    private final String systemEncoding;
    private FTPFile[] files;
    private final String urlString;
    private final Pair<String, String> pathAndName;

    private static String connectionString = null;
    private static String initialDirectory = null;
    private static FTPClient client = null;
    private static  boolean passiveMode;

    /**
     * @param urlString
     * @param passiveMode
     * @throws Exception
     */
    /*package*/ CachedFTPResource(final String urlString, boolean passiveMode, final String systemEncoding) throws Exception
    {
        super();
        this.urlString = urlString;
        this.parsedUrl = new ParsedUrl(urlString, FTP_DEFAULT_PORT);
        this.systemEncoding = systemEncoding;
        CachedFTPResource.passiveMode = passiveMode;
        synchronized (MUTEX)
		{
        	checkClient(urlString);

        	this.pathAndName = extractPathAndName(parsedUrl.getTarget());
        	client.changeDirectory(this.pathAndName.getFirst());
			files = client.list(convertFilenameUnsuportedChars(this.pathAndName.getSecond()));

			// if no result, try to get the file by listing its parent.
			// Work better when non ASCII char are in the name.
			if(files.length == 0)
			{
				for(final FTPFile sibling: client.list())
				{
					if(this.pathAndName.getSecond().equals(sibling.getName()))
					{
						this.files = new FTPFile[]{sibling};
						break;
					}
				}
			}

		}
    }

	private void checkClient(final String urlString) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException
	{
		if(client == null || connectionString == null || urlString.startsWith(connectionString) == false || client.isConnected() == false || client.isAuthenticated() == false)
        {
			try
        	{
        		client.logout();
        	}
        	catch(Exception ignore){}

        	try
        	{
        		client.disconnect(true);
        	}
        	catch(Exception ignore){}

        	client = new FTPClient();
        	client.setCharset(systemEncoding);
         	client.connect(parsedUrl.getHost(), parsedUrl.getPort());
         	client.setPassive(passiveMode);

         	final FTPListParser[] listParsers = client.getListParsers();
			for(final FTPListParser parser: listParsers)
    		{
				if(parser instanceof UnixListParser)
    			{
					client.removeListParser(parser);
    			}
    		}
    		client.addListParser(new DateSafeUnixListParser());

            if(parsedUrl.getUsername() != null)
            {
                client.login(parsedUrl.getUsername(), parsedUrl.getPassword());
            }

            // hold the root directory path
        	initialDirectory = client.currentDirectory();
            connectionString = urlString.substring(0, urlString.indexOf(parsedUrl.getTarget()));
        }
    	else
    	{
    		try
    		{
    			client.changeDirectory(initialDirectory);
    		}
    		catch(final Exception e)
    		{
    			// probably a timeout, for the reconnection
    			client = null;
    			checkClient(urlString);
    		}
    	}
	}

    public boolean exists()
    {
        return  files != null && files.length > 0;
    }

    public String getDescription()
    {
        return "FTP Custom Resource: " + urlString;
    }

    public String getFilename()
    {
    	if(exists())
    	{
    		return files[0].getName();
    	}
    	else
    	{
    		return null;
    	}
    }

    public boolean isOpen()
    {
    	return false;
    }

    public InputStream getInputStream() throws IOException
    {
    	if(!exists())
    	{
    		return null;
    	}

		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		return resolver.getResource(this.urlString).getInputStream();
    }

    public Resource createRelative(String relativePath) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public URL getURL() throws IOException
    {
        return null;
    }

    public File getFile() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    private final Pair<String, String> extractPathAndName(final String path)
	{
		int index = path.lastIndexOf('/');

		final String parent = "." + path.substring(0, index);
		final String name = path.substring(index + 1).trim();

		return new Pair<String, String>(parent, name);
	}

    public static String convertFilenameUnsuportedChars(final String name)
    {
    	return name.trim().replaceAll(REGEX_FILENAME_REPLACEMENT, "?");
    }

	/* (non-Javadoc)
	 * @see org.springframework.core.io.Resource#getURI()
	 */
	public URI getURI() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.core.io.Resource#isReadable()
	 */
	public boolean isReadable() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.core.io.Resource#lastModified()
	 */
	public long lastModified() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long contentLength() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
}
