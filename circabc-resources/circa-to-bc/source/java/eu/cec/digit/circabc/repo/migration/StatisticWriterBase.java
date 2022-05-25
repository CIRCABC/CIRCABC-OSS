package eu.cec.digit.circabc.repo.migration;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.cec.digit.circabc.service.migration.CategoryStat;
import eu.cec.digit.circabc.service.migration.InterestGroupStat;
import eu.cec.digit.circabc.service.migration.RootStat;
import eu.cec.digit.circabc.service.migration.StatisticsWriter;

public abstract class StatisticWriterBase implements StatisticsWriter
{
	private static final String GET_PREFIX = "get";
	private static final String IS_PREFIX = "is";

	private static final String GETTER_REGEX = "get.+|is.+";
	private static final List<String> NO_REFLEXION_METHODS = Arrays.asList(
	    		"getIgName",
	    		"getCategoryName",
	    		"getCategoryHeader",
	    		"getExportationImplementationName"
	    	);

	private String displayName;
	private String extension;
	private boolean needThreeFiles;


	public abstract void write(RootStat rootStat, OutputStream... outputStreams) throws IOException;

	public String getReaderDisplayName()
	{
		return displayName;
	}

	public String getExtension()
	{
		return extension;
	}

	public boolean isNeedThreeFiles()
	{
		return needThreeFiles;
	}

	public final void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public final void setExtension(String extension)
	{
		this.extension = extension;
	}

	public final void setNeedThreeFiles(boolean needThreeFiles)
	{
		this.needThreeFiles = needThreeFiles;
	}

	protected Comparator<InterestGroupStat> igComparator = new Comparator<InterestGroupStat>()
	    {
			public int compare(InterestGroupStat stat1, InterestGroupStat stat2)
			{
				return stat1.getIgName().compareTo(stat2.getIgName());
			}
	    };

	private Comparator<Method> methodComparator = new Comparator<Method>()
	    {
			public int compare(Method method1, Method method2)
			{
				return method1.getName().compareTo(method2.getName());
			}
	    };

	protected List<Method> getGetters(final Class clazz)
	{
	    final List<Method> getters = new ArrayList<Method>();
	    for(final Method method: clazz.getMethods())
	    {
	        if(method.getName().matches(GETTER_REGEX)
	        		&& NO_REFLEXION_METHODS.contains(method.getName()) == false
	                && method.getParameterTypes().length == 0
	                && !Collection.class.isAssignableFrom(method.getReturnType()))
	        {
	            getters.add(method);
	        }
	    }

	    Collections.sort(getters, methodComparator);

	    return getters;
	}

	protected Comparator<CategoryStat> categoryComparator = new Comparator<CategoryStat>()
    {
		public int compare(CategoryStat stat1, CategoryStat stat2)
		{
			return stat1.getCategoryName().compareTo(stat2.getCategoryName());
		}
    };

    protected String getMethodTitle(final String methodName)
    {
        final String displayName;

        if(methodName.startsWith(IS_PREFIX))
        {
            displayName = methodName.substring(IS_PREFIX.length());
        }
        else
        {
            displayName = methodName.substring(GET_PREFIX.length());
        }
        return displayName;
    }

    protected final Object safeInvoke(final Method method, final Object object)
    {
    	try
		{
			return method.invoke(object);
		}
    	catch (Exception e)
		{
    		return "-1";
		}
    }

    protected final byte[] toBytes(final Object value, final boolean eol, final Object ... params)
    {
    	final String strValue;
    	if(value instanceof String)
    	{
    		strValue = (String) value;
    	}
    	else
    	{
    		strValue = value.toString();
    	}

    	return toBytes(strValue, eol, params);
    }

    protected final byte[] toBytes(final String value, final boolean eol, final Object ... params)
    {
    	final String formatValue;
    	if(params == null || params.length < 1)
    	{
    		formatValue = value ;
    	}
    	else
    	{
    		formatValue = MessageFormat.format(value, params);
    	}

    	if(eol)
    	{
    		return (formatValue + "\n").getBytes();
    	}
    	else
    	{
    		return formatValue.getBytes();
    	}
    }
}