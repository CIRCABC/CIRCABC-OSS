import { DynamicPropertyType } from 'app/group/dynamic-properties/type/dynamic-property-type';

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

export function getDynamicPropertyTypes(): DynamicPropertyType[] {
  const res: DynamicPropertyType[] = [];
  res.push(DynamicPropertyTypes.TEXT_FIELD);
  res.push(DynamicPropertyTypes.TEXT_AREA);
  res.push(DynamicPropertyTypes.DATE_FIELD);
  res.push(DynamicPropertyTypes.SELECTION);
  res.push(DynamicPropertyTypes.MULTI_SELECTION);
  return res;
}
