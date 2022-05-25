/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.file;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;
import it.sauronsoftware.ftp4j.FTPListParser;
import it.sauronsoftware.ftp4j.listparsers.UnixListParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.cache.SimpleCache;

import eu.cec.digit.circabc.migration.processor.common.DateSafeUnixListParser;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.repo.migration.EncodingUtils;
import eu.cec.digit.circabc.service.migration.ExportationException;


/**
 * File client implementation for accessing to circa via ftp
 *
 * @author Yanick Pignot
 */
public class CircaFTP4jClient implements FileClient
{
	private Object mutex = new Object();

	private SimpleCache<String, CachableFTPFile[]> ftpListFilesCache;
	private SimpleCache<String, CachableFTPFile>   ftpFilesCache;

	private static final String IO_ERROR = "An I/O error occurs while either sending a command to the server or receiving a reply from the server";
	private static final String FTP_TRANPORT = "ftp://";
	private static final char FTP_AT = '@';
	private static final char URL_SEPARATOR = ':';
	private static final String HIDDEN_FILE_PREFIX = ".";

	private FTPClient client = null;

	private String ftpHost;
	private Integer ftpPort;
	private String ftpUser;
	private String ftpPassword;
	private Integer ftpTimeout;
	private String dataRoot;
	private String iconRoot;
	private String systemEncoding;

	private String libraryDataLocation;
	private String informationDataLocation;
	private String meetingsDataLocation;
	private String newsDataLocation;
	private String directoryDataLocation;
	private boolean passiveMode;

	private String initialDirectory;

	public String generateResouceString(final String basePath)
	{
		final StringBuffer buff = new StringBuffer(FTP_TRANPORT);

		if(ftpUser != null)
		{
			buff.append(ftpUser);

			if(ftpPassword != null)
			{
				buff
					.append(URL_SEPARATOR)
					.append(ftpPassword);
			}

			buff.append(FTP_AT);
		}

		buff
			.append(ftpHost)
			.append(URL_SEPARATOR)
			.append(ftpPort);

		if(basePath.charAt(0) == '.')
		{
			buff.append(basePath.substring(1));
		}
		else
		{
			buff.append(basePath);
		}

		return buff.toString();
	}

	public boolean isSameFileSystem()
	{
		return false;
	}

	public boolean exists(final String path) throws ExportationException
	{
		final CachableFTPFile file = getFile(path);
		return  file != null;
	}

	public void download(final String path, final OutputStream outStream) throws ExportationException
	{
		try
		{
			synchronized(mutex)
			{
				checkClient();

				client.setType(FTPClient.TYPE_BINARY);
				client.download(path, outStream, 0, null);
			}
		}
		catch (Exception e)
		{
			throw new ExportationException(IO_ERROR, e);
		}
	}

	public String downloadAsString(final String path) throws ExportationException
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			download(path, out);

			return EncodingUtils.changeToUTF8Encoding(out.toByteArray(), getSystemEncoding());
		}
		catch (UnsupportedEncodingException e)
		{
			return out.toString();
		}
		finally
		{
			try
			{
				out.close();
			} catch (IOException ignore){}
		}

	}

	public List<String> list(final String path, final boolean includeHidden, final boolean includeFiles, final boolean includeFolders) throws ExportationException
	{
		final CachableFTPFile[] childFiles = listFiles(path);

		final List<String> childsPath = new ArrayList<String>(childFiles.length);

		boolean filter = includeFiles == false || includeFolders == false;

		String childPath;
		for(final CachableFTPFile file: childFiles)
		{
			childPath = path + PATH_SEPARATOR + file.getName();
			boolean add = true;

			if(filter)
			{
				boolean isFile = isFile(file);

				if(isFile && !includeFiles)
				{
					add = false;
				}
				else if(!isFile && !includeFolders)
				{
					add = false;
				}
			}

			if(add && (includeHidden || !file.getName().startsWith(HIDDEN_FILE_PREFIX)))
			{
				childsPath.add(childPath);
			}
		}

		return childsPath;
	}

	public List<String> list(final String path, final boolean includeHidden) throws ExportationException
	{
		return list(path, includeHidden, true, true);
	}

	public boolean isFile(final String path) throws ExportationException
	{
		final CachableFTPFile file = getFile(path);
		boolean isFile = file != null && isFile(file);

		return isFile;
	}

	public long getFileSize(final String path) throws ExportationException
	{
		final CachableFTPFile file = getFile(path);
		if(file != null)
		{
			return file.getSize();
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Return true if the target element is a symbolic link
	 *
	 * @param path
	 * @return
	 * @throws ExportationException
	 */
	public boolean isSymbolicLink(final String path) throws ExportationException
	{
		final CachableFTPFile file = getFile(path);
		boolean isFile = file != null  && isLink(file);

		return isFile;
	}

	private CachableFTPFile[] listFiles(String path) throws ExportationException
	{
		if(ftpListFilesCache.get(path) != null)
		{
			return ftpListFilesCache.get(path);
		}

		FTPFile[] files;

		synchronized (mutex)
		{
			checkClient();

			try
			{
				client.changeDirectory(path);
				files = client.list();
			}
			catch (IllegalStateException e)
			{
				throw new ExportationException("Unexpected error accessing to " + path + ": " + e.getMessage(), e);
			}
			catch (IOException e)
			{
				throw new ExportationException(IO_ERROR + " for " + path + ": " + e.getMessage(), e);
			}
			catch (FTPIllegalReplyException e)
			{
				throw new ExportationException("Reply not recognized. Peharps the server is not compliant. " + path + ": " + e.getMessage(), e);
			}
			catch (FTPDataTransferException e)
			{
				throw new ExportationException("Data transfert aborted due to a network failure for " + path + ": " + e.getMessage(), e);
			}
			catch (FTPAbortedException e)
			{
				throw new ExportationException("Data transfert aborted due to a client aborted request for " + path + ": " + e.getMessage(), e);
			}
			catch (FTPListParseException e)
			{
				throw new ExportationException("Response sent by the server cannot be parsed through the list parsers known by the client for " + path , e);
			}
			catch (FTPException e)
			{
					// file not exists
				files = new FTPFile[0];
			}
		}

		final int fileLen = files.length;
		final CachableFTPFile[] cachableFFTPFiles = new CachableFTPFile[fileLen];
		for(int x = 0; x < fileLen; ++x)
		{
			final FTPFile file = files[x];
			String name;
			try
			{
				name = EncodingUtils.changeToUTF8Encoding(file.getName(), getSystemEncoding());
			}
			catch (UnsupportedEncodingException e)
			{
				name = file.getName();
			}

			cachableFFTPFiles[x] = new CachableFTPFile(name, file.getType(), file.getSize());

			ftpFilesCache.put(path + PATH_SEPARATOR + name, cachableFFTPFiles[x]);
		}

		ftpListFilesCache.put(path, cachableFFTPFiles);

		return cachableFFTPFiles;
	}

	private CachableFTPFile getFile(String path) throws ExportationException
	{
		if(ftpFilesCache.contains(path))
		{
			return ftpFilesCache.get(path);
		}

		CachableFTPFile file = null;

		synchronized (mutex)
		{
			checkClient();
			final String parent = FilePathUtils.retreiveParentPath(path);
			final String name = FilePathUtils.retreiveFileName(path);

			try
			{
				client.changeDirectory(parent);
				final FTPFile[] list = client.list();
				for(final FTPFile f: list)
				{
					if(f.getName().equals(name))
					{
						file = new CachableFTPFile(name, f.getType(), f.getSize());
						break;
					}
				}
			}
			catch (IllegalStateException e)
			{
				throw new ExportationException("Unexpected error accessing to " + path + ": " + e.getMessage(), e);
			}
			catch (IOException e)
			{
				throw new ExportationException(IO_ERROR + " for " + path + ": " + e.getMessage(), e);
			}
			catch (FTPIllegalReplyException e)
			{
				throw new ExportationException("Reply not recognized. Peharps the server is not compliant. " + path + ": " + e.getMessage(), e);
			}
			catch (FTPDataTransferException e)
			{
				throw new ExportationException("Data transfert aborted due to a network failure for " + path + ": " + e.getMessage(), e);
			}
			catch (FTPAbortedException e)
			{
				throw new ExportationException("Data transfert aborted due to a client aborted request for " + path + ": " + e.getMessage(), e);
			}
			catch (FTPListParseException e)
			{
				throw new ExportationException("Response sent by the server cannot be parsed through the list parsers known by the client for " + path + ": " + e.getMessage(), e);
			}
			catch (FTPException e)
			{
				// folder not exists
			}

		}

		ftpFilesCache.put(path, file);

		return file;
	}


	private void checkClient() throws ExportationException
	{
		try
    	{
			if(client == null || client.isConnected() == false || client.isAuthenticated() == false)
			{
				client = new FTPClient();
				client.setCharset(getSystemEncoding());
	    		client.connect(ftpHost, ftpPort);
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

	    		if(ftpUser != null)
	        	{
	    			client.login(ftpUser, ftpPassword);
	        	}

	    		this.initialDirectory = client.currentDirectory();
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
	    			checkClient();
	    		}
			}
    	}
		catch (Exception e)
    	{
    		try
			{
				client.disconnect(true);
			} catch (Exception ignore){}

    		throw new ExportationException(IO_ERROR, e);
    	}
	}

	private boolean isFile(final CachableFTPFile file)
	{
		return isType(file, FTPFile.TYPE_FILE);
	}

	private boolean isLink(final CachableFTPFile file)
	{
		return isType(file, FTPFile.TYPE_LINK);
	}

	private boolean isType(final CachableFTPFile file, final int type)
	{
		return type == file.getType();
	}

	public final void setFtpHost(final String ftpHost)
	{
		this.ftpHost = ftpHost;
	}

	public final void setFtpPassword(final String ftpPassword)
	{
		this.ftpPassword = ftpPassword;
	}

	public final void setFtpPort(final Integer ftpPort)
	{
		this.ftpPort = ftpPort;
	}

	public final void setFtpUser(final String ftpUser)
	{
		this.ftpUser = ftpUser;
	}

	public final void setDataRoot(final String dataRoot)
	{
		this.dataRoot = dataRoot;
	}

	public final void setFtpTimeout(final Integer ftpTimeout)
	{
		this.ftpTimeout = ftpTimeout;
	}

	public final String getDataRoot()
	{
		return dataRoot;
	}

	public final String getFtpHost()
	{
		return ftpHost;
	}

	public final String getFtpPassword()
	{
		return ftpPassword;
	}

	public final Integer getFtpPort()
	{
		return ftpPort;
	}

	public final Integer getFtpTimeout()
	{
		return ftpTimeout;
	}

	public final String getFtpUser()
	{
		return ftpUser;
	}

	/**
	 * @return the informationDataLocation
	 */
	public final String getInformationDataLocation()
	{
		return informationDataLocation;
	}

	/**
	 * @param informationDataLocation the informationDataLocation to set
	 */
	public final void setInformationDataLocation(String informationDataLocation)
	{
		this.informationDataLocation = informationDataLocation;
	}

	/**
	 * @return the libraryDataLocation
	 */
	public final String getLibraryDataLocation()
	{
		return libraryDataLocation;
	}

	/**
	 * @param libraryDataLocation the libraryDataLocation to set
	 */
	public final void setLibraryDataLocation(String libraryDataLocation)
	{
		this.libraryDataLocation = libraryDataLocation;
	}

	/**
	 * @return the meetingsDataLocation
	 */
	public final String getMeetingsDataLocation()
	{
		return meetingsDataLocation;
	}

	/**
	 * @param meetingsDataLocation the meetingsDataLocation to set
	 */
	public final void setMeetingsDataLocation(String meetingsDataLocation)
	{
		this.meetingsDataLocation = meetingsDataLocation;
	}

	/**
	 * @return the newsDataLocation
	 */
	public final String getNewsDataLocation()
	{
		return newsDataLocation;
	}

	/**
	 * @param newsDataLocation the newsDataLocation to set
	 */
	public final void setNewsDataLocation(String newsDataLocation)
	{
		this.newsDataLocation = newsDataLocation;
	}

	/**
	 * @return the directoryDataLocation
	 */
	public final String getDirectoryDataLocation()
	{
		return directoryDataLocation;
	}

	/**
	 * @param directoryDataLocation the directoryDataLocation to set
	 */
	public final void setDirectoryDataLocation(final String directoryDataLocation)
	{
		this.directoryDataLocation = directoryDataLocation;
	}

	/**
	 * @param ftpFilesCache the ftpFilesCache to set
	 */
	public final void setFtpFilesCache(final SimpleCache<String, CachableFTPFile> ftpFilesCache)
	{
		this.ftpFilesCache = ftpFilesCache;
	}


	/**
	 * Allow a ftpfile object to be cached. Make it serializable.
	 *
	 * @author Yanick Pignot
	 */
	static class CachableFTPFile implements Serializable
	{

		/** */
		private static final long serialVersionUID = 6576544679387468748L;

		private final String name;
		private final int type;
		private final long size;

		CachableFTPFile(final String name, final int type, final long size)
		{
			this.name = name;
			this.type = type;
			this.size = size;
		}

		public String getName()
		{
			return this.name;
		}

		public int getType()
		{
			return this.type;
		}

		public long getSize()
		{
			return this.size;
		}
	}


	public final void setFtpListFilesCache(SimpleCache<String, CachableFTPFile[]> ftpListFilesCache)
	{
		this.ftpListFilesCache = ftpListFilesCache;
	}

	public final boolean isPassiveMode()
	{
		return passiveMode;
	}

	public final void setPassiveMode(boolean passiveMode)
	{
		this.passiveMode = passiveMode;
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

	/**
	 * @return the iconRoot
	 */
	public final String getIconRoot()
	{
		return iconRoot;
	}

	/**
	 * @param iconRoot the iconRoot to set
	 */
	public final void setIconRoot(String iconRoot)
	{
		this.iconRoot = iconRoot;
	}
}
