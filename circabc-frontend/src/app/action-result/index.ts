// eslint-disable-next-line
import { ActionResult } from './action-result';
// eslint-disable-next-line
import { ActionType } from './action-type';

import { Node as ModelNode } from 'app/core/generated/circabc';

export * from './action-result';
export * from './action-type';
export * from './action-url';

export interface ActionEmitterResult {
  node?: ModelNode;
  type?: ActionType;
  result?: ActionResult;
}
