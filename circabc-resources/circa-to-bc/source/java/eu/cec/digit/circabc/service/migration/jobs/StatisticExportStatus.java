/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration.jobs;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;

/**
 * Hold status at a given time of statistic exportation plannification
 *
 * @author Yanick Pignot
 */
public class StatisticExportStatus extends MigrationStatusBase
{
    /** */
    private static final long serialVersionUID = 6477578986141935959L;

    private List<CategoryInterestGroupPair> interestGroups;

    private static final List<TokenConverter> CONVERTERS = new ArrayList<TokenConverter>(1);
    static{

    	CONVERTERS.add(new TokenConverter<StatisticExportStatus>()
                {
                    // interest group list converter

                    public String getStringValue(final StatisticExportStatus status)
                    {
                        return notNullStringList(status.getInterestGroups());
                    }

                    public void setValue(final StatisticExportStatus status, final String token, final int tokenCount) throws ParseException
                    {
                        if(token == null || token.length() < 1)
                        {
                            throw new ParseException("The interest group list is missing", tokenCount);
                        }

                        final StringTokenizer tokens = new StringTokenizer(token, LIST_SEPARATOR, false);

                        if(tokens.countTokens() < 1)
                        {
                        	throw new ParseException("At least one interest group must be defined", tokenCount);
                        }

                        final List<CategoryInterestGroupPair> pairs = new ArrayList<CategoryInterestGroupPair>(tokens.countTokens());
                        while(tokens.hasMoreTokens())
                        {
                        	pairs.add(CategoryInterestGroupPair.fromString(tokens.nextToken()));
                        }

                        status.setInterestGroups(pairs);
                    }
                }
            );
    }

    private StatisticExportStatus(){}
    /**
     * @param identifier
     * @param expectedFire
     * @param status
     */
    public StatisticExportStatus(final String identifier, final Date expectedFire, final List<CategoryInterestGroupPair> interestGroups, final List<String> notificationEmails)
    {
        super(identifier, expectedFire, notificationEmails);
        this.interestGroups = interestGroups;
    }

    protected List<TokenConverter> getAdditionalTokenConverter()
    {
        return CONVERTERS;
    }

    public static StatisticExportStatus buildFromString(final String fromString) throws ParseException
    {
    	final StatisticExportStatus status = new StatisticExportStatus();
    	MigrationStatusBase.fillFromString(status, fromString);

    	return status;
    }

	/**
	 * @return the interestGroups
	 */
	public final List<CategoryInterestGroupPair> getInterestGroups()
	{
		return interestGroups;
	}

	/**
	 * @param interestGroups the interestGroups to set
	 */
	public final void setInterestGroups(List<CategoryInterestGroupPair> interestGroups)
	{
		this.interestGroups = interestGroups;
	}
}