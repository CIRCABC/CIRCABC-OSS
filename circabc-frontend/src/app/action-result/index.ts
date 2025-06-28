// eslint-disable-next-line
import { ActionResult } from 'app/action-result/action-result';
// eslint-disable-next-line
import { ActionType } from 'app/action-result/action-type';

import { Node as ModelNode } from 'app/core/generated/circabc';

export * from 'app/action-result/action-result';
export * from 'app/action-result/action-type';
export * from 'app/action-result/action-url';

export interface ActionEmitterResult {
  node?: ModelNode;
  type?: ActionType;
  result?: ActionResult;
}
