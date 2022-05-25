/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.ibatis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import eu.cec.digit.circabc.migration.reader.impl.circa.dao.NewsDao;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.News;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.NewsLinguistic;

/**
 * Implementation of the circa news dao using ibatis sql mapper.
 *
 * @author Yanick Pignot
 */
public class NewsDaoImpl implements NewsDao
{
	private static final String QUERY_ALL_NEWS = "getAllNews";
	private static final String QUERY_SPECIFIC_NEWS = "getSingleNews";
	private static final String QUERY_SPECIFIC_NEWS_LINGUISTIC = "getNewsLinguistic";

	private static final String PARAM_TITLE = "title";

	public List<News> getAllNews(final String virtualCirca, final String interestGroup) throws SQLException, IOException
	{
		final List<News> allNews = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_ALL_NEWS) ;

		for(final News news: allNews)
		{
			fillLinguistic(virtualCirca, interestGroup, news);
		}

		return allNews;
	}

	public News getNewsByTitle(final String virtualCirca, final String interestGroup, final String title) throws SQLException, IOException
	{
		final News news = IbatisDaoManager.querySingle(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_SPECIFIC_NEWS, Collections.singletonMap(PARAM_TITLE, title));
		if(news != null)
		{
			fillLinguistic(virtualCirca, interestGroup, news);
		}
		return news;
	}

	private void fillLinguistic(final String virtualCirca, final String interestGroup, final News news) throws SQLException, IOException
	{
		final Map<String, String> param = Collections.singletonMap(PARAM_TITLE, news.getTitle());
		final List<NewsLinguistic> linguistic = IbatisDaoManager.query(DBUtil.getValidName(virtualCirca), DBUtil.getValidName(interestGroup), QUERY_SPECIFIC_NEWS_LINGUISTIC, param);
		news.setNewsLinguistics(linguistic);
	}

}
