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
 * Hold status at a given time of an iteration export plannification
 *
 * @author Yanick Pignot
 */
public class IterationExportStatus extends MigrationStatusBase
{
    /** */
    private static final long serialVersionUID = 6477598764542319359L;

    private String iterationName;
    private String iterationDescription;
    private List<CategoryInterestGroupPair> interestGroups;

    private static final List<TokenConverter> CONVERTERS = new ArrayList<TokenConverter>(3);
    static{
        CONVERTERS.add(0, new TokenConverter<IterationExportStatus>()
            {
                // iteration converter

                public String getStringValue(final IterationExportStatus status)
                {
                    return notNullString(status.getIterationName());
                }

                public void setValue(final IterationExportStatus status, final String token, final int tokenCount) throws ParseException
                {
                    if(token == null || token.length() < 1)
                    {
                        throw new ParseException("The iteration is missing", tokenCount);
                    }
                    status.setIterationName(token);
                }
            }
        );
        CONVERTERS.add(1, new TokenConverter<IterationExportStatus>()
                {
                    // iteration description

                    public String getStringValue(final IterationExportStatus status)
                    {
                    	return notNullString(status.getIterationName());
                    }

                    public void setValue(final IterationExportStatus status, final String token, final int tokenCount) throws ParseException
                    {
                    	status.setIterationDescription(token);
                    }
                }
            );
        CONVERTERS.add(2, new TokenConverter<IterationExportStatus>()
                {
                    // interest group list converter

                    public String getStringValue(final IterationExportStatus status)
                    {
                        return notNullStringList(status.getInterestGroups());
                    }

                    public void setValue(final IterationExportStatus status, final String token, final int tokenCount) throws ParseException
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

    private IterationExportStatus(){}

    /**
     * @param identifier
     * @param expectedFire
     * @param status
     */
    public IterationExportStatus(final String identifier, final Date expectedFire, final String iterationName, final String iterationDescription, final List<CategoryInterestGroupPair> interestGroups, final List<String> notificationEmails)
    {
        super(identifier, expectedFire, notificationEmails);
        this.iterationName = iterationName;
        this.iterationDescription = iterationDescription;
        this.interestGroups = interestGroups;
    }

    protected List<TokenConverter> getAdditionalTokenConverter()
    {
        return CONVERTERS;
    }

    public static IterationExportStatus buildFromString(final String fromString) throws ParseException
    {
    	final IterationExportStatus status = new IterationExportStatus();
    	MigrationStatusBase.fillFromString(status, fromString);

    	return status;
    }

    public final String getIterationName()
    {
        return iterationName;
    }

    private final void setIterationName(final String iterationName)
    {
        this.iterationName = iterationName;
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

	/**
	 * @return the iterationDescription
	 */
	public final String getIterationDescription()
	{
		return iterationDescription;
	}

	/**
	 * @param iterationDescription the iterationDescription to set
	 */
	public final void setIterationDescription(String iterationDescription)
	{
		this.iterationDescription = iterationDescription;
	}


}