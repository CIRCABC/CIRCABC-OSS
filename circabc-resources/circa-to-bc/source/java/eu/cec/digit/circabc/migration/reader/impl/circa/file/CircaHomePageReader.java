/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa.file;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Class that get the relevant information for the category management.
 *
 *  The category titles and category headers are found in the circa home page and not in a server.
 *
 * @author Yanick Pignot
 */
public class CircaHomePageReader
{
	private static final String URL_IRC_PART = "/irc/";
	private static final String SLASH = "/";
	private static final String URL_HOME_PART = "/home";
	private static final char HREF_END = '"';
	private static final String HREF_START = "href=\"";
	private static final char HTML_ELEMENT_END = '>';
	private static final char HTML_ELEMENT_START = '<';

	private String circaHomePage;
	private String topCatgeryRegex;
	private String categoryRegex;
	private String defaultHeaderName;
	private FileClient fileClient;

	private Map<String, String> categoryHeaders = null;
	private Map<String, String> categoryTitles = null;


	public String getCategoryTitle(final String categoryName) throws ExportationException
	{
		if(categoryTitles == null)
		{
			fillCategoryMaps();
		}

		return categoryTitles.get(categoryName);
	}

	public String getCategoryHeader(final String categoryName) throws ExportationException
	{
		if(categoryHeaders == null)
		{
			fillCategoryMaps();
		}

		return categoryHeaders.get(categoryName);
	}

	public Set<String> getAllCategoryHeaders() throws ExportationException
	{
		if(categoryHeaders == null)
		{
			fillCategoryMaps();
		}

		final Set<String> headers = new HashSet<String>();
		headers.addAll(categoryHeaders.values());

		return headers;
	}


	private void fillCategoryMaps() throws ExportationException
	{
		final String content = fileClient.downloadAsString(circaHomePage);
		final ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		categoryHeaders = new HashMap<String, String>();
		categoryTitles  = new HashMap<String, String>();

		String line = null;
		String lastCategoryHeader = null;
		String categoryName = null;
		String categoryTitle = null;

		try
		{
			while ((line = reader.readLine()) != null)
			{
				if(line.trim().matches(topCatgeryRegex))
				{
					lastCategoryHeader = skipHtmlNoise(line);
				}
				else if (line.trim().matches(categoryRegex))
				{
					categoryTitle = skipHtmlNoise(line);
					categoryName = retreiveCategoryName(getFirstHref(line));

					if(categoryName != null)
					{
						categoryHeaders.put(categoryName, lastCategoryHeader);
						categoryTitles.put(categoryName, categoryTitle);
					}
					// else{} not a category but an ig. Don't manage it.
				}
			}
		}
		catch (IOException e)
		{
			throw new ExportationException("Problem reading the circa home page '" + circaHomePage + "'", e);
		}
		finally
		{
			try
			{
				is.close();
				reader.close();
			} catch (IOException e)
			{
				throw new ExportationException("Problem closing reader the circa home page '" + circaHomePage + "'", e);
			}
		}

	}

	 private static String skipHtmlNoise(final String html)
	 {
		 final StringBuffer buffer = new StringBuffer();

		 boolean skip = false;
		 for(final char character: html.toCharArray())
		 {
			 if(character == HTML_ELEMENT_START)
			 {
				 skip = true;
			 }
			 else if(character == HTML_ELEMENT_END)
			 {
				 skip = false;
			 }
			 else if(skip == false)
			 {
				 buffer.append(character);
			 }
		 }

		 return buffer.toString().trim();
	 }

	 private static String getFirstHref(final String html)
	 {
		 int hrefPos = html.indexOf(HREF_START);
		 if(hrefPos < 0)
		 {
			 return null;
		 }
		 else
		 {
			 hrefPos = hrefPos + HREF_START.length();

			 int endRef = html.indexOf(HREF_END, hrefPos);

			 if(endRef < 0)
			 {
				 endRef = html.length();
			 }

			 return html.substring(hrefPos, endRef);
		 }
	 }

	 private static String retreiveCategoryName(final String url)
	 {
		 final int homePos = url.toLowerCase().indexOf(URL_HOME_PART);
		 if(homePos < 0 )
		 {
			 // some category urls seems like /cat/
			 return url.replace(SLASH, "");
		 }
		 else
		 {
			 // the other seems /?/irc/cat/home or /?/irc/cat/Home/main

			 final String urlWithNoHome = url.substring(0, homePos);
			 int nameStart = urlWithNoHome.lastIndexOf(URL_IRC_PART);

			 if(nameStart < 0)
			 {
				 nameStart = 0;
			 }
			 else
			 {
				 nameStart += URL_IRC_PART.length();
			 }

			 final String name = urlWithNoHome.substring(nameStart);

			 if(name.indexOf(SLASH) < 0)
			 {
				 return name;
			 }
			 else
			 {
				 // else the link target is an interest group and not a category
				 return null;
			 }
		 }
	 }

	/**
	 * @param categoryRegex the categoryRegex to set
	 */
	public final void setCategoryRegex(String categoryRegex)
	{
		this.categoryRegex = categoryRegex;
	}

	/**
	 * @param circaHomePage the circaHomePage to set
	 */
	public final void setCircaHomePage(String circaHomePage)
	{
		this.circaHomePage = circaHomePage;
	}

	/**
	 * @param topCatgeryRegex the topCatgeryRegex to set
	 */
	public final void setTopCatgeryRegex(String topCatgeryRegex)
	{
		this.topCatgeryRegex = topCatgeryRegex;
	}

	public final void setFileClient(final FileClient fileClient)
	{
		this.fileClient = fileClient;
	}

	public final void setDefaultHeaderName(String defaultHeaderName)
	{
		this.defaultHeaderName = defaultHeaderName;
	}

	public final String getDefaultHeaderName()
	{
		return defaultHeaderName;
	}
}
