/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.berkeley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sleepycat.db.Db;
import com.sleepycat.db.DbEnv;
import com.sleepycat.db.DbException;
import com.sleepycat.db.Dbc;
import com.sleepycat.db.Dbt;

import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.repo.migration.EncodingUtils;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Class that read the any berkeley db in Circa
 *
 * @author Yanick Pignot
 */
public final class DbClient
{
	private static final Log logger = LogFactory.getLog(DbClient.class);

	private static final ValueConverter<String> DEFAULT_CONVERTER = new StringValueConverter();

	private FileClient fileClient;

	/** a transactionally-safe cache to be injected */
    private SimpleCache<String, Map<String, ? extends Serializable>> berkeleyDBCache;

    private DbEnv dbEnv;



    /**
     * Get a single value for a given key in a db file.
     *
     * @param path
     * @param key
     * @return
     * @throws ExportationException
     */
    public final String readKey(final String path, final String key) throws ExportationException
    {
    	return readKey(path, key, DEFAULT_CONVERTER);
    }

    /**
     * Get a single value for a given key in a db file using a custom value converter.
     *
     * @param <T>
     * @param path
     * @param key
     * @param converter
     * @return
     * @throws ExportationException
     */
    public final <T> T readKey(final String path, final String key, final ValueConverter<T> converter) throws ExportationException
    {
    	ParameterCheck.mandatory("The key", key);

    	final Map<String, String> result = readFile(path);

    	return converter.adapt(result.get(key));
    }

    /**
     * Get all values of a db file.
     *
     * @param path
     * @return
     * @throws ExportationException
     */
    public final Map<String, String> readFile(final String path) throws ExportationException
    {
    	return readFile(path, DEFAULT_CONVERTER);
    }

    /**
     * Get all values of a db file using a custom value converter.
     *
     * @param <T>
     * @param path
     * @param converter
     * @return
     * @throws ExportationException
     */
    @SuppressWarnings("unchecked")
	public final <T extends Serializable> Map<String, T> readFile(final String path, final ValueConverter<T> converter) throws ExportationException
    {
    	if(berkeleyDBCache.get(path) != null)
    	{
    		return (Map<String, T>) berkeleyDBCache.get(path);
    	}

    	Map<String, T> results = null;

    	ParameterCheck.mandatory("The db path", path);
    	ParameterCheck.mandatory("The converter", converter);

    	final File dbFile;
    	File fsFile;

    	if(fileClient.exists(path))
    	{
    		// Impossible to read a db from a ftp stream then copy it in the temp directory.
        	if(fileClient.isSameFileSystem() == false)
        	{
        		for(int x = 0;; ++x)
        		{
        			 fsFile = new File(System.getProperty("java.io.tmpdir"), "bdb" + x + ".db" );
        			 if(fsFile.exists() == false)
        			 {
        				 break;
        			 }
        		}

        		OutputStream out = null;
    			try
    			{
    				out = new FileOutputStream(fsFile);
    				fileClient.download(path, out);

    				out.flush();

    	    		if(logger.isDebugEnabled())
            		{
            			logger.debug("Remote db file " + path + " succesfully copied locally to be read " + fsFile.getAbsolutePath());
            		}

    			}
    			catch (final Exception e)
    			{
    				throw new ExportationException("Error to access to the local copied db file: " + fsFile.getAbsolutePath(), e);
    			}
    			finally
    			{
    				if(out != null)
    				{
    	    			try
    					{
    	    				out.close();
    					}
    					catch (Exception ignore){}
    				}
    			}
    			dbFile = fsFile;
        	}
        	else
        	{
        		dbFile = new File(path);
        		fsFile = null;
        	}

        	try
    		{
        		results = readFile(dbFile, converter);
    		}
        	catch (final DbException e)
    		{
        		logger.error("Error reading the berkeley file. The file exists, then the data are perhaps corrupted. File: " + path, e);

        		if(fsFile != null)
        		{
        			logger.error("The remote file " + path + " was copied locally: " + dbFile.getAbsolutePath());
        		}
    		}

        	if(fsFile != null)
        	{
        		fsFile.delete();

        		if(logger.isDebugEnabled())
        		{
        			logger.debug("Locally copied db file " + fsFile.getAbsolutePath() + " successfully deleted.");
        		}
        	}

        	berkeleyDBCache.put(path, results);
    	}
    	else
    	{
    		if(logger.isDebugEnabled())
    		{
    			logger.debug("DB File " + path + " doesn't exist.");
    		}
    	}

    	return results == null ? Collections.<String, T> emptyMap() : results;
    }

	/**
	 * @param <T>
	 * @param converter
	 * @param results
	 * @param dbFile
	 * @throws ExportationException
	 * @throws DbException
	 */
	public <T> Map<String, T> readFile(final File dbFile, final ValueConverter<T> converter) throws ExportationException, DbException
	{
		final Map<String, T> results = new HashMap<String, T>();

		if(dbFile.exists() == false)
		{
			throw new ExportationException("Impossible to access to the berkeley db. " + dbFile.getAbsolutePath() + " not exists.");
		}

		final DbEnv env = getDbEnv();

		final Db table = Db.open(dbFile.getAbsolutePath(), Db.DB_UNKNOWN, Db.DB_RDONLY, 0644, env, null);

		// Acquire an iterator for the table.
		final Dbc iterator = table.cursor(null, 0);

		// We use the DB_DBT_MALLOC flag to ask DB to allocate byte arrays for the results.
		final Dbt key = new Dbt();
		key.set_flags(Db.DB_DBT_MALLOC);

		final Dbt data = new Dbt();
		data.set_flags(Db.DB_DBT_MALLOC);

		String keyAsString;
		String valueAsString;

		// workaroud for test purposes. It allow to call this method without starting the server
		final String encoding = fileClient == null ? "UTF-8" : fileClient.getSystemEncoding();

		while (iterator.get(key, data, Db.DB_NEXT) == 0)
		{
			try
			{
				keyAsString = EncodingUtils.changeToUTF8Encoding(key.get_data(), 0, key.get_size(), encoding);
				valueAsString =  EncodingUtils.changeToUTF8Encoding(data.get_data(), 0, data.get_size(), encoding);
			}
			catch (final UnsupportedEncodingException e)
			{
				keyAsString = new String(key.get_data(), 0, key.get_size());
				valueAsString =  new String(data.get_data(), 0, data.get_size());
			}

			results.put(keyAsString, converter.adapt(valueAsString));
		}

		if(logger.isDebugEnabled())
		{
			logger.debug(results.size() + " keys found in " + dbFile.getAbsolutePath());
		}

		iterator.close();
		table.close(0);

		return results;
	}

    /**
	 * @param fileClient the fileClient to set
	 */
	public final void setFileClient(FileClient fileClient)
	{
		this.fileClient = fileClient;
	}

	/**
	 * @param berkeleyDBCache the berkeleyDBCache to set
	 */
	public final void setBerkeleyDBCache(SimpleCache<String, Map<String, ? extends Serializable>> berkeleyDBCache)
	{
		this.berkeleyDBCache = berkeleyDBCache;
	}

	/**
	 * @return the dbEnv
	 */
	public final DbEnv getDbEnv() throws ExportationException
	{
		if(dbEnv == null)
		{
			try
			{
				dbEnv = new DbEnv(".", null, 0);
			}
			catch (final Throwable e)
			{
				throw new ExportationException("Impossible to read berkeley db files.\n" +
						"Ensure that the following  shared libraries are in the path: lidbd.so and libdb_java.so.\n" +
						"Property 'LD_LIBRARY_PATH': " + System.getProperty("LD_LIBRARY_PATH") + "\n" +
						"Property 'java.library.path': " + System.getProperty("java.library.path"), e);
			}

		}

		return dbEnv;
	}
}
