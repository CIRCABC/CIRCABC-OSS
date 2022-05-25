/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;

import eu.cec.digit.circabc.migration.entities.ElementsConverter;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.ContentNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Forum;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryContentVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.NamedNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.TitledNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordReferences;
import eu.cec.digit.circabc.migration.entities.generated.properties.ModerationAccepted;
import eu.cec.digit.circabc.migration.entities.generated.properties.ModerationPending;
import eu.cec.digit.circabc.migration.entities.generated.properties.ModerationRefused;

/**
 * Util methods to set the properties to a node. Can be used for each kind of exportation instance
 *
 * @author Yanick Pignot
 */
public abstract class MetadataUtils
{

	private static final String REGEX_MORE_ONE_SPACE = "\\ \\ +";

	private MetadataUtils(){}

	public static final void setInterestGroupProperty(final InterestGroup interestGroup, final MLText contactInformations, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Contact Info:  " + contactInformations);
		}


		if(contactInformations != null)
		{
			interestGroup.withI18NContactInfos(ElementsConverter.adpatMLText(contactInformations));
		}
	}


	public static final void setNodeProperty(final Node node, final Date created, final String creator, final Date modified, final String modifier, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Created:       " + created);
			logger.debug("  ---  Creator:       " + creator);
			logger.debug("  ---  Modified:      " + modified);
			logger.debug("  ---  Mofier:        " + modifier);
		}

		if(created != null)
		{
			node.setCreated(new TypedProperty.CreatedProperty(created));
		}
		else if(modifier != null)
		{
			node.setCreated(new TypedProperty.CreatedProperty(modified));
		}

		if(creator != null)
		{
			node.setCreator(new TypedProperty.CreatorProperty(creator.trim()));
		}
		else if(modifier != null)
		{
			node.setCreator(new TypedProperty.CreatorProperty(modifier.trim()));
		}

		if(modified != null)
		{
			node.setModified(new TypedProperty.ModifiedProperty(modified));
		}
		else if(created != null)
		{
			node.setModified(new TypedProperty.ModifiedProperty(created));
		}

		if(modifier != null)
		{
			node.setModifier(new TypedProperty.ModifierProperty(modifier.trim()));
		}
		else if(creator != null)
		{
			node.setModifier(new TypedProperty.ModifierProperty(creator.trim()));
		}
	}

	public static final void setTitledNodeProperty(final TitledNode titledNode, final MLText title, final MLText description, final String owner, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Title:         " + title);
			logger.debug("  ---  Description:   " + description);
			logger.debug("  ---  Owner:         " + owner);
		}

		if(title != null)
		{
			titledNode.withI18NTitles(ElementsConverter.adpatMLText(title));
		}
		if(description != null)
		{
			titledNode.withI18NDescriptions(ElementsConverter.adpatMLText(description));
		}
		if(owner != null)
		{
			titledNode.setOwner(new TypedProperty.OwnerProperty(owner.trim()));
		}
	}

	public static final void setNamedNodeProperty(final NamedNode namedNode, final String name, final Log logger)
	{
		setNamedNodeProperty(namedNode, name, true, logger);
	}

	public static final void setNamedNodeProperty(final NamedNode namedNode, final String name, boolean ensureNameUnicity, final Log logger)
	{
		if(name == null)
		{
			throw new IllegalArgumentException("The name is a mandatory parameter");
		}
		final String cleanName = NameProperty.toValidName(name).trim().replaceAll(REGEX_MORE_ONE_SPACE, " ");
		final String usedName;

		if(ensureNameUnicity)
		{
			usedName = computeUniqueName(cleanName, namedNode, logger);
		}
		else
		{
			usedName = cleanName;
		}

		namedNode.setName(new TypedProperty.NameProperty(usedName));

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Name:          " + usedName);
		}
	}


	public static final void setContentNodeProperty(final ContentNode contentNode, final String uri, final String versionLabel, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Uri:           " + uri);
		}

		if(uri == null)
		{
			throw new IllegalArgumentException("The uri is a mandatory parameter");
		}

		if(versionLabel == null || versionLabel.length() < 1)
		{
			throw new IllegalArgumentException("The version label is a mandatory parameter");
		}

		contentNode.setUri(uri);
		contentNode.setVersionLabel(new TypedProperty.VersionLabelProperty(versionLabel));
	}


	public static final void setContentNodeProperty(final Message message, final String uri, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Uri:           " + uri);
		}

		if(uri == null)
		{
			throw new IllegalArgumentException("The uri is a mandatory parameter");
		}

		message.setUri(uri);
	}


	public static final void setLinkProperty(final Link link, final String reference, final Log logger)
	{
		if(reference == null)
		{
			throw new IllegalArgumentException("The reference is a mandatory parameter");
		}

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Reference:     " + reference);
		}

		link.setReference(toValidReference(reference));
	}

	public static final void setSpaceProperty(final Space space, final Date expirationDate, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Expiration:    " + expirationDate);
		}

		if(expirationDate != null)
		{
			space.setExpirationDate(new TypedProperty.ExpirationDateProperty(expirationDate));
		}
	}

	public static final void setUrlProperty(final Url url, final String target, final Log logger)
	{
		if(target == null)
		{
			throw new IllegalArgumentException("The target is a mandatory parameter");
		}

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  target:        " + target);
		}


		url.setTarget(new TypedProperty.URLProperty(target));
	}

	public static final void setMLContentProperty(final MlContent content, final String author, final String securityRanking, final Date expirationDate, final Locale pivotLang, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  pivot:         " + pivotLang);
			logger.debug("  ---  Sec. Ranking:  " + securityRanking);
			logger.debug("  ---  Expiration:    " + expirationDate);
			logger.debug("  ---  Author:        " + author);
		}

		if(pivotLang == null )
		{
			throw new IllegalArgumentException("The lang is a mandatory parameter");
		}
		content.setPivotLang(new TypedProperty.LocaleProperty(pivotLang));

		if(securityRanking != null)
		{
			content.setSecurityRanking(new TypedProperty.SecurityRankingProperty(securityRanking));
		}
		if (expirationDate != null)
		{
			content.setExpirationDate(new TypedProperty.ExpirationDateProperty(expirationDate));
		}
		if (author != null)
		{
			content.setAuthor(new TypedProperty.AuthorProperty(author.trim()));
		}
	}

	public static final void setContentProperty(final Content content, final String securityRanking, final Date expirationDate, final String author, final String status, final Date issueDate, final String reference, final List<Integer> keywords, final Serializable dynamicProperty1, final Serializable dynamicProperty2, final Serializable dynamicProperty3, final Serializable dynamicProperty4, final Serializable dynamicProperty5, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Sec. Ranking:  " + securityRanking);
			logger.debug("  ---  Expiration:    " + expirationDate);
			logger.debug("  ---  Author:        " + author);
			logger.debug("  ---  Status:        " + status);
			logger.debug("  ---  Issue Date:    " + issueDate);
			logger.debug("  ---  Reference:     " + reference);
			logger.debug("  ---  Keywords:      " + keywords);
			logger.debug("  ---  Dyn. Property1:" + dynamicProperty1);
			logger.debug("  ---  Dyn. Property2:" + dynamicProperty2);
			logger.debug("  ---  Dyn. Property3:" + dynamicProperty3);
			logger.debug("  ---  Dyn. Property4:" + dynamicProperty4);
			logger.debug("  ---  Dyn. Property5:" + dynamicProperty5);
		}

		final List<Integer> dynAttrIndexes = getDefinedAttributes(content, logger);

		if(securityRanking != null)
		{
			content.setSecurityRanking(new TypedProperty.SecurityRankingProperty(securityRanking));
		}
		if (expirationDate != null)
		{
			content.setExpirationDate(new TypedProperty.ExpirationDateProperty(expirationDate));
		}
		if (author != null)
		{
			content.setAuthor(new TypedProperty.AuthorProperty(author));
		}
		if (status != null)
		{
			content.setStatus(new TypedProperty.StatusProperty(status));
		}
		if (issueDate != null)
		{
			content.setIssueDate(new TypedProperty.IssueDateProperty(issueDate));
		}
		if (reference != null)
		{
			content.setReference(new TypedProperty.ReferenceProperty(reference));
		}
		if (keywords != null && keywords.size() > 0)
		{
			content.setKeywords(new KeywordReferences(keywords));
		}
		if (dynamicProperty1 != null && dynAttrIndexes.contains(1))
		{
			content.setDynamicProperty1(new TypedProperty.DynamicProperty1(dynamicProperty1));
		}
		if (dynamicProperty2 != null && dynAttrIndexes.contains(2))
		{
			content.setDynamicProperty2(new TypedProperty.DynamicProperty2(dynamicProperty1));
		}
		if (dynamicProperty3 != null && dynAttrIndexes.contains(3))
		{
			content.setDynamicProperty3(new TypedProperty.DynamicProperty3(dynamicProperty3));
		}
		if (dynamicProperty4 != null && dynAttrIndexes.contains(4))
		{
			content.setDynamicProperty4(new TypedProperty.DynamicProperty4(dynamicProperty4));
		}
		if (dynamicProperty5 != null && dynAttrIndexes.contains(5))
		{
			content.setDynamicProperty5(new TypedProperty.DynamicProperty5(dynamicProperty5));
		}
	}

	public static final void setContentProperty(final LibraryContentVersion content, final String securityRanking, final Date expirationDate, final String author, final String status, final Date issueDate, final String reference, final List<Integer> keywords, final Serializable dynamicProperty1, final Serializable dynamicProperty2, final Serializable dynamicProperty3, final Serializable dynamicProperty4, final Serializable dynamicProperty5, final Log logger)
	{
		final List<Integer> dynAttrIndexes = getDefinedAttributes(content, logger);

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Sec. Ranking:  " + securityRanking);
			logger.debug("  ---  Expiration:    " + expirationDate);
			logger.debug("  ---  Author:        " + author);
			logger.debug("  ---  Status:        " + status);
			logger.debug("  ---  Issue Date:    " + issueDate);
			logger.debug("  ---  Reference:     " + reference);
			logger.debug("  ---  Keywords:      " + keywords);
			logger.debug("  ---  Dyn. Property1:" + dynamicProperty1);
			logger.debug("  ---  Dyn. Property2:" + dynamicProperty2);
			logger.debug("  ---  Dyn. Property3:" + dynamicProperty3);
			logger.debug("  ---  Dyn. Property4:" + dynamicProperty4);
			logger.debug("  ---  Dyn. Property5:" + dynamicProperty5);
		}

		if(securityRanking != null)
		{
			content.setSecurityRanking(new TypedProperty.SecurityRankingProperty(securityRanking));
		}
		if (expirationDate != null)
		{
			content.setExpirationDate(new TypedProperty.ExpirationDateProperty(expirationDate));
		}
		if (author != null)
		{
			content.setAuthor(new TypedProperty.AuthorProperty(author));
		}
		if (status != null)
		{
			content.setStatus(new TypedProperty.StatusProperty(status));
		}
		if (issueDate != null)
		{
			content.setIssueDate(new TypedProperty.IssueDateProperty(issueDate));
		}
		if (reference != null)
		{
			content.setReference(new TypedProperty.ReferenceProperty(reference));
		}
		if (keywords != null && keywords.size() > 0)
		{
			content.setKeywords(new KeywordReferences(keywords));
		}
		if (dynamicProperty1 != null && dynAttrIndexes.contains(1))
		{
			content.setDynamicProperty1(new TypedProperty.DynamicProperty1(dynamicProperty1));
		}
		if (dynamicProperty2 != null && dynAttrIndexes.contains(2))
		{
			content.setDynamicProperty2(new TypedProperty.DynamicProperty2(dynamicProperty1));
		}
		if (dynamicProperty3 != null && dynAttrIndexes.contains(3))
		{
			content.setDynamicProperty3(new TypedProperty.DynamicProperty3(dynamicProperty3));
		}
		if (dynamicProperty4 != null && dynAttrIndexes.contains(4))
		{
			content.setDynamicProperty4(new TypedProperty.DynamicProperty4(dynamicProperty4));
		}
		if (dynamicProperty5 != null && dynAttrIndexes.contains(5))
		{
			content.setDynamicProperty5(new TypedProperty.DynamicProperty5(dynamicProperty5));
		}
	}

	public static final void setTranslationProperty(final LibraryTranslation content, final String author, final String status, final Date issueDate, final String reference, final List<Integer> keywords, final Serializable dynamicProperty1, final Serializable dynamicProperty2, final Serializable dynamicProperty3, final Serializable dynamicProperty4, final Serializable dynamicProperty5, final Locale lang, final Log logger)
	{
		final List<Integer> dynAttrIndexes = getDefinedAttributes(content, logger);

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Lang:          " + lang);
			logger.debug("  ---  Author:        " + author);
			logger.debug("  ---  Status:        " + status);
			logger.debug("  ---  Issue Date:    " + issueDate);
			logger.debug("  ---  Reference:     " + reference);
			logger.debug("  ---  Keywords:      " + keywords);
			logger.debug("  ---  Dyn. Property1:" + dynamicProperty1);
			logger.debug("  ---  Dyn. Property2:" + dynamicProperty2);
			logger.debug("  ---  Dyn. Property3:" + dynamicProperty3);
			logger.debug("  ---  Dyn. Property4:" + dynamicProperty4);
			logger.debug("  ---  Dyn. Property5:" + dynamicProperty5);
		}

		if(lang == null )
		{
			throw new IllegalArgumentException("The lang is a mandatory parameter");
		}
		content.setLang(new TypedProperty.LocaleProperty(lang));

		if (author != null)
		{
			content.setAuthor(new TypedProperty.AuthorProperty(author.trim()));
		}
		if (status != null)
		{
			content.setStatus(new TypedProperty.StatusProperty(status));
		}
		if (issueDate != null)
		{
			content.setIssueDate(new TypedProperty.IssueDateProperty(issueDate));
		}
		if (reference != null)
		{
			content.setReference(new TypedProperty.ReferenceProperty(reference));
		}
		if (keywords != null && keywords.size() > 0)
		{
			content.setKeywords(new KeywordReferences(keywords));
		}
		if (dynamicProperty1 != null && dynAttrIndexes.contains(1))
		{
			content.setDynamicProperty1(new TypedProperty.DynamicProperty1(dynamicProperty1));
		}
		if (dynamicProperty2 != null && dynAttrIndexes.contains(2))
		{
			content.setDynamicProperty2(new TypedProperty.DynamicProperty2(dynamicProperty1));
		}
		if (dynamicProperty3 != null && dynAttrIndexes.contains(3))
		{
			content.setDynamicProperty3(new TypedProperty.DynamicProperty3(dynamicProperty3));
		}
		if (dynamicProperty4 != null && dynAttrIndexes.contains(4))
		{
			content.setDynamicProperty4(new TypedProperty.DynamicProperty4(dynamicProperty4));
		}
		if (dynamicProperty5 != null && dynAttrIndexes.contains(5))
		{
			content.setDynamicProperty5(new TypedProperty.DynamicProperty5(dynamicProperty5));
		}
	}

	public static final void setTranslationProperty(final LibraryTranslationVersion content, final String author, final String status, final Date issueDate, final String reference, final List<Integer> keywords, final Serializable dynamicProperty1, final Serializable dynamicProperty2, final Serializable dynamicProperty3, final Serializable dynamicProperty4, final Serializable dynamicProperty5, final Locale lang, final Log logger)
	{
		final List<Integer> dynAttrIndexes = getDefinedAttributes(content, logger);

		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Lang:          " + lang);
			logger.debug("  ---  Author:        " + author);
			logger.debug("  ---  Status:        " + status);
			logger.debug("  ---  Issue Date:    " + issueDate);
			logger.debug("  ---  Reference:     " + reference);
			logger.debug("  ---  Keywords:      " + keywords);
			logger.debug("  ---  Dyn. Property1:" + dynamicProperty1);
			logger.debug("  ---  Dyn. Property2:" + dynamicProperty2);
			logger.debug("  ---  Dyn. Property3:" + dynamicProperty3);
			logger.debug("  ---  Dyn. Property4:" + dynamicProperty4);
			logger.debug("  ---  Dyn. Property5:" + dynamicProperty5);
		}

		if(lang == null )
		{
			throw new IllegalArgumentException("The lang is a mandatory parameter");
		}
		content.setLang(new TypedProperty.LocaleProperty(lang));

		if (author != null)
		{
			content.setAuthor(new TypedProperty.AuthorProperty(author.trim()));
		}
		if (status != null)
		{
			content.setStatus(new TypedProperty.StatusProperty(status));
		}
		if (issueDate != null)
		{
			content.setIssueDate(new TypedProperty.IssueDateProperty(issueDate));
		}
		if (reference != null)
		{
			content.setReference(new TypedProperty.ReferenceProperty(reference));
		}
		if (keywords != null && keywords.size() > 0)
		{
			content.setKeywords(new KeywordReferences(keywords));
		}
		if (dynamicProperty1 != null && dynAttrIndexes.contains(1))
		{
			content.setDynamicProperty1(new TypedProperty.DynamicProperty1(dynamicProperty1));
		}
		if (dynamicProperty2 != null && dynAttrIndexes.contains(2))
		{
			content.setDynamicProperty2(new TypedProperty.DynamicProperty2(dynamicProperty1));
		}
		if (dynamicProperty3 != null && dynAttrIndexes.contains(3))
		{
			content.setDynamicProperty3(new TypedProperty.DynamicProperty3(dynamicProperty3));
		}
		if (dynamicProperty4 != null && dynAttrIndexes.contains(4))
		{
			content.setDynamicProperty4(new TypedProperty.DynamicProperty4(dynamicProperty4));
		}
		if (dynamicProperty5 != null && dynAttrIndexes.contains(5))
		{
			content.setDynamicProperty5(new TypedProperty.DynamicProperty5(dynamicProperty5));
		}
	}

	public static final void setTranslationProperty(final InformationTranslation content, final Locale lang, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Lang:          " + lang);
		}

		if(lang == null )
		{
			throw new IllegalArgumentException("The lang is a mandatory parameter");
		}
		content.setLang(new TypedProperty.LocaleProperty(lang));
	}

	public static final void setTranslationProperty(final InformationTranslationVersion content, final Locale lang, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Lang:          " + lang);
		}

		if(lang == null )
		{
			throw new IllegalArgumentException("The lang is a mandatory parameter");
		}
		content.setLang(new TypedProperty.LocaleProperty(lang));
	}

	public static final void setInformationProperty(final Information information, final String indexPage, final Boolean adapt, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Index Page:    " + indexPage);
			logger.debug("  ---  adapt:         " + adapt);
		}

		if(indexPage != null && indexPage.trim().length() > 0)
		{
			information.setIndexPage(new TypedProperty.IndexPageProperty(indexPage));
		}
		else
		{
			// ensure that the field is empty
			information.setIndexPage(null);
		}
		if(adapt != null)
		{
			information.setAdapt(new TypedProperty.AdaptProperty(adapt));
		}
	}

	public static final void setEventsProperty(final Events information, final String weekStartDay, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Week Start Day:" + weekStartDay);
		}

		if(weekStartDay != null)
		{
			information.setWeekStartDay(new TypedProperty.WeekStartDayProperty(weekStartDay));
		}
	}

	public static final void setForumProperty(final Forum forum, final boolean moderated, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Is moderated:" + moderated);
		}

		forum.setModerated(moderated);
	}

	public static final void setTopicProperty(final Topic topic, final String securityRanking, final Date expirationDate, final boolean moderated, final Log logger)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("  ---  Is moderated:  " + moderated);
			logger.debug("  ---  Sec. Ranking:  " + securityRanking);
			logger.debug("  ---  Expiration:    " + expirationDate);
		}

		topic.setModerated(moderated);

		if(securityRanking != null)
		{
			topic.setSecurityRanking(new TypedProperty.SecurityRankingProperty(securityRanking));
		}
		if (expirationDate != null)
		{
			topic.setExpirationDate(new TypedProperty.ExpirationDateProperty(expirationDate));
		}
	}

	public static final void setModerationProperty(final Message post, final boolean refused, final boolean accepted, final String moderator, final Date moderated, final String reason, final boolean fillMissingData, final Log logger)
	{
		if(ElementsHelper.getElementTopic(post).isModerated() == false)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("  ---  No moderation");
			}
		}
		else
		{
			if(refused && accepted)
			{
				throw new IllegalArgumentException("A post can't be either refused and rejected!");
			}
			else if (refused)
			{
				if(!fillMissingData && (moderated == null || moderator == null || moderator.length() < 1))
				{
					throw new IllegalArgumentException("The moderator name AND the moderated date are both mandatory. Moderator: " + moderator + ". Moderated: " + moderated);
				}

				post.setRefused(new ModerationRefused(
						moderator == null || moderator.length() < 1 ? AuthenticationUtil.getFullyAuthenticatedUser() : moderator,
						moderated == null ? new Date() : moderated,
						reason == null ? "" : reason
					));

				if(logger.isDebugEnabled())
				{
					logger.debug("  ---  Moderation Status: Refused" );
					logger.debug("  ---  Moderator: " + post.getRefused().getModerator());
					logger.debug("  ---  Moderate:  " + post.getRefused().getDate());
					logger.debug("  ---  Reason:    " + post.getRefused().getReason());
				}
			}
			else if (accepted)
			{

				if(!fillMissingData && (moderated == null || moderator == null || moderator.length() < 1))
				{
					throw new IllegalArgumentException("The moderator name AND the moderated date are both mandatory. Moderator: " + moderator + ". Moderated: " + moderated);
				}

				post.setAccepted(new ModerationAccepted(
						moderator == null || moderator.length() < 1 ? AuthenticationUtil.getFullyAuthenticatedUser() : moderator,
						moderated == null ? new Date() : moderated
					));

				if(logger.isDebugEnabled())
				{
					logger.debug("  ---  Moderation Status: Accepted" );
					logger.debug("  ---  Moderator: " + post.getAccepted().getModerator());
					logger.debug("  ---  Moderate:  " + post.getAccepted().getDate());
				}
			}
			else
			{
				post.setPending(new ModerationPending());

				if(logger.isDebugEnabled())
				{
					logger.debug("  ---  Moderation Status: Pending" );
				}
			}
		}
	}

	private static final List<Integer> getDefinedAttributes(final XMLNode node, final Log logger)
	{
		final List<Integer> indexes = new ArrayList<Integer>(5);
		final InterestGroup interestGroup = ElementsHelper.getElementInterestGroup(node);
		final DynamicPropertyDefinitions definitions = interestGroup.getDynamicPropertyDefinitions();

		if(definitions != null)
		{
			for(DynamicPropertyDefinition def : definitions.getDefinitions())
			{
				indexes.add(def.getId());
			}
		}

		return indexes;
	}

	private static final String QUERY_SIBLING_NAMES = "./*/name/value|./mlContents/translations/name/value";

	@SuppressWarnings("unchecked")
	public static String computeUniqueName(final String candidateName, final XMLNode child, final Log logger)
    {
		XMLNode parent = ElementsHelper.getNodeParent(child);
		if(parent instanceof MlContent)
		{
			parent = ElementsHelper.getNodeParent(parent);
		}

    	JXPathContext ctx = JXPathContext.newContext(parent);

    	final Map<String, String> names = new HashMap<String, String>();
    	for(String siblingName : (List<String>)ctx.selectNodes(QUERY_SIBLING_NAMES))
    	{
    		names.put(siblingName.toLowerCase(), siblingName);
    	}

    	final String nameToReturn;

    	if(!names.containsKey(candidateName.toLowerCase()))
    	{
    		nameToReturn = candidateName;
    	}
    	else
    	{
    		String newTry = null;
    		final int idx = candidateName.lastIndexOf('.');

    		for(int x = 0;; ++x)
    		{
    			if(idx < 0 )
    			{
    				newTry = candidateName + " (" + x + ')';
    			}
    			else
    			{
    				newTry = candidateName.substring(0, idx) + " (" + x + ')' + candidateName.substring(idx);
    			}

    			if(!names.containsKey(newTry.toLowerCase()))
    			{
    				break;
    			}
    		}

    		nameToReturn = newTry;
    	}

    	if(logger.isWarnEnabled() && nameToReturn.equals(candidateName) == false)
		{
			logger.warn("Duplicate node name. An other name is generated: ");
			logger.warn("----  Expected: " + candidateName);
			logger.warn("----  Used:     " + nameToReturn);
		}

    	return nameToReturn;
    }


	public static String toValidReference(final String reference)
	{
		if(reference == null)
		{
			return null;
		}

		final StringBuilder builder = new StringBuilder();
		final StringTokenizer tokens = new StringTokenizer(reference, "/", false);

		while(tokens.hasMoreTokens())
		{
			builder
				.append("/")
				.append(TypedProperty.NameProperty.toValidName(tokens.nextToken()));
		}

		if(reference.endsWith("/"))
		{
			builder.append("/");
		}

		return builder.toString();
	}
}
