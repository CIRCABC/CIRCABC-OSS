import {
  BulkImportUserData,
  InterestGroup,
  KeywordDefinition,
  Node as ModelNode,
  UserProfile,
} from 'app/core/generated/circabc';

export * from './quote';

/**
 * Mixin interface that adds a selection flag to a model type.
 *
 * Used to augment generated API models so that UI components (lists,
 * tables, checkboxes, etc.) can track whether an item is currently
 * selected without polluting the generated model definitions.
 */
interface Selectable {
  /** Whether the item is currently selected in the UI. */
  selected?: boolean;
}

/**
 * Mixin interface that adds a positional index to a model type.
 *
 * Used to augment generated API models with an ordering/position value
 * for display purposes (e.g. rendering an item's rank in a list).
 */
interface Indexed {
  /** Zero-based position of the item within its containing collection. */
  index?: number;
}
/**
 * A {@link ModelNode} (generated library node) enriched with a UI
 * selection flag so nodes can be selected in browsing and library views.
 */
export interface SelectableNode extends ModelNode, Selectable {}
/**
 * A {@link KeywordDefinition} enriched with a UI selection flag so
 * keywords can be selected in keyword-management views.
 */
export interface SelectableKeyword extends KeywordDefinition, Selectable {}

/**
 * A {@link UserProfile} enriched with a UI selection flag so user
 * profiles can be selected in member-management views.
 */
export interface SelectableUserProfile extends UserProfile, Selectable {}

/**
 * A {@link BulkImportUserData} entry enriched with a UI selection flag so
 * rows can be selected during bulk user import operations.
 */
export interface SelectableBulkImportUserData
  extends BulkImportUserData, Selectable {}

/**
 * An {@link InterestGroup} enriched with a positional index for ordered
 * display in interest-group listings.
 */
export interface IndexedInterestGroup extends Indexed, InterestGroup {}

/**
 * Minimal identifier/label pair used throughout the UI for options,
 * dropdowns and lookups where only an id and a human-readable name are
 * required.
 */
export interface IdName {
  /** Unique identifier of the entity. */
  id: string;
  /** Human-readable display name of the entity. */
  name: string;
}
