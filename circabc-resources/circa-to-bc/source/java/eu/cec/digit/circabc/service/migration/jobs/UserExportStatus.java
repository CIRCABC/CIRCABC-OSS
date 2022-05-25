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

import org.alfresco.util.ParameterCheck;



/**
 * Hold status at a given time of a bulk user exportation plannification
 *
 * @author Yanick Pignot
 */
public class UserExportStatus extends MigrationStatusBase
{
    /** */
    private static final long serialVersionUID = 6477578986141935959L;

    private String query;
    private Boolean conjunction;
    private Boolean negation;

    private static final List<TokenConverter> CONVERTERS = new ArrayList<TokenConverter>(2);
    static{

        CONVERTERS.add(0, new TokenConverter<UserExportStatus>()
            {
                // query converter

                public String getStringValue(final UserExportStatus status)
                {
                    return notNullString(status.getQuery());
                }

                public void setValue(final UserExportStatus status, final String token, final int tokenCount) throws ParseException
                {
                    if(token == null || token.length() < 1)
                    {
                        throw new ParseException("The query is missing", tokenCount);
                    }

                    status.setQuery(token);
                }
            }
        );
        CONVERTERS.add(1, new TokenConverter<UserExportStatus>()
            {
                // conjuction converter

                public String getStringValue(final UserExportStatus status)
                {
                    return notNullString(status.getConjunction());
                }

                public void setValue(final UserExportStatus status, final String token, final int tokenCount) throws ParseException
                {
                    if(token == null || token.length() < 1 || (token.equals(Boolean.TRUE.toString()) == false && token.equals(Boolean.FALSE.toString()) == false))
                    {
                        throw new ParseException("The conjunction is not a valid boolean: " + token, tokenCount);
                    }

                    status.setConjunction(Boolean.valueOf(token));
                }
            }
        );
        CONVERTERS.add(2, new TokenConverter<UserExportStatus>()
            {
                // negation converter

                public String getStringValue(final UserExportStatus status)
                {
                    return notNullString(status.getNegation());
                }

                public void setValue(final UserExportStatus status, final String token, final int tokenCount) throws ParseException
                {
                	if(token == null || token.length() < 1 || (token.equals(Boolean.TRUE.toString()) == false && token.equals(Boolean.FALSE.toString()) == false))
                    {
                        throw new ParseException("The negation is not a valid boolean: " + token, tokenCount);
                    }

                	status.setNegation(Boolean.valueOf(token));
                }
            }
        );
    }

    private UserExportStatus(){}

    /**
     * @param identifier
     * @param expectedFire
     * @param status
     */
    public UserExportStatus(final String identifier, final Date expectedFire, final String query, final Boolean conjunction, final Boolean negation, final List<String> notificationEmails)
    {
        super(identifier, expectedFire, notificationEmails);
        this.conjunction = conjunction;
        this.negation = negation;
        this.query = query;

        ParameterCheck.mandatory("The query", query);
        ParameterCheck.mandatory("Is cunjunction ", conjunction);
        ParameterCheck.mandatory("Is negation ", negation);
    }

    protected List<TokenConverter> getAdditionalTokenConverter()
    {
        return CONVERTERS;
    }

    public static UserExportStatus buildFromString(final String fromString) throws ParseException
    {
    	final UserExportStatus status = new UserExportStatus();
    	MigrationStatusBase.fillFromString(status, fromString);

    	return status;
    }


    /**
     * @return the emails
     */
    public final String getQuery()
    {
        return query;
    }

    /**
     * @return the cunjunction
     */
    public final Boolean getConjunction()
    {
        return conjunction;
    }

    /**
     * @return the negation
     */
    public final Boolean getNegation()
    {
        return negation;
    }

    private final void setConjunction(Boolean conjunction)
    {
        this.conjunction = conjunction;
    }

    private final void setNegation(Boolean negation)
    {
        this.negation = negation;
    }

    private final void setQuery(String query)
    {
        this.query = query;
    }
}