/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.xml.sax.SAXException;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;

/**
 * Helper class using Jaxb to Marshall (java to xml) / Unmarshall (xml to java)
 *
 * @author Yanick Pignot
 */
public abstract class JavaXmlBinder
{
	private JavaXmlBinder(){}


	public static Unmarshaller createUnmarshaller() throws JAXBException, SAXException
	{
		final String packageName = ImportRoot.class.getPackage().getName();
		final JAXBContext jc = JAXBContext.newInstance(packageName, ImportRoot.class.getClassLoader());
		final Unmarshaller unmarshaller = jc.createUnmarshaller();

		return unmarshaller;
	}

	public static Marshaller createMarshaller(Class clazz) throws JAXBException
	{
		final String packageName = clazz.getPackage().getName();
		final JAXBContext jc = JAXBContext.newInstance(packageName, clazz.getClassLoader());

		final Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		//marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "https://?????");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		return marshaller;
	}

	public static void marshall(final Serializable objectRoot, final OutputStream outputStream) throws JAXBException
	{
		createMarshaller(objectRoot.getClass()).marshal(objectRoot, outputStream);
	}

	public static String marshallInString(final Serializable objectRoot) throws JAXBException
	{
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		marshall(objectRoot, outputStream);

		try
		{
			return outputStream.toString("UTF-8");
		}
		catch(final UnsupportedEncodingException e)
		{
			return outputStream.toString();
		}
	}

	public static InputStream marshallInStream(final Serializable objectRoot) throws JAXBException
	{
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		createMarshaller(objectRoot.getClass()).marshal(objectRoot, outputStream);

		try
		{
			final String cleanString = EncodingUtils.stripNonValidXMLCharacters(outputStream.toString("UTF-8"));
			final byte[] cleanBytes = cleanString.getBytes("UTF-8");
			return new ByteArrayInputStream(cleanBytes);
		}
		catch(final UnsupportedEncodingException e)
		{
			return new ByteArrayInputStream(outputStream.toByteArray());
		}
	}

	/**
	 * @param root
	 * @param folderName
	 * @return
	 * @throws JAXBException
	 * @throws PropertyException
	 */
	public static NodeRef marshallInNode(final Serializable root, final NodeRef folder, final String fileName, final FileFolderService fileFolderService) throws JAXBException, PropertyException
	{
		final FileInfo xmlFileInfo = fileFolderService.create(folder, fileName, ContentModel.TYPE_CONTENT);
		final NodeRef contentNodeRef = xmlFileInfo.getNodeRef();

		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		createMarshaller(root.getClass()).marshal(root, out);

		// get a writer for the content and put the file
		final ContentWriter writer = fileFolderService.getWriter(contentNodeRef);

		// set the mimetype and encoding
		writer.setMimetype(MimetypeMap.MIMETYPE_XML);
		writer.setEncoding("UTF-8");

		String outAsString = null;
		try
		{
			outAsString = out.toString("UTF-8");
		}
		catch(final UnsupportedEncodingException e)
		{
			outAsString = out.toString();
		}

		writer.putContent(EncodingUtils.stripNonValidXMLCharacters(outAsString));

		return contentNodeRef;
	}
}
