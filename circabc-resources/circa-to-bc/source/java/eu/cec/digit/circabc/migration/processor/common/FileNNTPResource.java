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

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.core.io.Resource;

import eu.cec.digit.circabc.migration.processor.ResourceManager;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * Custom sping like resource to manage nntp contents (body and attachements).
 *
 * <pre><b>The access protocole is ftp, http, file or classpath.</b> The messages are stored as simple text files</pre>
 *
 * @author Yanick Pignot
 */
public class FileNNTPResource implements Resource
{
	public static final String NNTP_STORE_NAME = "nntp";
	public static final int NNTP_DEFAULT_PORT = 119;

	private final int contentIndex;
	private final String urlString;
	private final String fileResourceUrl;
	private final String fileName;
	private final InputStream stream;

	/**
	 * @param urlString
	 * @throws MessagingException
	 * @throws IOException
	 * @throws ImportationException
	 */
	FileNNTPResource(final String urlString, final ResourceManager resourceManager) throws MessagingException, IOException, ImportationException
	{
		super();
		this.urlString = urlString;

		// Retreive the content id
		final int contentLocationIdx = urlString.lastIndexOf('/');

		// example: nntp://ftp://... --> ftp://...
		fileResourceUrl = urlString.substring(7, contentLocationIdx);
		final String contentValueStr = urlString.substring(contentLocationIdx + 1);
		this.contentIndex = Integer.parseInt(contentValueStr);

		final Resource fileresource = resourceManager.adaptRessource(fileResourceUrl);

		final Session session = NNTPResource.getDefaultSession();
		final MimeMessage message = new MimeMessage(session, fileresource.getInputStream());
		final Object content = message.getContent();

		if(content instanceof MimeMultipart)
		{
			final MimeMultipart mimeMultipart = (MimeMultipart) content;
			final BodyPart bodyPart = mimeMultipart.getBodyPart(contentIndex);
			fileName = bodyPart.getFileName();
			stream = bodyPart.getInputStream();
		}
		else if(this.contentIndex == 0)
		{
			fileName = null;
			stream = new ByteArrayInputStream(((String) content).getBytes());
		}
		else
		{
			throw new MessagingException("Illegal message content index: " + this.contentIndex + ". Maximum: 0.");
		}
	}

	public boolean exists()
	{
		// true, always if the instanciation succeed!
		return true;
	}

	public String getDescription()
	{
		return "FILENNTP Custom Resource: " + urlString + "(For file " + fileResourceUrl + ")";
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
		return stream;
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
