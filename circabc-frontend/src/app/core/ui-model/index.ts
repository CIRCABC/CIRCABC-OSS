import {
  BulkImportUserData,
  InterestGroup,
  KeywordDefinition,
  Node as ModelNode,
  UserProfile,
} from 'app/core/generated/circabc';

export * from './quote';

interface Selectable {
  selected?: boolean;
}

interface Indexed {
  index?: number;
}
export interface SelectableNode extends ModelNode, Selectable {}
export interface SelectableKeyword extends KeywordDefinition, Selectable {}

export interface SelectableUserProfile extends UserProfile, Selectable {}

export interface SelectableBulkImportUserData
  extends BulkImportUserData,
    Selectable {}

export interface IndexedInterestGroup extends Indexed, InterestGroup {}

export interface IdName {
  id: string;
  name: string;
}
