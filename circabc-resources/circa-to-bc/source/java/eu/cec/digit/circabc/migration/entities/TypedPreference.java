/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities;

import java.io.Serializable;
import java.util.Locale;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.service.user.UserService;

/**
 * Interface that encapsulate any preference identified by a QName.
 *
 * @author Yanick Pignot
 */
public interface TypedPreference extends Serializable
{

	/**
	 * The Qname unique identifier of the property
	 *
	 * @return 				The qname that identify the property
	 */
	public QName getIdentifier();


	/**
	 * The value of the property in a serializable instance
	 *
	 * @return				The value of the property
	 */
	public Serializable getValue();


	/**
	 * Encapsulate a Content Filter Language property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.service.user.UserService#PREF_CONTENT_FILTER_LANGUAGE
	 * @author Yanick Pignot
	 */
	public class ContentFilterLanguagePreference extends TypedPreferenceBase
	{
		private static final long serialVersionUID = 1L;

		public ContentFilterLanguagePreference(final Locale value)
		{
			super(value, UserService.PREF_CONTENT_FILTER_LANGUAGE);
		}

		public ContentFilterLanguagePreference(final String value)
		{
			this(I18NUtil.parseLocale(value));
		}
	}

	/**
	 * Encapsulate a Content Filter Language property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.service.user.UserService#PREF_INTERFACE_LANGUAGE
	 * @author Yanick Pignot
	 */
	public class InterfaceLanguagePreference extends TypedPreferenceBase
	{
		private static final long serialVersionUID = 1L;

		public InterfaceLanguagePreference(final Locale value)
		{
			super(value, UserService.PREF_INTERFACE_LANGUAGE);
		}

		public InterfaceLanguagePreference(final String value)
		{
			this(I18NUtil.parseLocale(value));
		}
	}

	/**
	 * Base typed property class to perform generic operations and provide usefull operation
	 *
	 * @author Yanick Pignot
	 */
	abstract class TypedPreferenceBase implements TypedPreference
	{
		private final QName identifier;
		private Serializable value;

		protected TypedPreferenceBase(final Serializable value, final QName qname)
		{
			this.value = value;
			this.identifier = qname;
		}

		public QName getIdentifier()
		{
			return this.identifier;
		}

		public Serializable getValue()
		{
		 	return this.value;
		}

		@Override
		public String toString()
		{
			return identifier + "=" + value;
		}
	}
}
