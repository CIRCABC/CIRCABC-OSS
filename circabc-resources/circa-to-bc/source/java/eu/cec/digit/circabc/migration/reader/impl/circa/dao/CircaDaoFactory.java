/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.dao;


/**
 *
 * @author Yanick Pignot
 */
public class CircaDaoFactory
{
	private CalendarDao calendarDao;
	private DocumentDao documentDao;
	private GlobalDao globalDao;
	private NewsDao newsDao;
	private SectionDao sectionDao ;

	/**
	 * @return the calendarDao
	 */
	public final CalendarDao getCalendarDao()
	{
		return calendarDao;
	}
	/**
	 * @param calendarDao the calendarDao to set
	 */
	public final void setCalendarDao(final CalendarDao calendarDao)
	{
		this.calendarDao = calendarDao;
	}
	/**
	 * @return the documentDao
	 */
	public final DocumentDao getDocumentDao()
	{
		return documentDao;
	}
	/**
	 * @param documentDao the documentDao to set
	 */
	public final void setDocumentDao(final DocumentDao documentDao)
	{
		this.documentDao = documentDao;
	}
	/**
	 * @return the globalDao
	 */
	public final GlobalDao getGlobalDao()
	{
		return globalDao;
	}
	/**
	 * @param globalDao the globalDao to set
	 */
	public final void setGlobalDao(final GlobalDao globalDao)
	{
		this.globalDao = globalDao;
	}
	/**
	 * @return the newsDao
	 */
	public final NewsDao getNewsDao()
	{
		return newsDao;
	}
	/**
	 * @param newsDao the newsDao to set
	 */
	public final void setNewsDao(final NewsDao newsDao)
	{
		this.newsDao = newsDao;
	}
	/**
	 * @return the sectionDao
	 */
	public final SectionDao getSectionDao()
	{
		return sectionDao;
	}
	/**
	 * @param sectionDao the sectionDao to set
	 */
	public final void setSectionDao(final SectionDao sectionDao)
	{
		this.sectionDao = sectionDao;
	}
}
