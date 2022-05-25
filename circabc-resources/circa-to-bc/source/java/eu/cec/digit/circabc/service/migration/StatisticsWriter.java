package eu.cec.digit.circabc.service.migration;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base interface for a statistic writer.
 *
 * @author Yanick Pignot
 */
public interface StatisticsWriter
{

	/**
	 * Define the "user friendly" name of the writer
	 *
	 * @return			the reader display name
	 */
	public abstract String getReaderDisplayName();

	/**
	 * Define the unique extension for the file to create.
	 *
	 * @return			the file extension
	 */
	public abstract String getExtension();

	/**
	 * If true, three output streams will be setted and sent as parameter to the <code>write</code> method the first
	 * one for RootStat, the second for CategoryStat and the third one for the InterestGroupStat
	 *
	 * If false, only one stream will be sent to the <code>write</code> method
	 *
	 * @see	eu.cec.digit.circabc.service.migration.RootStat
	 * @see	eu.cec.digit.circabc.service.migration.CategoryStat
	 * @see	eu.cec.digit.circabc.service.migration.InterestGroupStat
	 *
	 * @return			if need to create three files or not (only one)
	 */
	public abstract boolean isNeedThreeFiles();

	/**
	 * Write the statistics in one or three streams
	 *
	 * @param rootStat			The root statistic
	 * @param outputStreams		The one or three stream where to write statitistics
	 * @throws IOException		If an IOException occurs
	 */
	public abstract void write(final RootStat rootStat, final OutputStream... outputStreams) throws IOException;

}