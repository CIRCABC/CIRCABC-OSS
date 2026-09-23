import { ActionResult } from 'app/action-result/action-result';
import { ActionType } from 'app/action-result/action-type';

import { Node as ModelNode } from 'app/core/generated/circabc';

export * from 'app/action-result/action-result';
export * from 'app/action-result/action-type';
export * from 'app/action-result/action-url';

/**
 * Describes the outcome emitted by an action after it completes.
 *
 * Components and services that perform node-related actions (such as
 * create, edit, move or delete operations) emit an `ActionEmitterResult`
 * so that listeners can react to what happened, on which node, and with
 * which result. All fields are optional because the relevant details
 * depend on the specific action being reported.
 */
export interface ActionEmitterResult {
  /** The node that the action was performed on, if applicable. */
  node?: ModelNode;
  /** The kind of action that was performed. */
  type?: ActionType;
  /** The result/status of the performed action. */
  result?: ActionResult;
}
