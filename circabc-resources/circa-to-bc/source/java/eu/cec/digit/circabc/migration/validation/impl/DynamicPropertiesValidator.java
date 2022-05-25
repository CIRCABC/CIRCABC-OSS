/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.adapter.DateAdapterUtils;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynPropertyType;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.JXPathValidator;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;

/**
 * Validate the dynamic properties setted in the given object graph.
 *
 * @author yanick pignot
 */
public class DynamicPropertiesValidator extends JXPathValidator
{
	private static final String DYNAMIC_PROPERTY = ".//dynamicProperty";
	private static final String DYNAMIC_PROPERTY_DEFINITIONS = "./dynamicPropertyDefinitions/definitions[id={0}]";

	@Override
	protected void validateCircabcImpl(JXPathContext context, final MigrationTracer<ImportRoot> journal)
	{
		for(InterestGroup interestGroup : getInterestGroups(context))
		{
			checkDynamicProperties(interestGroup, journal);
		}
	}

	@SuppressWarnings("unchecked")
	private void checkDynamicProperties(final InterestGroup interstGroup, final MigrationTracer<ImportRoot> journal)
	{
		JXPathContext context = JXPathContext.newContext(interstGroup);

		debug(journal, "Cheking Dynamic properties for interest group ", interstGroup);

		for(int x = 1; x <= DynamicPropertyService.MAX_PROPERTY_BY_IG_IN_CIRCA; ++x)
		{

			final DynamicPropertyDefinition definition = (DynamicPropertyDefinition) context.selectSingleNode(
					MessageFormat.format(DYNAMIC_PROPERTY_DEFINITIONS, new Object[]{"" + x}));
			final List<TypedProperty> values = (List<TypedProperty>) context.selectNodes(DYNAMIC_PROPERTY + x);

			checkDynamicProperties(definition, values, journal);
		}
	}

	private void checkDynamicProperties(final DynamicPropertyDefinition definition, List<TypedProperty> values, final MigrationTracer<ImportRoot> journal)
	{
		if(definition == null)
		{
			if(values != null && values.size() > 0)
			{
				error(journal, "Some dynamic attributes are setted without a corresponding dynamic property definition.", values);
			}
			// else{} OK
		}
		else
		{
			if(values == null)
			{
				values = Collections.<TypedProperty>emptyList();
			}

			debug(journal, values.size() + " objects defined the dynamic property definition", definition);

			final List<String> selections = definition.getSelectionCases();
			final boolean isDate = DynPropertyType.DATE_FIELD.equals(definition.getType());
			final boolean isMulti = DynPropertyType.SELECTION.equals(definition.getType());

			if(isMulti && (selections == null || selections.size() < 1))
			{
				error(journal, DynPropertyType.SELECTION  + " dynamic property must have at least one selection item.", definition);
			}
			else if(!isMulti && selections != null && selections.size() > 0)
			{
				warn(journal, "The selection cases will be ignored for properties of type " + definition.getType(), definition);
			}

			for(final TypedProperty value : values)
			{
				if(isDate && !isValidDate(value.getValue()))
				{
					 error(journal, definition.getType() + " dynamic property value must be setted as a date! Impossible to parse:", value);
				}
				else if(isMulti && !selections.contains(value.getValue()))
				{
					error(journal, definition.getType() + " dynamic property value must be included in the selection possible valiues! Found: " + value.getValue() + " but one of the followings expected: " + selections);
				}
				else
				{
					if(isDate)
					{
						try
						{
							value.setValue(DateAdapterUtils.unmarshalDateTime((String) value.getValue()));
						}
						catch (Exception e)
						{
							// should never appears
						}
					}

					debug(journal, definition.getType() + " dynamic property value successfully setted :" , value);
				}
			}
		}
	}

	private boolean isValidDate(Serializable value)
	{
		try
		{
			DateAdapterUtils.unmarshalDateTime(value.toString());

			return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}

}
