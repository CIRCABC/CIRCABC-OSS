/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.namespace.QName;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.model.UserModel;

/**
 * Interface that encapsulate any property identified by a QName.
 *
 * @author Yanick Pignot
 */
public interface TypedProperty extends Serializable
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
	 * Set a new value to the property
	 *
	 * @param newValue
	 */
	public void setValue(Serializable newValue);

	/**
	 * Encapsulate a Name property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_NAME
	 * @author Yanick Pignot
	 */
	public class NameProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		private static final String NAME_REPLACEALL_REGEX = "\\\"|\\*|\\\\|\\>|\\<|\\?|\\/|\\:|\\|";

		public NameProperty(final String value)
		{
			super(toValidName(value), ContentModel.PROP_NAME);
		}

		public static String toValidName(final String name)
		{
			if(name != null)
			{
				return name.trim().replaceAll(NAME_REPLACEALL_REGEX, "_");
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * Encapsulate a Created Date property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_CREATED
	 * @author Yanick Pignot
	 */
	public class CreatedProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public CreatedProperty(final Date value)
		{
			super((Serializable) value, ContentModel.PROP_CREATED);
		}
	}

	/**
	 * Encapsulate a Creator property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_CREATOR
	 * @author Yanick Pignot
	 */
	public class CreatorProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public CreatorProperty(final String value)
		{
			super(value, ContentModel.PROP_CREATOR);
		}
	}

	/**
	 * Encapsulate a Modified Date property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_MODIFIED
	 * @author Yanick Pignot
	 */
	public class ModifiedProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public ModifiedProperty(final Date value)
		{
			super((Serializable) value, ContentModel.PROP_MODIFIED);
		}
	}

	/**
	 * Encapsulate a Modifier property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_MODIFIER
	 * @author Yanick Pignot
	 */
	public class ModifierProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public ModifierProperty(final String value)
		{
			super(value, ContentModel.PROP_MODIFIER);
		}
	}

	/**
	 * Encapsulate an Owner  property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_OWNER
	 * @author Yanick Pignot
	 */
	public class OwnerProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public OwnerProperty(final String value)
		{
			super(value, ContentModel.PROP_OWNER);
		}
	}

	/**
	 * Encapsulate a Title property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_TITLE
	 * @author Yanick Pignot
	 */
	public class TitleProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public TitleProperty(final String value)
		{
			super(value, ContentModel.PROP_TITLE);
		}

		public TitleProperty(final MLText values)
		{
			super(values, ContentModel.PROP_TITLE);
		}
	}


	/**
	 * Encapsulate a Descriptions property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_DESCRIPTION
	 * @author Yanick Pignot
	 */
	public class DescriptionProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public DescriptionProperty(final String value)
		{
			super(value, ContentModel.PROP_DESCRIPTION);
		}

		public DescriptionProperty(final MLText values)
		{
			super(values, ContentModel.PROP_DESCRIPTION);
		}
	}

	/**
	 * Encapsulate an Author  property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_AUTHOR
	 * @author Yanick Pignot
	 */
	public class AuthorProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public AuthorProperty(final String value)
		{
			super(value, ContentModel.PROP_AUTHOR);
		}
	}

	/**
	 * Encapsulate a Locale property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_LOCALE
	 * @author Yanick Pignot
	 */
	public class LocaleProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public LocaleProperty(final Locale value)
		{
			super(value, ContentModel.PROP_LOCALE);
		}
	}

	/**
	 * Encapsulate a Vernsion Note property value and its QName identifier
	 *
	 * @see org.alfresco.repo.version.Version2Model#PROP_QNAME_VERSION_DESCRIPTION
	 * @author Yanick Pignot
	 */
	public class VersionNoteProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public VersionNoteProperty(final String value)
		{
			super(value, Version2Model.PROP_QNAME_VERSION_DESCRIPTION);
		}
	}

	/**
	 * Encapsulate a Vernsion Label property value and its QName identifier
	 *
	 * @see org.alfresco.repo.version.Version2Model#PROP_QNAME_VERSION_LABEL
	 * @author Yanick Pignot
	 */
	public class VersionLabelProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		private static final String VALUE_REGEX = "[0-9]+[\\.[0-9]+]+";

		public VersionLabelProperty(final String value)
		{
			super(value, Version2Model.PROP_QNAME_VERSION_LABEL);

			if(isValidVersionLabel(value) == false)
			{
				throw new IllegalArgumentException("Invalid value for version label: " + value + ". It must matches " + VALUE_REGEX);
			}
		}

		public static boolean isValidVersionLabel(final String value)
		{
			if(value != null && value.length() > 0)
			{
				return value.matches(VALUE_REGEX);
			}
			else
			{
				// null allowed
				return true;
			}
		}
	}

	/**
	 * Encapsulate a Contact Info property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.CircabcModel#PROP_CONTACT_INFORMATION
	 * @author Yanick Pignot
	 */
	public class ContactInfoProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public ContactInfoProperty(final String value)
		{
			super(value, CircabcModel.PROP_CONTACT_INFORMATION);
		}

		public ContactInfoProperty(final MLText values)
		{
			super(values, CircabcModel.PROP_CONTACT_INFORMATION);
		}
	}

	/**
	 * Encapsulate an Index Page property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.CircabcModel#PROP_INF_INDEX_PAGE
	 * @author Yanick Pignot
	 */
	public class IndexPageProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public IndexPageProperty(final String value)
		{
			super(value, CircabcModel.PROP_INF_INDEX_PAGE);
		}
	}

	/**
	 * Encapsulate an Adapt property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.CircabcModel#PROP_INF_ADAPT
	 * @author Yanick Pignot
	 */
	public class AdaptProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public AdaptProperty(final Boolean value)
		{
			super(value, CircabcModel.PROP_INF_ADAPT);
		}
	}

	/**
	 * Encapsulate a Week Start Day property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.EventModel#PROP_WEEK_START_DAY
	 * @author Yanick Pignot
	 */
	public class WeekStartDayProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public WeekStartDayProperty(final String value)
		{
			super(value, EventModel.PROP_WEEK_START_DAY);

			if(!isValueInList(value, EventModel.WEEK_START_DAY_CONSTRAINT_VALUES))
			{
				throw new IllegalArgumentException("Ivalid value for Week Start Day: '" + value + "' not contained in " + listToString(EventModel.WEEK_START_DAY_CONSTRAINT_VALUES));
			}
		}
	}


	/**
	 * Encapsulate a Status property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_STATUS
	 * @author Yanick Pignot
	 */
	public class StatusProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		private static final List<String> VALUES = DocumentModel.STATUS_VALUES;

		public StatusProperty(final String value)
		{
			super(value, DocumentModel.PROP_STATUS);

			if(!isValueInList(value, VALUES))
			{
				throw new IllegalArgumentException("Ivalid value for Status property: '" + value + "' not contained in " + listToString(VALUES));
			}
		}
	}

	/**
	 * Encapsulate an Issue Date property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_ISSUE_DATE
	 * @author Yanick Pignot
	 */
	public class IssueDateProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public IssueDateProperty(final Date value)
		{
			super(value, DocumentModel.PROP_ISSUE_DATE);
		}
	}

	/**
	 * Encapsulate a Reference property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_REFERENCE
	 * @author Yanick Pignot
	 */
	public class ReferenceProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public ReferenceProperty(final String value)
		{
			super(value, DocumentModel.PROP_REFERENCE);
		}
	}

	/**
	 * Encapsulate a Dynamic Attribute 1 property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_DYN_ATTR_1
	 * @author Yanick Pignot
	 */
	public class DynamicProperty1 extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public DynamicProperty1(final Serializable value)
		{
			super(value, DocumentModel.PROP_DYN_ATTR_1);
		}
	}

	/**
	 * Encapsulate a Dynamic Attribute 2 property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_DYN_ATTR_2
	 * @author Yanick Pignot
	 */
	public class DynamicProperty2 extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public DynamicProperty2(final Serializable value)
		{
			super(value, DocumentModel.PROP_DYN_ATTR_2);
		}
	}

	/**
	 * Encapsulate a Dynamic Attribute 3 property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_DYN_ATTR_3
	 * @author Yanick Pignot
	 */
	public class DynamicProperty3 extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public DynamicProperty3(final Serializable value)
		{
			super(value, DocumentModel.PROP_DYN_ATTR_3);
		}
	}

	/**
	 * Encapsulate a Dynamic Attribute 4 property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_DYN_ATTR_4
	 * @author Yanick Pignot
	 */
	public class DynamicProperty4 extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public DynamicProperty4(final Serializable value)
		{
			super(value, DocumentModel.PROP_DYN_ATTR_4);
		}
	}

	/**
	 * Encapsulate a Dynamic Attribute 5 property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_DYN_ATTR_5
	 * @author Yanick Pignot
	 */
	public class DynamicProperty5 extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public DynamicProperty5(final Serializable value)
		{
			super(value, DocumentModel.PROP_DYN_ATTR_5);
		}
	}

	/**
	 * Encapsulate an Expiration Date property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_EXPIRATION_DATE
	 * @author Yanick Pignot
	 */
	public class ExpirationDateProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public ExpirationDateProperty(final Date value)
		{
			super(value, DocumentModel.PROP_EXPIRATION_DATE);
		}
	}

	/**
	 * Encapsulate a Security Ranking property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_SECURITY_RANKING
	 * @author Yanick Pignot
	 */
	public class SecurityRankingProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		private static final List<String> VALUES = DocumentModel.SECURITY_RANKINGS;

		public SecurityRankingProperty(final String value)
		{
			super(value, DocumentModel.PROP_SECURITY_RANKING);

			if(!isValueInList(value, VALUES))
			{
				throw new IllegalArgumentException("Ivalid value for Security Ranking: '" + value + "' not contained in " + listToString(VALUES));
			}
		}
	}

	/**
	 * Encapsulate an Target Url property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.DocumentModel#PROP_URL
	 * @author Yanick Pignot
	 */
	public class URLProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public URLProperty(final String value)
		{
			super(value, DocumentModel.PROP_URL);
		}
	}

	/**
	 * Encapsulate an Username property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_USERNAME
	 * @author Yanick Pignot
	 */
	public class UserIdProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public UserIdProperty(final String value)
		{
			super(value, ContentModel.PROP_USERNAME);
		}
	}

	/**
	 * Encapsulate a Firstname property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_FIRSTNAME
	 * @author Yanick Pignot
	 */
	public class FirstNameProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public FirstNameProperty(final String value)
		{
			super(value, ContentModel.PROP_FIRSTNAME);
		}
	}

	/**
	 * Encapsulate a Lastname property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_LASTNAME
	 * @author Yanick Pignot
	 */
	public class LastNameProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public LastNameProperty(final String value)
		{
			super(value, ContentModel.PROP_LASTNAME);
		}
	}

	/**
	 * Encapsulate an Email property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_EMAIL
	 * @author Yanick Pignot
	 */
	public class EmailProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public EmailProperty(final String value)
		{
			super(value, ContentModel.PROP_EMAIL);
		}
	}


	/**
	 * Encapsulate a Title property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_TITLE
	 * @author Yanick Pignot
	 */
	public class UserTitleProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public UserTitleProperty(final String value)
		{
			super(value, UserModel.PROP_TITLE);
		}
	}

	/**
	 * Encapsulate a Organisation property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_ORGDEPNUMBER
	 * @author Yanick Pignot
	 */
	public class OrganisationProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public OrganisationProperty(final String value)
		{
			super(value, UserModel.PROP_ORGDEPNUMBER);
		}
	}

	/**
	 * Encapsulate a Phone property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_PHONE
	 * @author Yanick Pignot
	 */
	public class PhoneProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public PhoneProperty(final String value)
		{
			super(value, UserModel.PROP_PHONE);
		}
	}

	/**
	 * Encapsulate a PostalAddress property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_POSTAL_ADDRESS
	 * @author Yanick Pignot
	 */
	public class PostalAddressProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public PostalAddressProperty(final String value)
		{
			super(value, UserModel.PROP_POSTAL_ADDRESS);
		}
	}

	/**
	 * Encapsulate a User Description property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_DESCRIPTION
	 * @author Yanick Pignot
	 */
	public class UserDescriptionProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public UserDescriptionProperty(final String value)
		{
			super(value, UserModel.PROP_DESCRIPTION);
		}
	}

	/**
	 * Encapsulate a Fax property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_FAX
	 * @author Yanick Pignot
	 */
	public class FaxProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public FaxProperty(final String value)
		{
			super(value, UserModel.PROP_FAX);
		}
	}

	/**
	 * Encapsulate a Password property value and its QName identifier
	 *
	 * @see org.alfresco.model.ContentModel#PROP_PASSWORD
	 * @author Slobodan Filipovic
	 */


	public class PasswordProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public PasswordProperty(final String value)
		{
			super(value, ContentModel.PROP_PASSWORD);
		}
	}

	/**
	 * Encapsulate a URL Address property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_URL
	 * @author Yanick Pignot
	 */
	public class URLAddressProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public URLAddressProperty(final String value)
		{
			super(value, UserModel.PROP_URL);
		}
	}

	/**
	 * Encapsulate a Global Notification property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_GLOBAL_NOTIFICATION
	 * @author Yanick Pignot
	 */
	public class GlobalNotificationProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public GlobalNotificationProperty(final Boolean value)
		{
			super(value, UserModel.PROP_GLOBAL_NOTIFICATION);
		}
	}

	/**
	 * Encapsulate a Personal Information property value and its QName identifier
	 *
	 * @see eu.cec.digit.circabc.model.UserModel#PROP_VISISBILITY
	 * @author Yanick Pignot
	 */
	public class PersonalInformationProperty extends TypedPropertyBase
	{
		private static final long serialVersionUID = 1L;

		public PersonalInformationProperty(final Boolean value)
		{
			super(value, UserModel.PROP_VISISBILITY);
		}
	}

	/**
	 * Base typed property class to perform generic operations and provide usefull operation
	 *
	 * @author Yanick Pignot
	 */
	abstract class TypedPropertyBase implements TypedProperty
	{
		private final QName identifier;
		private Serializable value;

		protected TypedPropertyBase(final Serializable value, final QName qname)
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

		public void setValue(final Serializable value)
		{
			this.value = value;
		}

		protected String arrayToString(final Serializable[] array)
		{
			final StringBuffer buff = new StringBuffer("");
			for (final Serializable value : array)
			{
				buff.append(value);
			}

			return buff.toString();
		}

		protected <T> boolean isValueInArray(T value , T[] array)
		{
			for(T arrayValue : array)
			{
				if(arrayValue.equals(value))
				{
					return true;
				}
			}

			return false;
		}
		protected <T> boolean isValueInList(T value , List<T> array)
		{
			for(T arrayValue : array)
			{
				if(arrayValue.equals(value))
				{
					return true;
				}
			}

			return false;
		}
		protected String listToString(final List<String> array)
		{
			final StringBuffer buff = new StringBuffer("");
			for (final Serializable value : array)
			{
				buff.append(value).append(" ");
			}

			return buff.toString();
		}


		@Override
		public int hashCode()
		{
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + ((identifier == null) ? 0 : identifier.hashCode());
			result = PRIME * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final TypedPropertyBase other = (TypedPropertyBase) obj;
			if (identifier == null)
			{
				if (other.identifier != null)
					return false;
			} else if (!identifier.equals(other.identifier))
				return false;
			if (value == null)
			{
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

}
