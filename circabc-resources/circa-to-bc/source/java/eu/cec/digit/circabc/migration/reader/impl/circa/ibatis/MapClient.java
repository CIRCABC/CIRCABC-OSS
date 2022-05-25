/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/


package eu.cec.digit.circabc.migration.reader.impl.circa.ibatis;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * Singleton that manage the ibatis sql map client instance
 *
 * @author Yanick Pignot
 */
/*package*/ final class MapClient
{
	private static final Log logger = LogFactory.getLog(MapClient.class);
    //TODO should be configurable
    private static final  String RESOURCE = "alfresco/extension/migration/ibatis/circa-SqlMapConfig.xml";
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_WAIT = 750L;
    private static SqlSession sqlSession;
    private static Connection connection;

    private MapClient(){};

    /*package*/ static SqlSession getInstance() throws IOException, SQLException
    {
    	return getInstance(1);
    }

    private static SqlSession getInstance(int tries) throws IOException, SQLException
    {
        if(sqlSession == null)
        {
            final InputStream inputStream = Resources.getResourceAsStream(RESOURCE);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            sqlSession = sqlSessionFactory.openSession();

            if(connection != null && !connection.isClosed())
            {
            	connection.close();
            }

            connection = sqlSession.getConnection();

            return sqlSession;
        }
        else
        {
            try
            {
            	if(sqlSession == null || connection == null || connection.isClosed())
            	{
            		sqlSession = null;
            		connection = null;
                	return getInstance();
            	}
            	else
            	{
            		return sqlSession;
            	}
            }
            catch(final Exception e)
            {
            	try
            	{
            		if(connection != null && !connection.isClosed())
            		{
                    	connection.close();
                    }
            	}
            	catch(final Exception ignore){}
            	finally
            	{
            		connection = null;
            		sqlSession = null;
            	}

            	if(tries > MAX_RETRIES)
            	{
            		if(e instanceof IOException)
            		{
            			throw (IOException) e;
            		}
            		else if(e instanceof SQLException)
            		{
            			throw (SQLException) e;
            		}
            		else
            		{
            			if(logger.isInfoEnabled()) {
                    		logger.error("Error", e);
            			}
            			throw new IOException("Max retry reached: " + MAX_RETRIES  + ". Message: " + e.getMessage());
            		}
            	}
            	else
            	{
            		try
            		{
            			Thread.sleep(tries * RETRY_WAIT);
            		}
            		catch(final Exception ignore){}

            		return getInstance(++tries);
            	}
            }
        }
    }
}