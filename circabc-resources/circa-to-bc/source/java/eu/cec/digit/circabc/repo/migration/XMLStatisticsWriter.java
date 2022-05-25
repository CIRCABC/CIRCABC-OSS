/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import eu.cec.digit.circabc.service.migration.CategoryStat;
import eu.cec.digit.circabc.service.migration.InterestGroupStat;
import eu.cec.digit.circabc.service.migration.RootStat;

/**
 * Write statistics in an xml file format
 *
 * @author Yanick Pignot
 */
public class XMLStatisticsWriter extends StatisticWriterBase
{
	
	private static final String START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private static final String TOP_ELEMENT = "<statistics impl=\"{0}\">";
	private static final String TOP_ELEMENT_END = "</statistics>";

	private static final String ROOT_ELEMENT = "<root>";
	private static final String ROOT_ELEMENT_END = "</root>";

	private static final String CAT_ELEMENT = "<category name=\"{0}\" header=\"{1}\" hidden=\"{2}\">";
	private static final String CAT_ELEMENT_END = "</category>";

	private static final String IG_ELEMENT = "<interestgroup name=\"{0}\">";
	private static final String IG_ELEMENT_END = "</interestgroup>";

	private static final String VALUE_ELEMENTS = "<{0}>{1}</{0}>";

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.StatisticsWriter#write(eu.cec.digit.circabc.service.migration.RootStat, java.io.OutputStream)
	 */
    public synchronized void write(final RootStat rootStat, final OutputStream ... outputStreams) throws IOException
    {
    	final List<Method> rootMethods = getGetters(RootStat.class);
    	final List<Method> catMethods = getGetters(CategoryStat.class);
    	final List<Method> igMethods = getGetters(InterestGroupStat.class);

    	final OutputStream out = outputStreams[0];

    	out.write(toBytes(START, true));
    	out.write(toBytes(TOP_ELEMENT, true, rootStat.getExportationImplementationName()));
    	out.write(toBytes(ROOT_ELEMENT, true));

    	printMehodValues(out, rootMethods, rootStat);

    	for(final CategoryStat categoryStat: rootStat.getCategoryStats())
    	{
    		out.write(toBytes(CAT_ELEMENT, true, categoryStat.getCategoryName(), categoryStat.getCategoryHeader(), categoryStat.isHiddenCategoryHeader()));

    		printMehodValues(out, catMethods, categoryStat);

    		for(final InterestGroupStat interestGroupStat: categoryStat.getInterestGroupStats())
    		{
    			out.write(toBytes(IG_ELEMENT, true, interestGroupStat.getIgName()));

    			printMehodValues(out, igMethods, interestGroupStat);

    	    	out.write(toBytes(IG_ELEMENT_END, true));
    		}

    		out.write(toBytes(CAT_ELEMENT_END, true));
    	}

    	out.write(toBytes(ROOT_ELEMENT_END, true));
    	out.write(toBytes(TOP_ELEMENT_END, true));

    }

	/**
	 * @param out
	 * @param methods
	 * @param stat
	 * @throws IOException
	 */
	private void printMehodValues(final OutputStream out, final List<Method> methods, final Object stat) throws IOException
	{
		for(final Method method: methods)
		{
			out.write(toValueElement(method, stat));
		}
	}

    private final byte[] toValueElement(final Method method, final Object object)
    {
    	return toBytes(VALUE_ELEMENTS, true, getMethodTitle(method.getName()), safeInvoke(method, object));
    }
  }
