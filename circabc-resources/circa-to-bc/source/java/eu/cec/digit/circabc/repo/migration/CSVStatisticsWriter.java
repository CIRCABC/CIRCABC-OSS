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
public class CSVStatisticsWriter extends StatisticWriterBase
{
	private static final String SEPARATOR = ",";

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.StatisticsWriter#write(eu.cec.digit.circabc.service.migration.RootStat, java.io.OutputStream)
	 */
    public synchronized void write(final RootStat rootStat, final OutputStream ... outputStreams) throws IOException
    {
    	final List<Method> rootMethods = getGetters(RootStat.class);
    	final List<Method> catMethods = getGetters(CategoryStat.class);
    	final List<Method> igMethods = getGetters(InterestGroupStat.class);

    	final OutputStream rootOut = outputStreams[0];
    	final OutputStream catOut = outputStreams[1];
    	final OutputStream igOut = outputStreams[2];

    	writeFirstLine(rootMethods, rootOut);
    	writeFirstLine(catMethods, catOut, "getCategoryHeader", "getCategoryName");
    	writeFirstLine(igMethods, igOut, "getCategoryHeader", "getCategoryName", "getIgName");

    	writeValueLine(rootMethods, rootOut, rootStat);
    	for(final CategoryStat categoryStat: rootStat.getCategoryStats())
    	{
    		final String categoryName = categoryStat.getCategoryName();
			final String categoryHeader = categoryStat.getCategoryHeader();

			writeValueLine(catMethods, catOut, categoryStat, categoryHeader, categoryName);

			for(final InterestGroupStat interestGroupStat: categoryStat.getInterestGroupStats())
    		{
				writeValueLine(igMethods, igOut, interestGroupStat, categoryHeader, categoryName, interestGroupStat.getIgName());
    		}
    	}
    }


	private void writeFirstLine(final List<Method> methods, final OutputStream out, final String ... firstColumns) throws IOException
	{
		for(final String column: firstColumns)
		{
			out.write(toBytes(getMethodTitle(column) + SEPARATOR, false));
		}
		for(final Method method: methods)
    	{
			out.write(toBytes(getMethodTitle(method.getName()) + SEPARATOR, false));
    	}
		out.write(toBytes("", true));
	}

	private void writeValueLine(final List<Method> methods, final OutputStream out, final Object stat, final String ... firstColumns) throws IOException
	{
		for(final String column: firstColumns)
		{
			out.write(toBytes(column + SEPARATOR, false));
		}
		for(final Method method: methods)
	    {
			out.write(toBytes(safeInvoke(method, stat) + SEPARATOR, false));
	    }
		out.write(toBytes("", true));
	}
}
