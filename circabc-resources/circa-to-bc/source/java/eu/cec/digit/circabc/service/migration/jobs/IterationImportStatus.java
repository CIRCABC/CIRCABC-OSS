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

/**
 * Hold status at a given time of an iteration import plannification
 *
 * @author Yanick Pignot
 */
public class IterationImportStatus extends MigrationStatusBase
{
	/** */
	private static final long serialVersionUID = 6477598764542319359L;

	private String iterationName;

	private IterationImportStatus(){}

    private static final List<TokenConverter> CONVERTERS = new ArrayList<TokenConverter>(1);
    static{
        CONVERTERS.add(new TokenConverter<IterationImportStatus>()
            {
                // iteration converter

                public String getStringValue(final IterationImportStatus status)
                {
                    return notNullString(status.getIterationName());
                }

                public void setValue(final IterationImportStatus status, final String token, final int tokenCount) throws ParseException
                {
                    if(token == null || token.length() < 1)
                    {
                        throw new ParseException("The iteration is missing", tokenCount);
                    }
                    status.setIterationName(token);
                }
            }
        );
    }

	/**
	 * @param identifier
	 * @param expectedFire
	 * @param status
	 */
	public IterationImportStatus(final String identifier, final Date expectedFire, final String iterationName, final List<String> notificationEmails)
	{
		super(identifier, expectedFire, notificationEmails);
		this.iterationName = iterationName;
	}

	protected List<TokenConverter> getAdditionalTokenConverter()
    {
        return CONVERTERS;
    }

    public static IterationImportStatus buildFromString(final String fromString) throws ParseException
    {
    	final IterationImportStatus status = new IterationImportStatus();
    	MigrationStatusBase.fillFromString(status, fromString);

    	return status;
    }

	public final String getIterationName()
	{
		return iterationName;
	}

	private final void setIterationName(String iterationName)
	{
		this.iterationName = iterationName;
	}
}