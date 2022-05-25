/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.springframework.core.io.Resource;

/**
 * Custom sping like resource to manage nntp contents (body and attachements).
 *
 * <pre><b>The access protocole is nntp.</b></pre>
 *
 * @author Yanick Pignot
 */
public class NNTPResource implements Resource
{
    public static final String NNTP_STORE_NAME = "nntp";
    public static final int NNTP_DEFAULT_PORT = 119;

    private final String newsgroupName;
    private final int articleIndex;
    private final int contentIndex;
    private final ParsedUrl parsedUrl;

    private final String urlString;
    private final String fileName;
    private final Object part;

    /*package*/ NNTPResource(final String urlString) throws MessagingException, IOException
    {
        this(urlString, 0, 0);
    }

    /**
     * @param urlString
     * @throws MessagingException
     * @throws IOException
     */
    /*package*/ NNTPResource(final String urlString, final int connectiontimeout, final int timeout) throws MessagingException, IOException
    {
    	super();

    	this.urlString = urlString;

        parsedUrl = new ParsedUrl(urlString, NNTP_DEFAULT_PORT);

        final StringTokenizer tokens = new StringTokenizer(parsedUrl.getTarget(), "/", false);
        this.newsgroupName = tokens.nextToken();
        this.articleIndex = Integer.parseInt(tokens.nextToken());
        this.contentIndex = Integer.parseInt(tokens.nextToken());

        final Store store = getNNTPStore(connectiontimeout, timeout);
        store.connect(parsedUrl.getHost(), parsedUrl.getPort(), parsedUrl.getUsername(), parsedUrl.getPassword());

        final Folder folder = store.getFolder(this.newsgroupName);
        folder.open(Folder.READ_ONLY);
        Message message = null;

        for(final Message m: folder.getMessages())
        {
        	if(m.getMessageNumber() == this.articleIndex)
        	{
        		message = m;
        		break;
        	}
        }

        if(message == null)
        {
        	throw new MessagingException("Bad article number: " + this.articleIndex);
        }

        final Object content = message.getContent();

        if(content instanceof MimeMultipart)
        {
            final MimeMultipart mimeMultipart = (MimeMultipart) content;
            final BodyPart bodyPart = mimeMultipart.getBodyPart(contentIndex);
            this.part = bodyPart;
            fileName = bodyPart.getFileName();
        }
        else if(this.contentIndex == 0)
        {
            fileName = null;
            this.part = content;
        }
        else
        {
            throw new MessagingException("Illegal message content index: " + this.contentIndex + ". Maximum: 0.");
        }
        folder.close(false);

    }

    public boolean exists()
    {
        // true, always if the instanciation succeed!
        return true;
    }

    public String getDescription()
    {
        return "NNTP Custom Resource: " + urlString;
    }

    public String getFilename()
    {
        return fileName;
    }

    public boolean isOpen()
    {
        return false;
    }

    public InputStream getInputStream() throws IOException
    {
    	if(this.part instanceof BodyPart)
    	{
    		try
			{
				return ((BodyPart)this.part).getInputStream();
			}
    		catch (MessagingException e)
			{
				throw new IOException("Impossible to get BodyPart stream for " + this.urlString + ": " + e.getMessage());
			}
    	}
    	else
    	{
    		return new ByteArrayInputStream(this.part.toString().getBytes());
    	}
    }


    public Resource createRelative(String relativePath) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public URL getURL() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public File getFile() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public static Session getDefaultSession()
    {
        return getDefaultSession(null, null);
    }

    public static Store getNNTPStore(final int connectiontimeout, final int timeout) throws NoSuchProviderException
    {
        Session session = getDefaultSession(Integer.valueOf(connectiontimeout), Integer.valueOf(timeout));
        try
        {
            return session.getStore(NNTP_STORE_NAME);
        }
        catch (NoSuchProviderException e)
        {
            throw new NoSuchProviderException("NTTP Store not setted. See doc: http://java.sun.com/products/javamail/javadocs/javax/mail/Session.html");
        }
    }


    private static Session getDefaultSession(final Integer connectiontimeout, final Integer timeout)
    {
        Session session;
        Properties props = System.getProperties();
        if(connectiontimeout != null)
        {
            props.put("mail.nntp.connectiontimeout", String.valueOf(connectiontimeout));
        }
        if(timeout != null)
        {
            props.put("mail.nntp.timeout", "" + String.valueOf(timeout));
        }
        props.put("mail.nntp.listall", "true");
        session = Session.getDefaultInstance(props);
        return session;
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
