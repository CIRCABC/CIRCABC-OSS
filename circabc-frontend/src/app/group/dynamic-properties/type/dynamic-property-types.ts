import { DynamicPropertyType } from 'app/group/dynamic-properties/type/dynamic-property-type';

export class DynamicPropertyTypes {
  public static TEXT_FIELD: DynamicPropertyType = {
    type: 'TEXT_FIELD',
    display: 'Text',
  };

  public static DATE_FIELD: DynamicPropertyType = {
    type: 'DATE_FIELD',
    display: 'Date',
  };

  public static TEXT_AREA: DynamicPropertyType = {
    type: 'TEXT_AREA',
    display: 'Textarea',
  };

  public static SELECTION: DynamicPropertyType = {
    type: 'SELECTION',
    display: 'Select',
  };

  public static MULTI_SELECTION: DynamicPropertyType = {
    type: 'MULTI_SELECTION',
    display: 'Select multiple',
  };

  public static getTypes(): DynamicPropertyType[] {
    const res: DynamicPropertyType[] = [];
    res.push(this.TEXT_FIELD);
    res.push(this.TEXT_AREA);
    res.push(this.DATE_FIELD);
    res.push(this.SELECTION);
    res.push(this.MULTI_SELECTION);
    return res;
  }
}
