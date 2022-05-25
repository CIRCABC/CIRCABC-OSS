/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.LogEntry;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.processor.IGLogFilesUtils;
import eu.cec.digit.circabc.migration.reader.LogFileReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.logs.CircaLogConfig;
import eu.cec.digit.circabc.migration.reader.impl.circa.logs.LogEntryConfiguration;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.repo.migration.EncodingUtils;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Build an appointments list from circa database. *
 * @author Yanick Pignot
 */
public class LogFilesReaderImpl implements LogFileReader
{
	private static final String CIRCA_LOGFILES = "circaLogfiles";

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(LogFilesReaderImpl.class);

	private static final String FOLDER_NAME = "{0}/{1}/{2}";

	private FileClient fileClient;
	private CircabcServiceRegistry circabcServiceRegistry;
	private CircaLogConfig circaLogConfig;

	/** The root folder of log files */
	private String logFilesLocation;
	/** The filename of the current log file (not compressed yet) */
	private String currentLogFilename;
	/** The filename regex where are stored the old log files archive */
	private String gzipFilenameRegex;
	/** The log entries separator (in circa) */
	private String logSeparator;
	/** The log file date format */
	private SimpleDateFormat logDateFormat;

	private boolean ignoreErrorEntries = false;

	

	public void addLogEntries(final InterestGroup interestGroup, final OutputStream outputStream) throws ExportationException
	{
		fillLogs(interestGroup,  outputStream);
	}


	private void fillLogs(final InterestGroup interestGroup,  final OutputStream outputStream) throws ExportationException
	{
		final String location = getFoldername(interestGroup);
		final List<String> files = fileClient.list(location, false);

		for(final String file: files)
		{
			final String fileName = FilePathUtils.retreiveFileName(file);
			final boolean isGz = fileName.matches(this.gzipFilenameRegex);

			if(isGz || fileName.equals(this.currentLogFilename))
			{
				File tempFile = null;
				FileOutputStream fileOutputStream = null;
				try
				{
					tempFile = TempFileProvider.createTempFile(CIRCA_LOGFILES, null);
					fileOutputStream = new FileOutputStream(tempFile);
					fileClient.download(file, fileOutputStream);

					if(isGz)
					{
						parseFile(interestGroup, new GZIPInputStream(new FileInputStream(tempFile)), outputStream);
					}
					else
					{
						parseFile(interestGroup, new FileInputStream(tempFile),  outputStream);
					}
				}
				catch (final FileNotFoundException e)
				{
					throw new ExportationException("Error downloading the remote log file", e);
				}
				catch (final IOException e)
				{
					throw new ExportationException("Error reading the remote log file", e);
				}
				finally
				{
					if(fileOutputStream != null)
					{
						try
						{
							fileOutputStream.close();
						}
						catch (IOException ignore){}
					}

					if(tempFile != null && tempFile.exists() )
					{
						tempFile.delete();
					}
				}
			}
		}

	}

	private void parseFile(final InterestGroup interestGroup, final InputStream inputStream, final OutputStream outputStream) throws IOException, ExportationException
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		final CircaClientsRegistry registry = CircaClientsRegistry.getInstance(circabcServiceRegistry);

		String line, dateStr, user, key, desc, path, parseInfo;
		StringTokenizer tokens;
		Date date;
		LogEntryConfiguration logConfig;
		LogEntry entry;
		BufferedWriter writer;
		writer = new BufferedWriter(new OutputStreamWriter(outputStream, "8859_1"));
		

		while((line = reader.readLine()) != null)
		{
			try
			{
				tokens = new StringTokenizer(EncodingUtils.changeToUTF8Encoding(line, fileClient.getSystemEncoding()));

				dateStr = tokens.nextToken(this.logSeparator);
				user = tokens.nextToken(this.logSeparator);
				key = tokens.nextToken(this.logSeparator);
				// get all the end of the line
				desc = tokens.hasMoreTokens() ? tokens.nextToken("") : "";
				if(desc.startsWith(this.logSeparator))
				{
					// since this line is not getted with its token, the first letter is the token itself.
					desc = desc.substring(this.logSeparator.length());
				}

				try
				{
					date = this.logDateFormat.parse(dateStr.trim());
	            }
	            catch(ParseException ex)
	            {
	            	final String error = "The log file date is not parseable. Date: " + dateStr + ". Expected format: " + this.logDateFormat.toPattern();
	            	if(ignoreErrorEntries)
					{
						logger.warn(error, ex);

						continue;
					}
	            	else
	            	{
	            		throw new ExportationException(error, ex);
	            	}

	            }

	            logConfig = circaLogConfig.getLogEntryConfig(key);

	            if(logConfig == null )
	            {
	            	final String error = "Key '" + key + "'not found in the configuration file. Please to correct the problem.";
	            	if(ignoreErrorEntries)
	            	{
	            		logger.warn(error);
	            	}
	            	else
	            	{
	            		throw new ExportationException(error);
	            	}
	            }
	            else
	            {
	                path = logConfig.getPathParser().parsePath(interestGroup, registry, key, desc);
	                if(path.endsWith(FileClient.PATH_SEPARATOR))
	    			{
	    				path = path.substring(0, path.length() - 1);
	    			}

	    			parseInfo = logConfig.getInfoParser().parseInfo(interestGroup, key, desc);

	    			entry = new LogEntry()
	                	.withDate(date)
	                	.withUsername(user)
	                	.withService(logConfig.getService())
	                	.withActivity(logConfig.getActivity())
	                	.withOk(logConfig.getSuccess())
	                	.withPath(path)
	                	.withInfo(parseInfo);
	    			writer.write(IGLogFilesUtils.toString(entry));
	    			writer.newLine();
	            }
			}
			catch(Throwable t)
			{
				final String error = "Unxexpected error reading log files:  " + t.getMessage();
            	if(ignoreErrorEntries)
            	{
            		logger.warn(error, t);
            	}
            	else
            	{
            		throw new ExportationException(error, t);
            	}
			}

		}

		try
		{
			inputStream.close();
			reader.close();
		}
		catch (IOException ignore){}
	}

	private String getFoldername(final InterestGroup interestGroup)
	{
		final Category category = ElementsHelper.getElementCategory(interestGroup);

		return MessageFormat.format(FOLDER_NAME,
				logFilesLocation,
				category.getName().getValue(),
				interestGroup.getName().getValue());
	}


	public final void setFileClient(FileClient fileClient)
	{
		this.fileClient = fileClient;
	}

	/**
	 * @param circaLogConfig the circaLogConfig to set
	 */
	public final void setCircaLogConfig(CircaLogConfig circaLogConfig)
	{
		this.circaLogConfig = circaLogConfig;
	}

	/**
	 * @param currentLogFilename the currentLogFilename to set
	 */
	public final void setCurrentLogFilename(String currentLogFilename)
	{
		this.currentLogFilename = currentLogFilename;
	}

	/**
	 * @param gzipFilenameRegex the gzipFilenameRegex to set
	 */
	public final void setGzipFilenameRegex(String gzipFilenameRegex)
	{
		this.gzipFilenameRegex = gzipFilenameRegex;
	}

	/**
	 * @param logFilesLocation the logFilesLocation to set
	 */
	public final void setLogFilesLocation(String logFilesLocation)
	{
		this.logFilesLocation = logFilesLocation;
	}

	/**
	 * @param logSeparator the logSeparator to set
	 */
	public final void setLogSeparator(String logSeparator)
	{
		this.logSeparator = logSeparator;
	}

	/**
	 * @param logDateFormat the logDateFormat to set
	 */
	public final void setLogDateFormat(String logDateFormat)
	{
		if(logDateFormat != null)
		{
			this.logDateFormat = new SimpleDateFormat(logDateFormat);
		}
	}

	/**
	 * @param circabcServiceRegistry the circabcServiceRegistry to set
	 */
	public final void setCircabcServiceRegistry(CircabcServiceRegistry circabcServiceRegistry)
	{
		this.circabcServiceRegistry = circabcServiceRegistry;
	}

	/**
	 * @return the ignoreErrorEntries
	 */
	public final boolean isIgnoreErrorEntries()
	{
		return ignoreErrorEntries;
	}

	/**
	 * @param ignoreErrorEntries the ignoreErrorEntries to set
	 */
	public final void setIgnoreErrorEntries(boolean ignoreErrorEntries)
	{
		this.ignoreErrorEntries = ignoreErrorEntries;
	}


}
