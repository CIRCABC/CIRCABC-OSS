package eu.cec.digit.circabc.service.migration.jobs;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

public abstract class MigrationStatusBase implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6134673141265860537L;
	protected static final ThreadLocal<DateFormat> _DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
		}
	};
	protected static final String SEPARATOR = "|";
	protected static final String NOT_DEFINED = "/";
	protected static final String LIST_SEPARATOR = ",";

	private String identifier;
	private Date expectedFire;
	private List<String> notificationEmails;
	private Date realFire;
	private Date endDate;
	private NodeRef xmlFile;
	private int numberOfItems;
	private String errorMessage;

	protected interface TokenConverter <T extends MigrationStatusBase>
	{
		/**
		 * @param status
		 * @param token
		 */
		public void setValue(final T status, final String token, final int tokenCount) throws ParseException;

		/**
		 * @param status
		 * @param token
		 */
		public String getStringValue(final T status);
	}

	public enum FireStatus
	{
		WAITING,
		PENDING,
		SUCCESS,
		ERROR
	}

	protected MigrationStatusBase(){}

	protected MigrationStatusBase(final String identifier, final List<String> notificationEmails)
	{
		this(identifier, new Date(), notificationEmails);
	}

	protected MigrationStatusBase(final String identifier, final Date expectedFire, final List<String> notificationEmails)
	{
		super();
		this.identifier = identifier;
		this.expectedFire = expectedFire;
		this.notificationEmails = notificationEmails;

		ParameterCheck.mandatory("The fire date", expectedFire);
		ParameterCheck.mandatoryString("The identifier" , identifier);
	}

	protected abstract List<TokenConverter> getAdditionalTokenConverter();

	protected static Date getDate(final String dateStr, final int parseIdx, final boolean mandatory) throws ParseException
	{
		if(dateStr == null || dateStr.length() < 1 || dateStr.equals(NOT_DEFINED))
		{
			if(mandatory)
			{
				throw new ParseException("Mandatory date is not defined", parseIdx);
			}
			else
			{
				return null;
			}
		}
		else
		{
			try
			{
				return _DATE_FORMAT.get().parse(dateStr);
			}
			catch(NumberFormatException ex)
			{
				throw new ParseException("Date not valid: " + dateStr, parseIdx);
			}
		}
	}

	protected static List<String> getList(final String listStr, int parseIdx, boolean mandatory) throws ParseException
	{
		if(listStr == null || listStr.length() < 1 || listStr.equals(NOT_DEFINED))
		{
			if(mandatory)
			{
				throw new ParseException("Mandatory list is not defined", parseIdx);
			}
			else
			{
				return null;
			}
		}
		else
		{
			final StringTokenizer tokens = new StringTokenizer(listStr, LIST_SEPARATOR, false);
			final List<String> list = new ArrayList<String>(tokens.countTokens());
			while(tokens.hasMoreTokens())
			{
				list.add(tokens.nextToken());
			}

			return list;
		}
	}

	protected static String notNullString(Object o)
	{
		final String objStr = (o == null) ? "" : o.toString().trim();

		if(objStr.length() < 1)
		{
			return NOT_DEFINED;
		}
		else
		{
			return objStr;
		}
	}

	protected static String notNullStringList(final List<? extends Object> list)
	{
		if(list == null || list.size() < 1)
		{
			return NOT_DEFINED;
		}
		else
		{
			final StringBuilder builder = new StringBuilder("");
			for(final Object s: list)
			{
				builder
					.append(s.toString())
					.append(LIST_SEPARATOR);
			}

			return builder.toString();
		}
	}

	protected static String nextToken(StringTokenizer tokens)
	{
		final String token = tokens.nextToken();

		if(token.equals(NOT_DEFINED))
		{
			return "";
		}
		else
		{
			return token;
		}

	}

	/**
	 * @return the expectedFire
	 */
	public final Date getExpectedFire()
	{
		return expectedFire;
	}

	/**
	 * @return the realFire
	 */
	public final Date getRealFire()
	{
		return realFire;
	}

	/**
	 * @return the status
	 */
	public final FireStatus getStatus()
	{
		if(errorMessage != null && errorMessage.length() > 0 )
		{
			return FireStatus.ERROR;
		}
		else if(realFire == null)
		{
			return FireStatus.WAITING;
		}
		else
		{
			if(endDate != null)
			{
				return FireStatus.SUCCESS;
			}
			else
			{
				return FireStatus.PENDING;
			}
		}
	}

	/**
	 * @return the xmlFile
	 */
	public final NodeRef getXmlFile()
	{
		return xmlFile;
	}

	/**
	 * @return the identifier
	 */
	public final String getIdentifier()
	{
		return identifier;
	}

	/**
	 * @param realFire the realFire to set
	 */
	public final void setRealFire(Date realFire)
	{
		this.realFire = realFire;
	}

	/**
	 * @param xmlFile the xmlFile to set
	 */
	public final void setXmlFile(NodeRef xmlFile)
	{
		this.xmlFile = xmlFile;
	}

	/**
	 * @return the errorMessage
	 */
	public final String getErrorMessage()
	{
		return errorMessage;
	}


	/**
	 * @param errorMessage the errorMessage to set
	 */
	public final void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	/**
	 * @return	the number of items
	 */
	public final int getNumberOfItems()
	{
		return numberOfItems;
	}

	/**
	 * @param numberOfItems the number of items
	 */
	public final void setNumberOfItems(int numberOfItems)
	{
		this.numberOfItems = numberOfItems;
	}

	public final List<String> getNotificationEmails()
	{
		return notificationEmails;
	}

	@SuppressWarnings("unchecked")
	public String toString()
	{
		final StringBuilder builder = new StringBuilder("");
		builder
			.append(notNullString(getIdentifier())).append(SEPARATOR)
			.append(notNullString(formatDate(getExpectedFire()))).append(SEPARATOR)
			.append(notNullString(formatDate(getRealFire()))).append(SEPARATOR)
			.append(notNullString(formatDate(getEndDate()))).append(SEPARATOR)
			.append(notNullString(getXmlFile())).append(SEPARATOR)
			.append(notNullString(getNumberOfItems())).append(SEPARATOR)
			.append(notNullString(getErrorMessage())).append(SEPARATOR)
			.append(notNullStringList(getNotificationEmails())).append(SEPARATOR);

		for(int x = 0; x < getAdditionalTokenConverter().size(); ++x)
		{
			final TokenConverter<MigrationStatusBase> converter = getAdditionalTokenConverter().get(x);
			builder
				.append(converter.getStringValue(this))
				.append(SEPARATOR);
		}

		return builder.toString();
	}

	private String formatDate(final Date date) {
		return date == null ? null  : _DATE_FORMAT.get().format(date);
	}


	@SuppressWarnings("unchecked")
	protected static void fillFromString(final MigrationStatusBase status, final String fromString) throws ParseException
	{
		final StringTokenizer tokens = new StringTokenizer(fromString, SEPARATOR, false);

		// count hold the token index
		int count = 0;

		final int expectedTokens = status.getTokenCount(status);
		if(tokens == null || tokens.countTokens() != expectedTokens)
		{
			throw new ParseException(fromString + " not a valid Migration string representation (" + tokens.countTokens() + " tokens found) and not " + expectedTokens + " as expected." , 0);
		}

		// --	mandatory values

		final String identifierStr = nextToken(tokens);
		if(identifierStr.length() < 1)
		{
			throw new ParseException("The unique identifier is missing", count += 1);
		}
		status.setIdentifier(identifierStr);

		status.setExpectedFire(getDate(nextToken(tokens), count += 1, true));

		// --	optional values

		status.setRealFire(getDate(nextToken(tokens), count += 1, false));

		status.setEndDate(getDate(nextToken(tokens), count += 1, false));

		status.setXmlFile(getNodeRefOrNull(nextToken(tokens), count += 1));

		final String itemCount = nextToken(tokens);
		if(itemCount.length() > 0)
		{
			try
			{
				final int parseInt = Integer.parseInt(itemCount);
				status.setNumberOfItems(parseInt);

			}
			catch(NumberFormatException ex)
			{
				throw new ParseException("The number of user is not a valid number : " + itemCount, count += 1);
			}
		}

		status.setErrorMessage(nextToken(tokens));
		count += 1;

		status.setNotificationEmails(getList(nextToken(tokens), count += 1, false));

		// --	Custom values

		for(int x = 0; x < status.getAdditionalTokenConverter().size(); ++x)
		{
			final TokenConverter<MigrationStatusBase> converter = status.getAdditionalTokenConverter().get(x);
			converter.setValue(status, nextToken(tokens), count += 1);
		}


	}

	protected static NodeRef getNodeRefOrNull(final String xmlFileStr, int count) throws ParseException
	{
		NodeRef ref = null;

		if(xmlFileStr.length() > 0)
		{
			if(NodeRef.isNodeRef(xmlFileStr) == false)
			{
				throw new ParseException("The xml file ref is not a valid node reference : " + xmlFileStr, count);
			}
			else
			{
				ref = new NodeRef(xmlFileStr);
			}
		}

		return ref;
	}

	/**
	 * @param status
	 * @return
	 */
	private int getTokenCount(final MigrationStatusBase status)
	{
		return 8 + status.getAdditionalTokenConverter().size();
	}

	private final void setExpectedFire(Date expectedFire)
	{
		this.expectedFire = expectedFire;
	}

	private final void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	private final void setNotificationEmails(List<String> notificationEmails)
	{
		this.notificationEmails = notificationEmails;
	}

	/**
	 * @return the endDate
	 */
	public final Date getEndDate()
	{
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public final void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	public long getProcessTimeMillis()
	{
		if(endDate == null || realFire == null)
		{
			return -1;
		}
		else
		{
			return endDate.getTime() - realFire.getTime();
		}
	}
	
	public boolean isPending()
	{
		return FireStatus.PENDING.equals(getStatus());
	}
}