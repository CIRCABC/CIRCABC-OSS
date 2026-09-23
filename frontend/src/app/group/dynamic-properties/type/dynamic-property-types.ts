import { DynamicPropertyType } from 'app/group/dynamic-properties/type/dynamic-property-type';

/**
 * Registry of the dynamic property field types supported by the group
 * dynamic-properties feature.
 *
 * Each entry maps a stable machine-readable `type` identifier to a
 * human-friendly `display` label used in the UI. This constant acts as the
 * single source of truth for the available field kinds (text field, date
 * field, textarea, single and multiple selection) that a dynamic property
 * can be rendered as.
 */
const DynamicPropertyTypes = {
  TEXT_FIELD: {
    type: 'TEXT_FIELD',
    display: 'Text',
  },

  DATE_FIELD: {
    type: 'DATE_FIELD',
    display: 'Date',
  },

  TEXT_AREA: {
    type: 'TEXT_AREA',
    display: 'Textarea',
  },
  SELECTION: {
    type: 'SELECTION',
    display: 'Select',
  },

  MULTI_SELECTION: {
    type: 'MULTI_SELECTION',
    display: 'Select multiple',
  },
};

/**
 * Builds the ordered list of dynamic property types offered to the user.
 *
 * The types are returned in a deliberate presentation order: single-line
 * text, textarea, date, single selection and finally multiple selection.
 *
 * @returns An array of {@link DynamicPropertyType} entries describing every
 * supported field type, ordered for display in selection controls.
 */
export function getDynamicPropertyTypes(): DynamicPropertyType[] {
  const res: DynamicPropertyType[] = [];
  res.push(
    DynamicPropertyTypes.TEXT_FIELD,
    DynamicPropertyTypes.TEXT_AREA,
    DynamicPropertyTypes.DATE_FIELD,
    DynamicPropertyTypes.SELECTION,
    DynamicPropertyTypes.MULTI_SELECTION
  );
  return res;
}
