/**
 * Describes a single dynamic property type option available when configuring
 * or editing dynamic properties within a group.
 *
 * Instances pair a machine-readable type identifier with a human-friendly
 * label, typically used to populate selection controls (e.g. dropdowns) in
 * the dynamic-properties UI.
 */
export interface DynamicPropertyType {
  /** Machine-readable identifier of the property type (e.g. the value stored or sent to the backend). */
  type: string;
  /** Human-readable label shown to the user for this property type. */
  display: string;
}
